package com.oae.longhao
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.LocationManager
import android.util.Log



class CheckLocationEnabled(_mainActivity: Activity) {
    private val mainActivity = _mainActivity as Activity
    private val mainContext = _mainActivity as Context

    fun statusCheck(): Boolean {
        val manager = mainActivity.getSystemService(LOCATION_SERVICE) as LocationManager?
        return if(manager != null) {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } else {
            Log.v("CheckLocationEnabled/statusCheck","manager not found")
            false
        }
    }
/*
    @Composable
    private fun buildAlertMessageNoGps() {
        AlertDialog(
            onDismissRequest = {   },
            title = {
                Text(text = "位置情報を使いますか？")
            },
            text = {
                Text("GPSから位置情報を取得するとタイムラインを自動記録できます。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        mainContext.startActivity(i)
                    }
                ) {
                    Text("使う")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mainActivity.finish()
                    }
                ) {
                    Text("キャンセル")
                }
            },
        )
    }*/
}