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
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale


class LocationPermissions(_mainContext: Context) {
    private val mainContext = _mainContext
    private val mainActivity = _mainContext as Activity
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

    fun getLocationPermission() {
        if (shouldShowRequestPermissionRationale(mainActivity,Manifest.permission.ACCESS_FINE_LOCATION) ||
            shouldShowRequestPermissionRationale(mainActivity,Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            Toast.makeText(mainContext, "位置情報の権限を許可してください", Toast.LENGTH_LONG).show()
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
}