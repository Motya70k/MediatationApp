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
        color = Color.parseColor("#E6A2DC") // Цвет круга
        strokeWidth = 20f // Толщина линии
        style = Paint.Style.STROKE // Круг будет обводкой, а не заливкой
        isAntiAlias = true
    }

    private var progress: Float = 0f // Прогресс от 0 до 1

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.let {
            val centerX = width / 2f
            val centerY = height / 2f
            val radius = min(centerX, centerY) - paint.strokeWidth // Радиус круга

            // Рисуем полный круг для фона
            paint.color = Color.LTGRAY // Цвет фона круга
            it.drawCircle(centerX, centerY, radius, paint)

            // Рисуем прогресс-круг
            paint.color = Color.parseColor("#E6A2DC") // Цвет таймера
            val sweepAngle = 360 * progress
            it.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                -90f, // Начало с верхней точки
                sweepAngle,
                false, // Не заполняем сектор, рисуем дугу
                paint
            )
        }
    }

    // Функция для обновления прогресса
    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate() // Перерисовать view
    }
}