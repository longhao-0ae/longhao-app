package com.oae.longhao

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.navigation.NavController


class LocationPermissions(_mainContext: Context) {
    private val mainContext = _mainContext
    private val mainActivity = _mainContext as Activity
    private val checkLocationEnabled = CheckLocationEnabled(mainActivity)
    private val requestPermission by lazy {
        MainActivity().registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                grant ->
            if(grant[Manifest.permission.ACCESS_FINE_LOCATION] == true && grant[Manifest.permission.ACCESS_COARSE_LOCATION] == true){
                Toast.makeText(mainContext, "権限を取得しました。アプリを再起動してください。", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(mainContext, "権限を取得できませんでした", Toast.LENGTH_LONG).show()
            }
        }
    }

    public fun checkLocationPermission(): Boolean {
        //メモ 位置情報の権限ないと画面真っ白になる
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

    private fun getLocationPermission() {
        if (shouldShowRequestPermissionRationale(mainActivity,Manifest.permission.ACCESS_FINE_LOCATION) ||
            shouldShowRequestPermissionRationale(mainActivity,Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            Toast.makeText(mainContext, "位置情報の権限を許可してください", Toast.LENGTH_LONG).show()
            //メモ  Attempt to invoke virtual methodが出るときはアプリ再インストール
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            Toast.makeText(mainContext, "設定から位置情報の権限を許可してください", Toast.LENGTH_LONG).show()
            val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            i.addCategory(Intent.CATEGORY_DEFAULT)
            i.data = Uri.parse("package:com.oae.longhao")
            mainContext.startActivity(i)
        }
    }
    @Composable
    fun NeedPermissionScreen(navController: NavController) {
        val contentString:Map<String,String> = if (checkLocationPermission()) {
            if(checkLocationEnabled.statusCheck()) {
                mapOf("button" to "進む", "text" to "位置情報の権限と位置情報は有効です", "icon" to "NavigateNext")
            } else {
                mapOf("button" to "有効にする", "text" to "位置情報を有効にしてください", "icon" to "LocationOn")
            }
        } else {
            mapOf("button" to "権限を取得", "text" to "位置情報の権限が必要です", "icon" to "LocationOn")
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                contentString["text"]?.let { Text(it) }
                Button(onClick = {
                    if (checkLocationPermission()) {
                        if(checkLocationEnabled.statusCheck()){
                            navController.navigate("MainScreen")
                        } else {
                            val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            mainActivity.startActivity(i)
                        }
                    } else {
                        getLocationPermission()
                    }
                }) {
                    contentString["icon"]?.let { IconByName(name = it) }
                    contentString["button"]?.let { Text(it) }
                }
            }
        }
    }
}