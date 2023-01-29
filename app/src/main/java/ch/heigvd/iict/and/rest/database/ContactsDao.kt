package ch.heigvd.iict.and.rest.database

import androidx.lifecycle.LiveData
import androidx.room.*
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.Status

/**
 * DAO gérant les contacts
 *
 * @author Marengo Stéphane, Friedli Jonathan, Silvestri Géraud
 */
@Dao
abstract class ContactsDao {
    @Query("SELECT * FROM Contact WHERE status != :status")
    protected abstract fun getContactsWithout(status: Status): LiveData<List<Contact>>

    @Query("SELECT * FROM Contact WHERE status != :status")
    protected abstract suspend fun getContactsWithoutAsync(status: Status): List<Contact>

    @Insert
    abstract fun insert(contact: Contact): Long

    @Update
    abstract fun update(contact: Contact)

    @Delete
    abstract fun delete(contact: Contact)

    fun getContacts(): LiveData<List<Contact>> = getContactsWithout(Status.DELETED)

    suspend fun getUnsynchronizedContacts(): List<Contact> = getContactsWithoutAsync(Status.OK)

    @Query("SELECT * FROM Contact WHERE id = :id")
    abstract fun getContactById(id: Long): Contact?

    @Query("DELETE FROM Contact")
    abstract fun deleteAll()

    @Insert
    abstract fun insertAll(vararg contacts: Contact)

}