package ch.heigvd.iict.and.rest.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Contact(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    var remoteId: Long? = null,
    var status: Status = Status.NEW,
    var name: String,
    var firstname: String?,
    var birthday: Calendar?,
    var email: String?,
    var address: String?,
    var zip: String?,
    var city: String?,
    var type: PhoneType?,
    var phoneNumber: String?,
) {

    override fun toString(): String {
        return "Contact(id: $id, name: $name, firstname: $firstname, " +
                "birthday: $birthday, email :$email, address: $address, zip: $zip, city: $city, " +
                "type: $type, phoneNumber: $phoneNumber, status: $status)"
    }

    companion object {
        fun empty() = Contact(
            name = "",
            firstname = null,
            birthday = null,
            email = null,
            address = null,
            zip = null,
            city = null,
            type = null,
            phoneNumber = null
        )
    }
}