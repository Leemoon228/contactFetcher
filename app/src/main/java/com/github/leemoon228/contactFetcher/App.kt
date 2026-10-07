package com.github.leemoon228.contactFetcher

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun App(
    state: ContactsUiState,
    canRequestPermission: Boolean,
    onPermissionAction: () -> Unit,
    onRetry: () -> Unit,
    onDial: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.contacts_title)) })
        },
    ) { contentPadding ->
        when (state) {
            ContactsUiState.PermissionRequired -> MessageContent(
                message = stringResource(R.string.contacts_permission_message),
                actionLabel = stringResource(
                    if (canRequestPermission) {
                        R.string.contacts_permission_action
                    } else {
                        R.string.open_app_settings
                    },
                ),
                onAction = onPermissionAction,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            )

            ContactsUiState.Error -> MessageContent(
                message = stringResource(R.string.contacts_load_error),
                actionLabel = stringResource(R.string.retry),
                onAction = onRetry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            )

            is ContactsUiState.Loaded -> {
                if (state.contacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(stringResource(R.string.contacts_empty))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        items(items = state.contacts) { contact ->
                            ContactRow(contact = contact, onClick = { onDial(contact.phoneNumber) })
                        }
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
