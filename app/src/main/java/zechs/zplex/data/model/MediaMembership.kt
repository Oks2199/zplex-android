package zechs.zplex.data.model

enum class MediaMembership {
    NONE,
    WATCHLIST,
    LIBRARY
}

object MediaMembershipResolver {

    fun resolve(
        isSaved: Boolean,
        fileId: String?,
        isOffline: Boolean = false
    ): MediaMembership = when {
        isOffline || !fileId.isNullOrBlank() -> MediaMembership.LIBRARY
        isSaved -> MediaMembership.WATCHLIST
        else -> MediaMembership.NONE
    }

    fun isMissingFromRemote(fileId: String?, remoteIds: Set<String>): Boolean =
        !fileId.isNullOrBlank() && fileId !in remoteIds
}
