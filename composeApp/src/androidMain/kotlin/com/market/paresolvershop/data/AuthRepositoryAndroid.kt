package com.market.paresolvershop.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.market.paresolvershop.data.model.AuthUser
import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.domain.model.DataResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthRepositoryAndroid : AuthRepository {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    /**
     * **FUNCIÓN CENTRALIZADA:**
     * Obtiene los datos del usuario desde Firestore. Si el documento no existe
     * (porque es el primer login con un proveedor social como Google),
     * lo crea en Firestore.
     * @param firebaseUser El usuario de Firebase Auth.
     * @return El objeto AuthUser enriquecido con datos de Firestore.
     */
    private suspend fun getOrCreateAuthUserInFirestore(firebaseUser: FirebaseUser): AuthUser {
        val userDocRef = db.collection("users").document(firebaseUser.uid)
        val userDoc = userDocRef.get().await()

        // Si el usuario ya existe en Firestore, obtenemos sus datos
        return if (userDoc.exists()) {
            AuthUser(
                id = firebaseUser.uid,
                email = userDoc.getString("email") ?: firebaseUser.email ?: "Sin Email",
                name = userDoc.getString("name") ?: firebaseUser.displayName ?: "Sin Nombre",
                role = userDoc.getString("role") ?: "client" // Rol por defecto si no está en la BD
            )
        } else {
            // Si no existe (primer login con Google), lo creamos
            val newUser = AuthUser(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "Sin Email",
                name = firebaseUser.displayName ?: "Sin Nombre",
                role = "client" // Rol por defecto para nuevos usuarios
            )
            // Guardamos el nuevo usuario en Firestore
            userDocRef.set(newUser).await()
            newUser
        }
    }

    override fun getCurrentUser(): AuthUser? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let { AuthUser(it.uid, it.email ?: "", it.displayName ?: "", "client") } // Role should be fetched from DB if needed synchronously
    }

    /**
     * Flow que observa el estado de autenticación en tiempo real.
     * Ahora utiliza la función centralizada para obtener el perfil completo.
     */
    override val authState: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                // Lanzamos una corrutina para obtener el perfil de Firestore
                this.launch {
                    trySend(getOrCreateAuthUserInFirestore(firebaseUser))
                }
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): DataResult<AuthUser> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val user = authResult.user ?: error("Error de autenticación")
        // Obtenemos el perfil completo de Firestore
        val authUser = getOrCreateAuthUserInFirestore(user)
        DataResult.Success(authUser)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error al iniciar sesión")
    }

    /**
     * El login con Google ahora solo se encarga de la autenticación.
     * La creación del perfil en Firestore se delega a 'getOrCreateAuthUserInFirestore'.
     */
    override suspend fun signInWithGoogle(idToken: String): DataResult<AuthUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: error("Error al obtener el usuario de Firebase")
            // Obtenemos (o creamos) el perfil completo de Firestore
            val authUser = getOrCreateAuthUserInFirestore(user)
            DataResult.Success(authUser)
        } catch (e: Exception) {
            DataResult.Error(e.message ?: "Error al autenticar con Google en Firebase")
        }
    }

    /**
     * Crea el usuario en Auth y luego llama a 'getOrCreateAuthUserInFirestore'
     * para crear el documento en Firestore, evitando duplicar lógica.
     */
    override suspend fun signUp(name: String, email: String, password: String): DataResult<AuthUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("El usuario es nulo tras la creación")

        // Actualizamos el nombre en Firebase Auth
        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        user.updateProfile(profileUpdates).await()

        // Creamos el documento en Firestore usando la función centralizada
        // (es la primera vez, así que la función lo creará)
        val authUser = getOrCreateAuthUserInFirestore(user)
        DataResult.Success(authUser)
    }.getOrElse {
        DataResult.Error(it.message ?: "Error en el registro")
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
