package zechs.zplex.ui.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import retrofit2.Response
import zechs.zplex.R
import zechs.zplex.data.local.offline.OfflineEpisodeDao
import zechs.zplex.data.local.offline.OfflineMovieDao
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.WatchAvailabilityMapper
import zechs.zplex.data.model.entities.Movie
import zechs.zplex.data.model.entities.Show
import zechs.zplex.data.model.entities.WatchedMovie
import zechs.zplex.data.model.entities.WatchedShow
import zechs.zplex.data.model.offline.OfflineEpisode
import zechs.zplex.data.model.offline.OfflineMovie
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.data.model.tmdb.media.MovieResponse
import zechs.zplex.data.model.tmdb.media.TvResponse
import zechs.zplex.data.model.tmdb.search.SearchResponse
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.data.repository.WatchedRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.ui.home.adapter.HomeDataModel
import zechs.zplex.ui.home.adapter.HomeDataModel.Section
import zechs.zplex.ui.home.adapter.watched.WatchedDataModel
import zechs.zplex.ui.shared_adapters.banner.BannerItem
import zechs.zplex.utils.state.Resource
import java.io.File as LocalFile
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    app: Application,
    private val tmdbRepository: TmdbRepository,
    private val watchedRepository: WatchedRepository,
    private val offlineMovieDao: OfflineMovieDao,
    private val offlineEpisodeDao: OfflineEpisodeDao
) : BaseAndroidViewModel(app) {

    companion object {
        private const val HOME_ROW_LIMIT = 20
        private const val TRENDING_WINDOW = "week"
    }

    private enum class LocalSource {
        LIBRARY_MOVIES,
        LIBRARY_SHOWS,
        WATCHLIST_MOVIES,
        WATCHLIST_SHOWS,
        WATCHED_MOVIES,
        WATCHED_SHOWS,
        OFFLINE_MOVIES,
        OFFLINE_EPISODES
    }

    private data class MediaKey(val type: MediaType, val id: Int)

    private data class RemoteHome(
        val banner: BannerItem? = null,
        val trending: List<Media> = emptyList(),
        val theatres: List<Media> = emptyList(),
        val streaming: List<Media> = emptyList()
    )

    private val _homeMedia = MediatorLiveData<Resource<List<HomeDataModel>>>()
    val homeMedia: LiveData<Resource<List<HomeDataModel>>>
        get() = _homeMedia

    private val readySources = mutableSetOf<LocalSource>()
    private var libraryMovies = emptyList<Movie>()
    private var libraryShows = emptyList<Show>()
    private var watchlistMovies = emptyList<Movie>()
    private var watchlistShows = emptyList<Show>()
    private var watchedMovies = emptyList<WatchedMovie>()
    private var watchedShows = emptyList<WatchedShow>()
    private var offlineMovies = emptyList<OfflineMovie>()
    private var offlineEpisodes = emptyList<OfflineEpisode>()
    private var remoteHome = RemoteHome()
    private var remoteLoaded = false
    private var lastRemoteLibrarySignature: String? = null
    private var remoteRefreshJob: Job? = null

    init {
        _homeMedia.value = Resource.Loading()
        observeLocalMedia()
    }

    private fun observeLocalMedia() {
        _homeMedia.addSource(tmdbRepository.getLibraryMoviesAsLiveData()) {
            libraryMovies = it.orEmpty()
            localSourceReady(LocalSource.LIBRARY_MOVIES)
        }
        _homeMedia.addSource(tmdbRepository.getLibraryShowsAsLiveData()) {
            libraryShows = it.orEmpty()
            localSourceReady(LocalSource.LIBRARY_SHOWS)
        }
        _homeMedia.addSource(tmdbRepository.getWatchlistMoviesAsLiveData()) {
            watchlistMovies = it.orEmpty()
            localSourceReady(LocalSource.WATCHLIST_MOVIES)
        }
        _homeMedia.addSource(tmdbRepository.getWatchlistShowsAsLiveData()) {
            watchlistShows = it.orEmpty()
            localSourceReady(LocalSource.WATCHLIST_SHOWS)
        }
        _homeMedia.addSource(watchedRepository.getAllWatchedMovies()) {
            watchedMovies = it.orEmpty()
            localSourceReady(LocalSource.WATCHED_MOVIES)
        }
        _homeMedia.addSource(watchedRepository.getAllWatchedShows()) {
            watchedShows = it.orEmpty()
            localSourceReady(LocalSource.WATCHED_SHOWS)
        }
        _homeMedia.addSource(offlineMovieDao.getAllMovies()) {
            offlineMovies = it.orEmpty()
            localSourceReady(LocalSource.OFFLINE_MOVIES)
        }
        _homeMedia.addSource(offlineEpisodeDao.getAllEpisodesAsLiveData()) {
            offlineEpisodes = it.orEmpty()
            localSourceReady(LocalSource.OFFLINE_EPISODES)
        }
    }

    private fun localSourceReady(source: LocalSource) {
        readySources += source
        refreshRemoteHomeWhenLibraryChanges()
        if (readySources.size == LocalSource.entries.size) rebuildHome()
    }

    private fun refreshRemoteHomeWhenLibraryChanges() {
        if (LocalSource.LIBRARY_MOVIES !in readySources ||
            LocalSource.LIBRARY_SHOWS !in readySources
        ) return

        val signature = (
            libraryMovies.map { "movie:${it.id}:${it.fileId}:${it.modifiedTime}" } +
                libraryShows.map { "tv:${it.id}:${it.fileId}:${it.modifiedTime}" }
            ).sorted().joinToString("|")
        if (signature == lastRemoteLibrarySignature) return
        lastRemoteLibrarySignature = signature
        refreshRemoteHome()
    }

    private fun rebuildHome() {
        if (readySources.size != LocalSource.entries.size) return

        val rows = mutableListOf<HomeDataModel>()
        remoteHome.banner?.let { rows += HomeDataModel.Banner(listOf(it)) }

        val continueWatching = buildContinueWatching()
        if (continueWatching.isNotEmpty()) {
            rows += HomeDataModel.Header(
                Section.CONTINUE_WATCHING,
                context.getString(R.string.continue_watching)
            )
            rows += HomeDataModel.Watched(watched = continueWatching)
        }

        val continueKeys = continueWatching.mapTo(mutableSetOf()) { watched ->
            when (watched) {
                is WatchedDataModel.Movie -> MediaKey(MediaType.movie, watched.movie.tmdbId)
                is WatchedDataModel.Show -> MediaKey(MediaType.tv, watched.show.tmdbId)
            }
        }

        val driveMedia = (
            libraryMovies.map { it.toMedia() } + libraryShows.map { it.toMedia() }
            )
            .sortedByDescending { it.modifiedTime ?: Long.MIN_VALUE }
            .filterNot { it.key() in continueKeys }
            .take(HOME_ROW_LIMIT)
        addMediaSection(rows, Section.DRIVE, R.string.on_your_drive, driveMedia)

        val offlineMovieIds = validOfflineMovies().mapTo(mutableSetOf()) { it.id }
        val offlineShowIds = validOfflineEpisodes().mapTo(mutableSetOf()) { it.tmdbId }
        val watchlistMedia = (
            watchlistMovies.filterNot { it.id in offlineMovieIds }.map { it.toMedia() } +
                watchlistShows.filterNot { it.id in offlineShowIds }.map { it.toMedia() }
            )
            .sortedBy { (it.title ?: it.name).orEmpty().lowercase() }
            .take(HOME_ROW_LIMIT)
        addMediaSection(rows, Section.WATCHLIST, R.string.watchlist, watchlistMedia)

        val hasDiscoveries = remoteHome.trending.isNotEmpty() ||
            remoteHome.theatres.isNotEmpty() || remoteHome.streaming.isNotEmpty()
        if (hasDiscoveries) {
            rows += HomeDataModel.DiscoveryTitle(context.getString(R.string.to_discover))
            addMediaSection(
                rows,
                Section.TRENDING,
                R.string.trending_this_week,
                remoteHome.trending
            )
            addMediaSection(
                rows,
                Section.THEATRES,
                R.string.currently_in_theatres,
                remoteHome.theatres
            )
            if (remoteHome.streaming.isNotEmpty()) {
                rows += HomeDataModel.Header(
                    Section.STREAMING,
                    context.getString(R.string.available_streaming_france)
                )
                rows += HomeDataModel.Attribution(
                    context.getString(R.string.watch_availability_attribution)
                )
                rows += HomeDataModel.Media(Section.STREAMING, remoteHome.streaming)
            }
        }

        if (rows.isEmpty() && !remoteLoaded) {
            _homeMedia.postValue(Resource.Loading())
        } else {
            _homeMedia.postValue(Resource.Success(rows))
        }
    }

    private fun addMediaSection(
        rows: MutableList<HomeDataModel>,
        section: Section,
        titleRes: Int,
        media: List<Media>
    ) {
        if (media.isEmpty()) return
        rows += HomeDataModel.Header(section, context.getString(titleRes))
        rows += HomeDataModel.Media(section, media)
    }

    private fun buildContinueWatching(): List<WatchedDataModel> {
        val libraryMovieIds = libraryMovies.mapTo(mutableSetOf()) { it.id }
        val libraryShowIds = libraryShows.mapTo(mutableSetOf()) { it.id }
        val offlineMovieIds = validOfflineMovies().mapTo(mutableSetOf()) { it.id }
        val offlineEpisodeKeys = validOfflineEpisodes().mapTo(mutableSetOf()) {
            Triple(it.tmdbId, it.seasonNumber, it.episodeNumber)
        }

        val movies = watchedMovies.asSequence()
            .filter {
                HomePlaybackRules.isInProgress(it.watchedDuration, it.totalDuration)
            }
            .filter { watched ->
                HomePlaybackRules.hasPlayableSource(
                    savedFileId = watched.fileId,
                    savedAsOffline = watched.offline,
                    savedOfflineFileExists = watched.fileId?.let { LocalFile(it).isFile } == true,
                    indexedOnDrive = watched.tmdbId in libraryMovieIds,
                    matchingOfflineDownloadExists = watched.tmdbId in offlineMovieIds
                )
            }
            .groupBy { it.tmdbId }
            .values
            .mapNotNull { entries -> entries.maxByOrNull { it.createdAt } }
            .map { WatchedDataModel.Movie(it) }

        val shows = watchedShows.asSequence()
            .filter {
                HomePlaybackRules.isInProgress(it.watchedDuration, it.totalDuration)
            }
            .filter { watched ->
                val episodeKey = Triple(
                    watched.tmdbId,
                    watched.seasonNumber,
                    watched.episodeNumber
                )
                HomePlaybackRules.hasPlayableSource(
                    savedFileId = watched.fileId,
                    savedAsOffline = watched.offline,
                    savedOfflineFileExists = watched.fileId?.let { LocalFile(it).isFile } == true,
                    indexedOnDrive = watched.tmdbId in libraryShowIds,
                    matchingOfflineDownloadExists = episodeKey in offlineEpisodeKeys
                )
            }
            .groupBy { it.tmdbId }
            .values
            .mapNotNull { entries -> entries.maxByOrNull { it.createdAt } }
            .map { WatchedDataModel.Show(it) }

        return (movies + shows)
            .sortedByDescending { watched ->
                when (watched) {
                    is WatchedDataModel.Movie -> watched.movie.createdAt
                    is WatchedDataModel.Show -> watched.show.createdAt
                }
            }
            .take(HOME_ROW_LIMIT)
    }

    private fun validOfflineMovies() = offlineMovies.filter { LocalFile(it.filePath).isFile }

    private fun validOfflineEpisodes() = offlineEpisodes.filter { LocalFile(it.filePath).isFile }

    private fun refreshRemoteHome() {
        remoteRefreshJob?.cancel()
        remoteRefreshJob = viewModelScope.launch(Dispatchers.IO) {
            val refreshedHome = supervisorScope {
                val driveBanner = async { runCatching { findDriveBanner() }.getOrNull() }

                if (!hasInternetConnection()) {
                    return@supervisorScope RemoteHome(banner = driveBanner.await())
                }

                val trending = async {
                    runCatching { tmdbRepository.getTrending(TRENDING_WINDOW) }.getOrNull()
                }
                val theatres = async {
                    runCatching { tmdbRepository.getNowPlaying() }.getOrNull()
                }
                val streamingMovies = async {
                    runCatching { tmdbRepository.getStreamingMovies() }.getOrNull()
                }
                val streamingShows = async {
                    runCatching { tmdbRepository.getStreamingShows() }.getOrNull()
                }

                val trendingMedia = trending.await().mediaResults()
                    .mapNotNull { it.normalizedMediaType() }
                    .filter { it.poster_path != null }
                    .distinctBy { it.key() }
                    .take(HOME_ROW_LIMIT)

                val theatreMedia = theatres.await().mediaResults()
                    .map { it.copy(media_type = MediaType.movie) }
                    .filter { it.poster_path != null }
                    .distinctBy { it.key() }
                    .take(HOME_ROW_LIMIT)

                val streamingMedia = (
                    streamingMovies.await().mediaResults().map {
                        it.copy(media_type = MediaType.movie)
                    } + streamingShows.await().mediaResults().map {
                        it.copy(media_type = MediaType.tv)
                    }
                    )
                    .filter { it.poster_path != null }
                    .distinctBy { it.key() }
                    .sortedByDescending { it.popularity ?: 0.0 }
                    .take(HOME_ROW_LIMIT)

                val banner = driveBanner.await()
                    ?: theatreMedia.firstOrNull()?.let {
                        BannerItem(it, context.getString(R.string.in_theatres))
                    }
                    ?: streamingMedia.firstOrNull()?.let {
                        val label = runCatching { streamingBannerLabel(it) }
                            .getOrElse {
                                context.getString(R.string.available_on_streaming_attributed)
                            }
                        BannerItem(it, label)
                    }

                RemoteHome(
                    banner = banner,
                    trending = trendingMedia,
                    theatres = theatreMedia,
                    streaming = streamingMedia
                )
            }
            withContext(Dispatchers.Main) {
                remoteHome = refreshedHome
                remoteLoaded = true
                rebuildHome()
            }
        }
    }

    private suspend fun findDriveBanner(): BannerItem? {
        val candidates = (
            tmdbRepository.getLibraryMovies().map { it.toMedia() } +
                tmdbRepository.getLibraryShows().map { it.toMedia() }
            )
            .sortedByDescending { it.modifiedTime ?: Long.MIN_VALUE }

        candidates.forEach { localMedia ->
            val enriched = when (localMedia.media_type) {
                MediaType.movie -> tmdbRepository.getMovie(localMedia.id)
                    .body()
                    ?.toMedia(localMedia)
                MediaType.tv -> tmdbRepository.getShow(localMedia.id)
                    .body()
                    ?.toMedia(localMedia)
                else -> null
            }
            if (enriched?.poster_path != null) {
                return BannerItem(enriched, context.getString(R.string.on_your_drive))
            }
        }
        return null
    }

    private suspend fun streamingBannerLabel(media: Media): String {
        val providers = when (media.media_type) {
            MediaType.movie -> tmdbRepository.getMovieWatchProviders(media.id)
            MediaType.tv -> tmdbRepository.getShowWatchProviders(media.id)
            else -> null
        }
        val providerName = WatchAvailabilityMapper.map(providers?.body())
            .streamingProviders
            .firstOrNull()
            ?.providerName
        return providerName?.let {
            context.getString(R.string.available_on_provider_attributed, it)
        } ?: context.getString(R.string.available_on_streaming_attributed)
    }

    private fun Response<SearchResponse>?.mediaResults(): List<Media> =
        this?.takeIf { it.isSuccessful }?.body()?.results.orEmpty()

    private fun Media.normalizedMediaType(): Media? {
        val type = media_type ?: when {
            title != null -> MediaType.movie
            name != null -> MediaType.tv
            else -> null
        }
        return type?.takeIf { it == MediaType.movie || it == MediaType.tv }
            ?.let { copy(media_type = it) }
    }

    private fun Media.key() = MediaKey(
        media_type ?: if (title != null) MediaType.movie else MediaType.tv,
        id
    )

    private fun MovieResponse.toMedia(local: Media) = Media(
        id = id,
        media_type = MediaType.movie,
        name = null,
        poster_path = poster_path ?: local.poster_path,
        title = title ?: local.title,
        vote_average = vote_average ?: local.vote_average,
        backdrop_path = backdrop_path,
        overview = overview,
        release_date = release_date,
        first_air_date = null,
        fileId = local.fileId,
        modifiedTime = local.modifiedTime
    )

    private fun TvResponse.toMedia(local: Media) = Media(
        id = id,
        media_type = MediaType.tv,
        name = name ?: local.name,
        poster_path = poster_path ?: local.poster_path,
        title = null,
        vote_average = vote_average ?: local.vote_average,
        backdrop_path = backdrop_path,
        overview = overview,
        release_date = null,
        first_air_date = first_air_date,
        fileId = local.fileId,
        modifiedTime = local.modifiedTime
    )

    fun removeWatchedMedia(watched: WatchedDataModel) = viewModelScope.launch {
        when (watched) {
            is WatchedDataModel.Movie -> watchedRepository.deleteWatchedMovie(watched.movie.tmdbId)
            is WatchedDataModel.Show -> watchedRepository.deleteWatchedShow(watched.show.tmdbId)
        }
    }
}
