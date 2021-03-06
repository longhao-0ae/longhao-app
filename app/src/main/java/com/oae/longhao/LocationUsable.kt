package com.oae.longhao

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController

class LocationUsable(_mainActivity: Activity) {
    private val mainActivity = _mainActivity

    fun checkLocationPermission(): Boolean {
        return !(ActivityCompat.checkSelfPermission(
            mainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                ||
                ActivityCompat.checkSelfPermission(
                    mainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
    }

    fun checkLocationEnabled(): Boolean {
        val manager = mainActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return if(manager != null) {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } else {
            Log.v("checkLocationEnabled","manager not found")
            false
        }
    }

    @Composable
    fun NeedLocationScreen(navController: NavController) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Icon(Icons.Outlined.LocationOff,"offline",modifier = Modifier.fillMaxSize(0.3F))
                Text("??????????????????????????????????????????")
                Button(onClick = {
                    val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    mainActivity.startActivity(i)
                }) {
                    Text("???????????????")
                }
            }
        }
    }
}