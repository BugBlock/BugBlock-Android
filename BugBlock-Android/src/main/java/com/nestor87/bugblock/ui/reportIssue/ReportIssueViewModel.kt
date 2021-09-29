package com.nestor87.bugblock.ui.reportIssue
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.EditText
import com.nestor87.bugblock.R

internal class ReportIssueViewModel {

    fun loadBitmap(context: Context, name: String): Bitmap? {
        val fileInputStream = context.openFileInput("$name.png")
        val bitmap = BitmapFactory.decodeStream(fileInputStream)
        fileInputStream.close()
        return bitmap
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