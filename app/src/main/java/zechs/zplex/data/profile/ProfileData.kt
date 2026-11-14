package zechs.zplex.data.profile

import androidx.annotation.Keep

@Keep
internal enum class ProfileKind { MOVIE, SHOW, MOVIE_PROGRESS, EPISODE_PROGRESS }

@Keep
internal data class ProfileKey(
    val kind: ProfileKind,
    val tmdbId: Int,
    val season: Int = 0,
    val episode: Int = 0
)

@Keep
internal data class ProfileValue(
    val key: ProfileKey,
    val title: String,
    val poster: String? = null,
    val rating: Double? = null,
    val position: Long = 0,
    val duration: Long = 0,
    val watchedAt: Long = 0,
    val driveFileId: String? = null
)

@Keep
internal data class ProfileChange(
    val key: ProfileKey,
    val revision: Long,
    val deviceId: String,
    val value: ProfileValue?
)

@Keep
internal data class ProfileDocument(
    val profileId: String,
    val deviceId: String,
    val changes: List<ProfileChange>,
    val version: Int = 1
)

@Keep
internal data class ProfileState(
    val accountId: String,
    val deviceId: String,
    val profileId: String,
    val fileId: String? = null,
    val published: Boolean = false,
    val baseline: List<ProfileValue> = emptyList(),
    val own: List<ProfileChange> = emptyList(),
    val applied: List<ProfileChange> = emptyList()
) {
    fun record(current: List<ProfileValue>): ProfileState {
        val previous = baseline.associateBy { it.key }
        val currentByKey = current.associateBy { it.key }
        val changed = (previous.keys + currentByKey.keys).filterTo(hashSetOf()) {
            previous[it] != currentByKey[it]
        }
        if (changed.isEmpty()) return this

        val revision = maxOf(own.maxOfOrNull { it.revision } ?: 0, applied.maxOfOrNull { it.revision } ?: 0) + 1
        val changes = changed.map { ProfileChange(it, revision, deviceId, currentByKey[it]) }
        return copy(baseline = current, own = own.filterNot { it.key in changed } + changes)
    }
}

internal fun mergeProfileChanges(changes: Iterable<ProfileChange>): List<ProfileChange> {
    val order = compareBy<ProfileChange>({ it.revision }, { it.deviceId })
    val winners = mutableMapOf<ProfileKey, ProfileChange>()
    changes.forEach { change ->
        val previous = winners[change.key]
        if (previous == null || order.compare(change, previous) > 0) winners[change.key] = change
    }
    return winners.values.toList()
}
