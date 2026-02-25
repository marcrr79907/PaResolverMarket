package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.model.AuthUserEntity
import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.domain.model.DataResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val name: String,
    val role: String
)

class AuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _currentUser = MutableStateFlow<AuthUserEntity?>(null)
    override val authState: Flow<AuthUserEntity?> = _currentUser.asStateFlow()

    init {
        repositoryScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = status.session.user
                        if (user != null) {
                            try {
                                val profile = fetchProfileFromDb(user.id)
                                _currentUser.value = profile
                            } catch (e: Exception) {
                                // Error silencioso en el init para evitar loops de UI
                            }
                        }
                    }
                    is SessionStatus.NotAuthenticated, is SessionStatus.RefreshFailure -> {
                        _currentUser.value = null
                    }
                    is SessionStatus.Initializing -> {}
                }
            }
        }
    }

    private suspend fun fetchProfileFromDb(userId: String): AuthUserEntity {
        var profile: UserProfile? = null
        var lastError: String? = null

        repeat(3) { attempt ->
            try {
                profile = supabase.from("users")
                    .select {
                        filter { eq("id", userId) }
                    }
                    .decodeSingleOrNull<UserProfile>()

                if (profile != null) return@repeat
            } catch (e: Exception) {
                lastError = e.message
            }
            if (attempt < 2) delay(1000)
        }

        return profile?.let {
            AuthUserEntity(
                id = it.id,
                email = it.email,
                name = it.name,
                role = it.role
            )
        } ?: throw Exception(lastError ?: "Perfil de usuario no encontrado en la tabla 'users'.")
    }

    override fun getCurrentUser(): AuthUserEntity? = _currentUser.value

    override suspend fun signInWithEmailAndPassword(email: String, password: String): DataResult<AuthUserEntity> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = supabase.auth.currentUserOrNull() ?: error("Error en la sesión de Supabase")
            val profile = fetchProfileFromDb(user.id)

            _currentUser.value = profile
            DataResult.Success(profile)
        } catch (e: Exception) {
            DataResult.Error(mapAuthError(e))
        }
    }

    override suspend fun signInWithGoogle(idToken: String, nonce: String?): DataResult<AuthUserEntity> {
        return try {
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.nonce = nonce
                provider = Google
            }

            val user = supabase.auth.currentUserOrNull() ?: throw Exception("Error al autenticar con Google.")
            val profile = fetchProfileFromDb(user.id)

            _currentUser.value = profile
            DataResult.Success(profile)
        } catch (e: Exception) {
            DataResult.Error(mapAuthError(e))
        }
    }

    override suspend fun signUpWithEmail(name: String, email: String, password: String): DataResult<AuthUserEntity> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("name", name)
                }
            }

            val user = supabase.auth.currentUserOrNull()
            if (user == null || user.identities.isNullOrEmpty()) {
                return DataResult.Success(AuthUserEntity("", email, name, "customer"))
            }

            val profile = fetchProfileFromDb(user.id)
            _currentUser.value = profile
            DataResult.Success(profile)
        } catch (e: Exception) {
            DataResult.Error(mapAuthError(e))
        }
    }

    override suspend fun signOut() {
        supabase.auth.signOut()
        _currentUser.value = null
    }

    override suspend fun deleteAccount(): DataResult<Unit> {
        return try {
            supabase.postgrest.rpc("delete_user_account")
            signOut()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Fallo al ejecutar borrado en servidor.")
        }
    }

    private fun mapAuthError(e: Exception): String {
        val message = e.message ?: ""
        return when {
            message.contains("invalid_credentials", true) -> "Email o contraseña incorrectos."
            message.contains("Email not confirmed", true) -> "Confirma tu cuenta en tu email."
            message.contains("rate_limit", true) -> "Demasiados intentos. Espera un poco."
            message.contains("already registered", true) -> "Este email ya existe."
            message.contains("network", true) -> "Sin conexión a internet."
            // Si el error es de RLS o perfil faltante, lo mostramos tal cual para depurar
            message.contains("Perfil de usuario no encontrado", true) -> message
            else -> "Error: $message" // Cambiado de "Error inesperado" a mostrar el mensaje real
        }
    }
}
