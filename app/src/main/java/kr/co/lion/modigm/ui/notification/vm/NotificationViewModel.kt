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

    // 로딩 상태를 관리하는 StateFlow
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 데이터를 가져오는 함수
    fun fetchNotifications(userIdx: Int) {
        viewModelScope.launch {
            _isLoading.value = true // 로딩 시작
            try {
                val data = repository.getNotifications(userIdx) // 데이터베이스에서 데이터를 가져오는 메서드
                _notifications.value = data
            } catch (e: Exception) {
                // 에러 처리 로직
            } finally {
                _isLoading.value = false // 로딩 종료
            }
        }
    }

    // 데이터를 갱신하는 메서드
    fun refreshNotifications(userIdx: Int) {
        fetchNotifications(userIdx)
    }

    // 알림 데이터를 반환하는 함수
    suspend fun getNotifications(userIdx: Int): List<NotificationData> {
        return repository.getNotifications(userIdx) // 데이터베이스에서 데이터를 가져오는 메서드
    }

    suspend fun deleteNotification(notification: NotificationData): Boolean {
        return repository.deleteNotification(notification)
    }

    // 특정 알림을 읽음으로 표시하는 메서드
    suspend fun markNotificationAsRead(notificationIdx: Int): Boolean {
        return repository.markNotificationAsRead(notificationIdx)
    }

    // 모든 알림을 읽음으로 표시하는 메서드
    fun markAllNotificationsAsRead(userIdx: Int) {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(userIdx)
            refreshNotifications(userIdx) // 모든 알림 상태를 읽음으로 변경 후 데이터 갱신
        }
    }
}
