package com.github.leemoon228.contactFetcher

sealed interface ContactsUiState {
    data object PermissionRequired : ContactsUiState
    data class Loaded(val contacts: List<Contact>) : ContactsUiState
    data object Error : ContactsUiState
}
