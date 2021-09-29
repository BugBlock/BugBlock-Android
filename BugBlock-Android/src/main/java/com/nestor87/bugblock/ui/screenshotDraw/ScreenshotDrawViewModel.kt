package com.nestor87.bugblock.ui.screenshotDraw

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModel
import kotlin.math.abs


internal class ScreenshotDrawViewModel: ViewModel() {


    fun calculateViewRectOnScreen(view: View): RectF {
        return RectF(view.x, view.y,view.x + view.measuredWidth, view.y + view.measuredHeight)
    }

    fun calculateScreenshotImageHeight(marginVertical: Float, brushButton: ImageView, addDescriptionButton: CardView): Float {
        return abs(calculateViewRectOnScreen(brushButton).bottom - calculateViewRectOnScreen(addDescriptionButton).top) - marginVertical * 2
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height

        val matrix = Matrix()

        matrix.postScale(scaleWidth, scaleHeight)


        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, true
        )
        bm.recycle()
         return resizedBitmap
    }

    fun combineTwoDrawables(context: Context, drawable1: Drawable, drawable2: Drawable, gravity: Int = Gravity.CENTER,
                            secondDrawableWidthInDp: Float, secondDrawableHeightInDp: Float): Drawable {

        val finalDrawable = LayerDrawable(arrayOf(drawable1, drawable2))


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            finalDrawable.setLayerGravity(1, gravity)
            finalDrawable.setLayerSize(1, convertDpToPx(context, secondDrawableWidthInDp).toInt(), convertDpToPx(context, secondDrawableHeightInDp).toInt())
        }

        return finalDrawable
    }

    fun convertDpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun saveBitmap(context: Context, bitmap: Bitmap, name: String) {
        val fileOutputStream = context.openFileOutput("$name.png", Context.MODE_PRIVATE)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.close()
    }

    fun addRoundedCornersToBitmap(context: Context, bitmap: Bitmap, radiusDp: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = Color.WHITE
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        val radiusPx = convertDpToPx(context, radiusDp)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, radiusPx, radiusPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

}