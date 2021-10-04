package com.hieupt.audiowavesimulationprogressbar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.use
import kotlin.math.max
import kotlin.math.min

/**
 * Created by HieuPT on 10/3/2021.
 */
class AudioWaveSimulationView : View {

    var numOfWave: Int = DEFAULT_NUM_OF_WAVE
        set(value) {
            field = value
            requestLayout()
        }

    var numOfBar: Int = DEFAULT_NUM_OF_BAR
        set(value) {
            field = value
            requestLayout()
        }

    var maxBarHeight: Float = DEFAULT_MAX_BAR_HEIGHT
        set(value) {
            field = value
            requestLayout()
        }

    var minBarHeight: Float = DEFAULT_MIN_BAR_HEIGHT
        set(value) {
            field = value
            requestLayout()
        }

    var maxBarWidth: Float = DEFAULT_MAX_BAR_WIDTH
        set(value) {
            field = value
            requestLayout()
        }

    var barSpacingWidthMultiply = DEFAULT_SPACING_WIDTH_MULTIPLY
        set(value) {
            field = value
            postInvalidate()
        }

    var autoBarMaxHeight: Boolean = true
        set(value) {
            field = value
            requestLayout()
        }

    var barUseBackgroundAware: Boolean = false
        set(value) {
            field = value
            postInvalidate()
        }

    var barBackgroundColor: Int = Color.WHITE
        set(value) {
            field = value
            postInvalidate()
        }

    var barStrokeColor: Int = Color.BLACK
        set(value) {
            field = value
            postInvalidate()
        }

    var barStrokeWidth: Float = DEFAULT_BAR_STROKE_WIDTH
        set(value) {
            field = value
            postInvalidate()
        }

    var barCornerRadius: Float = DEFAULT_BAR_CORNER_RADIUS
        set(value) {
            field = value
            postInvalidate()
        }

    private val numOfWaveSpacing: Int
        get() = max(0, numOfWave - 1)

    private var barWidth: Float = 0.0f

    private val waveSpacingWidth: Float
        get() = barSpacingWidth

    private val barSpacingWidth: Float
        get() = barWidth * barSpacingWidthMultiply

    private val heightStep: Float
        get() = (maxBarHeight - minBarHeight) / max(1, numOfBar / 2)

    private val numOfSpacing: Int
        get() = max(0, numOfBar - 1)

    private val totalBarCount: Int
        get() = numOfBar * numOfWave

    private val totalSpacingCount: Int
        get() = max(0, totalBarCount - 1)

    private val calculatedWaveWidth: Float
        get() = numOfBar * barWidth + numOfSpacing * barSpacingWidth

