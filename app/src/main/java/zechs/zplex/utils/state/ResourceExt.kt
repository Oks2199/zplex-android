package zechs.zplex.utils.state

import android.content.Context
import zechs.zplex.R
import java.io.IOException

class ResourceExt {

    companion object {

        fun <T> postError(context: Context, exception: Exception): Resource<T> {
            return Resource.Error(
                message = if (exception is IOException) {
                    context.getString(R.string.network_failure)
                } else exception.message ?: context.getString(R.string.something_went_wrong),
                data = null
            )
        }

        fun <T> postError(context: Context, throwable: Throwable): Resource<T> {
            return Resource.Error(
                message = if (throwable is IOException) {
                    context.getString(R.string.network_failure)
                } else throwable.message ?: context.getString(R.string.something_went_wrong),
                data = null
            )
        }

    }

}
