package org.kami911.wifion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

/**
 * Foreground service that re-enables Wi-Fi whenever the screen turns on.
 *
 * Exists because OEM power-save managers (MIUI, Samsung, Huawei, ...) can kill
 * the app entirely while the device sleeps, so a plain BroadcastReceiver in the
 * manifest for ACTION_SCREEN_ON (not usable from the manifest anyway) or a
 * one-shot check isn't enough — this needs a live process watching for the
 * event, and battery-optimization exemption to survive.
 */
class WifiOnService : Service() {

    private val screenOnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_ON) {
                Log.d(TAG, "Screen on — checking Wi-Fi state")
                WifiHelper.enableWifiIfNeeded(context)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            else 0
        )
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenOnReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(screenOnReceiver, filter)
        }
        isRunning = true
        Log.d(TAG, "Service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try {
            unregisterReceiver(screenOnReceiver)
        } catch (_: IllegalArgumentException) {
            // already unregistered
        }
        isRunning = false
        Log.d(TAG, "Service stopped")
        super.onDestroy()
    }

    private fun buildNotification() =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wifi)
            .setContentTitle(getString(R.string.service_notification_title))
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()

    private fun ensureNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.service_notification_channel_name),
            NotificationManager.IMPORTANCE_MIN
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "WifiOn:Service"
        private const val CHANNEL_ID = "wifi_on_service_channel"
        private const val NOTIFICATION_ID = 1002

        @Volatile
        var isRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, WifiOnService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WifiOnService::class.java))
        }
    }
}