    private val backgroundAwareBarPaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.TRANSPARENT
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    private val _coloredBarPaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
        }
    }

    private val coloredBarPaint: Paint
        get() = _coloredBarPaint.apply {
            color = barBackgroundColor
        }

    private val barPaint: Paint
        get() = if (barUseBackgroundAware) backgroundAwareBarPaint else coloredBarPaint

    private val _strokeBarPaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
    }

    private val strokeBarPaint: Paint
        get() = _strokeBarPaint.apply {
            color = barStrokeColor
            strokeWidth = barStrokeWidth
        }

    private val rectF: RectF = RectF()

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
        context.obtainStyledAttributes(attrs, R.styleable.AudioWaveSimulationView, defStyleAttr, 0)
            .use {
                numOfWave = it.getInt(
                    R.styleable.AudioWaveSimulationView_audioWave_numOfWave,
                    DEFAULT_NUM_OF_WAVE
                )
                numOfBar =
                    it.getInt(
                        R.styleable.AudioWaveSimulationView_audioWave_numOfBar,
                        DEFAULT_NUM_OF_BAR
                    )
                barSpacingWidthMultiply = it.getFloat(
                    R.styleable.AudioWaveSimulationView_audioWave_barSpacingWidthMultiply,
                    DEFAULT_SPACING_WIDTH_MULTIPLY
                )
                maxBarWidth = it.getDimension(
                    R.styleable.AudioWaveSimulationView_audioWave_maxBarWidth,
                    DEFAULT_MAX_BAR_WIDTH
                )
                minBarHeight = it.getDimension(
                    R.styleable.AudioWaveSimulationView_audioWave_minBarHeight,
                    DEFAULT_MIN_BAR_HEIGHT
                )
                if (it.getType(R.styleable.AudioWaveSimulationView_audioWave_maxBarHeight) == TypedValue.TYPE_INT_DEC) {
                    autoBarMaxHeight = true
                    maxBarHeight = DEFAULT_MAX_BAR_HEIGHT
                } else {
                    autoBarMaxHeight = false
                    maxBarHeight = it.getDimension(
                        R.styleable.AudioWaveSimulationView_audioWave_maxBarHeight,
                        DEFAULT_MAX_BAR_HEIGHT
                    )
                }
                if (it.getType(R.styleable.AudioWaveSimulationView_audioWave_barBackgroundColor) == TypedValue.TYPE_INT_DEC) {
                    barUseBackgroundAware = true
                    barBackgroundColor = Color.WHITE
                } else {
                    barUseBackgroundAware = false
                    barBackgroundColor = it.getColor(
                        R.styleable.AudioWaveSimulationView_audioWave_barBackgroundColor,
                        Color.WHITE
                    )
                }
                barStrokeColor = it.getColor(
                    R.styleable.AudioWaveSimulationView_audioWave_barStrokeColor,
                    Color.BLACK
                )
                barStrokeWidth = it.getDimension(
                    R.styleable.AudioWaveSimulationView_audioWave_barStrokeWidth,
                    DEFAULT_BAR_STROKE_WIDTH
                )
                barCornerRadius = it.getDimension(
                    R.styleable.AudioWaveSimulationView_audioWave_barCornerRadius,
                    DEFAULT_BAR_CORNER_RADIUS
                )
            }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (numOfBar * numOfWave > 0) {
            val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
            val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
            barWidth = min(
                (measuredWidth - paddingStart - paddingEnd) / (totalBarCount + totalSpacingCount * barSpacingWidthMultiply),
                maxBarWidth
            )
            if (autoBarMaxHeight) {
                maxBarHeight = measuredHeight.toFloat() - paddingTop - paddingBottom
            }

            val newWidthMeasureSpec =
                if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
                    widthMeasureSpec
                } else {
                    MeasureSpec.makeMeasureSpec(
                        (totalBarCount * barWidth).toInt() + (totalSpacingCount * barSpacingWidth).toInt() + paddingStart + paddingEnd,
                        MeasureSpec.EXACTLY
                    )
                }

            val newHeightMeasureSpec =
                if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
                    heightMeasureSpec
                } else {
                    MeasureSpec.makeMeasureSpec(
                        max(minBarHeight, maxBarHeight).toInt() + paddingTop + paddingBottom,
                        MeasureSpec.EXACTLY
                    )
                }
            super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
        } else {
            setMeasuredDimension(0, 0)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawWaves(it)
        }
    }

    private fun drawWaves(canvas: Canvas) {
        repeat(numOfWave) {
            val offsetLeft =
                (width - calculatedWaveWidth * numOfWave - waveSpacingWidth * numOfWaveSpacing) / 2 + (calculatedWaveWidth + waveSpacingWidth) * it
            val offsetTop = (height - maxBarHeight) / 2
            drawWave(canvas, offsetLeft, offsetTop)
        }
    }

    private fun drawWave(canvas: Canvas, offsetLeft: Float, offsetTop: Float) {
        val centerNumber = numOfBar / 2
        if (numOfBar % 2 == 0) {
            for (i in 0 until centerNumber - 1) {
                drawBar(canvas, i, offsetLeft, offsetTop)
            }
            drawCenterBar(canvas, centerNumber - 1, offsetLeft, offsetTop)
            drawCenterBar(canvas, centerNumber, offsetLeft, offsetTop)
            for (i in (centerNumber + 1) until numOfBar) {
                drawBar(canvas, i, offsetLeft, offsetTop, true)
            }
        } else {
            for (i in 0 until centerNumber) {
                drawBar(canvas, i, offsetLeft, offsetTop)
            }
            drawCenterBar(canvas, centerNumber, offsetLeft, offsetTop)
            for (i in (centerNumber + 1) until numOfBar) {
                drawBar(canvas, i, offsetLeft, offsetTop, true)
            }
        }
    }

    private fun drawBar(
        canvas: Canvas,
        index: Int,
        offsetLeft: Float,
        offsetTop: Float,
        isInvert: Boolean = false
    ) {
        val calculateIndex = if (isInvert) {
            numOfBar - index - 1
        } else {
            index
        }
        val left = index * barSpacingWidth + index * barWidth + offsetLeft
        val top = heightStep / 2 * (numOfBar / 2) - calculateIndex * heightStep / 2 + offsetTop
        val right = left + barWidth
        val bottom = top + minBarHeight + calculateIndex * heightStep
        rectF.set(left, top, right, bottom)
        canvas.drawRoundRect(rectF, barCornerRadius, barCornerRadius, barPaint)
        canvas.drawRoundRect(rectF, barCornerRadius, barCornerRadius, strokeBarPaint)
    }

    private fun drawCenterBar(
        canvas: Canvas,
        index: Int,
        offsetLeft: Float,
        offsetTop: Float
    ) {
        val left = index * barSpacingWidth + index * barWidth + offsetLeft
        val top = offsetTop
        val right = left + barWidth
        val bottom = top + maxBarHeight
        rectF.set(left, top, right, bottom)
        canvas.drawRoundRect(rectF, barCornerRadius, barCornerRadius, barPaint)
        canvas.drawRoundRect(rectF, barCornerRadius, barCornerRadius, strokeBarPaint)
    }

    companion object {
        const val DEFAULT_NUM_OF_WAVE = 1
        const val DEFAULT_NUM_OF_BAR = 5
        const val DEFAULT_SPACING_WIDTH_MULTIPLY = 1.0f
        const val DEFAULT_MAX_BAR_WIDTH = 20.0f //px
        const val DEFAULT_MIN_BAR_HEIGHT = 0.0f //px
        const val DEFAULT_MAX_BAR_HEIGHT = 0.0f //px
        const val DEFAULT_BAR_STROKE_WIDTH = 1.0f //px
        const val DEFAULT_BAR_CORNER_RADIUS = 8.0f //px
    }
}