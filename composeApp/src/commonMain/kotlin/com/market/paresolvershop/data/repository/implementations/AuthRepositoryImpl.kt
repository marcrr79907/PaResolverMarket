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
                            val profile = fetchProfileFromDb(user.id)
                            _currentUser.value = profile
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

    /**
     * Consulta la tabla 'users' con reintentos para dar tiempo al Trigger de la DB.
     * Si falla tras los reintentos, lanza una excepción que será capturada por el ViewModel.
     */
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
        } ?: throw Exception(lastError ?: "No se encontró el perfil de usuario en la base de datos tras el registro/login.")
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
            DataResult.Error(e.message ?: "Error al iniciar sesión")
        }
    }

    override suspend fun signInWithGoogle(idToken: String): DataResult<AuthUserEntity> {
        return try {
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }

            val user = supabase.auth.currentUserOrNull() ?: throw Exception("Error al autenticar con Google.")
            val profile = fetchProfileFromDb(user.id)

            _currentUser.value = profile
            DataResult.Success(profile)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al autenticar con Google")
        }
    }

    override suspend fun signUp(name: String, email: String, password: String): DataResult<AuthUserEntity> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("name", name)
                }
            }

            val user = supabase.auth.currentUserOrNull() ?: error("Usuario no encontrado")
            val profile = fetchProfileFromDb(user.id)

            _currentUser.value = profile
            DataResult.Success(profile)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error en el registro")
        }
    }

    override suspend fun signOut() {
        supabase.auth.signOut()
        _currentUser.value = null
    }
}
