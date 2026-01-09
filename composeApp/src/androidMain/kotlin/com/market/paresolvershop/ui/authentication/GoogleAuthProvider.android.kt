package com.market.paresolvershop.ui.authentication

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.market.paresolvershop.R
import kotlinx.coroutines.launch

/**
 * Implementación 'actual' para Android usando la API de Credential Manager,
 * siguiendo las mejores prácticas de la documentación oficial.
 */
@Composable
actual fun GoogleAuthUiProvider(
    onGoogleSignInResult: (idToken: String?) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    content {
        scope.launch {
            try {
                // 1. PRIMER INTENTO: Buscar cuentas ya autorizadas.
                val result = getGoogleCredential(credentialManager, context, filterByAuthorizedAccounts = true)
                // CAMBIO: Manejamos el caso de que el resultado sea nulo (tipo de credencial inesperado)
                if (result != null) {
                    handleGoogleCredentialResult(result, onGoogleSignInResult)
                } else {
                    Log.e("GoogleAuth", "Tipo de credencial inesperado en el primer intento.")
                    onGoogleSignInResult(null)
                }
            } catch (e: NoCredentialException) {
                // 2. SEGUNDO INTENTO: Si no hay cuentas autorizadas, buscamos CUALQUIER cuenta de Google.
                Log.d("GoogleAuth", "No se encontraron cuentas autorizadas. Intentando con todas las cuentas.")
                try {
                    val result = getGoogleCredential(credentialManager, context, filterByAuthorizedAccounts = false)
                    // CAMBIO: Manejamos el caso nulo aquí también
                    if (result != null) {
                        handleGoogleCredentialResult(result, onGoogleSignInResult)
                    } else {
                        Log.e("GoogleAuth", "Tipo de credencial inesperado en el segundo intento.")
                        onGoogleSignInResult(null)
                    }
                } catch (e: GetCredentialException) {
                    // Si también falla, el usuario canceló o hubo otro error.
                    Log.e("GoogleAuth", "Error final al obtener credenciales de Google.", e)
                    onGoogleSignInResult(null)
                }
            } catch (e: GetCredentialException) {
                // Captura de otros errores en el primer intento.
                Log.e("GoogleAuth", "Error inicial al obtener credenciales de Google.", e)
                onGoogleSignInResult(null)
            }
        }
    }
}

/**
 * Función auxiliar para construir la petición y llamar al Credential Manager.
 */
private suspend fun getGoogleCredential(
    credentialManager: CredentialManager,
    context: Context,
    filterByAuthorizedAccounts: Boolean
): CustomCredential? {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
        .setServerClientId(context.getString(R.string.web_client_id))
        .setNonce(null) // Opcional, pero recomendado para producción
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    // Lanza la UI y espera el resultado. (Esto puede lanzar NoCredentialException)
    val result = credentialManager.getCredential(context, request)

    // Asegurar de que es del tipo correcto antes de devolver.
    return result.credential as? CustomCredential
}

/**
 * Función auxiliar para procesar el resultado y extraer el idToken.
 */
private fun handleGoogleCredentialResult(
    credential: CustomCredential?,
    onGoogleSignInResult: (idToken: String?) -> Unit
) {
    if (credential == null) {
        onGoogleSignInResult(null)
        return
    }

    // CAMBIO CLAVE: Comparamos el 'type' y usamos 'createFrom' como dice la documentación.
    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            // Convertimos la credencial genérica a una credencial de Google.
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            // ÉXITO: Enviamos el token al ViewModel.
            onGoogleSignInResult(googleIdTokenCredential.idToken)
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("GoogleAuth", "Error al parsear la credencial de Google.", e)
            onGoogleSignInResult(null)
        }
    } else {
        Log.e("GoogleAuth", "Tipo de credencial personalizada inesperado: ${credential.type}")
        onGoogleSignInResult(null)
    }
}
