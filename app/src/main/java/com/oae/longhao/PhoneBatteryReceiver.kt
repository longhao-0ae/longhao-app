package com.oae.longhao

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.os.BatteryManager
import android.util.Log

class PhoneBatteryReceiver : BroadcastReceiver() {
    private val TAG = "PhoneBatteryReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        val bm = context.getSystemService(BATTERY_SERVICE) as BatteryManager
        val batLevel: Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val batteryPlugged = bm.isCharging
        /*powerVariable[1] = batLevel
        powerVariable[2] = batteryPlugged*/
        Log.v("battery","changed")
        Log.v(TAG,batLevel.toString())
        Log.v(TAG,batteryPlugged.toString())
    }
}
