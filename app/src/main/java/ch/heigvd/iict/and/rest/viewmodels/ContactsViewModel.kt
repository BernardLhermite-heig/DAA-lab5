package ch.heigvd.iict.and.rest.viewmodels

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.ContactsSynchronizer
import ch.heigvd.iict.and.rest.models.Contact
import kotlinx.coroutines.launch

class ContactsViewModel(private val application: ContactsApplication) :
    AndroidViewModel(application) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    init {
        viewModelScope.launch {
            val uuid = ContactsSynchronizer.getUUID(prefs)
            synchronizer = ContactsSynchronizer(uuid)
        }
    }

    private val repository = application.repository
    private lateinit var synchronizer: ContactsSynchronizer

    val allContacts = repository.allContacts

    fun enroll() {
        viewModelScope.launch {
            repository.deleteAll()
            synchronizer.uuid = ContactsSynchronizer.getNewUUID(prefs)

            val contacts = synchronizer.getContacts()

            repository.addFromRemote(contacts)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.synchronize()
        }
    }

    fun delete(contact: Contact) {
        viewModelScope.launch {
            repository.delete(contact)
        }
    }

    fun save(contact: Contact) {
        viewModelScope.launch {
            if (repository.exists(contact))
                repository.update(contact)
            else
                repository.add(contact)
        }
    }

}

class ContactsViewModelFactory(private val application: ContactsApplication) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}