package com.oae.longhao

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.os.BatteryManager
import android.util.Log

import androidx.activity.ComponentActivity
/*
class PhoneBatteryReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            /*
          val bm = applicationContext.getSystemService(ComponentActivity.BATTERY_SERVICE) as BatteryManager
            val batLevel: Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val batteryPlugged = bm.isCharging
            Log.i("batLevel",batLevel.toString())
        //    return [batLevel,batteryPlugged]
         //   powerVariable[1] = batLevel
          //  powerVariable[2] = batteryPlugged
          */
        }
}
*/

    /*fun test(){
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(BatteryReceiver, intentFilter)
    }*/


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
