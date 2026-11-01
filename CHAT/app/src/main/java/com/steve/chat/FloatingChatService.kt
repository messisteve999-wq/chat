package com.steve.chat

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.View
import android.widget.*
import java.util.concurrent.Executors

class FloatingChatService : Service() {

    private lateinit var wm: WindowManager
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    private var bubble: TextView? = null
    private var panel: LinearLayout? = null
    private var messages: LinearLayout? = null
    private var input: EditText? = null
    private var scroll: ScrollView? = null
    private var opened = false
    private var busy = false

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
                setStroke(dp(3), Color.rgb(230, 35, 45))
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
            closePanel()
        } else {
            openPanel()
        }
    }

    private fun openPanel() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(12))
            background = GradientDrawable().apply {
                cornerRadius = dp(18).toFloat()
                setColor(Color.rgb(22, 22, 25))
                setStroke(dp(1), Color.rgb(210, 35, 45))
            }
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "CHAT · Gemini IA"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }

        val minimize = Button(this).apply {
            text = "−"
            textSize = 20f
            setOnClickListener { closePanel() }
        }

        header.addView(title, LinearLayout.LayoutParams(0, dp(48), 1f))
        header.addView(minimize, LinearLayout.LayoutParams(dp(48), dp(48)))
        root.addView(header)

        val separator = View(this).apply {
            setBackgroundColor(Color.DKGRAY)
        }
        root.addView(separator, LinearLayout.LayoutParams(
            -1, dp(1)
        ))

        scroll = ScrollView(this).apply {
            isFillViewport = false
        }

        messages = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(3), dp(8), dp(3), dp(8))
        }

        scroll!!.addView(messages)
        root.addView(scroll, LinearLayout.LayoutParams(
            -1, 0, 1f
        ))

        addMessage("CHAT", "Bonjour ! Je suis ton assistant Gemini. Comment puis-je t'aider ?")

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        input = EditText(this).apply {
            hint = "Écris ton message..."
            setHintTextColor(Color.LTGRAY)
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(dp(10), dp(5), dp(10), dp(5))
            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(Color.rgb(45, 45, 48))
            }
            minHeight = dp(48)
        }

        val send = Button(this).apply {
            text = "Envoyer"
            textSize = 12f
            setOnClickListener { sendMessage() }
        }

        bottom.addView(input, LinearLayout.LayoutParams(
            0, dp(52), 1f
        ).apply {
            marginEnd = dp(5)
        })

        bottom.addView(send, LinearLayout.LayoutParams(
            dp(90), dp(52)
        ))

        root.addView(bottom)

        val params = WindowManager.LayoutParams(
            dp(320), dp(440),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        try {
            wm.addView(root, params)
            panel = root
            opened = true
        } catch (e: Exception) {
            Toast.makeText(this, "Impossible d'ouvrir CHAT", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage() {
        if (busy) return

        val prompt = input?.text?.toString()?.trim().orEmpty()

        if (prompt.isEmpty()) {
            Toast.makeText(this, "Écris un message", Toast.LENGTH_SHORT).show()
            return
        }

        input?.setText("")
        addMessage("Toi", prompt)

        busy = true
        addMessage("CHAT", "Gemini réfléchit...")

        executor.execute {
            val response = try {
                GeminiClient.generate(prompt)
            } catch (e: Exception) {
                "Erreur de connexion : ${e.message ?: "serveur inaccessible"}"
            }

            handler.post {
                if (!isAdded()) return@post

                removeLastMessage()
                addMessage("Gemini", response)
                busy = false
            }
        }
    }

    private fun addMessage(author: String, content: String) {
        val container = messages ?: return

        val text = TextView(this).apply {
            this.text = "$author\n$content"
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(dp(11), dp(9), dp(11), dp(9))
            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(
                    if (author == "Toi") Color.rgb(65, 45, 45)
                    else Color.rgb(43, 43, 47)
                )
            }
        }

        container.addView(text, LinearLayout.LayoutParams(
            -1, -2
        ).apply {
            bottomMargin = dp(8)
        })

        scroll?.post {
            scroll?.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun removeLastMessage() {
        val container = messages ?: return
        if (container.childCount > 0) {
            container.removeViewAt(container.childCount - 1)
        }
    }

    private fun isAdded(): Boolean {
        return opened && panel != null && messages != null
    }

    private fun closePanel() {
        panel?.let {
            try {
                wm.removeView(it)
            } catch (_: Exception) {}
        }

        panel = null
        messages = null
        input = null
        scroll = null
        opened = false
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        closePanel()

        bubble?.let {
            try {
                wm.removeView(it)
            } catch (_: Exception) {}
        }

        bubble = null
        executor.shutdownNow()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
