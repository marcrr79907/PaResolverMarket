package com.market.paresolvershop.data.repository

import com.market.paresolvershop.data.model.AuthUser
import com.market.paresolvershop.domain.model.DataResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val authState: Flow<AuthUser?>

    fun getCurrentUser(): AuthUser?

    suspend fun signInWithEmailAndPassword(email: String, password: String): DataResult<AuthUser>

    // Añadida la nueva función para el login con Google
    suspend fun signInWithGoogle(idToken: String): DataResult<AuthUser>

    suspend fun signUp(name: String, email: String, password: String): DataResult<AuthUser>

    suspend fun signOut()

}
