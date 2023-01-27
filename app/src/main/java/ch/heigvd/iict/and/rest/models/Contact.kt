package ch.heigvd.iict.and.rest.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Serializable
@Entity
data class Contact(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @Transient
    var remoteId: Long? = null,
    @Transient
    var status: Status = Status.NEW,
    var name: String,
    var firstname: String?,
    @Serializable(with = CalendarSerializer::class)
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

    object CalendarSerializer : KSerializer<Calendar> {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Calendar", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Calendar) {
            encoder.encodeString(
                ZonedDateTime.ofInstant(value.toInstant(), value.timeZone.toZoneId())
                    .format(formatter)
            )
        }

        override fun deserialize(decoder: Decoder): Calendar {
            val zoneDateTime = ZonedDateTime.parse(decoder.decodeString(), formatter)
            return GregorianCalendar.from(zoneDateTime)
        }
    }
}