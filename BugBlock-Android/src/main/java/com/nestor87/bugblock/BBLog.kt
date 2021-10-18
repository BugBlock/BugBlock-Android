package com.nestor87.bugblock

import android.content.Context
import android.os.Build
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

class BBLog(val context: Context) {
    private lateinit var sharedPreferences: BBSharedPreferences

    companion object {
        internal lateinit var metadata: Metadata
        internal lateinit var appId: String
        internal lateinit var configuration: BBConfiguration
    }

    val okhttpLoggingInterceptor: Interceptor get() {
        return if (configuration.serverLoggingEnabled) {
            NetworkLogger.loggingInterceptor
        } else {
            NetworkLogger.emptyInterceptor
        }
    }

    fun start(appId: String, configuration: BBConfiguration) {
        BBLog.appId = appId
        BBLog.configuration = configuration
        sharedPreferences = BBSharedPreferences(context)
        metadata = getMetadata()

        if (configuration.invokeByScreenshot) {
            val screenshotObserver = ScreenshotObserver(context)
            screenshotObserver.start()
        }
        if (configuration.invokeByShake) {
            val shakeObserver = ShakeObserver(context)
            shakeObserver.start()
        }
        if (configuration.crashReportingEnabled) {
            CrashLogger.startCrashDetecting(context)
        }

        if (sharedPreferences.userUUID == null) {
            sharedPreferences.userUUID = UUID.randomUUID().toString()
        }

        GlobalScope.launch {
            Reporter.sendMetadata()
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

    private fun getMetadata(): Metadata {
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
}