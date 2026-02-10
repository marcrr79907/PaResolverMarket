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

    // Fuente de verdad reactiva: contiene el usuario enriquecido con el rol de la tabla 'users'
    private val _currentUser = MutableStateFlow<AuthUserEntity?>(null)
    override val authState: Flow<AuthUserEntity?> = _currentUser.asStateFlow()

    init {
        // Observamos el estado de la sesión de Supabase Auth de forma global
        repositoryScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = status.session.user
                        if (user != null) {
                            // Sincronizamos con la tabla 'users' de la base de datos
                            try {
                                val profile = fetchProfileFromDb(user.id)
                                _currentUser.value = profile
                            } catch (e: Exception) {
                                // Si no se encuentra el perfil aún (ej. trigger lento), no limpiamos el estado aún
                                // El retry de fetchProfileFromDb ya hace su trabajo
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
            if (attempt < 2) delay(1000) // Esperar 1 segundo antes de reintentar
        }

        return profile?.let {
            AuthUserEntity(
                id = it.id,
                email = it.email,
                name = it.name,
                role = it.role
            )
        } ?: throw Exception(lastError ?: "No se encontró el perfil de usuario.")
    }

    override fun getCurrentUser(): AuthUserEntity? = _currentUser.value

    override suspend fun signInWithEmailAndPassword(email: String, password: String): DataResult<AuthUserEntity> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = supabase.auth.currentUserOrNull() ?: error("Usuario no encontrado")
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

    private fun mapAuthError(e: Exception): String {
        val message = e.message ?: ""
        return when {
            message.contains("invalid_credentials", true) -> "Email o contraseña incorrectos."
            message.contains("Email not confirmed", true) -> "Por favor, confirma tu cuenta en tu correo electrónico."
            message.contains("rate_limit", true) -> "Demasiados intentos. Por favor, espera un momento."
            message.contains("already registered", true) -> "Este correo electrónico ya está registrado."
            message.contains("network", true) -> "Sin conexión a internet. Revisa tu red."
            message.contains("Invalid login credentials", true) -> "Credenciales inválidas."
            else -> "Ocurrió un error inesperado. Inténtalo de nuevo."
        }
    }
}
