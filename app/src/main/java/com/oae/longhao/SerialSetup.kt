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
    var serialMessage = "";

    override fun onRunError(e: Exception) {
        if (e.message != null){
            Log.v("serial", "Error! Message: " + e.message.toString())
        } else {
            Log.v("serial", "Unknown Error!")
        }
        serialMessage = ""
    }

    override fun onNewData(data: ByteArray) {
        serialMessage = StringBuilder().append(serialMessage).append(String(data,Charsets.UTF_8)).toString()
        if (serialMessage.endsWith("}\n")){
            Log.v("received Message",serialMessage)
            serialMessage = ""
        }
    }
}


fun setupSerial(manager: UsbManager){
    val availableDrivers: List<UsbSerialDriver> =
        UsbSerialProber.getDefaultProber().findAllDrivers(manager)
    if (availableDrivers.isEmpty()) {
        Log.v("USB","driver not found.")
        return
    }
    val driver = availableDrivers[0]
    if (driver.ports.size < 0) {
        Log.v("USB", "connection failed. reason -> not enough ports at device")
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
       // port?.write("{\"motor\":\"114514\"}".toByteArray(Charsets.UTF_8), 2000)
        Log.v("USB", "connection success!")
        usbIoManager = SerialInputOutputManager(port as UsbSerialPort?, mListener)
        usbIoManager?.start()
        Executors.newSingleThreadExecutor().submit(usbIoManager)
    }
}