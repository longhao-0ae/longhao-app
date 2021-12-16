package com.oae.longhao

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result

fun postData(RawBodyJson: String, ApiPoint: String){
    val bodyJson = RawBodyJson.trimIndent().replace(System.lineSeparator(), "").replace(" ", "")
    val url = "http://192.168.3.16${ApiPoint}"
    Fuel.post(url)
        .jsonBody(bodyJson)
        .responseJson { _, response, result ->
            when (result) {
                is Result.Success -> {
                    val json = result.value.obj()
                    val status = json.get("status")
                    Log.i(url, status.toString())
                }
                is Result.Failure -> {
                    when (response.statusCode) {
                        -1 -> {
                            Log.e(url,"Err: Unknown Error(Network?)")
                        }
                        500 -> {
                            Log.e(url,"Err: ServerError")
                        }
                        401 -> {
                            Log.e(url,"Err: Unauthorized")
                        }
                        404 -> {
                            Log.e(url,"Err: Not found")
                        }
                        else -> {
                            Log.e(url, "Err: " + result.error.message)
                        }
                    }
                }
            }
        }
}