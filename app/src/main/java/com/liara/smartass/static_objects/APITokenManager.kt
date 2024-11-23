package com.liara.smartass.static_objects

import android.content.Context
import android.util.Log
import com.auth0.android.jwt.JWT
import com.liara.smartass.static_objects.NetworkHelper.checkNetwork
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


object APITokenManager {
    private var currentToken: JWT? = null
    private const val TAG = "APITokenManager"
    fun getValidToken(context: Context): String{
        // Check if token is valid
        if (currentToken == null || currentToken!!.isExpired(30)) {
            currentToken = getNewToken(context)
        }

        return currentToken.toString()
    }

    private fun getNewToken(context: Context): JWT? {
        checkNetwork(context)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()

        val jsonObject = JSONObject()
        jsonObject.put("client_id", /*secret id*/)
        jsonObject.put("client_secret", /*secret key*/)
        jsonObject.put("audience", /*website domain or ip*/)
        jsonObject.put("grant_type", "client_credentials")

        val body = jsonObject.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://hdp-liara.us.auth0.com/oauth/token")
            .post(body)
            .build()
        var response: Response? = null

        try{
            response = client.newCall(request).execute()
            val jsonResponse = JSONObject(response.body!!.string())
            Log.d(TAG, "API Token: ${jsonResponse["access_token"].toString()}")
            return JWT(jsonResponse["access_token"].toString())
        }catch (e: IOException){
            e.printStackTrace()
            return null
        }
    }
}
