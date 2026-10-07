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
        while (cursor.moveToNext()) {
            val name = cursor.getString(
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
            ) ?: "N/A"
            val phoneNumber = cursor.getString(
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER),
            ) ?: "N/A"
            contacts.add(Contact(name, phoneNumber))
        }
        return contacts
    }
}
