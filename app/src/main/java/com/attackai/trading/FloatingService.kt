package com.attackai.trading

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Switch
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Timer
import java.util.TimerTask

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var timer: Timer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val inflater = LayoutInflater.from(this)
        floatingView = inflater.inflate(R.layout.floating_widget, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 100

        windowManager.addView(floatingView, params)

        val tvTime = floatingView.findViewById<TextView>(R.id.tvBangladeshTime)
        val switchToggle = floatingView.findViewById<Switch>(R.id.switchOnOff)

        // Bangladesh Time Clock (Asia/Dhaka)
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("Asia/Dhaka")
                val currentTime = sdf.format(Date())
                
                tvTime.post {
                    tvTime.text = "BD Time: $currentTime"
                }
            }
        }, 0, 1000)

        switchToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Attack Ai Live Analysis Starts
            } else {
                // Attack Ai Analysis Paused
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
