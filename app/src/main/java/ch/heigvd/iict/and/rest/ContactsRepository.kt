package ch.heigvd.iict.and.rest

import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(private val contactsDao: ContactsDao) {
    val allContacts = contactsDao.getContacts()
    suspend fun add(contact: Contact, synchronizer: ContactsSynchronizer) =
        withContext(Dispatchers.IO) {
            if (!synchronizer.insert(contact)) {
                contact.status = Status.NEW
            }
            contactsDao.insert(contact)
        }

    suspend fun addFromRemote(contacts: List<Contact>) =
        withContext(Dispatchers.IO) {
            contactsDao.insertAll(*contacts.toTypedArray())
        }

    suspend fun update(contact: Contact, synchronizer: ContactsSynchronizer) =
        withContext(Dispatchers.IO) {
            val synced: Boolean

            if (contact.status == Status.NEW) {
                synced = synchronizer.insert(contact)
            } else {
                synced = synchronizer.update(contact)
                if (!synced) {
                    contact.status = Status.MODIFIED
                }
            }

            contactsDao.update(contact)
            synced
        }

    suspend fun delete(contact: Contact, synchronizer: ContactsSynchronizer) =
        withContext(Dispatchers.IO) {
            if (contact.status == Status.NEW || synchronizer.delete(contact.remoteId!!)) {
                contactsDao.delete(contact)
                true
            } else {
                contact.status = Status.DELETED
                contactsDao.update(contact)
                false
            }
        }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        contactsDao.deleteAll()
    }

    suspend fun exists(id: Long) =
        withContext(Dispatchers.IO) {
            contactsDao.getContactById(id) != null
        }

    suspend fun synchronize(synchronizer: ContactsSynchronizer) = withContext(Dispatchers.IO) {
        for (contact in contactsDao.getUnsynchronizedContacts()) {
            val isOk = when (contact.status) {
                Status.NEW -> update(contact, synchronizer)
                Status.MODIFIED -> update(contact, synchronizer)
                Status.DELETED -> delete(contact, synchronizer)
                Status.OK -> continue
            }

            if (!isOk) {
                return@withContext false
            }
        }
    }
}