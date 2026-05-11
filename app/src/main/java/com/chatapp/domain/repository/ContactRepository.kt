package com.chatapp.domain.repository

import com.chatapp.core.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getContacts(): Flow<List<Contact>>
    fun getContact(userId: String): Contact?
    fun refreshContacts()
}
