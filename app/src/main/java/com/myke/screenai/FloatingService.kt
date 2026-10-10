package com.myke.screenai

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.view.MotionEvent
import android.widget.TextView

class FloatingService : Service() {
    private var windowManager: WindowManager? = null
    private var bubble: TextView? = null
    private var lastTap = 0L
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return START_NOT_STICKY }
        if (bubble != null) return START_STICKY
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        bubble = TextView(this).apply {
            text = "AI"; textSize = 16f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(103,80,164)); elevation = 12f
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val now = System.currentTimeMillis()
                    if (now - lastTap < 450) {
                        val open = Intent(this@FloatingService, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("capture", true)
                        startActivity(open)
                        lastTap = 0L
                    } else lastTap = now
                }
                true
            }
        }
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(62, 62, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL; x = 12; y = 0 }
        windowManager?.addView(bubble, params)
        return START_STICKY
    }
    override fun onDestroy() { try { if (bubble != null) windowManager?.removeView(bubble) } catch (_: Exception) {}; bubble = null; super.onDestroy() }
}
