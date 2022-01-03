package com.oae.longhao

import android.app.Application
import com.hoho.android.usbserial.util.SerialInputOutputManager

class globalVariable : Application(){
    // level = 1,plugged = 2
    //ん　更新されないことがある（時間経った時？）や　そもそもなんか認識してないとか変更できてないとかかも
    val powerVariable = mutableMapOf(1 to 9999, 2 to Boolean)

    companion object {
        private var instance : globalVariable? = null

        fun  getInstance(): globalVariable {
            if (instance == null)
                instance = globalVariable()

            return instance!!
        }
    }
}