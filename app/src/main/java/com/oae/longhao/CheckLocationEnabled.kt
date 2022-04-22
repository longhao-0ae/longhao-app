package com.oae.longhao
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
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
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController


class CheckLocationEnabled(_mainActivity: Activity) {
    private val mainActivity = _mainActivity

    fun statusCheck(): Boolean {
        val manager = mainActivity.getSystemService(LOCATION_SERVICE) as LocationManager?
        return if(manager != null) {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } else {
            Log.v("CheckLocationEnabled/statusCheck","manager not found")
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
                Text("位置情報を有効にしてください")
                Button(onClick = {
                    val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    mainActivity.startActivity(i)
                }) {
                    Text("設定を開く")
                }
            }
        }
    }
}