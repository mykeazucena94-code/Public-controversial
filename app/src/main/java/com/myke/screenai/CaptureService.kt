package com.myke.screenai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Base64
import android.view.WindowManager
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class CaptureService : Service() {
    private var projection: MediaProjection? = null
    private var display: VirtualDisplay? = null
    private var reader: ImageReader? = null
    private var thread: HandlerThread? = null
    private var handler: Handler? = null
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        val notification: Notification = if (Build.VERSION.SDK_INT >= 26) android.app.Notification.Builder(this, "screen_ai").setContentTitle("Screen AI").setContentText("Analyzing your approved screen capture").setSmallIcon(android.R.drawable.ic_menu_view).build() else @Suppress("DEPRECATION") android.app.Notification.Builder(this).setContentTitle("Screen AI").setContentText("Analyzing screen").setSmallIcon(android.R.drawable.ic_menu_view).build()
        startForeground(2, notification)
        val code = intent?.getIntExtra("resultCode", 0) ?: 0
        val data = if (Build.VERSION.SDK_INT >= 33) intent?.getParcelableExtra("data", Intent::class.java) else @Suppress("DEPRECATION") intent?.getParcelableExtra("data")
        if (data == null || code == 0) { sendResult("Capture permission data missing", ""); stopSelf(); return START_NOT_STICKY }
        try {
            val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projection = mgr.getMediaProjection(code, data)
            thread = HandlerThread("ScreenCapture").also { it.start() }; handler = Handler(thread!!.looper)
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            val metrics = android.util.DisplayMetrics()
            @Suppress("DEPRECATION") wm.defaultDisplay.getRealMetrics(metrics)
            reader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2)
            projection?.registerCallback(object : MediaProjection.Callback() { override fun onStop() { cleanup() } }, handler!!)
            display = projection?.createVirtualDisplay("ScreenAI", metrics.widthPixels, metrics.heightPixels, metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader?.surface, null, handler)
            reader?.setOnImageAvailableListener({ source ->
                val image = source.acquireLatestImage() ?: return@setOnImageAvailableListener
                try {
                    val plane = image.planes[0]; val buffer = plane.buffer
                    val pixelStride = plane.pixelStride; val rowStride = plane.rowStride
                    val rowPadding = rowStride - pixelStride * image.width
                    val full = Bitmap.createBitmap(image.width + rowPadding / pixelStride, image.height, Bitmap.Config.ARGB_8888)
                    full.copyPixelsFromBuffer(buffer)
                    val cropped = Bitmap.createBitmap(full, 0, 0, image.width, image.height)
                    full.recycle(); image.close(); reader?.setOnImageAvailableListener(null, null)
                    display?.release(); display = null; projection?.stop(); projection = null
                    val out = ByteArrayOutputStream(); cropped.compress(Bitmap.CompressFormat.JPEG, 75, out); cropped.recycle()
                    analyze(Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP))
                } catch (e: Exception) { try { image.close() } catch (_: Exception) {}; sendResult("Could not read screen image", e.message ?: ""); stopSelf() }
            }, handler)
        } catch (e: Exception) { sendResult("Screen capture failed", e.message ?: ""); stopSelf() }
        return START_NOT_STICKY
    }

    private fun analyze(imageBase64: String) {
        Thread {
            try {
                val endpoint = BuildConfig.BACKEND_URL.trimEnd('/') + "/api/analyze"
                val conn = URL(endpoint).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"; conn.connectTimeout = 20000; conn.readTimeout = 90000; conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                val body = JSONObject().put("imageBase64", imageBase64).toString().toByteArray(Charsets.UTF_8)
                conn.outputStream.use { it.write(body) }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val response = stream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                if (code in 200..299) sendResult("Analysis complete", json.optString("answer", "No answer returned")) else sendResult("AI backend error ($code)", json.optString("error", response))
                conn.disconnect()
            } catch (e: Exception) { sendResult("Could not reach AI backend", "Check the Render URL and backend logs. ${e.message ?: ""}") }
            finally { stopSelf() }
        }.start()
    }
    private fun sendResult(status: String, answer: String) { sendBroadcast(Intent("com.myke.screenai.RESULT").setPackage(packageName).putExtra("status", status).putExtra("answer", answer)) }
    private fun createChannel() { if (Build.VERSION.SDK_INT >= 26) { val channel = NotificationChannel("screen_ai", "Screen AI capture", NotificationManager.IMPORTANCE_LOW); (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel) } }
    private fun cleanup() { try { display?.release(); reader?.close(); thread?.quitSafely() } catch (_: Exception) {} }
    override fun onDestroy() { cleanup(); super.onDestroy() }
}
