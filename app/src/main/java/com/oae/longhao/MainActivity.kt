package com.oae.longhao

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Bundle
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
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.oae.longhao.ui.theme.LonghaoTheme
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.forEach
import kotlin.collections.mutableListOf
import kotlin.concurrent.schedule
import kotlin.math.roundToInt

public var port: UsbSerialPort? = null

class MainActivity : ComponentActivity() {
    private val _mainActivity = this
    val timer = Timer()
    val globalVar = globalVariable.getInstance()
    private val sg = Signals(_mainActivity as Context)
    private lateinit var sseConnection: SseConnection
    private val permissions = Permissions(_mainActivity as Context)
    private val checkLocationEnabled = CheckLocationEnabled(_mainActivity)
    private val locationClass = Location(_mainActivity)
    private val settings = """
    [
        {
            "Permissions":[
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION"
            ],
            "Name":"位置情報"
        },
        {
            "Permissions":[
                "android.permission.READ_PHONE_STATE"
            ],
            "Name":"電話情報"
        }
    ]
        """
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSerial(getSystemService(USB_SERVICE) as UsbManager)
        appSettings(window)
       // permissions.meinnkamo(settings)
        locationClass.start()
        //バッテリ
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(PhoneBatteryReceiver(), intentFilter)
        //sse
        //有効にするの忘れないで
        SseConnection("http://192.168.3.16/operation/stream")
        timer.schedule(0, 5000) {
            val zonedDateTimeString = LocalDateTime.now().toString()
            if(sg.isOnline()){
                sendLocation(zonedDateTimeString)
            }
           // Log.v("signalStrength",sg.getSignalStrength().toString())
            Log.v("ifOnline",sg.isOnline().toString())
        }

        timer.schedule(0, 60000) {
            val zonedDateTimeString = LocalDateTime.now().toString()
            if(sg.isOnline()){
                sendBattery(zonedDateTimeString)
            }
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
        port?.close()
        timer.cancel()
        locationClass.stop()
        try {
            sseConnection.closeSse()
        }catch(e:Exception){
            Log.e("sseConnection","cannot close sseConnection. error:${e}")
        }
    }

    private fun sendLocation(zonedDateTimeString: String) {
        val locationVariable = locationClass.getValue()
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
                    MotorSlider(navController)
                    // DebuggingBtn()
                }
            }
            composable(route = "PermissionPage") {
                permissions.NeedPermissionScreen(navController, settings)
            }
            composable(route = "NeedNetwork") {
                sg.NeedNetworkScreen(navController)
            }
            composable(route = "NeedLocation") {
                checkLocationEnabled.NeedLocationScreen(navController)
            }
        }

        if(sg.isOnline().not()){
            navController.navigate("NeedNetwork")
        } else if(!checkLocationEnabled.statusCheck()){
            navController.navigate("NeedLocation")
        }
    }

    // メモ 一個前のが送信される
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
                    val value = intPosition.toString()
                    port?.write("{\"motor\":\"$value\"}".toByteArray(Charsets.UTF_8),100)
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
                        val value = intPosition.toString()
                        port?.write("{\"motor\":\"$value\"}".toByteArray(Charsets.UTF_8),100)
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
