package ru.shvetsov.meditationapp.presentation.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class MeditationTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.parseColor("#E6A2DC")
        strokeWidth = 20f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private var progress: Float = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.let {
            val centerX = width / 2f
            val centerY = height / 2f
            val radius = min(centerX, centerY) - paint.strokeWidth

            paint.color = Color.LTGRAY
            it.drawCircle(centerX, centerY, radius, paint)

            paint.color = Color.parseColor("#E6A2DC")
            val sweepAngle = 360 * progress
            it.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                -90f,
                sweepAngle,
                false,
                paint
            )
        }
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }
}