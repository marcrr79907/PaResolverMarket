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
    val role: String = "client"
)

class AuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // El StateFlow es la fuente de verdad para toda la App (incluye el rol de la BD)
    private val _currentUser = MutableStateFlow<AuthUserEntity?>(null)
    override val authState: Flow<AuthUserEntity?> = _currentUser.asStateFlow()

    init {
        // Observamos el estado de la sesión globalmente
        repositoryScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = status.session.user
                        if (user != null) {
                            // Cuando hay sesión, vamos a buscar el perfil a la tabla 'users'
                            fetchAndCacheProfile(
                                userId = user.id,
                                email = user.email ?: "",
                                name = user.userMetadata?.get("name") as? String ?: ""
                            )
                        }
                    }
                    is SessionStatus.NotAuthenticated, is SessionStatus.RefreshFailure -> {
                        _currentUser.value = null
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Consulta la tabla 'users' de la base de datos pública para obtener el rol real.
     */
    private suspend fun fetchAndCacheProfile(userId: String, email: String, name: String) {
        try {
            val profile = supabase.from("users")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<UserProfile>()

            if (profile != null) {
                _currentUser.value = AuthUserEntity(
                    id = profile.id,
                    email = profile.email,
                    name = profile.name,
                    role = profile.role // Aquí ya vendrá "admin" si el trigger/DB lo tiene
                )
            } else {
                // Si por alguna razón el trigger falló o es muy lento, creamos un estado básico
                _currentUser.value = AuthUserEntity(id = userId, email = email, name = name, role = "client")
            }
        } catch (e: Exception) {
            _currentUser.value = AuthUserEntity(id = userId, email = email, name = name, role = "client")
        }
    }

    override fun getCurrentUser(): AuthUserEntity? = _currentUser.value

    override suspend fun signInWithEmailAndPassword(email: String, password: String): DataResult<AuthUserEntity> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            // Esperamos un momento a que el perfil se cargue desde la BD
            val user = supabase.auth.currentUserOrNull() ?: error("Usuario no encontrado")
            fetchAndCacheProfile(user.id, user.email ?: email, "")
            DataResult.Success(_currentUser.value!!)
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
            val user = supabase.auth.currentUserOrNull() ?: error("Usuario no encontrado")
            fetchAndCacheProfile(user.id, user.email ?: "", "")
            DataResult.Success(_currentUser.value!!)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error con Google")
        }
    }

    override suspend fun signUp(name: String, email: String, password: String): DataResult<AuthUserEntity> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject { put("name", name) }
            }
            val user = supabase.auth.currentUserOrNull() ?: error("Usuario no encontrado")
            fetchAndCacheProfile(user.id, email, name)
            DataResult.Success(_currentUser.value!!)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error en el registro")
        }
    }

    override suspend fun signOut() {
        supabase.auth.signOut()
        _currentUser.value = null
    }
}
