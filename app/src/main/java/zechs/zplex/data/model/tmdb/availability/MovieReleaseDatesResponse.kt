package zechs.zplex.data.model.tmdb.availability

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class MovieReleaseDatesResponse(
    val id: Int,
    val results: List<CountryReleaseDates> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class CountryReleaseDates(
    @Json(name = "iso_3166_1")
    val countryCode: String,
    @Json(name = "release_dates")
    val releaseDates: List<MovieReleaseDate> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class MovieReleaseDate(
    val certification: String? = null,
    @Json(name = "iso_639_1")
    val languageCode: String? = null,
    @Json(name = "release_date")
    val releaseDate: String,
    val type: Int
)
