package com.ah.taplock

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.animation.AccelerateDecelerateInterpolator

class TapLockAccessibilityService : AccessibilityService() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null
    private var lockSequenceRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Intent.ACTION_SCREEN_OFF && !lockSequenceRunning) {
            val prefs = getSharedPreferences(getString(R.string.shared_pref_name), Context.MODE_PRIVATE)
            val flickerProtection = prefs.getBoolean("flicker_protection", false)

            if (!flickerProtection) {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            } else {
                startScreenOffSequence()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {
        cancelPendingSequence()
        hideBlackOverlay()
    }

    override fun onDestroy() {
        cancelPendingSequence()
        hideBlackOverlay()
        super.onDestroy()
    }

    private fun startScreenOffSequence() {
        lockSequenceRunning = true
        showBlackOverlay()

        mainHandler.postDelayed(
            {
                try {
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                } finally {
                    mainHandler.postDelayed(
                        {
                            hideBlackOverlay()
                            lockSequenceRunning = false
                        },
                        OVERLAY_CLEANUP_DELAY_MS
                    )
                }
            },
            OVERLAY_BEFORE_LOCK_DELAY_MS
        )

        mainHandler.postDelayed(
            {
                hideBlackOverlay()
                lockSequenceRunning = false
            },
            FAILSAFE_CLEANUP_DELAY_MS
        )
    }

    private fun showBlackOverlay() {
        if (overlayView != null) {
            return
        }

        val windowManager = getSystemService(WindowManager::class.java) ?: run {
            lockSequenceRunning = false
            return
        }
        val view = View(this).apply {
            setBackgroundColor(Color.BLACK)
            alpha = 0f
            visibility = View.VISIBLE
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            title = "TapLockBlackOverlay"
        }

        try {
            windowManager.addView(view, layoutParams)
            overlayView = view
            view.animate().cancel()
            view.animate()
                .alpha(1f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(OVERLAY_FADE_IN_DURATION_MS)
                .start()
        } catch (_: WindowManager.BadTokenException) {
            lockSequenceRunning = false
        } catch (_: IllegalStateException) {
            lockSequenceRunning = false
        }
    }

    private fun hideBlackOverlay() {
        val view = overlayView ?: return
        val windowManager = getSystemService(WindowManager::class.java) ?: run {
            overlayView = null
            return
        }

        try {
            view.animate().cancel()
            windowManager.removeViewImmediate(view)
        } catch (_: IllegalArgumentException) {
        } finally {
            overlayView = null
        }
    }

    private fun cancelPendingSequence() {
        mainHandler.removeCallbacksAndMessages(null)
        lockSequenceRunning = false
    }

    private companion object {
        const val OVERLAY_BEFORE_LOCK_DELAY_MS = 350L
        const val OVERLAY_CLEANUP_DELAY_MS = 350L
        const val OVERLAY_FADE_IN_DURATION_MS = 350L
        const val FAILSAFE_CLEANUP_DELAY_MS = 1_000L
    }

}
