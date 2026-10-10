package com.myke.screenai

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.graphics.Color

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var answer: TextView
    private val captureRequest = 7401
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            status.text = intent?.getStringExtra("status") ?: "Done"
            answer.text = intent?.getStringExtra("answer") ?: "No answer received. Check backend settings and logs."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = IntentFilter("com.myke.screenai.RESULT")
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED) else registerReceiver(receiver, filter)

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 36, 28, 24); setBackgroundColor(Color.rgb(248,246,252)) }
        root.addView(TextView(this).apply { text = "Screen AI"; textSize = 30f; setTextColor(Color.rgb(35,30,48)) })
        root.addView(TextView(this).apply { text = "Double-tap the floating bubble over another app to ask AI about your screen. Screen capture always requires your approval."; textSize = 16f; setPadding(0,16,0,20) })
        val overlay = Button(this).apply { text = "Enable floating bubble"; setOnClickListener { enableBubble() } }
        root.addView(overlay, ViewGroup.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        val capture = Button(this).apply { text = "Capture this screen and ask AI"; setOnClickListener { requestCapture() } }
        root.addView(capture, ViewGroup.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        status = TextView(this).apply { text = "Ready"; textSize = 16f; setPadding(0,20,0,8) }
        root.addView(status)
        val scroller = ScrollView(this)
        answer = TextView(this).apply { text = "Your AI answer will appear here."; textSize = 17f; setTextColor(Color.rgb(35,30,48)); setPadding(0,8,0,8) }
        scroller.addView(answer)
        root.addView(scroller, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(TextView(this).apply { text = "Privacy: captured screen content is sent to your configured AI backend for analysis. Avoid capturing passwords, payment details, or private messages."; textSize = 12f; setPadding(0,18,0,0) })
        setContentView(root)
        if (intent?.getBooleanExtra("capture", false) == true) requestCapture()
    }

    private fun enableBubble() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            status.text = "Allow display over other apps, then return and tap Enable floating bubble again."
            return
        }
        startService(Intent(this, FloatingService::class.java))
        status.text = "Floating bubble enabled. Double-tap it to capture and ask AI."
    }

    private fun requestCapture() {
        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        status.text = "Approve Android's screen-capture prompt to continue."
        startActivityForResult(manager.createScreenCaptureIntent(), captureRequest)
    }

    @Deprecated("Activity result used for broad Android compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == captureRequest) {
            if (resultCode == RESULT_OK && data != null) {
                val service = Intent(this, CaptureService::class.java).putExtra("resultCode", resultCode).putExtra("data", data)
                if (Build.VERSION.SDK_INT >= 26) startForegroundService(service) else startService(service)
                status.text = "Screen captured. AI is analyzing it…"
            } else status.text = "Screen capture cancelled. Nothing was sent."
        }
    }

    override fun onDestroy() { try { unregisterReceiver(receiver) } catch (_: Exception) {}; super.onDestroy() }
}
