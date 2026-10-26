package com.jinny.ffaimtrainer

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.FrameLayout
import kotlin.math.max

class MainActivity : Activity() {
    private val bg = Color.rgb(5, 12, 28)
    private val panel = Color.rgb(10, 25, 48)
    private val cyan = Color.rgb(0, 229, 255)
    private val muted = Color.rgb(139, 178, 205)
    private lateinit var arena: AimArenaView
    private lateinit var scoreText: TextView
    private lateinit var hitText: TextView
    private lateinit var fovText: TextView
    private lateinit var speedText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = 0

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(14))
            setBackgroundColor(bg)
            layoutParams = ViewGroup.LayoutParams(-1, -1)
        }
        root.addView(label("JINNY  /  TRAINING LAB", 11, cyan, true))
        root.addView(label("FF AIM TRAINER", 28, Color.WHITE, true).apply {
            setPadding(0, dp(3), 0, dp(2))
        })
        root.addView(label("Luyện kéo tâm • Không kết nối với Free Fire", 12, muted, false))

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(panel, 16)
        }
        scoreText = statBlock(stats, "ĐIỂM", "0")
        hitText = statBlock(stats, "TRÚNG", "0")
        root.addView(stats, LinearLayout.LayoutParams(-1, dp(64)).apply { topMargin = dp(16) })

        val arenaFrame = FrameLayout(this).apply {
            background = rounded(Color.rgb(7, 19, 38), 18, Color.rgb(0, 105, 140), dp(1))
            clipToPadding = true
            clipChildren = true
        }
        arena = AimArenaView(this, cyan, panel) { score, hits ->
            scoreText.text = score.toString()
            hitText.text = hits.toString()
        }
        arenaFrame.addView(arena, FrameLayout.LayoutParams(-1, -1))
        root.addView(arenaFrame, LinearLayout.LayoutParams(-1, 0, 1f).apply {
            topMargin = dp(14); bottomMargin = dp(14); height = 0
        })

        val settings = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(panel, 18)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        settings.addView(label("TÙY CHỈNH BÀI TẬP", 12, cyan, true))
        val fovRow = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        fovText = label("FOV  •  120°", 13, Color.WHITE, true)
        fovRow.addView(fovText, LinearLayout.LayoutParams(0, -2, 1f))
        settings.addView(fovRow)
        settings.addView(slider(40, 220, 120) { value ->
            fovText.text = "FOV  •  ${value}°"
            arena.fovDegrees = value.toFloat(); arena.invalidate()
        })

        val speedRow = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        speedText = label("TỐC ĐỘ MỤC TIÊU  •  5", 13, Color.WHITE, true)
        speedRow.addView(speedText, LinearLayout.LayoutParams(0, -2, 1f))
        settings.addView(speedRow)
        settings.addView(slider(1, 10, 5) { value ->
            speedText.text = "TỐC ĐỘ MỤC TIÊU  •  $value"
            arena.targetSpeed = value / 5f
        })

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val reset = actionButton("CHƠI LẠI") { arena.resetRound() }
        val pause = actionButton("TẠM DỪNG") { arena.togglePause() }
        buttons.addView(reset, LinearLayout.LayoutParams(0, dp(46), 1f).apply { rightMargin = dp(6) })
        buttons.addView(pause, LinearLayout.LayoutParams(0, dp(46), 1f).apply { leftMargin = dp(6) })
        settings.addView(buttons, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        root.addView(settings, LinearLayout.LayoutParams(-1, -2))
        root.addView(label("CHẠM VÀO MỤC TIÊU • KÉO TÂM BẰNG NGÓN TAY", 10, muted, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, 0)
        })
        setContentView(root)
    }

    private fun statBlock(parent: LinearLayout, title: String, initial: String): TextView {
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
        column.addView(label(title, 10, muted, true))
        val value = label(initial, 20, Color.WHITE, true)
        column.addView(value)
        parent.addView(column, LinearLayout.LayoutParams(0, -1, 1f))
        return value
    }

    private fun slider(min: Int, max: Int, initial: Int, changed: (Int) -> Unit): SeekBar = SeekBar(this).apply {
        this.max = max - min
        progress = initial - min
        progressTintList = android.content.res.ColorStateList.valueOf(cyan)
        thumbTintList = android.content.res.ColorStateList.valueOf(cyan)
        progressBackgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(40, 65, 85))
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { changed(progress + min) }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun actionButton(text: String, action: () -> Unit): TextView = TextView(this).apply {
        this.text = text
        textSize = 12f
        setTextColor(cyan)
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        background = rounded(Color.rgb(8, 38, 59), 12, cyan, dp(1))
        setOnClickListener { action() }
    }

    private fun label(text: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        this.text = text; textSize = size.toFloat(); setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }
    private fun rounded(color: Int, radius: Int, stroke: Int? = null, strokeWidth: Int = 0) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
        if (stroke != null) setStroke(strokeWidth, stroke)
    }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}

