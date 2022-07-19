package com.oae.longhao

import android.Manifest
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController

class Signals(_MainContext: Context) {
    private val mainContext = _MainContext

    fun getSignalStrength(): String {
        val sigLevel: Int;
        var networkType = "0";
        val telephonyManager: TelephonyManager =
            mainContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            sigLevel = telephonyManager.signalStrength?.level ?: -1
            if (sigLevel > 0) {
                if (ActivityCompat.checkSelfPermission(
                        mainContext,
                        Manifest.permission.READ_PHONE_STATE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    networkType =
                        when (telephonyManager.dataNetworkType) {
                            NETWORK_TYPE_EDGE, NETWORK_TYPE_GPRS, NETWORK_TYPE_CDMA, NETWORK_TYPE_IDEN, NETWORK_TYPE_1xRTT ->
                                "2G"
                            NETWORK_TYPE_UMTS, NETWORK_TYPE_HSDPA, NETWORK_TYPE_HSPA, NETWORK_TYPE_HSPAP, NETWORK_TYPE_EVDO_0, NETWORK_TYPE_EVDO_A, NETWORK_TYPE_EVDO_B ->
                                "3G"
                            NETWORK_TYPE_LTE -> "4G"
                            NETWORK_TYPE_NR -> "5G"
                            else -> "Unknown"
                        }
                }
            } else {
                networkType = "-1"
            }
        }
        return networkType
    }
    fun getWifiInfo(): String {
        val wifiManager: WifiManager =
            mainContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info: WifiInfo = wifiManager.connectionInfo
        val ipAdr = info.ipAddress
        val ip = String.format(
            "%02d.%02d.%02d.%02d",
            ipAdr shr 0 and 0xff,
            ipAdr shr 8 and 0xff,
            ipAdr shr 16 and 0xff,
            ipAdr shr 24 and 0xff
        )
        return String.format(
            """
            {
            "type": "WIFI"
            "ipAddress": "%s",
            "ssid": %s,
            "signalStrength": "%s",
            "frequency": "%s"
            }
        """,
            ip,
            info.ssid,
            WifiManager.calculateSignalLevel(info.rssi, 5).toString(),
            info.frequency.toString()
        ).trimIndent()
    }
    fun checkNetworkType(): Int {
        val connectivityManager = mainContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return -1
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return -1
        return when {
            // Indicates this network uses a Wi-Fi transport,
            // or WiFi has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 1
            // Indicates this network uses a Cellular transport. or
            // Cellular has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> 2
            // else return false
            else -> 0
        }
    }
    fun isOnline(): Boolean {
        val connMgr =
            mainContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connMgr.getNetworkCapabilities(connMgr.activeNetwork)
        return capabilities != null
    }
    @Composable
    fun NeedNetworkScreen(navController: NavController) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Icon(Icons.Outlined.WifiOff,"offline",modifier = Modifier.fillMaxSize(0.3F))
                Text("インターネット接続が必要です")
            }
        }
    }
}