package com.oae.longhao

import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.util.Log

val phoneStateListener = object : PhoneStateListener() {
    override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
        super.onSignalStrengthsChanged(signalStrength)
        val level = signalStrength?.level
        //んー simささないと動いてるかわからん
        Log.v("StrengthLevel", level.toString())
    }
}