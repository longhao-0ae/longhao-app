package com.oae.longhao

import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.activity.ComponentActivity
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.lang.Exception
import java.util.concurrent.Executors
var usbIoManager: SerialInputOutputManager? = null

private val mListener: SerialInputOutputManager.Listener = object : SerialInputOutputManager.Listener {
    var serialMessage:String = "";

    override fun onRunError(e: Exception) {
        if (e.message != null){

        }
        //TODO 壊れないように連結中のデータがあったら破棄
        Log.v("serial", "Error! Message: " + e.message.toString())
        serialMessage = ""
    }

    override fun onNewData(data: ByteArray) {
        val sb = StringBuilder()
        sb.append(serialMessage)
        sb.append(String(data,Charsets.UTF_8))
        serialMessage = sb.toString()
        if (sb.toString().endsWith("}\n")){
            Log.v("received data",sb.toString())
            serialMessage = ""
            Log.v("serial","serialMessage is Cleared!")
        }
    }
}

fun setupSerial(manager: UsbManager){
    val availableDrivers: List<UsbSerialDriver> =
        UsbSerialProber.getDefaultProber().findAllDrivers(manager)
    if (availableDrivers.isEmpty()) {
        Log.v("USB","driver not found")
        return
    }
    val driver = availableDrivers[0]
    if (driver.ports.size < 0) {
        Log.v("USB", "connection failed,reason -> not enough ports at device")
        return
    }
    val connection = manager.openDevice(driver.device)
        ?:
        // USBデバイスへのアクセス権限がなかった時の処理
        return

    port = driver.ports[0]
    if(port != null) {
        port?.open(connection)
        port?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        port?.dtr = true;
        port?.write("Start".toByteArray(Charsets.UTF_8), 2000)
        Log.v("USB", "connection success!")
        usbIoManager = SerialInputOutputManager(port as UsbSerialPort?, mListener)
        usbIoManager?.start()
        Executors.newSingleThreadExecutor().submit(usbIoManager)
    }
}