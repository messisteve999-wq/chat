package com.steve.chat

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class FloatingChatService : Service() {
    private lateinit var wm: WindowManager
    private var bubble: TextView? = null
    private var panel: TextView? = null
    private var opened = false

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createBubble()
    }

    private fun createBubble() {
        val view = TextView(this).apply {
            text = "CHAT"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(20, 20, 20))
                setStroke(3, Color.rgb(230, 35, 45))
            }
            setOnClickListener { togglePanel() }
        }
        val params = WindowManager.LayoutParams(
            dp(58), dp(58),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = dp(8)
        }
        wm.addView(view, params)
        bubble = view
    }

    private fun togglePanel() {
        if (opened) {
            panel?.let { try { wm.removeView(it) } catch (_: Exception) {} }
            panel = null
            opened = false
            return
        }
        val view = TextView(this).apply {
            text = "CHAT · Assistant IA\\n\\nLes notifications WhatsApp et WhatsApp Business seront enregistrées ici.\\n\\nSuggestions Gemini : prochaine étape d'intégration.\\n\\nAppuie sur la bulle pour fermer."
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = GradientDrawable().apply {
                cornerRadius = dp(18).toFloat()
                setColor(Color.rgb(25, 25, 28))
                setStroke(dp(1), Color.rgb(180, 35, 45))
            }
            setOnClickListener { togglePanel() }
        }
        val params = WindowManager.LayoutParams(
            dp(300), dp(380),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
        wm.addView(view, params)
        panel = view
        opened = true
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        bubble?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        panel?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
