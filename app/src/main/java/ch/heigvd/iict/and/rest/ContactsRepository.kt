package ch.heigvd.iict.and.rest

import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(private val contactsDao: ContactsDao) {
    val allContacts = contactsDao.getContacts()

    suspend fun add(contact: Contact) = withContext(Dispatchers.IO) {
        contact.status = Status.NEW
        contact.remoteId = null

        contactsDao.insert(contact)
    }

    suspend fun update(contact: Contact) = withContext(Dispatchers.IO) {
        contact.status = Status.MODIFIED

        contactsDao.update(contact)
    }

    suspend fun delete(contact: Contact) = withContext(Dispatchers.IO) {
        contact.status = Status.DELETED

        contactsDao.update(contact)


    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        contactsDao.deleteAll()
    }
}