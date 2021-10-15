package com.nestor87.bugblock.ui.reportIssue
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.nestor87.bugblock.R
import kotlin.math.abs

internal class ReportIssueViewModel {

    fun loadBitmap(context: Context, name: String): Bitmap? {
        val fileInputStream = context.openFileInput("$name.png")
        val bitmap = BitmapFactory.decodeStream(fileInputStream)
        fileInputStream.close()
        return bitmap
    }

    fun calculateScreenshotImageHeight(marginVertical: Float, descriptionEditText: View, sendReportButton: View): Float {
        return abs(calculateViewRectOnScreen(descriptionEditText).bottom - calculateViewRectOnScreen(sendReportButton).top) - marginVertical * 2
    }

    fun calculateViewRectOnScreen(view: View): RectF {
        return RectF(view.x, view.y,view.x + view.measuredWidth, view.y + view.measuredHeight)
    }

    fun checkFieldsNotEmpty(context: Context, emailEditText: EditText, descriptionEditText: EditText, isScreenshotIncluded: Boolean): Boolean {
        return if (emailEditText.text.toString().isBlank()) {
            emailEditText.error = context.getString(R.string.error_field_blank)
            false
        } else if (descriptionEditText.text.toString().isBlank() && !isScreenshotIncluded) {
            descriptionEditText.error = context.getString(R.string.error_field_blank)
            false
        } else {
            true
        }
    }

}