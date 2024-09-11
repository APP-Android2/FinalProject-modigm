package kr.co.lion.modigm.ui.notification

import NotificationViewModel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentNotificationBinding
import kr.co.lion.modigm.model.NotificationData
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.notification.adapter.NotificationAdapter
import kr.co.lion.modigm.util.ModigmApplication

class NotificationFragment : VBBaseFragment<FragmentNotificationBinding>(FragmentNotificationBinding::inflate) {

    private val viewModel: NotificationViewModel by viewModels()

    private lateinit var adapter: NotificationAdapter

    private val dataRefreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // 데이터 갱신 요청 수신 시 호출되는 메소드
            refreshData()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeViewModel()

        val userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)
        viewModel.fetchNotifications(userIdx) // 알림 데이터 가져오기

        // 데이터 새로고침 브로드캐스트 리시버 등록
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(dataRefreshReceiver, IntentFilter("ACTION_REFRESH_DATA"))
    }

    private fun initView() {
        settingToolbar()
        binding.recyclerviewNotification.layoutManager = LinearLayoutManager(requireContext())
        // NotificationAdapter 생성 시 onDeleteClick 람다 전달
        adapter = NotificationAdapter(
            mutableListOf(),
            { notification -> deleteNotification(notification) }, // X 아이콘 클릭 시 호출될 메서드
            { notification -> markNotificationAsRead(notification) } // 읽음 상태로 표시하는 메서드
        )
        binding.recyclerviewNotification.adapter = adapter
    }

    private fun observeViewModel() {
        // Fragment에서 lifecycleScope을 사용하여 StateFlow 구독
        lifecycleScope.launchWhenStarted {
            viewModel.notifications.collect { notifications ->
                if (notifications.isNotEmpty()) {
                    binding.blankLayoutNotification.visibility = View.GONE
                    binding.recyclerviewNotification.visibility = View.VISIBLE
                    adapter.updateData(notifications) // RecyclerView 어댑터 데이터 업데이트
                } else {
                    binding.blankLayoutNotification.visibility = View.VISIBLE
                    binding.recyclerviewNotification.visibility = View.GONE
                    clearBadgeOnBottomNavigation() // 알림이 없을 때 배지를 지움
                }
            }
        }

        // 로딩 상태 관찰
        lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collect { isLoading ->
                showLoading(isLoading)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        // 로딩 중에는 프로그래스 바만 표시하고, 다른 모든 뷰는 숨김
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.recyclerviewNotification.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.blankLayoutNotification.visibility = if (isLoading) View.GONE else View.VISIBLE // 로딩 중에는 빈 레이아웃 숨김
    }

    private fun clearBadgeOnBottomNavigation() {
        // 모든 알림이 삭제된 경우, BottomNaviFragment에 배지를 숨기라는 브로드캐스트를 전송합니다.
        val intent = Intent("ACTION_HIDE_NOTIFICATION_BADGE")
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    private fun deleteNotification(notification: NotificationData) {
        val userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)

        lifecycleScope.launch {
            val result = viewModel.deleteNotification(notification) // MySQL에서 삭제
            if (result) {
                viewModel.refreshNotifications(userIdx) // RecyclerView 갱신
                checkAndUpdateAllReadStatus() // 모든 알림 삭제 후 상태 업데이트
            }
        }
    }

    private fun checkAndUpdateAllReadStatus() {
        val userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)

        lifecycleScope.launch {
            val notifications = viewModel.getNotifications(userIdx)
            if (notifications.isEmpty()) {
                setAllNotificationsRead()
                clearBadgeOnBottomNavigation()
            }
        }
    }

    private fun setAllNotificationsRead() {
        // 모든 알림을 읽음 상태로 변경하는 로직 추가
        val userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)
        viewModel.markAllNotificationsAsRead(userIdx)
    }

    private fun settingToolbar() {
        with(binding) {
            toolBarNotification.title = "알림"
            toolBarNotification.inflateMenu(R.menu.menu_notification_toolbar) // 메뉴 설정
            toolBarNotification.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.notification_toolbar_refresh -> {
                        refreshData() // 새로고침 실행
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun refreshData() {
        // 현재 사용자 ID 가져오기
        val userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)
        // ViewModel을 통해 알림 데이터를 다시 가져옵니다.
        viewModel.fetchNotifications(userIdx)
    }

    override fun onResume() {
        super.onResume()
        // 화면으로 돌아올 때 모든 알림을 읽음으로 표시
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent("ACTION_MARK_ALL_READ"))
    }

    override fun onPause() {
        super.onPause()
        // 알림 화면을 벗어날 때 모든 알림을 읽음으로 표시
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent("ACTION_MARK_ALL_READ"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 리시버 해제
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(dataRefreshReceiver)
        adapter.onDestroy() // Adapter의 리시버 해제
    }

    private fun markNotificationAsRead(notification: NotificationData) {
        lifecycleScope.launch {
            viewModel.markNotificationAsRead(notification.notificationIdx) // 서버에 읽음 상태 업데이트
        }
    }

}
