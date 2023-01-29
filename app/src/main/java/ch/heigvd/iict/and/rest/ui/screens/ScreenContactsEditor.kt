package ch.heigvd.iict.and.rest.ui.screens

import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Détermine le type d'action lors de la fermeture de l'écran d'édition.
 */
enum class ActionType {
    CANCEL, DELETE, SAVE
}

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

/**
 * Écran d'édition d'un contact.
 *
 * @author Marengo Stéphane, Friedli Jonathan, Silvestri Géraud
 */
@Composable
fun ScreenContactEditor(
    contact: Contact? = null,
    onClose: (ActionType, Contact?) -> Unit
) {
    val context = LocalContext.current
    val newContact = remember { contact?.copy() ?: Contact.empty() }
    val formattedBirthday = remember {
        mutableStateOf(
            newContact.birthday?.let {
                ZonedDateTime.ofInstant(it.toInstant(), it.timeZone.toZoneId())
                    .format(dateFormatter)
            } ?: ""
        )
    }
    val phoneTypes = PhoneType.values()
    val selectedPhoneType = remember {
        mutableStateOf(contact?.type ?: phoneTypes.first())
    }

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = stringResource(if (contact == null) R.string.contactTitleNew else R.string.contactTitleEdit),
            fontSize = 20.sp,
            color = Color.Gray,
        )
        TextFieldWithLabel("Name", newContact.name) {
            newContact.name = it
        }
        TextFieldWithLabel("Firstname", newContact.firstname) {
            newContact.firstname = it
        }
        TextFieldWithLabel("E-mail", newContact.email) {
            newContact.email = it
        }
        TextFieldWithLabel("Birthday", formattedBirthday.value) {
            formattedBirthday.value = it
        }
        TextFieldWithLabel("Address", newContact.address) {
            newContact.address = it
        }
        TextFieldWithLabel("Zip", newContact.zip) {
            newContact.zip = it
        }
        TextFieldWithLabel("City", newContact.city) {
            newContact.city = it
        }

        Row {
            Text("Phone type", color = Color.Gray)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            phoneTypes.forEach { type ->
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = selectedPhoneType.value == type,
                            onClick = {
                                selectedPhoneType.value = type
                                newContact.type = type
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selectedPhoneType.value == type,
                        onClick = {
                            selectedPhoneType.value = type
                            newContact.type = type
                        },
                    )
                    Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }

        TextFieldWithLabel("Phone number", contact?.phoneNumber) {
            newContact.phoneNumber = it
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(onClick = { onClose(ActionType.CANCEL, null) }) {
                Text("Cancel")
            }

            if (contact != null) {
                Button(onClick = { onClose(ActionType.DELETE, contact) }) {
                    Text("Delete")
                    Icon(Icons.Filled.Delete, "Delete contact")
                }
            }

            Button(onClick = {
                if (formattedBirthday.value.isNotEmpty()) {
                    val date = runCatching {
                        LocalDate.parse(
                            formattedBirthday.value,
                            dateFormatter
                        )
                    }.getOrElse {
                        LocalDate.now()
                    }
                    newContact.birthday = Calendar.getInstance().apply {
                        set(date.year, date.monthValue - 1, date.dayOfMonth)
                    }
                }

                if (newContact.name.isEmpty()) {
                    Toast.makeText(context, "Name is required", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                onClose(ActionType.SAVE, newContact)
            }) {
                Text("Save")
                Icon(Icons.Filled.Done, "Save contact")
            }
        }
    }
}

/**
 * Champ de texte avec un label.
 *
 * @param label Label du champ de texte.
 * @param value Valeur du champ de texte.
 * @param onChanged Événement déclenché lors de la modification de la valeur.
 */
@Composable
fun TextFieldWithLabel(label: String, value: String? = null, onChanged: (String) -> Unit = {}) {
    val focusManager = LocalFocusManager.current
    val text = remember { mutableStateOf(value ?: "") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier
                .padding(end = 16.dp)
                .weight(2f),
            color = Color.Gray
        )

        TextField(
            value = text.value,
            onValueChange = {
                text.value = it
                onChanged(it)
            },
            modifier = Modifier
                .weight(4f)
                .onPreviewKeyEvent {
                    if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_TAB && it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        focusManager.moveFocus(FocusDirection.Down)
                        true
                    } else {
                        false
                    }
                },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        )
    }
}