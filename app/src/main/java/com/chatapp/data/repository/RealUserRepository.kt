package com.chatapp.data.repository

import com.chatapp.core.model.User
import com.chatapp.data.remote.ApiException
import com.chatapp.data.remote.NetworkConfig
import com.chatapp.data.remote.TokenStore
import com.chatapp.data.remote.dto.CurrentUserResponse
import com.chatapp.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealUserRepository @Inject constructor(
    private val tokenStore: TokenStore,
) : UserRepository {

    override suspend fun getCurrentUser(): User = withContext(Dispatchers.IO) {
        val token = tokenStore.getAccessToken()
            ?: throw ApiException(401, "Not logged in")
        val raw = NetworkConfig.getJson("/users/me", token)
        val resp = NetworkConfig.parseResponse<CurrentUserResponse>(raw)
        if (resp.code != 0 || resp.data == null) throw Exception(resp.message)
        resp.data.toModel()
    }
}
