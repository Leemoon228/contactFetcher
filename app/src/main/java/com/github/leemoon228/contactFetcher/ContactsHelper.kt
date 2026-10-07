package com.github.leemoon228.contactFetcher

import android.content.Context
import android.provider.ContactsContract

fun Context.fetchAllContacts(): List<Contact> {
    contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null,
    ).use { cursor ->
        if (cursor == null) return emptyList()
        val contacts = ArrayList<Contact>()
        val nameColumn = cursor.getColumnIndexOrThrow(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        )
        val phoneNumberColumn = cursor.getColumnIndexOrThrow(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )

        while (cursor.moveToNext()) {
            val name = cursor.getString(nameColumn) ?: "N/A"
            val phoneNumber = cursor.getString(phoneNumberColumn) ?: "N/A"
            contacts.add(Contact(name, phoneNumber))
        }
        return contacts
    }
}
