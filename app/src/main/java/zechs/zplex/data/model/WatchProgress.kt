package zechs.zplex.data.model

internal object WatchProgress {
    fun percentage(position: Long, duration: Long): Int {
        if (position <= 0 || duration <= 0) return 0
        return ((position.toDouble() / duration).coerceAtMost(1.0) * 100).toInt()
    }

    fun isFinished(position: Long, duration: Long): Boolean =
        duration > 0 && position.toDouble() / duration > 0.90
}
