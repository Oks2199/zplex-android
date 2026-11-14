package zechs.zplex.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import zechs.zplex.data.local.api_cache.ApiCacheDao
import zechs.zplex.data.model.drive.DriveProfileAccess
import zechs.zplex.data.profile.ProfileSyncRepository
import zechs.zplex.data.profile.ProfileSyncStatus
import zechs.zplex.service.IndexingStateFlow
import zechs.zplex.service.ProfileSyncScheduler
import zechs.zplex.utils.SessionManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val indexingStateFlow: IndexingStateFlow,
    private val apiCacheDao: ApiCacheDao,
    profiles: ProfileSyncRepository,
    private val profileScheduler: ProfileSyncScheduler
) : ViewModel() {

    companion object {
        const val TAG = "SettingsViewModel"
    }

    fun saveMoviesFolder(id: String) = viewModelScope.launch {
        sessionManager.saveMovieFolder(id)
    }

    fun saveShowsFolder(id: String) = viewModelScope.launch {
        sessionManager.saveShowsFolder(id)
    }

    val hasMovieFolder = sessionManager.fetchMovieFolderFlow()
    val hasShowsFolder = sessionManager.fetchShowsFolderFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    fun logOut() = viewModelScope.launch(Dispatchers.IO) {
        _loading.value = true
        sessionManager.resetDataStore()

        _loading.value = false
    }

    fun resetCache() = viewModelScope.launch(Dispatchers.IO) {
        apiCacheDao.resetCache()
    }

    val isLoggedIn = sessionManager.isLoggedIn()
    val profileStatus = sessionManager.driveProfileAccess.combine(profiles.status) { access, status ->
        when (access) {
            DriveProfileAccess.SIGNED_OUT -> ProfileSyncStatus.SIGNED_OUT
            DriveProfileAccess.NEEDS_PERMISSION -> ProfileSyncStatus.NEEDS_PERMISSION
            DriveProfileAccess.GRANTED ->
                if (status == ProfileSyncStatus.SIGNED_OUT) ProfileSyncStatus.IDLE else status
        }
    }

    fun syncProfile() = profileScheduler.request()

    val indexingServiceStatus = indexingStateFlow.serviceState

    val cacheCount = apiCacheDao.getCacheCountLiveData()
}
