package com.ah.taplock

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings

fun isAccessibilityEnabled(context: Context): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    val serviceName = "${context.packageName}/${TapLockAccessibilityService::class.java.name}"
    return enabledServices?.contains(serviceName) == true
}

fun lockScreen(context: Context, vibrate: Boolean = true) {
    if (!isAccessibilityEnabled(context)) {
        val intent = Intent(context, SettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)

        return
    }

    val accessibilityIntent = Intent(context, TapLockAccessibilityService::class.java)
    accessibilityIntent.action = Intent.ACTION_SCREEN_OFF
    context.startService(accessibilityIntent)

    if (vibrate) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val vibrationEffect = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    vibrator.areAllEffectsSupported(VibrationEffect.EFFECT_CLICK) == Vibrator.VIBRATION_EFFECT_SUPPORT_YES -> {
                // API >= 30 con supporto effetti predefiniti
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                // API >= 26
                VibrationEffect.createOneShot(20, 50)
            }
            else -> {
                // API < 26 (deprecato)
                null
            }
        }

        if (vibrationEffect != null) {
            vibrator.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }
}
