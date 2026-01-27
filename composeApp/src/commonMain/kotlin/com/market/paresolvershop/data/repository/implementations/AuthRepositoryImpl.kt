package com.market.paresolvershop.data.repository.implementations

import com.market.paresolvershop.data.model.AuthUser
import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.domain.model.DataResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    /**
     * Obtiene o crea el perfil del usuario en la tabla 'users' de Supabase.
     */
    private suspend fun getOrCreateUserProfile(userId: String, email: String, name: String): AuthUser {
        return try {
            // Intentar obtener el perfil existente
            val profile = supabase.from("users")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<UserProfile>()

            if (profile != null) {
                AuthUser(
                    id = profile.id,
                    email = profile.email,
                    name = profile.name,
                    role = profile.role
                )
            } else {
                // Si no existe, crear nuevo perfil
                val newProfile = UserProfile(
                    id = userId,
                    email = email,
                    name = name,
                    role = "client"
                )
                supabase.from("users").insert(newProfile)
                
                AuthUser(
                    id = newProfile.id,
                    email = newProfile.email,
                    name = newProfile.name,
                    role = newProfile.role
                )
            }
        } catch (e: Exception) {
            // Si falla, imprimir el error y devolver usuario básico
            println("ERROR al crear/obtener perfil de usuario: ${e.message}")
            e.printStackTrace()
            AuthUser(
                id = userId,
                email = email,
                name = name,
                role = "client"
            )
        }
    }

    override fun getCurrentUser(): AuthUser? {
        val session = supabase.auth.currentSessionOrNull()
        val user = session?.user ?: return null
        
        return AuthUser(
            id = user.id,
            email = user.email ?: "",
            name = user.userMetadata?.get("name") as? String ?: "",
            role = "client" // El rol se obtiene de forma asíncrona
        )
    }

    override val authState: Flow<AuthUser?> = flow {
        supabase.auth.sessionStatus.collect { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    val user = status.session.user
                    val userId = user?.id
                    if (userId != null) {
                        val email = user.email ?: ""
                        val name = user.userMetadata?.get("name") as? String ?: ""
                        
                        // Obtener perfil completo de la BD
                        val authUser = getOrCreateUserProfile(userId, email, name)
                        emit(authUser)
                    } else {
                        emit(null)
                    }
                }
                is SessionStatus.NotAuthenticated -> {
                    emit(null)
                }
                SessionStatus.Initializing -> {
                    // No emitir nada durante inicialización
                }
                is SessionStatus.RefreshFailure -> {
                    emit(null)
                }
            }
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): DataResult<AuthUser> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val user = supabase.auth.currentUserOrNull() ?: error("Usuario no encontrado")
            val userId = user.id
            val name = user.userMetadata?.get("name") as? String ?: ""
            
            val authUser = getOrCreateUserProfile(userId, user.email ?: email, name)
            DataResult.Success(authUser)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al iniciar sesión")
        }
    }

    override suspend fun signInWithGoogle(idToken: String): DataResult<AuthUser> {
        return try {
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }
            
            val user = supabase.auth.currentUserOrNull() ?: error("Usuario no encontrado")
            val userId = user.id ?: error("ID de usuario no disponible")
            val email = user.email ?: ""
            val name = user.userMetadata?.get("full_name") as? String 
                ?: user.userMetadata?.get("name") as? String 
                ?: ""
            
            val authUser = getOrCreateUserProfile(userId, email, name)
            DataResult.Success(authUser)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al autenticar con Google")
        }
    }

    override suspend fun signUp(name: String, email: String, password: String): DataResult<AuthUser> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("name", name)
                }
            }
            
            val user = supabase.auth.currentUserOrNull() ?: error("Usuario no encontrado")
            val userId = user.id
            val authUser = getOrCreateUserProfile(userId, email, name)
            
            DataResult.Success(authUser)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error en el registro")
        }
    }

    override suspend fun signOut() {
        supabase.auth.signOut()
    }
}
