package com.ah.taplock

import android.content.Context
import android.provider.Settings

fun isAccessibilityEnabled(context: Context): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    val serviceName = "${context.packageName}/${TapLockAccessibilityService::class.java.name}"
    return enabledServices?.contains(serviceName) == true
}