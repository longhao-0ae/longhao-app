package com.oae.longhao

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialProber

class CustomProber {
    companion object {
        fun getCustomProber(): UsbSerialProber {
            val customTable = ProbeTable()//
                .addProduct(0x10C4, 0xEA60, CdcAcmSerialDriver::class.java)
            return UsbSerialProber(customTable)
        }
    }
}