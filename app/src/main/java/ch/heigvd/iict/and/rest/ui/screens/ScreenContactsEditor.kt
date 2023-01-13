package ch.heigvd.iict.and.rest.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.ui.theme.MyComposeApplicationTheme

@Composable
fun ScreenContactEditor() {
    Column{
        myTextField("Name","")
        myTextField("Firstname","")
        myTextField("E-mail","")
        myTextField("Birthday","")
        myTextField("Address","")
        myTextField("Zip","")
        myTextField("City","")
        //todo radio button
        myTextField("Phone number","")
    }
}

@Composable
fun myTextField(title: String, value : String) {
    Row ( modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween){
        var name by remember { mutableStateOf(value) }
            Text(title)
            TextField(value = name, onValueChange = {name = it} )
    }
}


val contacts = listOf(
    Contact(null, "Dupont", "Roger", null, null, "", "1400", "Yverdon", PhoneType.HOME, "+41 21 944 23 55"),
    Contact(null, "Dupond", "Tatiana", null, null, "", "1000", "Lausanne", PhoneType.OFFICE, "+41 24 763 34 12"),
    Contact(null, "Toto", "Tata", null, null, "", "1400", "Yverdon", PhoneType.MOBILE, "+41 21 456 25 36")
)
