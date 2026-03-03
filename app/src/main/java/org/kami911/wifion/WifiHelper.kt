package org.kami911.wifion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object WifiHelper {

    private const val TAG = "WifiOn:WifiHelper"
    private const val CHANNEL_ID = "wifi_on_channel"
    private const val NOTIFICATION_ID = 1001

    /**
     * Check current Wi-Fi state and enable it if needed.
     * On Android 10+ (API 29+), direct programmatic enabling is restricted,
     * so a notification is shown prompting the user to open Wi-Fi settings.
     */
    fun enableWifiIfNeeded(context: Context) {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (wifiManager.isWifiEnabled) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Wi-Fi is already enabled — nothing to do")
            }
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Android 8 & 9: direct enable is permitted
            @Suppress("DEPRECATION")
            val result = wifiManager.setWifiEnabled(true)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "setWifiEnabled(true) result: $result")
            }
        } else {
            // Android 10+: cannot enable Wi-Fi programmatically
            Log.i(TAG, "Android 10+: showing notification to enable Wi-Fi")
            showEnableWifiNotification(context)
        }
    }

    /**
     * Returns true if Wi-Fi is currently enabled.
     */
    fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

    /**
     * Shows a persistent notification that opens Wi-Fi settings when tapped.
     * Only posted when the app has POST_NOTIFICATIONS permission (Android 13+
     * runtime grant handled in MainActivity; older versions grant at install).
     */
    fun showEnableWifiNotification(context: Context) {
        ensureNotificationChannel(context)

        val settingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            settingsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wifi_off)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted — notification skipped", e)
        }
    }

    private fun ensureNotificationChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        manager.createNotificationChannel(channel)
    }
}
