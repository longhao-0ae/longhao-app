package com.oae.longhao

import android.util.Log
import com.here.oksse.OkSse
import com.here.oksse.ServerSentEvent
import com.squareup.moshi.Json
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


class SseConnection(url: String) {
    private lateinit var sse: ServerSentEvent
    private lateinit var request: Request
    private lateinit var okSse: OkSse

    init {
        request = Request.Builder().url(url).build()
        okSse = OkSse()
        runSse()
    }

    fun runSse() {

        sse = okSse.newServerSentEvent(request, object: ServerSentEvent.Listener {
            override fun onOpen(sse: ServerSentEvent?, response: Response?) {
                Log.d("sse", "Connection Open")
            }

            override fun onMessage(
                sse: ServerSentEvent?,
                id: String?,
                event: String?,
                message: String?
            ) {
                if (message != null){
                    val parsedMessage = parseJson(message.removePrefix("b\'").removeSuffix("\'"))
                    Log.v("motor",parsedMessage.first.toString())
                    Log.v("helm",parsedMessage.second.toString())
                }
            }

            override fun onComment(sse: ServerSentEvent?, comment: String?) {
                Log.d("sse-Comment", comment!!)
            }

            override fun onRetryTime(sse: ServerSentEvent?, milliseconds: Long): Boolean {
                return true
            }

            override fun onRetryError(
                sse: ServerSentEvent?,
                throwable: Throwable?,
                response: Response?
            ): Boolean {
                Log.d("sse", "RetryError")
                return true
            }

            override fun onClosed(sse: ServerSentEvent?) {
                Log.d("sse", "Connection Closed")
            }
            override fun onPreRetry(sse: ServerSentEvent?, originalRequest: Request): Request {
                Log.i("TAG", "onPreRetry")
                return originalRequest
            }
        }
        )
    }

    data class umm (
        val motor:Int,
        val helm:Int
    )

    fun parseJson(json: String):Pair<Int,Int> {
        var moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        var parser = moshi.adapter(umm::class.java)
        return try {
            val parsedJson: umm? = parser.fromJson(json)
            if (parsedJson != null) {
                Pair(parsedJson.motor,parsedJson.helm)
            } else {
                Pair(88888,88888)
            }
        } catch (e: Exception) {
            Log.e("json parse error", e.toString())
            Pair(88888,88888)
        }
        return Pair(88888,88888)
    }


}