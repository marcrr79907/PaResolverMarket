package com.market.paresolvershop.data.repository

import com.market.paresolvershop.data.model.AuthUserEntity
import com.market.paresolvershop.domain.model.DataResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val authState: Flow<AuthUserEntity?>

    fun getCurrentUser(): AuthUserEntity?

    suspend fun signInWithEmailAndPassword(email: String, password: String): DataResult<AuthUserEntity>

    // Añadida la nueva función para el login con Google
    suspend fun signInWithGoogle(idToken: String, nonce: String?): DataResult<AuthUserEntity>

    suspend fun signUpWithEmail(name: String, email: String, password: String): DataResult<AuthUserEntity>

    suspend fun signOut()

}
