package com.ah.taplock

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.annotation.RequiresPermission

fun isAccessibilityEnabled(context: Context): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    val serviceName = "${context.packageName}/${TapLockAccessibilityService::class.java.name}"
    return enabledServices?.contains(serviceName) == true
}

@RequiresPermission(Manifest.permission.VIBRATE)
fun lockScreen(context: Context, source: String) {
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

    val prefs = context.getSharedPreferences(R.string.shared_pref_name.toString(), Context.MODE_PRIVATE)
    val vibrate = when (source) {
        "widget" -> prefs.getBoolean("vibrate_widget", false)
        "tile" -> prefs.getBoolean("vibrate_tile", false)
        "launcher" -> prefs.getBoolean("vibrate_launcher", false)
        else -> true
    }

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
