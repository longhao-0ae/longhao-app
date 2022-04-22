package com.oae.longhao
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
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
    /*
    fun getSignalStrength(): Int {
        val sigLevel: Int;
        val telephonyManager: TelephonyManager =
            mainContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            sigLevel = telephonyManager.signalStrength?.level ?: -1
            if (sigLevel > 0) {
                val networkType =
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
            -1
        }
    }*/
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