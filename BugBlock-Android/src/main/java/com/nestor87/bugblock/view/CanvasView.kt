package com.nestor87.bugblock.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.annotation.ColorInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


internal class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var screenshotBitmap: Bitmap
    private val strokeWidth = 7f
    private var drawColor = Color.RED

    private val paint = Paint().apply {
        color = drawColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = this@CanvasView.strokeWidth
    }

    private val drawnPaths = linkedMapOf<Path, @androidx.annotation.ColorInt Int>()
    private val pathsForRedo = linkedMapOf<Path, @androidx.annotation.ColorInt Int>()

    private var currentPath = Path()


    private val _isUndoPossible = MutableLiveData(false)
    val isUndoPossible: LiveData<Boolean> = _isUndoPossible

    private val _isRedoPossible = MutableLiveData(false)
    val isRedoPossible: LiveData<Boolean> = _isRedoPossible

    private lateinit var viewRect: RectF

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        viewRect = RectF(0F, 0F, width.toFloat(), height.toFloat())
        val viewCornerRadius = convertDpToPx(context, 10F)
        val clipPath = Path()
        clipPath.addRoundRect(viewRect, viewCornerRadius, viewCornerRadius, Path.Direction.CW)
        canvas.clipPath(clipPath)

        if (this::screenshotBitmap.isInitialized) {
            canvas.drawBitmap(screenshotBitmap, 0f, 0f, null)
        }

        drawnPaths.forEach {
            paint.color = it.value
            canvas.drawPath(it.key, paint)
            paint.color = drawColor
        }
        canvas.drawPath(currentPath, paint)

        super.onDraw(canvas)


    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath.moveTo(event.x, event.y)
                pathsForRedo.clear()
                _isRedoPossible.value = false
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(event.x, event.y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                drawnPaths[currentPath] = paint.color
                currentPath = Path()
                _isUndoPossible.value = true
            }
        }
        return true
    }


    fun drawBitmap(bitmap: Bitmap) {
        screenshotBitmap = bitmap
        invalidate()
    }

    fun undo() {

        pathsForRedo[drawnPaths.keys.last()] = drawnPaths.values.last()

        drawnPaths.remove(drawnPaths.keys.last())

        if (drawnPaths.size == 0) {
            _isUndoPossible.value = false
        }
        _isRedoPossible.value = true

        invalidate()
    }

    fun redo() {
        drawnPaths[pathsForRedo.keys.last()] = pathsForRedo.values.last()

        pathsForRedo.remove(pathsForRedo.keys.last())

        if (pathsForRedo.size == 0) {
            _isRedoPossible.value = false
        }
        _isUndoPossible.value = true

        invalidate()
    }

    fun changePaintColor(@ColorInt color: Int) {
        paint.color = color
        drawColor = color
    }

    fun capture(window: Window, bitmapCallback: (Bitmap) -> Unit) {
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
            val location = IntArray(2)
            this.getLocationInWindow(location)
            PixelCopy.request(window,
                Rect(location[0], location[1], location[0] + this.width, location[1] + this.height),
                bitmap,
                {
                    if (it == PixelCopy.SUCCESS) {
                        bitmapCallback.invoke(bitmap)
                    }
                },
                Handler(Looper.getMainLooper()) )
        } else {
            val tBitmap = Bitmap.createBitmap(
                this.width, this.height, Bitmap.Config.RGB_565
            )
            val canvas = Canvas(tBitmap)

            this.draw(canvas)
            canvas.setBitmap(null)
            bitmapCallback.invoke(tBitmap)
        }
    }

    private fun convertDpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}