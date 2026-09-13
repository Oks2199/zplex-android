package zechs.zplex.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import zechs.zplex.data.model.entities.Movie

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovie(media: Movie): Long

    @Query("SELECT * FROM movies WHERE fileId IS NOT NULL AND fileId != '' ORDER BY modifiedTime DESC")
    fun getLibraryMoviesAsLiveData(): LiveData<List<Movie>>

    @Query("SELECT * FROM movies WHERE fileId IS NOT NULL AND fileId != '' ORDER BY modifiedTime DESC")
    fun getLibraryMovies(): List<Movie>

    @Query("SELECT * FROM movies WHERE fileId IS NULL OR fileId = '' ORDER BY title COLLATE NOCASE")
    fun getWatchlistMoviesAsLiveData(): LiveData<List<Movie>>

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    fun observeMovieById(id: Int): LiveData<Movie?>

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    suspend fun getMovieById(id: Int): Movie?

    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteMovieById(id: Int)

}
