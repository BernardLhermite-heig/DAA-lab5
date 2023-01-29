package ch.heigvd.iict.and.rest

import android.content.SharedPreferences
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository responsable de la gestion des contacts. Gère également la synchronisation avec le serveur.
 *
 * @author Marengo Stéphane, Friedli Jonathan, Silvestri Géraud
 */
class ContactsRepository(private val contactsDao: ContactsDao) {
    private var synchronizer: ContactsSynchronizer? = null
    val allContacts = contactsDao.getContacts()

    /**
     * Initialise le synchroniseur de contacts.
     */
    suspend fun initSynchronizer(prefs: SharedPreferences) = withContext(Dispatchers.IO) {
        ContactsSynchronizer.getOrNewUUID(prefs).onSuccess { uuid ->
            synchronizer = ContactsSynchronizer(uuid)
        }
    }

    /**
     * Ajoute un nouveau contact.
     *
     * @return l'id du contact ajouté.
     */
    suspend fun add(contact: Contact) =
        withContext(Dispatchers.IO) {
            if (synchronizer?.insert(contact) == false) {
                contact.status = Status.NEW
            }
            contactsDao.insert(contact)
        }

    /**
     * Met à jour un contact.
     *
     * @return vrai si le contact a été synchronisé avec le serveur, faux sinon.
     */
    suspend fun update(contact: Contact) =
        withContext(Dispatchers.IO) {
            val synced: Boolean

            if (contact.status == Status.NEW) {
                synced = synchronizer?.insert(contact) == true
            } else {
                synced = synchronizer?.update(contact) == true

                if (!synced) {
                    contact.status = Status.MODIFIED
                }
            }

            contactsDao.update(contact)
            synced
        }

    /**
     * Supprime un contact.
     *
     * @return vrai si le contact a été synchronisé avec le serveur, faux sinon.
     */
    suspend fun delete(contact: Contact) =
        withContext(Dispatchers.IO) {
            if (contact.status == Status.NEW || synchronizer?.delete(contact.remoteId!!) == true) {
                contactsDao.delete(contact)
                true
            } else {
                contact.status = Status.DELETED
                contactsDao.update(contact)
                false
            }
        }

    /**
     * Récupère un nouveau UUID et de nouveaux contacts depuis le serveur.
     */
    suspend fun enroll(prefs: SharedPreferences) = withContext(Dispatchers.IO) {
        contactsDao.deleteAll()
        ContactsSynchronizer.newUUID(prefs).onSuccess { uuid ->
            synchronizer = ContactsSynchronizer(uuid)
            val contacts = synchronizer?.getContacts() ?: return@withContext
            contactsDao.insertAll(*contacts.toTypedArray())
        }
    }

    /**
     * Détermine si un contact existe dans la base de données.
     *
     * @return vrai si le contact existe, faux sinon.
     */
    suspend fun exists(id: Long) =
        withContext(Dispatchers.IO) {
            contactsDao.getContactById(id) != null
        }

    /**
     * Synchronise les contacts avec le serveur.
     *
     * @return vrai si la synchronisation a réussi, faux sinon.
     */
    suspend fun synchronize() = withContext(Dispatchers.IO) {
        for (contact in contactsDao.getUnsynchronizedContacts()) {
            val isOk = when (contact.status) {
                Status.NEW -> update(contact)
                Status.MODIFIED -> update(contact)
                Status.DELETED -> delete(contact)
                Status.OK -> continue
            }

            if (!isOk) {
                return@withContext false
            }
        }

        true
    }
}