package com.chatapp.data.repository

import com.chatapp.core.model.Contact
import com.chatapp.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeContactRepository @Inject constructor() : ContactRepository {
    override fun getContacts(): Flow<List<Contact>> = flowOf(FakeData.contacts)

    override fun getContact(userId: String): Contact? =
        FakeData.contacts.find { it.user.id == userId }
}
