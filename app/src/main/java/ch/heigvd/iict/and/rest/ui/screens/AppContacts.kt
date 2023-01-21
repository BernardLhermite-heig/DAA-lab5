package ch.heigvd.iict.and.rest.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory

@Composable
fun AppContact(
    application: ContactsApplication,
    contactsViewModel: ContactsViewModel = viewModel(
        factory = ContactsViewModelFactory(application)
    )
) {
    val context = LocalContext.current
    val contacts: List<Contact> by contactsViewModel.allContacts.observeAsState(initial = emptyList())
    var editionMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (editionMode) {
                BackTopBar {
                    editionMode = false
                }
            } else {
                HomeTopBar(contactsViewModel)
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (!editionMode) {
                FloatingActionButton(onClick = {
                    editionMode = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        },
    )
    { padding ->
        Column(modifier = Modifier.padding(padding)) { }

        Box(Modifier.padding(8.dp)) {
            var selectedContact: Contact? by remember { mutableStateOf(null) }

            if (editionMode) {
                ScreenContactEditor(selectedContact) { type, contact ->
                    when (type) {
                        ActionType.CANCEL -> {
                            selectedContact = null
                        }
                        ActionType.SAVE -> {
                            contactsViewModel.save(contact!!)
                        }
                        ActionType.DELETE -> {
                            contactsViewModel.delete(contact!!)
                        }
                    }
                    editionMode = false
                }
            } else {
                ScreenContactList(contacts) { contact ->
                    selectedContact = contact
                    editionMode = true
                }
            }
        }
    }
}

@Composable
fun BackTopBar(onBackPressed: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        navigationIcon = {
            IconButton(onClick = { onBackPressed() }) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        }
    )
}

@Composable
fun HomeTopBar(contactsViewModel: ContactsViewModel) {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
            IconButton(onClick = { contactsViewModel.enroll() }) {
                Icon(painterResource(R.drawable.populate), "Populate")
            }
            IconButton(onClick = { contactsViewModel.refresh() }) {
                Icon(painterResource(R.drawable.synchronize), "Synchronize")
            }
        }
    )
}