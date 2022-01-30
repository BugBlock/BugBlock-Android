package com.bugblock.data

data class BBConfiguration (
    var serverLoggingEnabled: Boolean = false,
    var consoleLoggingEnabled: Boolean = false,
    var crashReportingEnabled: Boolean = false,
    var invokeByScreenshot: Boolean = false,
    var invokeByShake: Boolean = false
)
