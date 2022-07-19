package com.oae.longhao

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.Type

class Permissions(_MainContext: Context) {
    private val mainContext = _MainContext
    private val mainActivity = _MainContext as Activity
    private val requestPermission by lazy {
        MainActivity().registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grant ->
            if (grant[Manifest.permission.ACCESS_FINE_LOCATION] == true && grant[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                Toast.makeText(mainContext, "権限を取得しました。アプリを再起動してください。", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(mainContext, "権限を取得できませんでした", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkPermission(Permission:String): Boolean {
        return ActivityCompat.checkSelfPermission(
            mainActivity,
            Permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldDisplayAuthorityAcquisition(Permission: Array<String>): Boolean {
        Permission.forEach { elem ->
            if(!ActivityCompat.shouldShowRequestPermissionRationale(mainActivity, elem)){
                return false
            }
        }
        return true
    }

    private fun getPermission(Permission: Array<String>,PermName: String) {
        Toast.makeText(mainContext, PermName + "の権限を許可してください", Toast.LENGTH_LONG).show()
        //メモ  Attempt to invoke virtual methodが出るときはアプリ再インストール
        requestPermission.launch(
            Permission
        )
    }

    private fun openPermissionSettings(PermName: String) {
        Toast.makeText(mainContext, "設定から" + PermName + "の権限を許可してください", Toast.LENGTH_LONG).show()
        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.data = Uri.parse("package:com.oae.longhao")
        mainContext.startActivity(i)
    }

    @Composable
    fun NeedPermissionScreen(navController: NavController,settings: String) {
        var needPermList = arrayOf<Pair<String,Array<String>>>()

        parseJson(settings)?.forEach { elem ->
            var permList = arrayOf<String>()
            elem.Permissions.forEach { permElem ->
                if(!checkPermission(permElem)){
                    permList += permElem
                }
            }
            if(permList.isNotEmpty()) {
                needPermList += Pair(elem.Name,permList)
            }
        }
/*
        if(needPermList.isEmpty()){
            navController.navigate("MainScreen")
        }
*/
        fun gpFunc(data: Pair<String,Array<String>>){
            val name = data.first
            val permList = data.second
            Log.v("permList", permList.contentToString())
            if(shouldDisplayAuthorityAcquisition(permList)){
                getPermission(permList,name)
            } else {
                openPermissionSettings(name)
            }
        }

        val contentString:Map<String,String> = if (needPermList.isEmpty()) {
            mapOf("button" to "進む", "text" to "全ての権限が有効です", "icon" to "NavigateNext")
        } else {
            mapOf("button" to "権限を取得", "text" to "付与されていない権限があります", "icon" to "Bolt")
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                contentString["text"]?.let { Text(it) }
                Button(onClick = {
                    if (needPermList.isEmpty()) {
                            navController.navigate("MainScreen")
                    } else {
                        needPermList.forEach { it ->
                            gpFunc(it)
                        }
                    }
                }) {
                    contentString["icon"]?.let { IconByName(name = it) }
                    contentString["button"]?.let { Text(it) }
                }
            }
        }
    }


    data class Kata(
        val Name: String,
       val Permissions:List<String>
    )
    class MoshiJsonListAdaptersFactory : JsonAdapter.Factory {
        override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
            return when (type) {
                Types.newParameterizedType(ArrayList::class.java, Kata::class.java) ->
                    moshi.adapter<ArrayList<Kata>>(type)
                else -> null
            }
        }
    }
    private fun parseJson(data: String): List<Kata>? {
        val moshi = Moshi.Builder().add(MoshiJsonListAdaptersFactory()).add(KotlinJsonAdapterFactory()).build()
        val listType = Types.newParameterizedType(List::class.java, Kata::class.java)
        val adapter: JsonAdapter<List<Kata>> = moshi.adapter(listType)
        return adapter.fromJson(data)
    }
}