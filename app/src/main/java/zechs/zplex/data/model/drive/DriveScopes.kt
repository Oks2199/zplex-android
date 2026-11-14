package zechs.zplex.data.model.drive

const val DRIVE_READ_SCOPE = "https://www.googleapis.com/auth/drive.readonly"
const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"

fun TokenResponse.hasScope(scopeName: String): Boolean =
    scope.orEmpty().split(' ').contains(scopeName)

enum class DriveProfileAccess { SIGNED_OUT, NEEDS_PERMISSION, GRANTED }
