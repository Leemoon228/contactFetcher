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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.github.leemoon228.contactFetcher.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private var hasContactsPermission by mutableStateOf(false)

    private val requestContactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasContactsPermission = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasContactsPermission = checkContactsPermission()
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            testTagsAsResourceId = true
                        },
                ) { innerPadding ->
                    App(
                        hasContactsPermission = hasContactsPermission,
                        onRequestPermission = {
                            requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
                        },
                        onDial = ::openDialer,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }

        if (!hasContactsPermission) {
            requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    override fun onResume() {
        super.onResume()
        hasContactsPermission = checkContactsPermission()
    }

    private fun checkContactsPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS,
        ) == PackageManager.PERMISSION_GRANTED

    private fun openDialer(phoneNumber: String) {
        try {
            startActivity(
                Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)),
            )
        } catch (exception: ActivityNotFoundException) {
            Log.w(TAG, "No dialer app is available", exception)
            Toast.makeText(this, R.string.dialer_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private companion object {
        const val TAG = "ContactsActivity"
    }
}
