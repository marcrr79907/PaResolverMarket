package com.market.paresolvershop.ui.authentication

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.market.paresolvershop.config.SupabaseConfig
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

@Composable
actual fun GoogleAuthUiProvider(
    onGoogleSignInResult: (idToken: String?, nonce: String?) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    content {
        val credentialManager = CredentialManager.create(context)

        // 1. Generar Nonce (Recomendado por Supabase)
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(SupabaseConfig.webClientId)
            .setNonce(hashedNonce)
            .setAutoSelectEnabled(false)
            .setFilterByAuthorizedAccounts(false)
            .build()

        Log.i("googleIdOption", "$googleIdOption")
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        Log.i("request", "$request")
        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                Log.i("result", "$result")
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)


                // Devolvemos el token Y el nonce original (rawNonce)
                onGoogleSignInResult(googleIdTokenCredential.idToken, rawNonce)

            } catch (e: Exception) {
                Log.e("Credential Error", "$e")
                onGoogleSignInResult(null, null)
            }
        }
    }
}