import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.NotificationData
import kr.co.lion.modigm.repository.NotificationRepository

class NotificationViewModel : ViewModel() {
    private val repository = NotificationRepository()

    // StateFlow로 알림 데이터 관리
    private val _notifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val notifications: StateFlow<List<NotificationData>> get() = _notifications

    // 데이터를 가져오는 함수
    fun fetchNotifications(userIdx: Int) {
        viewModelScope.launch {
            val data = repository.getNotifications(userIdx) // 데이터베이스에서 데이터를 가져오는 메서드
            _notifications.value = data
        }
    }

    // 데이터를 갱신하는 메서드
    fun refreshNotifications(userIdx: Int) {
        fetchNotifications(userIdx)
    }

    suspend fun deleteNotification(notification: NotificationData): Boolean {
        return repository.deleteNotification(notification)
    }
}
