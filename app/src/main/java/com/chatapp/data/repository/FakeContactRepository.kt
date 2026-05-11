package com.chatapp.data.repository

import com.chatapp.core.model.Contact
import com.chatapp.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FakeContactRepository @Inject constructor() : ContactRepository {

    private val contactsFlow = MutableStateFlow(FakeData.contacts)

    fun refresh() {
        contactsFlow.value = FakeData.contacts.toList()
    }

    override fun getContacts(): Flow<List<Contact>> = contactsFlow

    override fun getContact(userId: String): Contact? =
        FakeData.contacts.find { it.user.id == userId }

    override fun refreshContacts() {
        contactsFlow.value = FakeData.contacts.toList()
    }
}
