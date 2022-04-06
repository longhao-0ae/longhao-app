package com.oae.longhao

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.oae.longhao.ui.theme.LonghaoTheme
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.forEach
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.concurrent.schedule
import kotlin.math.roundToInt

public var port: UsbSerialPort? = null
private lateinit var locationCallback: LocationCallback


class MainActivity : ComponentActivity() {
    private val _mainActivity = this
    val timer = Timer()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val globalVar = globalVariable.getInstance()
    private val sg = Signals(_mainActivity as Context)
    private lateinit var sseConnection: SseConnection
    private val locationPermissions = LocationPermissions(_mainActivity as Context)
    private val checkLocationEnabled = CheckLocationEnabled(_mainActivity)
    // latitude,longitude,altitude
    val locationVariable = mutableMapOf(1 to 0.0, 2 to 0.0, 3 to 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSerial(getSystemService(USB_SERVICE) as UsbManager)
        appSettings(window)
        /*
        Location().test()

        //バッテリ
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(PhoneBatteryReceiver(), intentFilter)

        //sse
        //有効にするの忘れないで
        //SseConnection("http://192.168.3.16/operation/stream")

        //メモ writeAsyncみたいなので送信できたはず
        fusedLocationClient = FusedLocationProviderClient(_mainActivity)
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
        /*
        /// 位置情報を更新
        if(locationPermissions.checkLocationPermission() && checkLocationEnabled.statusCheck()) {
            Looper.myLooper()?.let {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    it
                )
            }
        }
        */

        timer.schedule(0, 5000) {
            val zonedDateTimeString = LocalDateTime.now().toString()
            sendBattery(zonedDateTimeString)
            sendLocation(zonedDateTimeString)
            Log.v("signalStrength",sg.getSignalStrength().toString())
            Log.v("ifOnline",sg.isOnline().toString())
            //port?.write("1032".toByteArray(Charsets.UTF_8),2000)
        }
        */

        setContent {
            LonghaoTheme {
                Surface(color = MaterialTheme.colors.background) {
                    App()
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        usbIoManager?.stop()
        port?.close()
        timer.cancel()
        try {
            sseConnection.closeSse()
        }catch(e:Exception){
            Log.e("sseConnection","cannot close sseConnection. error:${e}")
        }
        Log.i("info", "Destroy")
    }

    private fun sendLocation(zonedDateTimeString: String) {
        val bodyJson = """
            {
                "location":[${locationVariable[2].toString()},${locationVariable[1].toString()}],
                "last_time":"$zonedDateTimeString"
            }
        """
        postData(bodyJson, "/api/location")
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
    @Composable
    fun App() {
        val navController = rememberNavController()
        NavHost(navController, startDestination = "PermissionPage") {
            composable(route = "MainScreen") {
                Column {
                    //MotorSlider(navController)
                    DebuggingBtn()
                }
            }
            composable(route = "PermissionPage") {
                locationPermissions.NeedPermissionScreen(navController)
            }
            composable(route = "NeedNetwork") {
                sg.NeedNetworkScreen(navController)
            }
        }
        if(locationPermissions.checkLocationPermission() && checkLocationEnabled.statusCheck()){
            if(sg.isOnline().not()){
                navController.navigate("NeedNetwork")
            } else {
                navController.navigate("MainScreen")
            }
        }
    }
    @Composable
    fun MotorSlider(navController: NavController) {
        var sliderPosition by remember { mutableStateOf(1000f) }
        val intPosition = (sliderPosition).roundToInt()
        @Composable
        fun radioButtonRow(OkValue: Int){
            RadioButton(
                selected = sliderPosition == OkValue.toFloat(),
                onClick = {
                    sliderPosition = OkValue.toFloat()
                    port?.write(intPosition.toString().toByteArray(Charsets.UTF_8),2000)
                    //TODO USBioManagerとportの関係に関連がありそう 調べる
                }
            )
            Text(
                text = OkValue.toString(),
                modifier = Modifier.padding(start = 5.dp,end = 8.dp)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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
                        Log.v("slider'sRadio", "changed")
                        //intPosition.toString().toByteArray(Charsets.UTF_8)
                        port?.write("{\"motor\":\"114514\"}".toByteArray(Charsets.UTF_8),100)
                    },
                    valueRange = 1000f..2000f,
                    //steps = 1
                )
            }
        }
    }
    @Composable
    fun DebuggingBtn(){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("デバッグ用")
                Button(onClick = {
                    port?.write("{\"motor\":\"1032\"}".toByteArray(Charsets.UTF_8),100)
                }) {
                    Text("1032")
                }
                Button(onClick = {
                    port?.write("{\"motor\":\"114514\"}".toByteArray(Charsets.UTF_8),100)
                }) {
                    Text("114514")
                }
                Button(onClick = {
                    port?.write("{\"motor\":\"123456\"}".toByteArray(Charsets.UTF_8),100)
                }) {
                    Text("123456")
                }
            }
        }
    }
}
