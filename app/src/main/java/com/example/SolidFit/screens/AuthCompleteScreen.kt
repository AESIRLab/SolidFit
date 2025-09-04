package com.example.SolidFit.screens

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.nimbusds.jwt.SignedJWT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.aesirlab.mylibrary.generateDPoPKey
import org.aesirlab.mylibrary.sharedfunctions.buildTokenRequest
import org.aesirlab.mylibrary.sharedfunctions.createUnsafeOkHttpClient
import org.json.JSONObject
import org.skCompiler.generatedModel.AuthTokenStore

private const val TAG = "AuthCompleteScreen"
@Composable
fun AuthCompleteScreen(
    tokenStore: AuthTokenStore,
    onFinishedAuth: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val activity = (context as Activity).intent
    val intentData = activity.data
    val code = intentData?.getQueryParameter("code")

    runBlocking(Dispatchers.IO) {
        preliminaryAuth(tokenStore, code)
    }
    onFinishedAuth()
}

private suspend fun preliminaryAuth(tokenStore: AuthTokenStore, code: String?)  {
    val clientId = tokenStore.getClientId().first()
    val rClientSecret = tokenStore.getClientSecret().first()
    val tokenUrl = tokenStore.getTokenUri().first()
    val codeVerifier = tokenStore.getCodeVerifier().first()
    val redirectUri = tokenStore.getRedirectUri().first()

    var clientSecret: String? = null
    if (rClientSecret != "") {
        clientSecret = rClientSecret
    }

    val dpop = generateDPoPKey()

//    val authForm = DPoPAuth(tokenUri = tokenUrl)
//    val authString = authForm.generateAuthString("POST")
    tokenStore.setSigner(dpop.toJSONObject().toString())
//    val authString = generateAuthString("POST", tokenUrl, dpop)
//    tokenStore.setSigner(authString)
//    val response = tokenRequest(
//        clientId,
//        clientSecret,
//        tokenUrl,
//        code!!,
//        codeVerifier,
//        redirectUri,
//        authString
//    )
    val tokenRequest = buildTokenRequest(
        clientId,
        tokenUrl,
        codeVerifier,
        redirectUri,
        dpop,
        clientSecret,
        code!!
    )
    val response = createUnsafeOkHttpClient().newCall(tokenRequest).execute()
//    val responseDict = parseTokenResponseBody(response.body.)
    val json = JSONObject(response.body!!.string())
    val accessToken = json.getString("access_token")

    val idToken: String
    try {
        idToken = json.getString("id_token")

        tokenStore.setIdToken(idToken)

        try {
            val jwtObject = SignedJWT.parse(idToken)
            val body = jwtObject.payload
            val jsonBody = JSONObject(body.toJSONObject())
            val webId = jsonBody.getString("webid")
            Log.d(TAG, webId)
            tokenStore.setWebId(webId)
        } catch (e: Exception) {
            e.message?.let { Log.d("error", it) }
        }
    } catch (e: Exception) {
        e.message?.let { Log.d("error", it) }
    }

    val refreshToken: String
    try {
        refreshToken = json.getString("refresh_token")
        tokenStore.setRefreshToken(refreshToken)
    } catch (e: Exception){
        e.message?.let { Log.d("error", it) }
    }

    tokenStore.setAccessToken(accessToken)
}