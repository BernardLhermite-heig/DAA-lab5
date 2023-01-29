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

/**
 * Classe responsable de la synchronisation des contacts avec le serveur.
 *
 * @author Marengo Stéphane, Friedli Jonathan, Silvestri Géraud
 */
class ContactsSynchronizer(private val uuid: UUID) {
    companion object {
        private const val baseURL = "https://daa.iict.ch"
        private const val enrollURL = "$baseURL/enroll"
        private const val contactsURL = "$baseURL/contacts"
        private const val UUID_KEY = "UUID_KEY"

        /**
         * Récupère l'UUID stocké dans les préférences, ou en demande un nouveau si aucun n'est trouvé.
         *
         * @return l'UUID récupéré ou une erreur
         */
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

        /**
         * Demande un nouvel UUID au serveur.
         *
         * @return l'UUID récupéré ou une erreur
         */
        suspend fun newUUID(sharedPreferences: SharedPreferences): Result<UUID> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val uuid = UUID.fromString(enrollURL.toURL().readText())
                    sharedPreferences.edit().putString(UUID_KEY, uuid.toString()).apply()
                    uuid
                }
            }
    }

    /**
     * Récupère les contacts du serveur.
     *
     * @return la liste des contacts récupérés ou une liste vide en cas d'erreur
     */
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

    /**
     * Crée une nouvelle requête HTTP en y ajoutant l'UUID dans le header.
     *
     * @return la HttpURLConnection correspondante
     */
    private fun newRequest(url: URL, method: String): HttpURLConnection =
        (url.openConnection() as HttpURLConnection).apply {
            setRequestProperty("X-UUID", uuid.toString())
            requestMethod = method
        }

    /**
     * Exécute une requête HTTP et retourne le code de réponse et le contenu de la réponse.
     *
     * @return le code de réponse et le contenu de la réponse ou une erreur
     */
    private suspend fun execute(url: URL, method: String): Result<Pair<Int, String>> =
        withContext(Dispatchers.IO) {
            newRequest(url, method).runCatching {
                responseCode to inputStream.bufferedReader().readText()
            }
        }

    /**
     * Exécute une requête HTTP avec un payload et retourne le code de réponse et le contenu de la réponse.
     *
     * @return le code de réponse et le contenu de la réponse ou une erreur
     */
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

    /**
     * Tente d'insérer le contact donné sur le serveur.
     *
     * @return true si l'insertion a réussi, false sinon
     */
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

    /**
     * Tente de mettre à jour le contact donné sur le serveur.
     *
     * @return true si la mise à jour a réussi, false sinon
     */
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

    /**
     * Tente de supprimer le contact donné sur le serveur.
     *
     * @return true si la suppression a réussi, false sinon
     */
    suspend fun delete(id: Long): Boolean = withContext(Dispatchers.IO) {
        val (code, _) = execute("$contactsURL/$id".toURL(), "DELETE").getOrElse {
            return@withContext false
        }

        code == HttpURLConnection.HTTP_NO_CONTENT
    }
}

/**
 * Convertit une chaîne de caractères en URL.
 */
private fun String.toURL(): URL {
    return URL(this)
}