package zechs.zplex.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import zechs.zplex.data.model.entities.Show

@Dao
interface ShowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertShow(media: Show): Long

    @Query("SELECT * FROM shows WHERE fileId IS NOT NULL AND fileId != '' ORDER BY modifiedTime DESC")
    fun getLibraryShowsAsLiveData(): LiveData<List<Show>>

    @Query("SELECT * FROM shows WHERE fileId IS NOT NULL AND fileId != '' ORDER BY modifiedTime DESC")
    fun getLibraryShows(): List<Show>

    @Query("SELECT * FROM shows WHERE fileId IS NULL OR fileId = '' ORDER BY name COLLATE NOCASE")
    fun getWatchlistShowsAsLiveData(): LiveData<List<Show>>

    @Query("SELECT * FROM shows WHERE id = :id LIMIT 1")
    fun observeShowById(id: Int): LiveData<Show?>

    @Query("SELECT * FROM shows WHERE id = :id LIMIT 1")
    suspend fun getShowById(id: Int): Show?

    @Query("DELETE FROM shows WHERE id = :id")
    suspend fun deleteShowById(id: Int)
}
