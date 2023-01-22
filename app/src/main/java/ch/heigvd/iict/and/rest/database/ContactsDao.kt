package ch.heigvd.iict.and.rest.database

import androidx.lifecycle.LiveData
import androidx.room.*
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.Status

@Dao
abstract class ContactsDao {
    @Query("SELECT * FROM Contact WHERE status != :status")
    protected abstract fun getContactsWithout(status: Status): LiveData<List<Contact>>

    @Insert
    abstract fun insert(contact: Contact): Long

    @Update
    abstract fun update(contact: Contact)

    @Delete
    abstract fun delete(contact: Contact)

    fun getContacts(): LiveData<List<Contact>> = getContactsWithout(Status.DELETED)

    @Query("SELECT * FROM Contact WHERE id = :id")
    abstract fun getContactById(id: Long): Contact?

    @Query("SELECT COUNT(*) FROM Contact")
    abstract fun getCount(): Int

    @Query("DELETE FROM Contact")
    abstract fun clearAllContacts()

    @Query("DELETE FROM Contact")
    abstract fun deleteAll()

}