package com.chatapp.data.repository

import com.chatapp.core.model.Contact
import com.chatapp.data.remote.ApiException
import com.chatapp.data.remote.NetworkConfig
import com.chatapp.data.remote.TokenStore
import com.chatapp.data.remote.dto.ContactsResponse
import com.chatapp.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealContactRepository @Inject constructor(
    private val tokenStore: TokenStore,
) : ContactRepository {

    private val contactsFlow = MutableStateFlow<List<Contact>>(emptyList())

    override fun getContacts(): Flow<List<Contact>> = contactsFlow

    override fun getContact(userId: String): Contact? =
        contactsFlow.value.find { it.user.id == userId }

    override fun refreshContacts() {
        try {
            val token = tokenStore.getAccessToken() ?: return
            android.util.Log.d("ContactRepo", "GET /contacts token=${token.take(8)}...")
            val raw = NetworkConfig.getJson("/contacts", token)
            val resp = NetworkConfig.parseResponse<ContactsResponse>(raw)
            val contacts = resp.data?.contacts?.map { it.toModel() } ?: emptyList()
            android.util.Log.d("ContactRepo", "GET /contacts -> code=${resp.code}, count=${contacts.size}")
            if (resp.code == 0) {
                contactsFlow.value = contacts
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactRepo", "refreshContacts failed", e)
        }
    }

    suspend fun fetchContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val token = tokenStore.getAccessToken() ?: return@withContext emptyList()
        android.util.Log.d("ContactRepo", "GET /contacts token=${token.take(8)}...")
        val raw = NetworkConfig.getJson("/contacts", token)
        val resp = NetworkConfig.parseResponse<ContactsResponse>(raw)
        val contacts = if (resp.code == 0) resp.data?.contacts?.map { it.toModel() } ?: emptyList()
                       else emptyList()
        android.util.Log.d("ContactRepo", "GET /contacts -> code=${resp.code}, count=${contacts.size}")
        contactsFlow.value = contacts
        contacts
    }
}