private class AimArenaView(
    context: android.content.Context,
    private val neon: Int,
    private val panelColor: Int,
    private val onStats: (Int, Int) -> Unit
) : View(context) {
    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    private val random = java.util.Random()
    private var targetX = 0f
    private var targetY = 0f
    private var vx = 2.8f
    private var vy = 2.1f
    private var aimX = 0f
    private var aimY = 0f
    private var started = false
    private var paused = false
    private var score = 0
    private var hits = 0
    private var misses = 0
    var fovDegrees = 120f
    var targetSpeed = 1f

    private val tick = object : Runnable {
        override fun run() {
            if (!paused && width > 0 && height > 0) {
                if (!started) {
                    targetX = width * .68f; targetY = height * .38f
                    aimX = width * .5f; aimY = height * .58f
                    started = true
                }
                targetX += vx * targetSpeed
                targetY += vy * targetSpeed
                val radius = targetRadius()
                if (targetX < radius + 8 || targetX > width - radius - 8) vx = -vx
                if (targetY < radius + 8 || targetY > height - radius - 8) vy = -vy
                targetX = targetX.coerceIn(radius + 8, max(radius + 8, width - radius - 8))
                targetY = targetY.coerceIn(radius + 8, max(radius + 8, height - radius - 8))
                invalidate()
            }
            postDelayed(this, 16)
        }
    }

    init { setLayerType(View.LAYER_TYPE_SOFTWARE, null); post(tick) }
    private fun targetRadius() = (width.coerceAtLeast(1) * .045f).coerceIn(18f, 30f)

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat()
        canvas.drawColor(Color.rgb(7, 19, 38))
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.argb(32, 0, 229, 255)
        val step = 32f
        var x = 0f
        while (x < w) { canvas.drawLine(x, 0f, x, h, paint); x += step }
        var y = 0f
        while (y < h) { canvas.drawLine(0f, y, w, y, paint); y += step }

        val fovRadius = (w * (fovDegrees / 360f)).coerceIn(35f, w * .48f)
        paint.color = Color.argb(105, 0, 229, 255); paint.strokeWidth = 2f
        canvas.drawCircle(aimX, aimY, fovRadius, paint)
        paint.color = Color.argb(28, 0, 229, 255); paint.style = android.graphics.Paint.Style.FILL
        canvas.drawCircle(aimX, aimY, fovRadius, paint)

        if (!started) { targetX = w * .68f; targetY = h * .38f; aimX = w * .5f; aimY = h * .58f; started = true }
        val r = targetRadius()
        paint.style = android.graphics.Paint.Style.FILL
        paint.color = Color.argb(40, 0, 229, 255)
        canvas.drawCircle(targetX, targetY, r * 1.8f, paint)
        paint.color = neon
        canvas.drawCircle(targetX, targetY, r, paint)
        paint.color = Color.rgb(4, 22, 38)
        canvas.drawCircle(targetX, targetY, r * .67f, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(targetX, targetY, r * .22f, paint)
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.WHITE
        canvas.drawCircle(targetX, targetY, r + 5, paint)

        paint.color = neon; paint.strokeWidth = 2.2f
        canvas.drawLine(aimX - 17, aimY, aimX - 6, aimY, paint)
        canvas.drawLine(aimX + 6, aimY, aimX + 17, aimY, paint)
        canvas.drawLine(aimX, aimY - 17, aimX, aimY - 6, paint)
        canvas.drawLine(aimX, aimY + 6, aimX, aimY + 17, paint)
        paint.style = android.graphics.Paint.Style.FILL
        paint.textSize = 12f; paint.typeface = Typeface.DEFAULT_BOLD
        paint.color = neon
        canvas.drawText("TARGET", targetX - 24, targetY - r - 13, paint)
        paint.color = Color.rgb(139, 178, 205); paint.textSize = 11f
        canvas.drawText("CHẠM MỤC TIÊU ĐỂ GHI ĐIỂM", 14f, h - 18f, paint)
        if (paused) {
            paint.color = Color.argb(190, 5, 12, 28); canvas.drawRect(0f, 0f, w, h, paint)
            paint.color = Color.WHITE; paint.textSize = 24f
            canvas.drawText("TẠM DỪNG", w / 2 - 66f, h / 2, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                aimX = event.x; aimY = event.y
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    val dx = event.x - targetX; val dy = event.y - targetY
                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                    if (distance <= targetRadius() * 1.4f) {
                        score += 10; hits++
                        targetX = random.nextFloat() * (width * .72f) + width * .14f
                        targetY = random.nextFloat() * (height * .65f) + height * .15f
                        vx = (if (random.nextBoolean()) 1 else -1) * (2f + random.nextFloat() * 2f)
                        vy = (if (random.nextBoolean()) 1 else -1) * (1.5f + random.nextFloat() * 2f)
                    } else { misses++ }
                    onStats(score, hits)
                }
                invalidate(); return true
            }
        }
        return true
    }

    fun resetRound() { score = 0; hits = 0; misses = 0; onStats(score, hits); targetX = width * .68f; targetY = height * .38f; invalidate() }
    fun togglePause() { paused = !paused; invalidate() }
}
