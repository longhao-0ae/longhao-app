package com.oae.longhao

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity

import com.oae.longhao.ui.theme.LonghaoTheme

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*

import android.util.Log
import androidx.activity.compose.setContent

import android.content.Intent

import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.util.*
import kotlin.concurrent.schedule
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager

import android.hardware.usb.UsbManager
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.*

import com.hoho.android.usbserial.util.SerialInputOutputManager

import java.time.LocalDateTime
import kotlin.math.roundToInt
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


var usbIoManager: SerialInputOutputManager? = null
private lateinit var locationCallback: LocationCallback

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val globalVar = globalVariable.getInstance()

    // latitude = 1,longitude = 2,altitude = 3
    val locationVariable = mutableMapOf(1 to 0.0, 2 to 0.0, 3 to 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSerial(getSystemService(USB_SERVICE) as UsbManager)
        appSettings(window)
        Location().test()

        //バッテリ
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(PhoneBatteryReceiver(), intentFilter)

        //電波強度?
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)

        //sse
        SseConnection("http://192.168.3.16/operation/stream")

        //メモ writeAsyncみたいなので送信できたはず
        //位置情報の権限チェック
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        fusedLocationClient = FusedLocationProviderClient(this)
        val locationRequest = LocationRequest.create().apply {
            // 精度重視(電力大)と省電力重視(精度低)を両立するため2種類の更新間隔を指定
            // 今回は公式のサンプル通り
            interval = 10000                                   // 最遅の更新間隔(但し不正確)
            fastestInterval = 5000                             // 最短の更新間隔
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 精度重視
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location != null) {
                        locationVariable[1] = location.latitude
                        locationVariable[2] = location.longitude
                        locationVariable[3] = location.altitude
                        Log.v("updated", "gps")
                    }
                }
            }
        }
        /// 位置情報を更新
        Looper.myLooper()?.let {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                it
            )
        }

        Timer().schedule(0, 5000) {
            val zonedDateTimeString = LocalDateTime.now().toString()
            sendBattery(zonedDateTimeString)
            sendLocation(zonedDateTimeString)
            usbIoManager?.writeAsync("1032".toByteArray(Charsets.UTF_8))
            //   this.cancel()
        }
        setContent {
            LonghaoTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        MotorSlider()
                    }
                }
            }
        }

    }

    private fun sendLocation(zonedDateTimeString: String) {
        val bodyJson = """
            {
                "location":[${locationVariable[2].toString()},${locationVariable[1].toString()}],
                "last_time":"$zonedDateTimeString"
            }
        """
        //    postData(bodyJson,"/api/location")
    }

    private fun sendBattery(zonedDateTimeString: String) {
        Log.v("plugged", globalVar.powerVariable[2].toString())
        //zonedDateTimeに問題あり
        val bodyJson = """
            {
                "boat":{
                    "level":${"0"},
                    "charging":${false},
                    "last_time":"$zonedDateTimeString"
                 },
                "phone":{
                    "level": ${globalVar.powerVariable[1].toString()},
                    "charging":${globalVar.powerVariable[2].toString()},
                    "last_time":"$zonedDateTimeString"
                    }
            }
            """
        postData(bodyJson, "/api/battery")
    }
}

    @Composable
    fun MotorSlider() {
        var sliderPosition by remember { mutableStateOf(1000f) }
        val intPosition = (sliderPosition).roundToInt()
        @Composable
        fun radioButtonRow(OkValue: Int){
            RadioButton(
                selected = sliderPosition == OkValue.toFloat(),
                onClick = {
                    sliderPosition = OkValue.toFloat()
                    usbIoManager?.writeAsync(intPosition.toString().toByteArray(Charsets.UTF_8))
                }
            )
            Text(
                text = OkValue.toString(),
                modifier = Modifier.padding(start = 5.dp,end = 8.dp)
            )
        }

        Text(text = intPosition.toString())

        Row(Modifier.selectableGroup()) {
            val radioList = mutableListOf(1000, 1032, 1300, 1500, 2000)
            radioList.forEach { OkValue: Int ->
                radioButtonRow(OkValue)
            }
        }
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it.roundToInt().toFloat()
                usbIoManager?.writeAsync(intPosition.toString().toByteArray(Charsets.UTF_8))
                            },
            valueRange = 1000f..2000f,
            //steps = 1
            )
    }