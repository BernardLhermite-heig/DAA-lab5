package ch.heigvd.iict.and.rest.viewmodels

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.models.Contact
import kotlinx.coroutines.launch

class ContactsViewModel(application: ContactsApplication) :
    AndroidViewModel(application) {
    private val prefs = EncryptedSharedPreferences.create(
        this.javaClass.name,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        application,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private val repository = application.repository
    val allContacts = repository.allContacts

    init {
        viewModelScope.launch {
            repository.initSynchronizer(prefs)
        }
    }

    fun enroll() {
        viewModelScope.launch {
            repository.enroll(prefs)
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
            if (contact.id != null && repository.exists(contact.id!!))
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