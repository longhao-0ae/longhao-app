package com.oae.longhao

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity

import com.oae.longhao.ui.theme.LonghaoTheme

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.ui.Modifier

import android.util.Log
import androidx.activity.compose.setContent
import android.os.BatteryManager

import android.content.Intent

import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.util.*
import kotlin.concurrent.schedule

private lateinit var fusedLocationClient : FusedLocationProviderClient

class MainActivity : ComponentActivity(){
    //1 = health,2=plugged,3=level
    val powervariable = mutableMapOf(1 to "", 2 to Boolean, 3 to 9999)
    //,FragmentManager.OnBackStackChangedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //画面常時オン
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //ナビゲーションバーとステータスバー隠す
        window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }

            var mylist: MutableList<String> = mutableListOf("CPU", "Memory", "Mouse")

        //バッテリ系
        val intentfilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        // //これサブスクみたいなのが増え続けてるんでは
        registerReceiver(BatteryReceiver, intentfilter)


        fusedLocationClient = FusedLocationProviderClient(this)

        // どのような取得方法を要求
        val locationRequest = LocationRequest().apply {
            // 精度重視(電力大)と省電力重視(精度低)を両立するため2種類の更新間隔を指定
            // 今回は公式のサンプル通りにする。
            interval = 10000                                   // 最遅の更新間隔(但し正確ではない。)
            fastestInterval = 5000                             // 最短の更新間隔
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 精度重視
        }
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                // 更新直後の位置が格納されているはず
                val location = locationResult?.lastLocation ?: return
                Toast.makeText(this@MainActivity,
                    "緯度:${location.latitude}, 経度:${location.longitude}", Toast.LENGTH_LONG).show()
            }
        }

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
        /// 位置情報を更新
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())


        Timer().schedule(0, 5000) {
            Log.v("nullpo", "ga")
            GetBatterylebel()
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
                    greeting("Android")
                   buttontest()
                        MessageList(seriallist = mylist)
                    }
                }
            }
        }
    }/*
    override fun onNewData(data: ByteArray) {
        Log.v("TerminalFragment", "onNewData")
    }*/

    private val BatteryReceiver: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bm = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager
            val batLevel:Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val batteryPlugged = bm.isCharging

            powervariable[3] = batLevel
            powervariable[2] = batteryPlugged
            // Log.v("batterylevel","Current battery charge\n$Batterylevel%")
            /* val batteryLevel = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val batteryPlugged = getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val batteryHealth = getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            Log.d("Battery", "Level: $batteryLevel")
            Log.d("Battery", "Ifplugged: ${batteryPluggCheck(batteryPlugged)}")
            Log.d("Battery", "Health: ${batteryHealthCheck(batteryHealth)}")*/

        }
    }

    private fun batteryHealthCheck(bh: Int): String? {
        var health: String? = null
        if (bh == BatteryManager.BATTERY_HEALTH_GOOD) {
            health = "GOOD"
        } else if (bh == BatteryManager.BATTERY_HEALTH_DEAD) {
            health = "DEAD"
        } else if (bh == BatteryManager.BATTERY_HEALTH_COLD) {
            health = "COLD"
        } else if (bh == BatteryManager.BATTERY_HEALTH_OVERHEAT) {
            health = "OVERHEAT"
        } else if (bh == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE) {
            health = "OVER_VOLTAGE"
        } else if (bh == BatteryManager.BATTERY_HEALTH_UNKNOWN) {
            health = "UNKNOWN"
        } else if (bh == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE) {
            health = "UNSPECIFIED_FAILURE"
        }
        return health
    }



    fun GetBatterylebel(){
        Log.v("plugged",powervariable[2].toString())
        Log.v("level",powervariable[3].toString())
    }

    @Composable
    fun greeting(name: String) {
        Text(text = "Hello $name!")
    }

    @Composable
    fun buttontest() {
        Button(
            onClick = { /* ... */ },
            // Uses ButtonDefaults.ContentPadding by default
        ){
                // Inner content including an icon and a text label
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Like")
            }
    }
    @Composable
    fun MessageList(seriallist: MutableList<String>) {
        Column { 
            seriallist.forEach { message ->
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onNewIntent(intent: Intent) {
        if ("android.hardware.usb.action.USB_DEVICE_ATTACHED" == intent.action) {
            val terminal = supportFragmentManager.findFragmentByTag("terminal") as TerminalFragment?
            terminal?.status("USB device detected")
        }
        super.onNewIntent(intent)
    }*/
}

