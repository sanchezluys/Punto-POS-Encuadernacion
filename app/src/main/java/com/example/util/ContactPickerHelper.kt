package com.example.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract

data class ContactInfo(
    val name: String,
    val phone: String,
    val email: String
)

object ContactPickerHelper {

    fun extractContactInfo(context: Context, contactUri: Uri): ContactInfo {
        var name = ""
        var phone = ""
        var email = ""

        try {
            val contentResolver = context.contentResolver

            // 1. Get Contact ID & Display Name from Contact URI
            val cursor: Cursor? = contentResolver.query(
                contactUri,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER
                ),
                null,
                null,
                null
            )

            var contactId: String? = null
            var hasPhoneNumber = 0

            cursor?.use {
                if (it.moveToFirst()) {
                    val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                    if (idIndex != -1) contactId = it.getString(idIndex)
                    if (nameIndex != -1) name = it.getString(nameIndex) ?: ""
                    if (hasPhoneIndex != -1) hasPhoneNumber = it.getInt(hasPhoneIndex)
                }
            }

            // 2. Query Phone Number if available
            if (contactId != null && hasPhoneNumber > 0) {
                val phoneCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )
                phoneCursor?.use {
                    if (it.moveToFirst()) {
                        val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (numIdx != -1) {
                            phone = it.getString(numIdx) ?: ""
                        }
                    }
                }
            }

            // 3. Query Email if available
            if (contactId != null) {
                val emailCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
                    "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )
                emailCursor?.use {
                    if (it.moveToFirst()) {
                        val emailIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                        if (emailIdx != -1) {
                            email = it.getString(emailIdx) ?: ""
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ContactInfo(
            name = name.trim(),
            phone = phone.trim(),
            email = email.trim()
        )
    }
}
