package com.bugblock.loggers

import android.util.Log
import com.bugblock.data.BBConfiguration
import com.bugblock.data.dto.ConsoleData
import com.bugblock.data.dto.ConsoleLogLevel

internal class ConsoleLogger {
    companion object {
        private val _consoleLogs = mutableListOf<ConsoleData>()
        val consoleLogs: List<ConsoleData> = _consoleLogs

        fun log(configuration: BBConfiguration, tag: String, message: String, logLevel: ConsoleLogLevel) {
            if (configuration.consoleLoggingEnabled) {
                when (logLevel) {
                    ConsoleLogLevel.INFO -> Log.i(tag, message)
                    ConsoleLogLevel.DEBUG -> Log.d(tag, message)
                    ConsoleLogLevel.WARNING -> Log.w(tag, message)
                    ConsoleLogLevel.ERROR -> Log.e(tag, message)
                }
            }
            _consoleLogs.add(ConsoleData(message = "$tag: $message", logLevel = logLevel))
        }
    }
}