package com.nestor87.bugblock.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import com.nestor87.bugblock.ui.screenshotDraw.ScreenshotDrawActivity
import java.io.File

internal class ScreenshotObserverService : Service() {
    private lateinit var contentObserver: ContentObserver
    private var lastUri = ""


    companion object {

        fun startService(context: Context) {
            val startIntent = Intent(context, ScreenshotObserverService::class.java)
            context.startService(startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ScreenshotObserverService::class.java)
            context.stopService(stopIntent)
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startObserveScreenshot()
        startObserveLogs()

        return START_NOT_STICKY
    }

    private fun startObserveScreenshot() {
        val screenshotsPath = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Screenshots")

        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)

                if (uri.toString() != lastUri) {
                    lastUri = uri.toString()

                    vibrate(300L)
                    val intent = Intent(this@ScreenshotObserverService, ScreenshotDrawActivity::class.java)
                    intent.putExtra("screenshotUri", uri.toString())
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }

        applicationContext.contentResolver.registerContentObserver(
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
        applicationContext.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
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
        applicationContext.contentResolver.query(uri, projection,null, null, null)?.use { cursor ->
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

    private fun startObserveLogs() {
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        applicationContext.contentResolver.unregisterContentObserver(contentObserver)
        super.onDestroy()
    }

    fun vibrate(duration: Long) {
        val vibrator = applicationContext?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(duration)
        }
    }
}