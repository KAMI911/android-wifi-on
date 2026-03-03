package org.kami911.wifion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Boot completed received — checking Wi-Fi state")
        }

        WifiHelper.enableWifiIfNeeded(context)
    }

    companion object {
        private const val TAG = "WifiOn:BootReceiver"
    }
}
