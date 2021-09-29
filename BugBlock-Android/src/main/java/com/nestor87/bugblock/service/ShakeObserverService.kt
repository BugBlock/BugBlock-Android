package com.nestor87.bugblock.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import com.nestor87.bugblock.ui.reportIssue.ReportIssueActivity
import com.nestor87.bugblock.ui.screenshotDraw.ScreenshotDrawActivity
import kotlin.math.abs
import kotlin.math.sqrt

internal class ShakeObserverService : Service(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var firstDirectionChangeTime: Long = 0
    private var lastDirectionChangeTime: Long = 0
    private var directionChangeCount = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    companion object {
        fun startService(context: Context) {
            val startIntent = Intent(context, ShakeObserverService::class.java)
            context.startService(startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ShakeObserverService::class.java)
            context.stopService(stopIntent)
        }

        private const val MIN_FORCE = 11
        private const val MIN_DIRECTION_CHANGE = 3
        private const val MAX_PAUSE_BETWEEN_DIRECTION_CHANGE = 200
        private const val MAX_TOTAL_DURATION_OF_SHAKE = 500

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager!!.registerListener(this, sensorManager!!
            .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        return START_STICKY
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

    override fun onDestroy() {
        sensorManager!!.unregisterListener(this)
        super.onDestroy()
    }

    private fun resetShakeParameters() {
        firstDirectionChangeTime = 0
        directionChangeCount = 0
        lastDirectionChangeTime = 0
        lastX = 0F
        lastY = 0F
        lastZ = 0F
    }

    private fun onShakeDetected() {
        if (!ReportIssueActivity.running) {
            vibrate(300L)
            val intent = Intent(this@ShakeObserverService, ReportIssueActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
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