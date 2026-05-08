package com.chatapp.data.repository

import com.chatapp.core.model.User
import com.chatapp.domain.repository.UserRepository
import javax.inject.Inject

class FakeUserRepository @Inject constructor() : UserRepository {
    override suspend fun getCurrentUser(): User = FakeData.me
}
