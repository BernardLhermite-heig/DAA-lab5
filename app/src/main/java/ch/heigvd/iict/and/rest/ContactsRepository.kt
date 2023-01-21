package ch.heigvd.iict.and.rest

import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact

class ContactsRepository(private val contactsDao: ContactsDao) {

    val allContacts = contactsDao.getAllContactsLiveData()

    fun addContact(contact: Contact) {
        contactsDao.insert(contact)
    }

    fun updateContact(contact: Contact) {
        contactsDao.update(contact)
    }

    fun deleteContact(contact: Contact) {
        contactsDao.delete(contact)
    }
}