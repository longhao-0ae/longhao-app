package com.oae.longhao

import android.util.Log
import com.here.oksse.OkSse
import com.here.oksse.ServerSentEvent
import okhttp3.Request
import okhttp3.Response
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class SseConnection(url: String) {
    private lateinit var sse: ServerSentEvent
    private var request: Request = Request.Builder().url(url).build()
    private var okSse: OkSse = OkSse()

    init {
        runSse()
    }

    private fun runSse() {
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
                    if (parsedMessage.first in 1000..2000){
                        Log.v("write","ok")
                        usbIoManager?.writeAsync(parsedMessage.first.toString().toByteArray(Charsets.UTF_8))
                    } else {
                        Log.v("wr","over 2000 or min 1000")
                        usbIoManager?.writeAsync("1000".toByteArray(Charsets.UTF_8))
                    }
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
                Log.i("sse", "onPreRetry")
                return originalRequest
            }
        }
        )
    }

    data class Umm (
        val motor:Int,
        val helm:Int
    )

    fun parseJson(json: String):Pair<Int,Int> {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val parser = moshi.adapter(Umm::class.java)
        return try {
            val parsedJson: Umm? = parser.fromJson(json)
            if (parsedJson != null) {
                Pair(parsedJson.motor,parsedJson.helm)
            } else {
                Pair(88888,88888)
            }
        } catch (e: Exception) {
            Log.e("json parse error", e.toString())
            Pair(88888,88888)
        }
    }
    public fun closeSse(){
        sse.close()
    }
}