package zechs.zplex.data.model.tmdb.availability

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class MovieWatchProvidersResponse(
    val id: Int,
    val results: Map<String, CountryWatchProviders> = emptyMap()
)

@Keep
@JsonClass(generateAdapter = true)
data class CountryWatchProviders(
    val link: String? = null,
    val flatrate: List<WatchProvider> = emptyList(),
    val free: List<WatchProvider> = emptyList(),
    val ads: List<WatchProvider> = emptyList(),
    val rent: List<WatchProvider> = emptyList(),
    val buy: List<WatchProvider> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class WatchProvider(
    @Json(name = "provider_id")
    val providerId: Int,
    @Json(name = "provider_name")
    val providerName: String,
    @Json(name = "logo_path")
    val logoPath: String? = null,
    @Json(name = "display_priority")
    val displayPriority: Int = Int.MAX_VALUE
)
