package com.saveetha.flownotify

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class PieChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var percentage: Float = 0f

    private val piePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.light_gray) // A light gray for the background
    }

    private val rect = RectF()

    fun setPercentage(percentage: Float) {
        this.percentage = percentage
        invalidate() // Redraw the view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val radius = Math.min(width, height) / 2

        rect.set(width / 2 - radius, height / 2 - radius, width / 2 + radius, height / 2 + radius)

        // Draw the background circle
        canvas.drawArc(rect, 0f, 360f, true, backgroundPaint)

        // Set color based on percentage
        val colorRes = when {
            percentage >= 80 -> R.color.green
            percentage >= 50 -> R.color.orange
            else -> R.color.red
        }
        piePaint.color = ContextCompat.getColor(context, colorRes)

        // Draw the progress arc
        val sweepAngle = 360 * (percentage / 100)
        canvas.drawArc(rect, -90f, sweepAngle, true, piePaint)
    }
}
