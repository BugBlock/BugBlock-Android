# BugBlock-Android Quick start
BugBlock Android SDK is providing you with the ability to get quick bug reports from users with network and custom console logs and an automatic crash reporting tool. To start you need to get an SDK token in our [dashbord.bugblock.io](https://dashboard.bugblock.io "dashboard.bugblock.io")

[![](https://jitpack.io/v/BugBlock/BugBlock-Android.svg)](https://jitpack.io/#BugBlock/BugBlock-Android)


## Le's get started.

Add following repositoty to your root `build.gradle` at the end of repositories:
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
    implementation 'com.github.BugBlock:BugBlock-Android:0.0.11-alpha'
}
```

## First start

After importing an SDK you need to import the project in your App:

``` kotlin
val config = BBConfiguration()
val bbLog = BBLog(context)
bbLog.start(appId = "", configuration = config)
```

You're done.



# Configurations

Once you are already done with the initial setup. Let's go through some tips that may help you configure SDK to your needs.

Before starting SDK we need to create configuration. Struct `BBConfiguration` is responsible for the SDK configuration. All properties are `false` by default. If you need to enable something just pass true to respective property. Here is full list of possible properties:

``` kotlin
val config = BBConfiguration()
config.consoleLoggingEnabled = true
config.serverLoggingEnabled = true
config.crashReportingEnabled = true
config.invokeByScreenshot = true
config.invokeByShake = true
```
All are self-describing but one is special it's `serverLoggingEnabled` it should be used with network configuration only, otherwise, it will not take any effect.


### Network configuration
 
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


## Console logging

To prevent you from reading long lists of console logs produced by the iOS application. You can pass only the logs you need. It's pretty easy to make a custom console log: 



Console log example:
``` kotlin
bbLog.consoleLog(tag = "tag", message = "test message", logLevel = ConsoleLogLevel.INFO)
```

To make it easy to differentiate log by level we've created those levels:
``` kotlin
enum class ConsoleLogLevel {
   DEBUG, INFO, WARNING, ERROR
}
```


## User identity

If you want to identify a user when he reports an issue it will be easier to do with the user identity feature. There is two function that can help you to recognize a user in the report: 

``` kotlin
bbLog.user(
   BBUser(
       uuid = "UUID", // will be generated automatically by default
       name = "name",
       email = "email@example.com"
   )
)
```

## Toolbar customization

Toolbar background color in the report activities is `colorPrimary`, text on it is `colorOnPrimary` 


## Silent reports 

By using this feature you can also send a silent report with user acknowledgment

``` kotlin
bbLog.report(email = "test@example.com", description = "issue description")
```


-------
Then you need to add the code below in `onResume` method of every activity or fragment (for screenshot taking)
``` kotlin
bbLog.setForegroundActivity(this) // for activity
```
``` kotlin
bbLog.setForegroundActivity(activity) // for fragment 
```




