package com.github.leemoon228.contactFetcher

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun App(
    hasContactsPermission: Boolean,
    onRequestPermission: () -> Unit,
    onDial: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var contactsLoaded by rememberSaveable { mutableStateOf(false) }
    var savedContacts by rememberSaveable(
        stateSaver = listSaver(
            save = { contacts -> contacts },
            restore = { contacts -> contacts },
        ),
    ) { mutableStateOf(emptyList<String>()) }
    var loadFailed by rememberSaveable { mutableStateOf(false) }
    var toastContactCount by remember { mutableIntStateOf(0) }
    var showContactsToast by remember { mutableStateOf(false) }
    val contactsFoundFormat = stringResource(R.string.contacts_found)
    val contacts = remember(savedContacts, contactsLoaded) {
        if (contactsLoaded) {
            savedContacts.chunked(2).map { (name, number) -> Contact(name, number) }
        } else {
            null
        }
    }

    LaunchedEffect(showContactsToast) {
        if (showContactsToast) {
            Toast.makeText(
                context,
                String.format(contactsFoundFormat, toastContactCount),
                Toast.LENGTH_SHORT,
            ).show()
            showContactsToast = false
        }
    }

    LaunchedEffect(hasContactsPermission, loadFailed) {
        if (!hasContactsPermission) {
            contactsLoaded = false
            savedContacts = emptyList()
            loadFailed = false
        } else if (!contactsLoaded && !loadFailed) {
            try {
                val fetchedContacts = context.fetchAllContacts()
                savedContacts = fetchedContacts.flatMap { contact ->
                    listOf(contact.name, contact.phoneNumber)
                }
                contactsLoaded = true
                toastContactCount = fetchedContacts.size
                showContactsToast = true
            } catch (exception: SecurityException) {
                Log.w(TAG, "Contacts permission was revoked while loading contacts", exception)
                loadFailed = true
            } catch (exception: RuntimeException) {
                Log.e(TAG, "Unable to load contacts", exception)
                loadFailed = true
            }
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Text(
                text = stringResource(R.string.contacts_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )

            when {
                !hasContactsPermission -> MessageContent(
                    message = stringResource(R.string.contacts_permission_message),
                    actionLabel = stringResource(R.string.contacts_permission_action),
                    onAction = onRequestPermission,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                loadFailed -> MessageContent(
                    message = stringResource(R.string.contacts_load_error),
                    actionLabel = stringResource(R.string.retry),
                    onAction = { loadFailed = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                contacts.isNullOrEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (contacts == null) {
                            stringResource(R.string.contacts_loading)
                        } else {
                            stringResource(R.string.contacts_empty)
                        },
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(items = contacts) { contact ->
                        ContactRow(contact = contact, onClick = { onDial(contact.phoneNumber) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageContent(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onAction) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun ContactRow(contact: Contact, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Text(text = contact.name, style = MaterialTheme.typography.titleMedium)
        Text(
            text = contact.phoneNumber,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider()
}

private const val TAG = "ContactsScreen"
