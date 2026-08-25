package com.mdsultanmahamud.eyesavemode.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.mdsultanmahamud.eyesavemode.R
import com.mdsultanmahamud.eyesavemode.data.SettingsRepository
import com.mdsultanmahamud.eyesavemode.receiver.NotificationActionReceiver

/**
 * Home screen widget: shows current ON/OFF state and dimming %, and toggles
 * Eye Save Mode with a single tap. Reuses [NotificationActionReceiver]'s
 * existing, already-tested ACTION_TOGGLE_POWER handling instead of
 * duplicating the overlay start/stop logic here.
 */
class EyeSaveWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, buildRemoteViews(context))
        }
    }

    companion object {
        /**
         * Called from [SettingsRepository] whenever settings change (power
         * toggled, dimming adjusted, schedule/shake/tile actions, etc.) so
         * every placed widget instance reflects the latest state immediately
         * instead of waiting for the periodic updatePeriodMillis refresh.
         */
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, EyeSaveWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            if (ids.isEmpty()) return

            val views = buildRemoteViews(context)
            ids.forEach { id ->
                appWidgetManager.updateAppWidget(id, views)
            }
        }

        private fun buildRemoteViews(context: Context): RemoteViews {
            val settings = SettingsRepository(context).settings.value
            val views = RemoteViews(context.packageName, R.layout.widget_eye_save)

            if (settings.isEnabled) {
                views.setImageViewResource(R.id.widget_status_dot, R.drawable.widget_status_dot_on)
                views.setTextViewText(R.id.widget_status_text, "ON")
                views.setTextColor(R.id.widget_status_text, context.getColor(R.color.widget_amber_primary))
                views.setTextViewText(R.id.widget_dim_text, "${settings.dimmingPercent}% Dim")
            } else {
                views.setImageViewResource(R.id.widget_status_dot, R.drawable.widget_status_dot_off)
                views.setTextViewText(R.id.widget_status_text, "OFF")
                views.setTextColor(R.id.widget_status_text, context.getColor(R.color.widget_text_muted))
                views.setTextViewText(R.id.widget_dim_text, "Tap to enable")
            }

            val toggleIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_TOGGLE_POWER
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context,
                5001,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, togglePendingIntent)

            return views
        }
    }
}
