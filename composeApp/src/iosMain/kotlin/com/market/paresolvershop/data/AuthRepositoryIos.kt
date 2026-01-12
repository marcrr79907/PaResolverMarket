package com.market.paresolvershop.data

import com.market.paresolvershop.data.model.AuthUser
import com.market.paresolvershop.data.repository.AuthRepository
import com.market.paresolvershop.domain.model.DataResult
import kotlinx.coroutines.flow.Flow

class AuthRepositoryIos: AuthRepository {

    override val authState: Flow<AuthUser?>
        get() = TODO("Not yet implemented")

    override fun getCurrentUser(): AuthUser? {
        return null
    }

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): DataResult<AuthUser> {
        TODO("Not yet implemented")
    }

    override suspend fun signInWithGoogle(idToken: String): DataResult<AuthUser> {
        TODO("Not yet implemented")
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String
    ): DataResult<AuthUser> {
        TODO("Not yet implemented")
    }

    override suspend fun signOut() {
        TODO("Not yet implemented")
    }

}