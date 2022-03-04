package com.oae.longhao

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
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
import androidx.navigation.NavController

class Signals(_MainContext: Context) {
    private val mainContext = _MainContext
    fun getSignalStrength(): Int {
        val telephonyManager: TelephonyManager =
            mainContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            telephonyManager.signalStrength?.level ?: -1
        } else {
            -1
        }
    }
    fun isOnline(): Boolean {
        val connMgr =
            mainContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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