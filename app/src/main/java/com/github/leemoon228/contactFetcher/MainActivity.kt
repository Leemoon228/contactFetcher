package com.github.leemoon228.contactFetcher

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.github.leemoon228.contactFetcher.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private var contactsUiState by mutableStateOf<ContactsUiState>(ContactsUiState.PermissionRequired)
    private var hasRequestedPermission by mutableStateOf(false)

    private val requestContactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasRequestedPermission = true
            if (granted) {
                loadContacts()
            } else {
                contactsUiState = ContactsUiState.PermissionRequired
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasRequestedPermission =
            savedInstanceState?.getBoolean(REQUESTED_PERMISSION_KEY) ?: false
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val canRequestPermission = !hasRequestedPermission ||
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)

                App(
                    state = contactsUiState,
                    canRequestPermission = canRequestPermission,
                    onPermissionAction = {
                        if (canRequestPermission) {
                            hasRequestedPermission = true
                            requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
                        } else {
                            openAppSettings()
                        }
                    },
                    onRetry = ::loadContacts,
                    onDial = ::openDialer,
                )
            }
        }

        if (hasContactsPermission()) {
            loadContacts()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!hasContactsPermission()) {
            contactsUiState = ContactsUiState.PermissionRequired
        } else if (contactsUiState == ContactsUiState.PermissionRequired) {
            loadContacts()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(REQUESTED_PERMISSION_KEY, hasRequestedPermission)
        super.onSaveInstanceState(outState)
    }

    private fun hasContactsPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS,
        ) == PackageManager.PERMISSION_GRANTED

    private fun loadContacts() {
        if (!hasContactsPermission()) {
            contactsUiState = ContactsUiState.PermissionRequired
            return
        }

        try {
            val contacts = fetchAllContacts()
            contactsUiState = ContactsUiState.Loaded(contacts)
            Toast.makeText(
                this,
                resources.getQuantityString(R.plurals.contacts_found, contacts.size, contacts.size),
                Toast.LENGTH_SHORT,
            ).show()
        } catch (exception: SecurityException) {
            Log.w(TAG, "Contacts permission was revoked while loading contacts", exception)
            contactsUiState = ContactsUiState.PermissionRequired
        } catch (exception: RuntimeException) {
            Log.e(TAG, "Unable to load contacts", exception)
            contactsUiState = ContactsUiState.Error
        }
    }

    private fun openDialer(phoneNumber: String) {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_DIAL,
                    Uri.fromParts("tel", phoneNumber, null),
                ),
            )
        } catch (exception: ActivityNotFoundException) {
            Log.w(TAG, "No dialer app is available", exception)
            Toast.makeText(this, R.string.dialer_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppSettings() {
        startActivity(
            Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
            ),
        )
    }

    private companion object {
        const val REQUESTED_PERMISSION_KEY = "requested_contacts_permission"
        const val TAG = "ContactsActivity"
    }
}
