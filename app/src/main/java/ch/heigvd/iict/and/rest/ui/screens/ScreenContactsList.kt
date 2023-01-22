package ch.heigvd.iict.and.rest.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.models.Status
import ch.heigvd.iict.and.rest.ui.theme.MyComposeApplicationTheme

@Composable
fun ScreenContactList(contacts: List<Contact>, onContactSelected: (Contact) -> Unit) {
    Column {
        Text(text = stringResource(R.string.screen_list_title), fontSize = 24.sp)
        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.screen_list_empty),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(contacts) { item ->
                    ContactItemView(item) { clickedContact ->
                        onContactSelected(clickedContact)
                    }
                }
            }

        }
    }
}

@Composable
fun ContactItemView(contact: Contact, onClick: (Contact) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(2.dp)
            .clickable {
                onClick(contact)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.contact),
            contentDescription = stringResource(id = R.string.screen_list_contacticon_ctndesc),
            colorFilter = ColorFilter.tint(
                when (contact.status) {
                    Status.NEW -> Color.Blue
                    Status.MODIFIED -> Color.Yellow
                    Status.DELETED -> Color.Red
                    Status.OK -> Color.Green
                }
            )
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "${contact.firstname} ${contact.name}")
            Text(text = "${contact.phoneNumber}")
        }
        Image(
            painter = painterResource(
                id = when (contact.type) {
                    PhoneType.MOBILE -> R.drawable.cellphone
                    PhoneType.FAX -> R.drawable.fax
                    PhoneType.HOME -> R.drawable.phone
                    PhoneType.OFFICE -> R.drawable.office
                    else -> R.drawable.office
                }
            ),
            contentDescription = stringResource(id = R.string.screen_list_contacttype_ctndesc),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ContactListPreview() {
    MyComposeApplicationTheme {
        ScreenContactList(contactsDemo, {})
    }
}

@Preview(showBackground = true)
@Composable
fun ContactItemPreview() {
    MyComposeApplicationTheme {
        ContactItemView(contactsDemo[0], {})
    }
}

val contactsDemo = listOf(
    Contact(
        null,
        null,
        Status.NEW,
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
        null,
        Status.NEW,
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
        null,
        Status.NEW,
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
