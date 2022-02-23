package com.oae.longhao

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Looper
import android.net.Uri
import android.provider.Settings
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.*
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.oae.longhao.ui.theme.LonghaoTheme
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.roundToInt


var usbIoManager: SerialInputOutputManager? = null
private lateinit var locationCallback: LocationCallback

//TODO:そのうちLocationPermissionsをLocationに継承して位置情報関連をまとめる
class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val globalVar = globalVariable.getInstance()
    private lateinit var sseConnection: SseConnection

    private val locationPermissions = LocationPermissions(this)

    // latitude,longitude,altitude
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
        //有効にするの忘れないで
        //SseConnection("http://192.168.3.16/operation/stream")

        //メモ writeAsyncみたいなので送信できたはず
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
        if(locationPermissions.checkLocationPermission()) {
            Looper.myLooper()?.let {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    it
                )
            }
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
                    App()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        usbIoManager?.stop()
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
        Log.v("sendlocation", "test")
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
                    MotorSlider(navController)
                }
            }
            composable(route = "PermissionPage") {
                NeedPermissionScreen(navController)
            }
        }
        if(locationPermissions.checkLocationPermission()){
            navController.navigate("MainScreen")
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
                Log.v("slider'sRadio","changed")
                //usbIoManager?.writeAsync(intPosition.toString().toByteArray(Charsets.UTF_8))
                usbIoManager?.writeAsync("1032".toByteArray(Charsets.UTF_8))
                Log.v("test", usbIoManager?.state.toString())
            },
            valueRange = 1000f..2000f,
            //steps = 1
        )
    }
    @Composable
    fun NeedPermissionScreen(navController: NavController) {
        val contentString:Map<String,String> = if (locationPermissions.checkLocationPermission()) {
            mapOf("button" to "進む", "text" to "位置情報の権限は既に取得されています", "icon" to "NavigateNext")
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
                    if (locationPermissions.checkLocationPermission()) {
                        navController.navigate("MainScreen")
                    } else {
                        locationPermissions.getLocationPermission()
                    }
                }) {
                    contentString["icon"]?.let { IconByName(name = it) }
                    contentString["button"]?.let { Text(it) }
                }
            }
        }
    }
    @Composable
    fun IconByName(name: String) {
        val icon: ImageVector? = remember(name) {
            try {
                val cl = Class.forName("androidx.compose.material.icons.outlined.${name}Kt")
                val method = cl.declaredMethods.first()
                method.invoke(null, Icons.Outlined) as ImageVector
            } catch (_: Throwable) {
                null
            }
        }
        if (icon != null) {
            Icon(icon, "$name icon")
        }
    }
}
