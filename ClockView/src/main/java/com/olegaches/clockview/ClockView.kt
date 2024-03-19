package com.olegaches.clockview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.getDimensionPixelSizeOrThrow
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.properties.Delegates.notNull


class ClockView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_COLOR = Color.BLACK
        private const val HOUR_STROKE_WIDTH = 16f
        private const val MINUTE_STROKE_WIDTH = 8f
        private const val SECOND_STROKE_WIDTH = 3f
        private const val CIRCLE_STROKE_WIDTH = 30f
        private const val DEFAULT_ARROW_LENGTH = 100f
    }

    private var hourPaint: Paint = Paint().apply {
        color = DEFAULT_COLOR
        strokeWidth = HOUR_STROKE_WIDTH
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private var minutePaint: Paint = Paint().apply {
        color = DEFAULT_COLOR
        strokeWidth = MINUTE_STROKE_WIDTH
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private var secondPaint: Paint = Paint().apply {
        color = DEFAULT_COLOR
        strokeWidth = SECOND_STROKE_WIDTH
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private var circlePaint: Paint = Paint().apply {
        color = DEFAULT_COLOR
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private var dividerPaint: Paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = SECOND_STROKE_WIDTH
        isAntiAlias = true
    }
    private var numberPaint: Paint = Paint().apply {
        color = DEFAULT_COLOR
        isAntiAlias = true
    }
    private var hourLength: Float = 100f
    private var minuteLength: Float = 100f
    private var secondLength: Float = 100f

    private var mHeight: Int by notNull()
    private var mWidth: Int by notNull()
    private var mMinimum: Int by notNull()
    private var mRadius: Int by notNull()
    private var mCentreX: Int by notNull()
    private var mCentreY: Int by notNull()
    private var mHourHandSize: Int by notNull()
    private var mHandSize: Int by notNull()

    private var mPadding = 50
    private var mIsInit = false
    private var mRect = Rect()
    private var mNumbers = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    private var mFontSize: Float = 80f
    private var hourRotation = 0f
    private var minuteRotation = 0f
    private var secondRotation = 0f

    init {
        if(attrs != null && context != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockView)
            val defaultColor = DEFAULT_COLOR
            hourPaint.color = typedArray.getColor(R.styleable.ClockView_hourArrowColor, defaultColor)
            minutePaint.color = typedArray.getColor(R.styleable.ClockView_minuteArrowColor, defaultColor)
            secondPaint.color = typedArray.getColor(R.styleable.ClockView_secondArrowColor, defaultColor)
            val primaryColor = typedArray.getColor(R.styleable.ClockView_primaryColor, defaultColor)
            dividerPaint.color = primaryColor
            numberPaint.color = primaryColor
            val circlePaint = circlePaint
            circlePaint.color = primaryColor
            circlePaint.strokeWidth = typedArray.getDimension(R.styleable.ClockView_circleWidth, CIRCLE_STROKE_WIDTH)
            val defaultArrowLength = DEFAULT_ARROW_LENGTH
            hourLength = typedArray.getDimension(R.styleable.ClockView_hourArrowLength, defaultArrowLength)
            minuteLength = typedArray.getDimension(R.styleable.ClockView_minuteArrowLength, defaultArrowLength)
            secondLength = typedArray.getDimension(R.styleable.ClockView_secondArrowLength, defaultArrowLength)
            mFontSize = typedArray.getDimension(R.styleable.ClockView_fontSize, mFontSize)
            typedArray.recycle()
        }
    }

    private fun init() {
        mHeight = height
        mWidth = width
        mCentreX = mWidth / 2
        mCentreY = mHeight / 2
        mMinimum = min(mHeight.toDouble(), mWidth.toDouble()).toInt()
        mRadius = mMinimum / 2 - mPadding
        mHourHandSize = mRadius - mRadius / 2
        mHandSize = mRadius - mRadius / 4
        mIsInit = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!mIsInit) {
            init()
        }

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        hourRotation = (hour % 12 + minute / 60f) * 360 / 12
        minuteRotation = (minute + second / 60f) * 360 / 60
        secondRotation = second * 360f / 60

        val (centerX, centerY, radius) = listOf(mCentreX, mCentreY, mRadius)

        drawCircle(canvas, centerX, centerY, radius, circlePaint)
        drawArrow(canvas, centerX, centerY, hourLength, hourRotation, hourPaint)
        drawArrow(canvas, centerX, centerY, minuteLength, minuteRotation, minutePaint)
        drawArrow(canvas, centerX, centerY, secondLength, secondRotation, secondPaint)
        drawCenterDot(canvas, centerX, centerY, 10f, dividerPaint)
        drawNumerals(canvas, centerX, centerY, radius - 120, mFontSize, numberPaint, mRect, mNumbers)
        drawDividers(canvas, centerX, centerY, radius - 40, dividerPaint)
        postInvalidateDelayed(250)
    }

    private fun drawArrow(canvas: Canvas, x: Int, y: Int, length: Float, rotation: Float, paint: Paint) {
        val offset = length / 4

        val startX = x - offset * cos(Math.toRadians(rotation.toDouble())).toFloat()
        val startY = y - offset * sin(Math.toRadians(rotation.toDouble())).toFloat()

        val endX = x + length * cos(Math.toRadians(rotation.toDouble())).toFloat()
        val endY = y + length * sin(Math.toRadians(rotation.toDouble())).toFloat()

        canvas.drawLine(startX, startY, endX, endY, paint)
    }

    private fun drawCenterDot(canvas: Canvas, x: Int, y: Int, radius: Float, paint: Paint) {
        canvas.drawCircle(x.toFloat(), y.toFloat(), radius, paint)
    }

    private fun drawCircle(canvas: Canvas, x: Int, y: Int, radius: Int, paint: Paint) {
        canvas.drawCircle(x.toFloat(), y.toFloat(), radius.toFloat(), paint)
    }

    private fun drawNumerals(
        canvas: Canvas,
        x: Int,
        y: Int,
        radius: Int,
        fontSize: Float,
        paint: Paint,
        rect: Rect,
        numbers: IntArray
    ) {
        paint.textSize = fontSize
        for (number in numbers) {
            val num = number.toString()
            paint.getTextBounds(num, 0, num.length, rect)
            val angle = Math.PI / 6 * (number - 3)
            val cx = (x + radius * cos(angle).toFloat() - rect.width() / 2)
            val cy = (y + radius * sin(angle).toFloat() + rect.height() / 2)
            canvas.drawText(num, cx, cy, paint)
        }
    }

    private fun drawDividers(canvas: Canvas, x: Int, y: Int, radius: Int, paint: Paint) {
        for (i in 0 until 60) {
            val angle = Math.toRadians((i * 6).toDouble())
            val cx = x + radius * cos(angle).toFloat()
            val cy = y + radius * sin(angle).toFloat()

            canvas.drawCircle(cx, cy, 5f, paint)
        }
    }
}