package org.kami911.wifion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.kami911.wifion.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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

        requestNotificationPermissionIfNeeded()
        updateWifiStatus()

        binding.btnEnableWifi.setOnClickListener {
            WifiHelper.enableWifiIfNeeded(this)
            updateWifiStatus()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !WifiHelper.isWifiEnabled(this)) {
                Toast.makeText(this, R.string.toast_open_settings, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateWifiStatus()
    }

    private fun updateWifiStatus() {
        val enabled = WifiHelper.isWifiEnabled(this)
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
