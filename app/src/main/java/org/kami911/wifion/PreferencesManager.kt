package org.kami911.wifion

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Re-enable Wi-Fi whenever the screen turns on (e.g. after the device wakes from power-save). */
    var autoEnableOnScreenOn: Boolean
        get() = prefs.getBoolean(KEY_AUTO_ENABLE_SCREEN_ON, true)
        set(value) = prefs.edit { putBoolean(KEY_AUTO_ENABLE_SCREEN_ON, value) }

    companion object {
        private const val PREFS_NAME = "wifi_on_prefs"
        private const val KEY_AUTO_ENABLE_SCREEN_ON = "auto_enable_screen_on"
    }
}
