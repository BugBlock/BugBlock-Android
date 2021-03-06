package com.bugblock.loggers

import android.util.Log
import com.bugblock.reporter.Reporter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class CrashLogger {
    companion object {
        private var defaultUncaughtExceptionHandler : Thread.UncaughtExceptionHandler? = null

        fun startCrashDetecting() {
            if (defaultUncaughtExceptionHandler == null) {
                defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
            }
            Thread.setDefaultUncaughtExceptionHandler { t, e ->
                var crashLogs = Log.getStackTraceString(e)
                if (e.cause != null) {
                    crashLogs += Log.getStackTraceString(e.cause)
                }
                GlobalScope.launch {
                    Reporter.reportCrash(crashLogs)
                    defaultUncaughtExceptionHandler?.uncaughtException(t, e)
                }
            }
        }

        fun stopCrashDetecting() {
            if (defaultUncaughtExceptionHandler != null) {
                Thread.setDefaultUncaughtExceptionHandler { t, e ->
                    defaultUncaughtExceptionHandler?.uncaughtException(t, e)
                }
            }
        }
    }
}