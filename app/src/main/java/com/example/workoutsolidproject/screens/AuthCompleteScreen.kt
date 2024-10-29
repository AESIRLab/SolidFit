package com.example.workoutsolidproject.screens

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.nimbusds.jwt.SignedJWT
import com.solidannotations.AuthTokenStore
import com.solidannotations.DPoPAuth
import com.solidannotations.tokenRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

private const val TAG = "AuthCompleteScreen"
@Composable
fun AuthCompleteScreen(
    tokenStore: AuthTokenStore,
    onFinishedAuth: () -> Unit,
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

    val authForm = DPoPAuth(tokenUri = tokenUrl)
    val authString = authForm.generateAuthString("POST")
    tokenStore.setSigner(authForm.key!!.toJSONObject().toString())

    val response = tokenRequest(
        clientId,
        clientSecret,
        tokenUrl,
        code!!,
        codeVerifier,
        redirectUri,
        authString
    )

    val json = JSONObject(response)
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