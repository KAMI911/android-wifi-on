package org.kami911.wifion

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.kami911.wifion.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PreferencesManager

    /** Guards against the switch's change listener firing while we set it programmatically. */
    private var isUpdatingSwitch = false

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "POST_NOTIFICATIONS granted: $granted")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PreferencesManager(this)

        requestNotificationPermissionIfNeeded()
        updateWifiStatus()

        binding.btnEnableWifi.setOnClickListener {
            WifiHelper.enableWifiIfNeeded(this)
            updateWifiStatus()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !WifiHelper.isWifiEnabled(this)) {
                Toast.makeText(this, R.string.toast_open_settings, Toast.LENGTH_LONG).show()
            }
        }

        setSwitch(prefs.autoEnableOnScreenOn)
        binding.switchAutoEnable.setOnCheckedChangeListener { _, checked ->
            if (isUpdatingSwitch) return@setOnCheckedChangeListener
            prefs.autoEnableOnScreenOn = checked
            if (checked) {
                WifiOnService.start(this)
                requestBatteryOptimizationExemptionIfNeeded()
            } else {
                WifiOnService.stop(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        healServiceIfNeeded()
        updateWifiStatus()
    }

    /**
     * The "auto-enable on screen on" preference records what the user wants, not
     * what is actually running — OEM power-save managers can kill the foreground
     * service without clearing it. Restart it here if it's supposed to be running
     * but isn't.
     */
    private fun healServiceIfNeeded() {
        if (prefs.autoEnableOnScreenOn && !WifiOnService.isRunning) {
            WifiOnService.start(this)
        }
    }

    private fun setSwitch(checked: Boolean) {
        isUpdatingSwitch = true
        binding.switchAutoEnable.isChecked = checked
        isUpdatingSwitch = false
    }

    @SuppressLint("BatteryLife")
    private fun requestBatteryOptimizationExemptionIfNeeded() {
        val powerManager = getSystemService(PowerManager::class.java)
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) return

        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:$packageName")
        )
        try {
            startActivity(intent)
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    private fun updateWifiStatus() {
        val enabled = WifiHelper.isWifiEnabled(this)
        binding.ivWifiStatus.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, if (enabled) R.color.wifi_status_on else R.color.wifi_status_off)
        )
        if (enabled) {
            binding.ivWifiStatus.setImageResource(R.drawable.ic_wifi)
            binding.tvStatus.setText(R.string.status_wifi_on)
            binding.tvInfo.setText(R.string.info_wifi_on)
        } else {
            binding.ivWifiStatus.setImageResource(R.drawable.ic_wifi_off)
            binding.tvStatus.setText(R.string.status_wifi_off)
            binding.tvInfo.setText(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    R.string.info_wifi_off_android10
                else
                    R.string.info_wifi_off
            )
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) return

        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    companion object {
        private const val TAG = "WifiOn:MainActivity"
    }
}
