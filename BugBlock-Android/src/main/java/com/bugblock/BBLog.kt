package com.bugblock

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.bugblock.data.BBConfiguration
import com.bugblock.data.BBSharedPreferences
import com.bugblock.data.BBUser
import com.bugblock.data.dto.ConsoleLogLevel
import com.bugblock.data.network.Network
import com.bugblock.loggers.ConsoleLogger
import com.bugblock.loggers.CrashLogger
import com.bugblock.loggers.NetworkLogger
import com.bugblock.observers.ScreenshotObserver
import com.bugblock.data.dto.Metadata
import com.bugblock.reporter.Reporter
import com.bugblock.observers.ShakeObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import java.util.*

class BBLog : Application.ActivityLifecycleCallbacks {
    private var sharedPreferences: BBSharedPreferences
    private var shakeObserver: ShakeObserver
    private var screenshotObserver: ScreenshotObserver
    private var isRunning = false

    companion object {
        internal lateinit var metadata: Metadata
        internal lateinit var appId: String
        internal lateinit var configuration: BBConfiguration
    }

    val okhttpLoggingInterceptor: Interceptor
        get() {
            if (!isRunning) {
                throw Exception("BBLog is not started")
            }
            return if (configuration.serverLoggingEnabled) {
                NetworkLogger.loggingInterceptor
            } else {
                throw Exception("Server logging is disabled. Enable it in the configuration")
            }
        }

    constructor(application: Application) {
        sharedPreferences = BBSharedPreferences(application)
        metadata = getMetadata(application)
        screenshotObserver = ScreenshotObserver(application)
        shakeObserver = ShakeObserver(application)

        application.registerActivityLifecycleCallbacks(this)
    }

    fun start(appId: String, configuration: BBConfiguration) {
        BBLog.appId = appId
        BBLog.configuration = configuration

        if (configuration.invokeByScreenshot) {
            screenshotObserver.start()
        }
        if (configuration.invokeByShake) {
            shakeObserver.start()
        }
        if (configuration.crashReportingEnabled) {
            CrashLogger.startCrashDetecting()
        }

        if (sharedPreferences.userUUID == null) {
            sharedPreferences.userUUID = UUID.randomUUID().toString()
        }

        GlobalScope.launch {
            Reporter.sendMetadata()
        }

        isRunning = true
    }

    fun stop() {
        if (configuration.invokeByScreenshot) {
            screenshotObserver.stop()
        }
        if (configuration.invokeByShake) {
            shakeObserver.stop()
        }
        if (configuration.crashReportingEnabled) {
            CrashLogger.stopCrashDetecting()
        }
        isRunning = false
    }

    private fun setForegroundActivity(activity: Activity) {
        if (configuration.invokeByScreenshot) {
            screenshotObserver.setForegroundActivity(activity)
        }
        if (configuration.invokeByShake) {
            shakeObserver.setForegroundActivity(activity)
        }
    }

    fun user(user: BBUser) {
        if (user.uuid != null) {
            sharedPreferences.userUUID = user.uuid
        }
        sharedPreferences.userName = user.name
        sharedPreferences.userEmail = user.email
    }

    fun consoleLog(
        tag: String,
        message: String,
        logLevel: ConsoleLogLevel = ConsoleLogLevel.INFO
    ) = ConsoleLogger.log(configuration, tag, message, logLevel)

    fun report(email: String, description: String) {
        GlobalScope.launch {
            Reporter.reportIssue(email, description)
        }
    }


    private fun getMetadata(context: Context): Metadata {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return Metadata(
            osVersion = Build.VERSION.SDK_INT,
            appVersion = packageInfo.versionName,
            appBuild = packageInfo.versionCode,
            appPackageName = context.packageName,
            deviceName = "${Build.MANUFACTURER} ${Build.PRODUCT}",
            networkType = Network.getNetworkType(context),
            userUUID = sharedPreferences.userUUID,
            userEmail = sharedPreferences.userEmail,
            userName = sharedPreferences.userName
        )
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        setForegroundActivity(activity)
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}