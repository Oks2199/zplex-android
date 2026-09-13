package zechs.zplex.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import zechs.zplex.data.local.api_cache.ApiCacheDao
import zechs.zplex.service.IndexingStateFlow
import zechs.zplex.utils.SessionManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val indexingStateFlow: IndexingStateFlow,
    private val apiCacheDao: ApiCacheDao
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
    val indexingServiceStatus = indexingStateFlow.serviceState

    val cacheCount = apiCacheDao.getCacheCountLiveData()
}
