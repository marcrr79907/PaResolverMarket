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
import io.github.jan.supabase.postgrest.rpc
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
    val role: String = "customer"
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
                                // Error silencioso en inicialización
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

        if (profile == null) {
            throw Exception("ProfileNotFound: ${lastError ?: "No se encontró la fila en public.users"}")
        }

        val isAdmin = checkIsAdmin()

        return AuthUserEntity(
            id = profile!!.id,
            email = profile!!.email,
            name = profile!!.name,
            role = if (isAdmin) "admin" else profile!!.role
        )
    }

    override fun getCurrentUser(): AuthUserEntity? = _currentUser.value

    override suspend fun getAllUsers(): DataResult<List<AuthUserEntity>> {
        return try {
            val entities = supabase.from("users")
                .select()
                .decodeList<UserProfile>()

            val users = entities.map { profile ->
                AuthUserEntity(
                    id = profile.id,
                    email = profile.email,
                    name = profile.name,
                    role = profile.role
                )
            }
            DataResult.Success(users)
        } catch (e: Exception) {
            DataResult.Error("Error al cargar usuarios.")
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): DataResult<AuthUserEntity> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = supabase.auth.currentUserOrNull() ?: error("Error de sesión")
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

            val user = supabase.auth.currentUserOrNull() ?: throw Exception("Google Auth Error")
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
                // Usuario creado pero requiere confirmar email
                return DataResult.Success(AuthUserEntity(user?.id ?: "", email, name, "customer"))
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

    suspend fun checkIsAdmin(): Boolean = try {
        val result = supabase.postgrest.rpc("is_admin")
        result.decodeAs<Boolean>()
    } catch (e: Exception) {
        false
    }

    private fun mapAuthError(e: Exception): String {
        val message = e.message ?: ""
        return when {
            message.contains("invalid_credentials", true) || message.contains("Invalid login credentials", true) -> 
                "Email o contraseña incorrectos."
            message.contains("Email not confirmed", true) -> 
                "Por favor, confirma tu cuenta en tu correo electrónico."
            message.contains("ProfileNotFound", true) -> 
                "Error de sincronización: No se encontró tu perfil. Inténtalo de nuevo en unos segundos."
            message.contains("already registered", true) -> 
                "Este correo electrónico ya está registrado."
            message.contains("network", true) -> 
                "Sin conexión a internet."
            else -> "Error: ${e.message ?: "Ocurrió un error inesperado."}"
        }
    }
}
