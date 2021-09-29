package com.nestor87.bugblock.data

import android.content.Context
import android.content.SharedPreferences

internal class BBSharedPreferences(context: Context) {
    private val PREFERENCE_USER_UUID  = "userUUID"
    private val PREFERENCE_USER_NAME  = "userName"
    private val PREFERENCE_USER_EMAIL  = "userEmail"

    private val preferences: SharedPreferences? = context.getSharedPreferences("BBSharedPreferences", Context.MODE_PRIVATE)

    var userUUID: String?
        get() = preferences?.getString(PREFERENCE_USER_UUID, null)
        set(value) = preferences?.edit()?.putString(PREFERENCE_USER_UUID, value)?.apply()!!

    var userName: String?
        get() = preferences?.getString(PREFERENCE_USER_NAME, null)
        set(value) = preferences?.edit()?.putString(PREFERENCE_USER_NAME, value)?.apply()!!

    var userEmail: String?
        get() = preferences?.getString(PREFERENCE_USER_EMAIL, null)
        set(value) = preferences?.edit()?.putString(PREFERENCE_USER_EMAIL, value)?.apply()!!
}