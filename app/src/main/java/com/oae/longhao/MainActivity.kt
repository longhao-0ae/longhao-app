package com.oae.longhao

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity

import com.oae.longhao.ui.theme.LonghaoTheme

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*

import android.util.Log
import androidx.activity.compose.setContent
import android.os.BatteryManager

import android.content.Intent

import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.util.*
import kotlin.concurrent.schedule
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.view.WindowInsetsController
import com.hoho.android.usbserial.driver.UsbSerialProber

import com.hoho.android.usbserial.driver.UsbSerialDriver

import android.hardware.usb.UsbManager
import java.io.IOException


private lateinit var locationCallback: LocationCallback

class MainActivity : ComponentActivity(){
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    // level = 1,plugged = 2
    //ん　更新されないことがある（時間経った時？）や　そもそもなんか認識してないとか変更できてないとかかも
    val powerVariable = mutableMapOf(1 to 9999, 2 to Boolean)
    // latitude = 1,longitude = 2,altitude = 3
    val locationVariable = mutableMapOf(1 to 0.0,2 to 0.0,3 to 0.0)
    //
  //  lateinit var usb: UsbSerialDriver
//   var device: UsbDevice? = null

    //,FragmentManager.OnBackStackChangedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //serial
        val manager = getSystemService(USB_SERVICE) as UsbManager
        val availableDrivers: List<UsbSerialDriver> = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            return
        }
        val driver = availableDrivers[0]
        if (driver == null) {
            Log.v("connection failed", "no driver for device")
            return
        } else if (driver.getPorts().size < 0) {
            Log.v("connection failed","not enough ports at device")
            return
        }
        //availableDriversの取得はできた　こっからどーすんだろ

        /*
        usb = UsbSerialProber.acquire(manager)
        if (usb != null) {
            try {
                usb.open()
                usb.setBaudRate(9600)
                start_read_thread() // シリアル通信を読むスレッドを起動
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }*/

        //画面常時オン
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //ナビゲーションバーとステータスバー隠す
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            window.decorView.windowInsetsController?.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
            window.decorView.windowInsetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
             window.decorView.systemUiVisibility =
                 (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
         }

        val myList: MutableList<String> = mutableListOf("CPU", "Memory", "Mouse")

        //バッテリ
        val intentfilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(BatteryReceiver, intentfilter)

        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)



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
                        Log.v("updated","gps")
                    }
                }
            }
        }
        /// 位置情報を更新
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,Looper.myLooper())


        Timer().schedule(0, 5000) {
            getBatteryLevel()
            getLocation()
            //   this.cancel()
        }

        /*   val READ_WAIT_MILLIS = 2000
       // var usbSerialPort: UsbSerialPort? = null

        // Open a connection to the first available driver.
        var device: UsbDevice? = null
        var deviceId = 0
        //deviceIdは0じゃないかも
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
     /*   val availableDrivers: List<UsbSerialDriver> = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            return
        }
        val driver = availableDrivers[0]
        */
        for (v in manager.getDeviceList().values) if (v.getDeviceId() == deviceId) device = v

        var driver: UsbSerialDriver? = UsbSerialProber.getDefaultProber().probeDevice(device)
        if (driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device)
        }
        if (driver == null) {
            Log.v("connection failed", "no driver for device")
            return
        }
        if (driver.getPorts().size < 0) {
            Log.v("connection failed","not enough ports at device")
            return
        }
        //usbSerialPort = driver.getPorts().get(0)

        val connection = manager.openDevice(driver.device)
            ?: // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            return

        val port = driver.ports[0] // Most devices have just one port (port 0)
        Log.v("port",port.toString())
        port.open(connection)
        port.setParameters(9200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        val buffer = ByteArray(8192)
        var len = port.read(buffer, READ_WAIT_MILLIS);
        Log.v("TerminalFragment", len.toString())
        Log.v("Buffer", buffer.toString())
        /*setContentView(R.layout.activity_main)
        //val toolbar: Toolbar = findViewById(R.id.toolbar)
        supportFragmentManager.addOnBackStackChangedListener(this)
        if (savedInstanceState == null) supportFragmentManager.beginTransaction()
            .add(R.id.fragment, DevicesFragment(), "devices").commit() else onBackStackChanged()*/

            */

        setContent {
            LonghaoTheme {
                // A surface container using the 'background' color from the theme
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
    /*
    override fun onNewData(data: ByteArray) {
        Log.v("TerminalFragment", "onNewData")
    }*/


    private val phoneStateListener = object : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)
            val level = signalStrength?.level
            //んー simささないと動いてるかわからん
            Log.v("Strengthlevel",level.toString())
        }
    }

    private val BatteryReceiver: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bm = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager
            val batLevel:Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val batteryPlugged = bm.isCharging
            powerVariable[1] = batLevel
            powerVariable[2] = batteryPlugged
        }
    }
    private fun getLocation(){
        Log.v("gps-latitude",locationVariable[1].toString())
    }

    private fun getBatteryLevel(){
        Log.v("plugged",powerVariable[2].toString())
        Log.v("level",powerVariable[1].toString())
    }

    @Composable
    fun Greeting(name: String) {
        Text(text = "Hello $name!")
    }

    @Composable
    fun TestButton() {
        Button(
            onClick = { /* ... */ },
        ){
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
/*
    fun onBackStackChanged() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
    }

    override fun onNewIntent(intent: Intent) {
        if ("android.hardware.usb.action.USB_DEVICE_ATTACHED" == intent.action) {
            val terminal = supportFragmentManager.findFragmentByTag("terminal") as TerminalFragment?
            terminal?.status("USB device detected")
        }
        super.onNewIntent(intent)
    }*/
}

