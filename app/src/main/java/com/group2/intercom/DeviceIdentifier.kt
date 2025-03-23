package com.group2.intercom

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.net.NetworkInterface
import java.util.*

object DeviceIdentifier {

    @SuppressLint("HardwareIds", "MissingPermission")
    fun getDeviceIMEI(context: Context): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // IMEI access restricted; fallback to Android ID
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } else {
                // Use Android ID on older devices as well
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            }
        } catch (e: Exception) {
            "unknown_imei"
        }
    }

    fun getMacAddress(): String {
        return try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.name.equals("wlan0", ignoreCase = true)) {
                    val mac = intf.hardwareAddress ?: return "00:00:00:00:00:00"
                    return mac.joinToString(":") { String.format("%02X", it) }
                }
            }
            "00:00:00:00:00:00"
        } catch (e: Exception) {
            "00:00:00:00:00:00"
        }
    }

    fun generateSeed(context: Context): String {
        val imei = getDeviceIMEI(context)
        val mac = getMacAddress()
        Log.d("DEVICE_INFO", "IMEI: $imei")
        Log.d("DEVICE_INFO", "MAC: $mac")
        return imei + mac
    }
}
