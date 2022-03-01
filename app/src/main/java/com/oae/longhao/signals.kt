package com.oae.longhao

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager

class signals(_MainContext: Context) {
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

}