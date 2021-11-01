# BugBlock-Android

## Installation

Add it in your root `build.gradle` at the end of repositories:
``` gradle
allprojects {
    repositories {
        ....
        maven { url 'https://jitpack.io' }
    }
}
```
Add the dependency:
``` gradle
dependencies {
    implementation 'com.github.BugBlock:BugBlock-Android:0.0.6-alpha'
}
```

## Usage

### Library starting
First you need to declare config, for example:
``` kotlin
val config = BBConfiguration()
config.consoleLoggingEnabled = true
config.serverLoggingEnabled = true
config.crashReportingEnabled = true
```

All possible config parameters are: 
``` kotlin
data class BBConfiguration (
    var serverLoggingEnabled: Boolean = false,
    var consoleLoggingEnabled: Boolean = false,
    var crashReportingEnabled: Boolean = false,
    var invokeByScreenshot: Boolean = false,
    var invokeByShake: Boolean = false
)
```

Then create an instance of BBLog  and start the library: 
``` kotlin
val bbLog = BBLog(context)
bbLog.start(appId = "", configuration = config)
```

Set user data(optional):
``` kotlin
bbLog.user(
   BBUser(
       uuid = "UUID", // will be generated automatically by default
       name = "name",
       email = "email@example.com"
   )
)
```

### Report simulation programmatically

``` kotlin
bbLog.report(email = "test@example.com", description = "issue description")
```

### Console logging

Console log example:
``` kotlin
bbLog.consoleLog(tag = "tag", message = "test message", logLevel = ConsoleLogLevel.INFO)
```

Console log levels:
``` kotlin
enum class ConsoleLogLevel {
   DEBUG, INFO, WARNING, ERROR
}
```

### Network traffic logging
 
Create an okHttp client with interceptor from library (you need to enable server logging in configuration):
``` kotlin
 val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(bbLog.okhttpLoggingInterceptor)
    .build()
```

Use client (example with [Retrofit](https://github.com/square/retrofit)):
``` kotlin
val retrofit = Retrofit.Builder()
  .baseUrl("https://example.com")
  .addConverterFactory(GsonConverterFactory.create())
  .client(okHttpClient)
  .build()
```

