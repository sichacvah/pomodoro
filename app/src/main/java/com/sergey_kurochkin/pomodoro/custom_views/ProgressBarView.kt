package com.sergey_kurochkin.pomodoro.custom_views

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import com.sergey_kurochkin.pomodoro.R

class ProgressBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val min = width.coerceAtMost(height)
        setMeasuredDimension(min, min)
        rectF.set(0 + strokeWidth / 2, 0 + strokeWidth / 2, min - strokeWidth / 2, min - strokeWidth / 2)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawOval(rectF, backgroundPaint)
        val angle: Float = 360 * progress / (max - min)
        canvas?.drawArc(rectF, startAngle, angle, false, foregroundPaint)
    }


    private var strokeWidth = 4f
    private var animator: ValueAnimator? = null
    fun changeProgress(toProgress: Float) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(progress, toProgress).apply {
            duration = 300
            addUpdateListener {value ->
                progress = value.animatedValue as Float
            }
            start()
        }
    }

    var progress = 0F
        set(value) {
            field = value

            invalidate()
        }
    private val min = 0
    private val max = 100

    private val startAngle = -90F
    private var progressBarColor = Color.RED

    fun changeProgressBarColor(@ColorInt id: Int) {
        progressBarColor = id
        foregroundPaint.color = id
        invalidate()
    }

    private var backgroundBarColor = Color.TRANSPARENT

    private var rectF: RectF = RectF()
    private var backgroundPaint: Paint
    private var foregroundPaint: Paint


    init {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ProgressBarView, 0, 0)
        try {
            strokeWidth = typedArray.getDimension(R.styleable.ProgressBarView_progressBarThickness, strokeWidth)
            progress = typedArray.getFloat(R.styleable.ProgressBarView_progress, progress)
            progressBarColor = typedArray.getColor(R.styleable.ProgressBarView_progressbarColor, progressBarColor)
            backgroundBarColor = typedArray.getColor(R.styleable.ProgressBarView_backgroundColor, backgroundBarColor)
        } finally {
            typedArray.recycle()
        }

        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint.color = backgroundBarColor
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = strokeWidth

        foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        foregroundPaint.color = progressBarColor
        foregroundPaint.style = Paint.Style.STROKE
        foregroundPaint.strokeWidth = strokeWidth
    }

}