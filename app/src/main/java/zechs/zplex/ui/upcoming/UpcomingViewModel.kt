package zechs.zplex.ui.upcoming

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import zechs.zplex.R
import zechs.zplex.data.model.tmdb.search.SearchResponse
import zechs.zplex.data.repository.TmdbRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.utils.state.Resource
import zechs.zplex.utils.state.ResourceExt.Companion.postError
import zechs.zplex.utils.util.Converter
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UpcomingViewModel @Inject constructor(
    app: Application,
    private val tmdbRepository: TmdbRepository
) : BaseAndroidViewModel(app) {

    private val _upcoming = MutableLiveData<Resource<SearchResponse>>()
    val upcoming: LiveData<Resource<SearchResponse>>
        get() = _upcoming

    private var upcomingResponse: SearchResponse? = null
    private var page = 1
    private val requests = Mutex()

    init {
        getUpcoming()
    }

    fun getUpcoming() = viewModelScope.launch(Dispatchers.IO) {
        requests.withLock {
            if (upcomingResponse?.let { it.page >= it.total_pages } == true) return@withLock
            _upcoming.postValue(Resource.Loading())
            try {
                if (hasInternetConnection()) {
                    var result = handleBrowseResponse(tmdbRepository.getUpcoming(page))
                    for (attempt in 1..5) {
                        val data = (result as? Resource.Success)?.data
                        if (data == null || data.results.size >= 5 || data.page >= data.total_pages) {
                            break
                        }
                        result = handleBrowseResponse(tmdbRepository.getUpcoming(page))
                    }
                    _upcoming.postValue(result)
                } else {
                    _upcoming.postValue(Resource.Error(context.getString(R.string.no_internet_connection)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _upcoming.postValue(postError(context, e))
            }
        }
    }

    private fun handleBrowseResponse(
        response: Response<SearchResponse>
    ): Resource<SearchResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                val today = LocalDate.now()
                val responseList = resultResponse.copy(
                    results = (upcomingResponse?.results.orEmpty() + resultResponse.results)
                        .asSequence()
                        .mapNotNull { item ->
                            val date = item.release_date?.takeIf(String::isNotBlank)
                                ?: item.first_air_date
                            Converter.parseIsoDateOrNull(date)?.let { item to it }
                        }
                        .filter { (_, date) -> date.isAfter(today) }
                        .sortedBy { (_, date) -> date }
                        .distinctBy { (item, _) -> item.id }
                        .map { (item, _) -> item }
                        .toMutableList()
                )
                page = resultResponse.page + 1
                upcomingResponse = responseList
                return Resource.Success(responseList)
            }
        }
        return Resource.Error(response.message())
    }


}
