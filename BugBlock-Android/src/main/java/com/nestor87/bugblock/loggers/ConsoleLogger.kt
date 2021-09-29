package com.nestor87.bugblock.loggers

import android.util.Log
import com.nestor87.bugblock.data.BBConfiguration
import com.nestor87.bugblock.data.dto.ConsoleData
import com.nestor87.bugblock.data.dto.ConsoleLogLevel

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
            _consoleLogs.add(ConsoleData(string = "$tag: $message", logLevel = logLevel))
        }
    }
}