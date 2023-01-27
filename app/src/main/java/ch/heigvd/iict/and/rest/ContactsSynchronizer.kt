package ch.heigvd.iict.and.rest

import android.content.SharedPreferences
import ch.heigvd.iict.and.rest.models.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class ContactsSynchronizer(var uuid: UUID) {
    companion object {
        private val baseURL = URL("https://daa.iict.ch")
        private val enrollURL = URL(baseURL, "enroll")
        private val contactsURL = URL(baseURL, "contacts")
        private const val UUID_KEY = "UUID_KEY"

        suspend fun getOrNewUUID(sharedPreferences: SharedPreferences): UUID =
            withContext(Dispatchers.IO) {
                val storedUUID = sharedPreferences.getString(UUID_KEY, null)?.let {
                    UUID.fromString(it)
                }
                storedUUID ?: newUUID(sharedPreferences)
            }

        suspend fun newUUID(sharedPreferences: SharedPreferences): UUID =
            withContext(Dispatchers.IO) {
                val uuid = UUID.fromString(enrollURL.readText())
                sharedPreferences.edit().putString(UUID_KEY, uuid.toString()).apply()
                uuid
            }
    }

    suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val json = get(contactsURL)
        Json.decodeFromString(ListSerializer(Contact.serializer()), json)
    }

    private fun get(url: URL): String = (url.openConnection() as HttpURLConnection).apply {
        setRequestProperty("X-UUID", uuid.toString())
    }.inputStream.bufferedReader().readText()
}