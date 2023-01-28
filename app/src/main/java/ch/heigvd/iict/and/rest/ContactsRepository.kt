package ch.heigvd.iict.and.rest

import android.content.SharedPreferences
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(private val contactsDao: ContactsDao) {
    private var synchronizer: ContactsSynchronizer? = null
    val allContacts = contactsDao.getContacts()

    suspend fun initSynchronizer(prefs: SharedPreferences) = withContext(Dispatchers.IO) {
        ContactsSynchronizer.getOrNewUUID(prefs).onSuccess { uuid ->
            synchronizer = ContactsSynchronizer(uuid)
        }
    }

    suspend fun add(contact: Contact) =
        withContext(Dispatchers.IO) {
            if (synchronizer?.insert(contact) == false) {
                contact.status = Status.NEW
            }
            contactsDao.insert(contact)
        }

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

    suspend fun enroll(prefs: SharedPreferences) = withContext(Dispatchers.IO) {
        contactsDao.deleteAll()
        ContactsSynchronizer.newUUID(prefs).onSuccess { uuid ->
            synchronizer = ContactsSynchronizer(uuid)
            val contacts = synchronizer?.getContacts() ?: return@withContext
            contactsDao.insertAll(*contacts.toTypedArray())
        }
    }

    suspend fun exists(id: Long) =
        withContext(Dispatchers.IO) {
            contactsDao.getContactById(id) != null
        }

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
    }
}