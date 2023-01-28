package ch.heigvd.iict.and.rest

import android.content.SharedPreferences
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class ContactsSynchronizer(private val uuid: UUID) {
    companion object {
        private const val baseURL = "https://daa.iict.ch"
        private const val enrollURL = "$baseURL/enroll"
        private const val contactsURL = "$baseURL/contacts"
        private const val UUID_KEY = "UUID_KEY"

        suspend fun getOrNewUUID(sharedPreferences: SharedPreferences): Result<UUID> =
            withContext(Dispatchers.IO) {
                val storedUUID = sharedPreferences.getString(UUID_KEY, null)?.let {
                    UUID.fromString(it)
                }
                if (storedUUID != null) {
                    Result.success(storedUUID)
                } else {
                    newUUID(sharedPreferences)
                }
            }

        suspend fun newUUID(sharedPreferences: SharedPreferences): Result<UUID> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val uuid = UUID.fromString(enrollURL.toURL().readText())
                    sharedPreferences.edit().putString(UUID_KEY, uuid.toString()).apply()
                    uuid
                }
            }
    }

    suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val (code, json) = execute(contactsURL.toURL(), "GET").getOrElse {
            return@withContext emptyList<Contact>()
        }

        if (code != HttpURLConnection.HTTP_OK) {
            return@withContext emptyList<Contact>()
        }

        Json.decodeFromString(ListSerializer(Contact.serializer()), json).map {
            it.status = Status.OK
            it.remoteId = it.id
            it.id = null
            it
        }
    }

    private fun newRequest(url: URL, method: String): HttpURLConnection =
        (url.openConnection() as HttpURLConnection).apply {
            setRequestProperty("X-UUID", uuid.toString())
            requestMethod = method
        }

    private suspend fun execute(url: URL, method: String): Result<Pair<Int, String>> =
        withContext(Dispatchers.IO) {
            newRequest(url, method).runCatching {
                responseCode to inputStream.bufferedReader().readText()
            }
        }

    private suspend fun execute(
        url: URL,
        method: String,
        payload: String
    ): Result<Pair<Int, String>> = withContext(Dispatchers.IO) {
        newRequest(url, method).runCatching {
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            outputStream.bufferedWriter().use {
                it.write(payload)
            }
            responseCode to inputStream.bufferedReader().readText()
        }
    }

    suspend fun insert(contact: Contact): Boolean = withContext(Dispatchers.IO) {
        val id = contact.id
        contact.id = null

        val payload = Json.encodeToString(Contact.serializer(), contact)
        val (code, json) = execute(contactsURL.toURL(), "POST", payload).apply {
            contact.id = id
        }.getOrElse {
            return@withContext false
        }

        if (code != HttpURLConnection.HTTP_CREATED) {
            return@withContext false
        }

        Json.decodeFromString(Contact.serializer(), json).run {
            contact.remoteId = this.id
            contact.status = Status.OK
        }
        true
    }

    suspend fun update(contact: Contact): Boolean = withContext(Dispatchers.IO) {
        val id = contact.id
        contact.id = contact.remoteId

        val payload = Json.encodeToString(Contact.serializer(), contact)
        val (code, json) = execute("$contactsURL/${contact.id!!}".toURL(), "PUT", payload)
            .apply {
                contact.remoteId = contact.id
                contact.id = id
            }.getOrElse {
                return@withContext false
            }

        if (code != HttpURLConnection.HTTP_OK) {
            return@withContext false
        }

        Json.decodeFromString(Contact.serializer(), json).run {
            contact.id = id
            contact.remoteId = this.id
            contact.status = Status.OK
        }

        true
    }

    suspend fun delete(id: Long): Boolean = withContext(Dispatchers.IO) {
        val (code, _) = execute("$contactsURL/$id".toURL(), "DELETE").getOrElse {
            return@withContext false
        }

        code == HttpURLConnection.HTTP_NO_CONTENT
    }
}

private fun String.toURL(): URL {
    return URL(this)
}