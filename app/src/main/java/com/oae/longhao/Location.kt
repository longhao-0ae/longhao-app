package com.oae.longhao

import android.app.Activity
import android.content.Context
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

class Location(_mainActivity: Activity){
    private val mainActivity = _mainActivity
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationUsable = LocationUsable(mainActivity)
    // latitude,longitude,altitude
    var locationVariable = mutableMapOf(1 to 0.0, 2 to 0.0, 3 to 0.0)
    val locationRequest = LocationRequest.create().apply {
        // 精度重視(電力大)と省電力重視(精度低)を両立するため2種類の更新間隔を指定
        interval = 1000
        fastestInterval = 100
        maxWaitTime = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 精度重視
    }

    private val locationCallback:LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                if (location != null) {
                    locationVariable[1] = location.latitude
                    locationVariable[2] = location.longitude
                    locationVariable[3] = location.altitude
                    Log.v("UpdatedLocation", locationVariable.toString())
                }
            }
        }
    }
    public fun start() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity)
        /// 位置情報を更新
        if (locationUsable.checkLocationPermission() && locationUsable.checkLocationEnabled()) {
            Looper.myLooper()?.let {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    it
                )
            }
        }
    }
    fun stop(){
        if(::fusedLocationClient.isInitialized){
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    public fun getValue (): MutableMap<Int, Double> {
        return locationVariable
    }
}