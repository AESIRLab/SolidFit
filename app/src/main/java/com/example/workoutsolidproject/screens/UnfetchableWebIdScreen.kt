package com.example.workoutsolidproject.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workoutsolidproject.R
import com.example.workoutsolidproject.StartButton
import com.solidannotations.AuthTokenStore
import com.solidannotations.fetchAuth
import com.solidannotations.fetchConfig
import com.solidannotations.fetchRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import net.openid.appauth.CodeVerifierUtil

@Composable
fun UnfetchableWebIdScreen(
    tokenStore: AuthTokenStore,
    renderError: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement =  Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment =  Alignment.CenterHorizontally,
    ) {
        val appTitle = "Android Workout Tracker Solid"
        var solidProvider by rememberSaveable {
            mutableStateOf("")
        }

        Image(
            painter = painterResource(id = R.drawable.exercise_black_24px),
            contentDescription = "App logo"
        )
        Text(
            text=appTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        TextField(
            value = solidProvider,
            onValueChange =  { solidProvider = it },
            label = { Text("Solid Provider", fontStyle = FontStyle.Italic) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )
        val context = LocalContext.current
        StartButton(text = "Fetch OIDC Config") {

            val redirectUris = listOf("app://www.solid-oidc.com/callback")
            CoroutineScope(Dispatchers.IO).launch {

                val configResponse = fetchConfig(solidProvider)

                val responseBody = configResponse.body!!.string()
                val configJSON = JSONObject(responseBody)

                val registrationEndpoint = configJSON.getString("registration_endpoint")
                val tokenEndpoint = configJSON.getString("token_endpoint")
                val authUrl = configJSON.getString("authorization_endpoint")

                tokenStore.setTokenUri(tokenEndpoint)

                val registrationResponse = fetchRegistration(appTitle, registrationEndpoint, redirectUris)
                val registrationString = registrationResponse.body!!.string()

                val registrationJSON = JSONObject(registrationString)
                val clientId = registrationJSON.getString("client_id")
                var clientSecret: String? = null
                try {
                    clientSecret = registrationJSON.getString("client_secret")
                    tokenStore.setClientSecret(clientSecret)

                } catch (e: JSONException) {
                    Log.d("JSONException", "no client_secret returned")
                }

                tokenStore.setClientId(clientId)

                val codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier()
                val codeVerifierChallenge = CodeVerifierUtil.deriveCodeVerifierChallenge(codeVerifier)

                tokenStore.setCodeVerifier(codeVerifier)

                val httpUrl = fetchAuth(authUrl, clientId, clientSecret, codeVerifierChallenge, redirectUris[0])
                val sendUri = Uri.parse(httpUrl.toString())
                context.startActivity(Intent(Intent.ACTION_VIEW, sendUri))
            }
        }
    }
}