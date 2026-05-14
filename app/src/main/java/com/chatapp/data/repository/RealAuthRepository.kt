package com.chatapp.data.repository

import com.chatapp.core.model.User
import com.chatapp.data.remote.NetworkConfig
import com.chatapp.data.remote.TokenStore
import com.chatapp.data.remote.dto.*
import com.chatapp.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealAuthRepository @Inject constructor(
    private val tokenStore: TokenStore,
) : AuthRepository {

    override suspend fun login(email: String, password: String): User = withContext(Dispatchers.IO) {
        val raw = NetworkConfig.postJson("/auth/login", LoginRequest(email, password))
        val resp = NetworkConfig.parseResponse<AuthResponse>(raw)
        if (resp.code != 0 || resp.data == null) throw Exception(resp.message)
        tokenStore.saveAccessToken(resp.data.accessToken)
        resp.data.user.toModel()
    }

    override suspend fun register(email: String, password: String, nickname: String): User = withContext(Dispatchers.IO) {
        val raw = NetworkConfig.postJson("/auth/register", RegisterRequest(email, password, nickname))
        val resp = NetworkConfig.parseResponse<AuthResponse>(raw)
        if (resp.code != 0 || resp.data == null) throw Exception(resp.message)
        tokenStore.saveAccessToken(resp.data.accessToken)
        resp.data.user.toModel()
    }

    override suspend fun logout() {
        tokenStore.clearTokens()
    }

    override suspend fun isLoggedIn(): Boolean = tokenStore.isLoggedIn()

    override suspend fun checkSavedSession(): User? {
        val token = tokenStore.getAccessToken() ?: return null
        return try {
            val raw = NetworkConfig.getJson("/users/me", token)
            val resp = NetworkConfig.parseResponse<CurrentUserResponse>(raw)
            if (resp.code != 0) {
                tokenStore.clearTokens()
                null
            } else {
                resp.data?.toModel()
            }
        } catch (e: java.net.SocketTimeoutException) {
            null // network timeout, keep token
        } catch (e: java.net.UnknownHostException) {
            null // no network, keep token
        } catch (e: java.io.IOException) {
            null // network error, keep token
        } catch (e: Exception) {
            tokenStore.clearTokens()
            null
        }
    }
}
