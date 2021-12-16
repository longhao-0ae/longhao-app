package com.oae.longhao

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity

import com.oae.longhao.ui.theme.LonghaoTheme

import androidx.compose.runtime.Composable
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
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import com.hoho.android.usbserial.driver.UsbSerialProber

import com.hoho.android.usbserial.driver.UsbSerialDriver

import android.hardware.usb.UsbManager
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.hoho.android.usbserial.driver.UsbSerialPort

import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.util.concurrent.Executors
import java.lang.Exception

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import java.time.LocalDateTime

private lateinit var locationCallback: LocationCallback
var usbIoManager: SerialInputOutputManager? = null

private val mListener: SerialInputOutputManager.Listener =
    object : SerialInputOutputManager.Listener {
        override fun onRunError(e: Exception) {
            if (e.message != null) Log.v("シリアルエラー", e.message.toString())
        }
        override fun onNewData(data: ByteArray) {
            //途切れるのはしゃーないらしい　こっちで貯めてくっつけなきゃないぽい
            Log.v("received data",String(data,Charsets.UTF_8))
        }
    }

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // level = 1,plugged = 2
    //ん　更新されないことがある（時間経った時？）や　そもそもなんか認識してないとか変更できてないとかかも
    val powerVariable = mutableMapOf(1 to 9999, 2 to Boolean)

    // latitude = 1,longitude = 2,altitude = 3
    val locationVariable = mutableMapOf(1 to 0.0, 2 to 0.0, 3 to 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //シリアル
        val manager = getSystemService(USB_SERVICE) as UsbManager
        val availableDrivers: List<UsbSerialDriver> =
            UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            Log.v("USB","driver not found")
            return
        }
        val driver = availableDrivers[0]
        if (driver.ports.size < 0) {
            Log.v("connection failed", "not enough ports at device")
            return
        }
        val connection = manager.openDevice(driver.device)
            ?:
            // USBデバイスへのアクセス権限がなかった時の処理
            return

        val port = driver.ports[0]
        port.open(connection)
        port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        port.write("Start".toByteArray(Charsets.UTF_8),2000)
        usbIoManager = SerialInputOutputManager(port, mListener)
        Executors.newSingleThreadExecutor().submit(usbIoManager)
        appSettings(window)
        Location().test()
        val myList: MutableList<String> = mutableListOf("CPU", "Memory", "Mouse")

        //バッテリ
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(PhoneBatteryReceiver(), intentFilter)

        //電波強度?
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
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
        val locationRequest = LocationRequest().apply {
            // 精度重視(電力大)と省電力重視(精度低)を両立するため2種類の更新間隔を指定
            // 今回は公式のサンプル通り
            interval = 10000                                   // 最遅の更新間隔(但し不正確)
            fastestInterval = 5000                             // 最短の更新間隔
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 精度重視
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
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
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )

        Timer().schedule(0, 5000) {
            val zonedDateTimeString = LocalDateTime.now().toString()
            sendBattery(zonedDateTimeString)
            sendLocation(zonedDateTimeString)
            //   this.cancel()
        }
        //短くするとなんかピッピって鳴る でもあとで短くしなきゃ
        Timer().schedule(0, 10000) {
            getMotorRPM()
            //   this.cancel()
        }
        setContent {
            LonghaoTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        Greeting("Android")
                        TestButton()
                        MessageList(serialList = myList)
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
        postData(bodyJson,"/api/location")
    }

    private fun sendBattery(zonedDateTimeString: String) {
        Log.v("plugged", powerVariable[2].toString())
        //zonedDateTimeに問題あり
        val bodyJson = """
            {
                "boat":{
                    "level":${"0"},
                    "charging":${false},
                    "last_time":"$zonedDateTimeString"
                 },
                "phone":{
                    "level": ${powerVariable[1].toString()},
                    "charging":${powerVariable[2].toString()},
                    "last_time":"$zonedDateTimeString"
                    }
            }
            """
        postData(bodyJson,"/api/battery")
        }

    private fun getMotorRPM() {
        // 非同期処理
        "http://192.168.3.16/api/motor_rpm".httpGet().responseJson{ _, _, result ->
            when (result) {
                is Result.Success -> {
                    val json = result.value.obj()
                    val value = json.get("value")
                    Log.i("getServer", value.toString())
                    usbIoManager?.writeAsync(value.toString().toByteArray(Charsets.UTF_8))
                }
                is Result.Failure -> {
                    Log.e("getServer","Err")
                    usbIoManager?.writeAsync("1000".toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    @Composable
    fun Greeting(name: String) {
        Text(text = "Hello $name!")
    }

    @Composable
    fun TestButton() {
        Button(
            onClick = { /* ... */ },
        ) {
            Text("Like")
        }
    }

    @Composable
    fun MessageList(serialList: MutableList<String>) {
        Column {
            serialList.forEach { message ->
                MessageRow(message)
            }
        }
    }

    @Composable
    fun MessageRow(message: String) {
        Text(text = message)
    }
}