package zechs.zplex.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class IndexingStateFlow {

    private val _serviceState = MutableStateFlow<ServiceState>(ServiceState.Checking)
    val serviceState = _serviceState.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.Default) + SupervisorJob()

    fun serviceStarted() {
        coroutineScope.launch {
            _serviceState.value = ServiceState.Running()
        }
    }

    fun serviceStopped() {
        coroutineScope.launch {
            _serviceState.value = ServiceState.Stopped()
        }
    }

}

sealed interface ServiceState {
    data object Checking : ServiceState
    data class Running(val lastRun: String = getTimestamp()) : ServiceState
    data class Stopped(val lastRun: String = getTimestamp()) : ServiceState
}

private fun getTimestamp() = formatDate(Calendar.getInstance().timeInMillis)

private fun formatDate(timeInMillis: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeInMillis
    val dateFormat = SimpleDateFormat("d MMM yyyy 'à' HH:mm", Locale.FRENCH)
    return dateFormat.format(calendar.time)
}
