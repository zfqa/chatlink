package com.chatapp.data.di

import com.chatapp.data.repository.RealConversationRepository
import com.chatapp.data.repository.RealAuthRepository
import com.chatapp.data.repository.RealContactRepository
import com.chatapp.data.repository.RealFriendRepository
import com.chatapp.data.repository.FakeUserRepository
import com.chatapp.domain.repository.ContactRepository
import com.chatapp.domain.repository.ConversationRepository
import com.chatapp.domain.repository.AuthRepository
import com.chatapp.domain.repository.FriendRepository
import com.chatapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: RealAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(impl: RealConversationRepository): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: RealContactRepository): ContactRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: FakeUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindFriendRepository(impl: RealFriendRepository): FriendRepository
}
