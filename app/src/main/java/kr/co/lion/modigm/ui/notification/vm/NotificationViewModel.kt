import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.model.NotificationData
import kr.co.lion.modigm.repository.NotificationRepository
import kr.co.lion.modigm.util.ModigmApplication

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
                // 만약 데이터가 비어있지 않으면 업데이트
                if (data.isNotEmpty()) {
                    _notifications.value = data
                } else {
                    // 알림이 없는 경우 처리
                    _notifications.value = emptyList()
                }
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
        return repository.deleteNotification(notification).also { success ->
            if (success) {
                // 성공적으로 삭제된 경우 알림 목록에서 해당 알림 제거
                _notifications.value = _notifications.value.filter { it.notificationIdx != notification.notificationIdx }
            }
        }
    }

    // 특정 알림을 읽음으로 표시하는 메서드
    fun markNotificationAsRead(notificationIdx: Int) {
        viewModelScope.launch {
            val result = repository.markNotificationAsRead(notificationIdx)
            if (result) {
                // 읽음 상태로 변경해도 알림 목록에서 제거하지 않고 그대로 유지
                _notifications.value = _notifications.value.map { notification ->
                    if (notification.notificationIdx == notificationIdx) {
                        notification.copy(isNew = false)
                    } else {
                        notification
                    }
                }
            }
        }
    }


    // 모든 알림을 읽음으로 표시하는 메서드
    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            val userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)
            repository.markAllNotificationsAsRead(userIdx)
            refreshNotifications(userIdx) // 모든 알림 상태를 읽음으로 변경 후 데이터 갱신

            // 모든 알림을 읽음 처리한 후, 브로드캐스트 전송
            sendMarkAllReadBroadcast()
        }
    }

    // 브로드캐스트 전송을 ViewModel에서 처리하도록 함수 추가
    private fun sendMarkAllReadBroadcast() {
        LocalBroadcastManager.getInstance(ModigmApplication.instance)
            .sendBroadcast(Intent("ACTION_MARK_ALL_READ"))
    }

    // 서버에서 FCM 토큰을 삭제하는 메서드
    fun removeFcmTokenFromServer(userIdx: Int) {
        viewModelScope.launch {
            try {
                val result = repository.removeFcmToken(userIdx)
                if (result) {
                    // 성공적으로 삭제되었을 때 추가 로직 처리 가능
                    Log.d("NotificationViewModel", "FCM token successfully removed from server")
                } else {
                    Log.e("NotificationViewModel", "Failed to remove FCM token from server")
                }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error removing FCM token from server", e)
            }
        }
    }
}
