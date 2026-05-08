package com.chatapp.feature.contacts

import com.chatapp.core.model.Contact

data class ContactsUiData(
    val contacts: List<Contact>,
    val query: String = "",
) {
    val filtered: List<Contact>
        get() = if (query.isBlank()) contacts
                else contacts.filter {
                    it.user.nickname.contains(query, ignoreCase = true)
                }
}
