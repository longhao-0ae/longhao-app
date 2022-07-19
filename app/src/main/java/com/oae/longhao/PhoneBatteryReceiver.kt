package com.oae.longhao

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.os.BatteryManager
import android.util.Log

class PhoneBatteryReceiver : BroadcastReceiver() {
    private val logTag = "PhoneBatteryReceiver"
    private val globalVar = globalVariable.getInstance()
    override fun onReceive(context: Context, intent: Intent) {
        val bm = context.getSystemService(BATTERY_SERVICE) as BatteryManager
        val batLevel: Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val batteryPlugged = bm.isCharging
        globalVar.powerVariable[1] = batLevel
        globalVar.powerVariable[2] = batteryPlugged
        Log.v("battery","changed")
        Log.v(logTag,batLevel.toString())
        Log.v(logTag,batteryPlugged.toString())
    }
}
