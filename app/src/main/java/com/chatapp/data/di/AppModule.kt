package com.chatapp.data.di

import com.chatapp.data.repository.FakeContactRepository
import com.chatapp.data.repository.FakeConversationRepository
import com.chatapp.data.repository.FakeAuthRepository
import com.chatapp.data.repository.FakeUserRepository
import com.chatapp.domain.repository.ContactRepository
import com.chatapp.domain.repository.ConversationRepository
import com.chatapp.domain.repository.AuthRepository
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
    abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(impl: FakeConversationRepository): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: FakeContactRepository): ContactRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: FakeUserRepository): UserRepository
}
