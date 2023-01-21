package ch.heigvd.iict.and.rest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import java.text.DateFormat
import java.util.*

enum class ActionType {
    CANCEL, DELETE, SAVE
}

@Composable
fun ScreenContactEditor(
    contact: Contact? = null,
    onClose: (ActionType, Contact?) -> Unit
) {
    val newContact = contact?.copy() ?: Contact.empty()

    var formattedBirthday = newContact.birthday?.let {
        "${it.get(Calendar.DAY_OF_MONTH)}.${it.get(Calendar.MONTH + 1)}.${it.get(Calendar.YEAR)}"
    } ?: ""
    val phoneTypes = PhoneType.values()
    val (selectedPhoneType, setSelectedPhoneType) = remember {
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
        TextFieldWithLabel("Birthday", formattedBirthday) {
            formattedBirthday = it
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
                            selected = selectedPhoneType == type,
                            onClick = {
                                setSelectedPhoneType(type)
                                newContact.type = type
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selectedPhoneType == type,
                        onClick = {
                            setSelectedPhoneType(type)
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
                DateFormat.getInstance().parse(formattedBirthday)?.let { date ->
                    newContact.birthday = Calendar.getInstance().apply {
                        time = date
                    }
                }

                onClose(ActionType.SAVE, newContact)
            }) {
                Text("Save")
                Icon(Icons.Filled.Done, "Save contact")
            }
        }
    }
}

@Composable
fun TextFieldWithLabel(label: String, value: String? = null, onChanged: (String) -> Unit = {}) {
    val (text, setText) = remember { mutableStateOf(value ?: "") }

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
            value = text,
            onValueChange = {
                setText(it)
                onChanged(it)
            },
            modifier = Modifier
                .weight(4f),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
            ),
        )
    }
}

val contacts = listOf(
    Contact(
        null,
        "Dupont",
        "Roger",
        null,
        null,
        "",
        "1400",
        "Yverdon",
        PhoneType.HOME,
        "+41 21 944 23 55"
    ),
    Contact(
        null,
        "Dupond",
        "Tatiana",
        null,
        null,
        "",
        "1000",
        "Lausanne",
        PhoneType.OFFICE,
        "+41 24 763 34 12"
    ),
    Contact(
        null,
        "Toto",
        "Tata",
        null,
        null,
        "",
        "1400",
        "Yverdon",
        PhoneType.MOBILE,
        "+41 21 456 25 36"
    )
)
