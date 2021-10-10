package com.nestor87.bugblock.observers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import com.nestor87.bugblock.ui.screenshotDraw.ScreenshotDrawActivity
import java.io.File


internal class ScreenshotObserver(val context: Context) {

    private lateinit var contentObserver: ContentObserver
    private var lastUri = ""

    fun start() {
        startObserveScreenshot()
    }

    fun stop() {
        context.contentResolver.unregisterContentObserver(contentObserver)
    }


    private fun startObserveScreenshot() {
        val screenshotsPath = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Screenshots")

        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)

                if (uri.toString() != lastUri) {
                    lastUri = uri.toString()

                    vibrate(300L)
                    saveBitmap(
                        context,
                        takeScreenshot(), // take screenshot of root view (to get screenshot without permission)
                        "screenshot"
                    )
                    context.startActivity(Intent(context, ScreenshotDrawActivity::class.java))
                }
            }
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            queryRelativeDataColumn(Uri.fromFile(screenshotsPath))
        } else {
            queryDataColumn(Uri.fromFile(screenshotsPath))
        }

    }

    private fun queryDataColumn(uri: Uri) {
        val projection = arrayOf(
            MediaStore.Images.Media.DATA
        )
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                val path = cursor.getString(dataColumn)
                if (path.contains("screenshot", true)) {
                    // do something
                }
            }
        }
    }

    private fun queryRelativeDataColumn(uri: Uri) {
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH
        )
        context.contentResolver.query(uri, projection,null, null, null)?.use { cursor ->
            val relativePathColumn = cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
            val displayNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val name = cursor.getString(displayNameColumn)
                val relativePath = cursor.getString(relativePathColumn)
                if (name.contains("screenshot", true) or
                    relativePath.contains("screenshot", true)
                ) {}
            }
        }
    }

    private fun vibrate(duration: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(duration)
        }
    }

    private fun takeScreenshot(): Bitmap {
        val rootView = (context as Activity).window.decorView.rootView
        val bitmap = Bitmap.createBitmap(
            rootView.width,
            rootView.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        rootView.draw(canvas)
        return bitmap
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap, name: String) {
        val fileOutputStream = context.openFileOutput("$name.png", Context.MODE_PRIVATE)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.close()
    }

}