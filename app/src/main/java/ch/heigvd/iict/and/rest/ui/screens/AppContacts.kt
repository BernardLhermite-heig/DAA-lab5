package ch.heigvd.iict.and.rest.ui.screens

import android.widget.Toast
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
            FloatingActionButton(onClick = {
                Toast.makeText(context, "TODO - Création d'un nouveau contact", Toast.LENGTH_SHORT)
                    .show()
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
    )
    { padding ->
        Column(modifier = Modifier.padding(padding)) { }

        var selectedContact: Contact? = null
        if (editionMode) {
            ScreenContactEditor(selectedContact) {
                editionMode = false
            }
        } else {
            ScreenContactList(contacts) { contact ->
                editionMode = true
                selectedContact = contact
                println("Contact selected")
                //Toast.makeText(context, "TODO - Edition de ${selectedContact.firstname} ${selectedContact.name}", Toast.LENGTH_SHORT).show()
            }
        }

    }

}

@Composable
fun BackTopBar(onBackPressed: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) },
        navigationIcon = {
            IconButton(onClick = {
                onBackPressed()
            }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
fun HomeTopBar(contactsViewModel: ContactsViewModel) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) },
        actions = {
            IconButton(onClick = {
                contactsViewModel.enroll()
            }) {
                Icon(
                    painter = painterResource(R.drawable.populate),
                    contentDescription = null
                )
            }
            IconButton(onClick = {
                contactsViewModel.refresh()
            }) {
                Icon(
                    painter = painterResource(R.drawable.synchronize),
                    contentDescription = null
                )
            }
        }
    )
}