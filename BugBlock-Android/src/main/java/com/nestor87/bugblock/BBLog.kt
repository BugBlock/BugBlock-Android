package com.nestor87.bugblock

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.nestor87.bugblock.data.BBConfiguration
import com.nestor87.bugblock.data.BBSharedPreferences
import com.nestor87.bugblock.data.BBUser
import com.nestor87.bugblock.data.dto.ConsoleLogLevel
import com.nestor87.bugblock.data.network.Network
import com.nestor87.bugblock.loggers.ConsoleLogger
import com.nestor87.bugblock.loggers.CrashLogger
import com.nestor87.bugblock.loggers.NetworkLogger
import com.nestor87.bugblock.observers.ScreenshotObserver
import com.nestor87.bugblock.data.dto.Metadata
import com.nestor87.bugblock.reporter.Reporter
import com.nestor87.bugblock.observers.ShakeObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import java.util.*

class BBLog : Application.ActivityLifecycleCallbacks {
    private var sharedPreferences: BBSharedPreferences
    private var shakeObserver: ShakeObserver
    private var screenshotObserver: ScreenshotObserver

    companion object {
        internal lateinit var metadata: Metadata
        internal lateinit var appId: String
        internal lateinit var configuration: BBConfiguration
    }

    val okhttpLoggingInterceptor: Interceptor
        get() {
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