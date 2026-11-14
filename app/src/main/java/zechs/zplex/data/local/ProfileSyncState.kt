package zechs.zplex.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "profile_sync_state")
data class ProfileSyncState(
    @PrimaryKey val id: Int = 1,
    val data: String
)

@Dao
interface ProfileSyncDao {
    @Query("SELECT * FROM profile_sync_state WHERE id = 1")
    suspend fun get(): ProfileSyncState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(state: ProfileSyncState)
}
