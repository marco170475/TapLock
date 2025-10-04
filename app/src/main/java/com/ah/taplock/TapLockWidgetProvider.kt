package com.ah.taplock

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.annotation.RequiresPermission

class TapLockWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_WIDGET_TAP = "com.ah.taplock.widget.TAP"
        private var lastTapTime = 0L
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_WIDGET_TAP -> {
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    handleWidgetTap(context)
                }
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
            setOnClickPendingIntent(
                R.id.widget_container,
                getPendingSelfIntent(context, appWidgetId)
            )
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun handleWidgetTap(context: Context) {
        val prefs = context.getSharedPreferences(R.string.shared_pref_name.toString(), Context.MODE_PRIVATE)
        val timeout = prefs.getInt(context.getString(R.string.double_tap_timeout), 300)

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTime < timeout) {
            lockScreen(context, "widget")
        }
        lastTapTime = currentTime
    }

    private fun getPendingSelfIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, TapLockWidgetProvider::class.java).apply {
            this.action = ACTION_WIDGET_TAP
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}