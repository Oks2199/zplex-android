package zechs.zplex.data.profile

import androidx.room.withTransaction
import com.squareup.moshi.Moshi
import zechs.zplex.data.local.ProfileSyncState
import zechs.zplex.data.local.WatchlistDatabase
import zechs.zplex.data.model.entities.Movie
import zechs.zplex.data.model.entities.Show
import zechs.zplex.data.model.entities.WatchedMovie
import zechs.zplex.data.model.entities.WatchedShow
import java.io.IOException
import javax.inject.Inject

internal class ProfileLocalStore @Inject constructor(
    private val database: WatchlistDatabase,
    moshi: Moshi
) {
    private val stateAdapter = moshi.adapter(ProfileState::class.java)
    private val state = database.getProfileSyncDao()
    private val movies = database.getMovieDao()
    private val shows = database.getShowDao()
    private val watchedMovies = database.getWatchedMovieDao()
    private val watchedShows = database.getWatchedShowDao()

    suspend fun readState(): ProfileState? = state.get()?.let {
        stateAdapter.fromJson(it.data) ?: throw IOException("Missing profile sync state")
    }

    suspend fun save(profile: ProfileState) {
        state.save(ProfileSyncState(data = stateAdapter.toJson(profile)))
    }

    suspend fun prepareSeed(profile: ProfileState): ProfileState = database.withTransaction {
        profile.record(snapshot()).also { save(it) }
    }

    suspend fun reconcile(
        profile: ProfileState,
        remote: List<ProfileChange>,
        firstImport: Boolean
    ): ProfileState = database.withTransaction {
        val current = snapshot()
        val recorded = if (firstImport) profile else profile.record(current)
        val merged = mergeProfileChanges(recorded.applied + remote + recorded.own)
        val previous = recorded.applied.associateBy { it.key }
        val byKey = current.associateBy { it.key }

        if (firstImport) {
            val sharedKeys = merged.mapTo(hashSetOf()) { it.key }
            current.filterNot { it.key in sharedKeys }.forEach { remove(it.key) }
        }
        for (change in merged) {
            if (!firstImport && previous[change.key] == change) continue
            if (byKey[change.key] == change.value) continue
            val value = change.value
            if (value == null) remove(change.key) else write(value)
        }
        recorded.copy(applied = merged, baseline = snapshot()).also { save(it) }
    }

    private suspend fun snapshot(): List<ProfileValue> = buildList {
        movies.getWatchlistMovies().filter { it.id > 0 }.forEach {
            add(ProfileValue(ProfileKey(ProfileKind.MOVIE, it.id), it.title, it.poster_path, it.vote_average))
        }
        shows.getWatchlistShows().filter { it.id > 0 }.forEach {
            add(ProfileValue(ProfileKey(ProfileKind.SHOW, it.id), it.name, it.poster_path, it.vote_average))
        }
        watchedMovies.getProfileProgress().filter { it.tmdbId > 0 }.distinctBy { it.tmdbId }.forEach {
            add(
                ProfileValue(
                    ProfileKey(ProfileKind.MOVIE_PROGRESS, it.tmdbId), it.name, it.posterPath,
                    position = it.watchedDuration, duration = it.totalDuration,
                    watchedAt = it.createdAt, driveFileId = it.fileId.takeUnless { _ -> it.offline }
                )
            )
        }
        watchedShows.getProfileProgress()
            .filter { it.tmdbId > 0 && it.seasonNumber >= 0 && it.episodeNumber >= 0 }
            .distinctBy { Triple(it.tmdbId, it.seasonNumber, it.episodeNumber) }.forEach {
                add(
                    ProfileValue(
                        ProfileKey(ProfileKind.EPISODE_PROGRESS, it.tmdbId, it.seasonNumber, it.episodeNumber),
                        it.name, it.posterPath, position = it.watchedDuration, duration = it.totalDuration,
                        watchedAt = it.createdAt, driveFileId = it.fileId.takeUnless { _ -> it.offline }
                    )
                )
            }
    }

    private suspend fun remove(key: ProfileKey) {
        when (key.kind) {
            ProfileKind.MOVIE -> movies.deleteWatchlistMovie(key.tmdbId)
            ProfileKind.SHOW -> shows.deleteWatchlistShow(key.tmdbId)
            ProfileKind.MOVIE_PROGRESS -> watchedMovies.deleteWatchedMovie(key.tmdbId)
            ProfileKind.EPISODE_PROGRESS ->
                watchedShows.deleteEpisodeProgress(key.tmdbId, key.season, key.episode)
        }
    }

    private suspend fun write(value: ProfileValue) {
        val key = value.key
        when (key.kind) {
            ProfileKind.MOVIE -> if (movies.getMovieById(key.tmdbId)?.fileId.isNullOrBlank()) {
                movies.upsertMovie(Movie(key.tmdbId, value.title, "movie", value.poster, value.rating, null))
            }
            ProfileKind.SHOW -> if (shows.getShowById(key.tmdbId)?.fileId.isNullOrBlank()) {
                shows.upsertShow(Show(key.tmdbId, value.title, "tv", value.poster, value.rating, null))
            }
            ProfileKind.MOVIE_PROGRESS -> {
                val local = watchedMovies.getWatchedMovie(key.tmdbId)
                val offline = local?.offline == true && value.driveFileId == null
                watchedMovies.deleteWatchedMovie(key.tmdbId)
                watchedMovies.upsertWatchedMovie(
                    WatchedMovie(
                        key.tmdbId, value.title, "movie", value.poster, value.position, value.duration,
                        value.watchedAt, fileId = if (offline) local?.fileId else value.driveFileId,
                        offline = offline
                    )
                )
            }
            ProfileKind.EPISODE_PROGRESS -> {
                val local = watchedShows.getWatchedShow(key.tmdbId, key.season, key.episode)
                val offline = local?.offline == true && value.driveFileId == null
                watchedShows.deleteEpisodeProgress(key.tmdbId, key.season, key.episode)
                watchedShows.upsertWatchedShow(
                    WatchedShow(
                        key.tmdbId, value.title, "tv", value.poster, key.season, key.episode,
                        value.position, value.duration, value.watchedAt,
                        fileId = if (offline) local?.fileId else value.driveFileId, offline = offline
                    )
                )
            }
        }
    }
}
