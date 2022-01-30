package com.bugblock.observers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.bugblock.ui.reportIssue.ReportIssueActivity
import com.bugblock.ui.screenshotDraw.ScreenshotDrawActivity
import kotlin.math.abs

internal class ShakeObserver(val context: Context): SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var firstDirectionChangeTime: Long = 0
    private var lastDirectionChangeTime: Long = 0
    private var directionChangeCount = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var foregroundActivity: Activity? = null

    companion object {
        private const val MIN_FORCE = 11
        private const val MIN_DIRECTION_CHANGE = 3
        private const val MAX_PAUSE_BETWEEN_DIRECTION_CHANGE = 200
        private const val MAX_TOTAL_DURATION_OF_SHAKE = 500

    }

    fun start() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager!!.registerListener(this, sensorManager!!
            .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    fun setForegroundActivity(activity: Activity) {
        foregroundActivity = activity
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val totalMovement = abs(x + y + z - lastX - lastY - lastZ)

        if (totalMovement > MIN_FORCE) {
            val now = System.currentTimeMillis()

            if (firstDirectionChangeTime == 0L) {
                firstDirectionChangeTime = now
                lastDirectionChangeTime = now
            }

            if (now - lastDirectionChangeTime < MAX_PAUSE_BETWEEN_DIRECTION_CHANGE) {
                lastDirectionChangeTime = now
                directionChangeCount++

                lastX = x
                lastY = y
                lastZ = z

                if (directionChangeCount >= MIN_DIRECTION_CHANGE) {

                    val totalDuration = now - firstDirectionChangeTime
                    if (totalDuration < MAX_TOTAL_DURATION_OF_SHAKE) {
                        onShakeDetected()
                        resetShakeParameters()
                    }
                }

            } else {
                resetShakeParameters()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private fun resetShakeParameters() {
        firstDirectionChangeTime = 0
        directionChangeCount = 0
        lastDirectionChangeTime = 0
        lastX = 0F
        lastY = 0F
        lastZ = 0F
    }

    private fun onShakeDetected() {
        if (!ReportIssueActivity.running && foregroundActivity != null) {
            vibrate(300L)
            saveBitmap(
                context,
                takeScreenshot(), // take screenshot of root view (to get screenshot without permission)
                "screenshot"
            )
            foregroundActivity!!.startActivity(Intent(context, ScreenshotDrawActivity::class.java))
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
        val rootView = foregroundActivity!!.window.decorView.rootView
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