package kr.co.lion.modigm.ui.notification

import NotificationViewModel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
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
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class NotificationFragment : VBBaseFragment<FragmentNotificationBinding>(FragmentNotificationBinding::inflate) {

    private val viewModel: NotificationViewModel by viewModels()

    private lateinit var adapter: NotificationAdapter

    private val dataRefreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // 데이터 갱신 요청 수신 시 호출되는 메소드
            fetchAndDisplayNotifications()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        registerReceiver()
        fetchAndDisplayNotifications()
    }

    private fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun registerReceiver() {
        // 데이터 새로고침 브로드캐스트 리시버 등록
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(dataRefreshReceiver, IntentFilter("ACTION_REFRESH_DATA"))
    }
    private fun setupToolbar() {
        with(binding.toolBarNotification) {
            title = "알림"
            inflateMenu(R.menu.menu_notification_toolbar) // 메뉴 설정
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.notification_toolbar_refresh -> {
                        fetchAndDisplayNotifications() // 새로고침 실행
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setupRecyclerView() {
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
        observeNotifications()
        observeLoadingState()
    }

    private fun observeNotifications() {
        // Fragment에서 lifecycleScope을 사용하여 StateFlow 구독
        lifecycleScope.launchWhenStarted {
            viewModel.notifications.collect { notifications ->
                Log.d("NotificationFragment", "Notifications: $notifications")
                updateNotificationUI(notifications) // 알림 UI 업데이트
            }
        }
    }

    private fun observeLoadingState() {
        // 로딩 상태 관찰
        lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) showLoading() else hideLoading()
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        hideContent()
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        updateNotificationUI(viewModel.notifications.value)
    }

    private fun updateNotificationUI(notifications: List<NotificationData>) {
        if (notifications.isNotEmpty()) {
            displayNotifications(notifications)
        } else {
            displayEmptyState()
        }
    }

    private fun displayNotifications(notifications: List<NotificationData>) {
        binding.blankLayoutNotification.visibility = View.GONE
        binding.recyclerviewNotification.visibility = View.VISIBLE
        adapter.updateData(notifications)
    }

    private fun displayEmptyState() {
        binding.blankLayoutNotification.visibility = View.VISIBLE
        binding.recyclerviewNotification.visibility = View.GONE
        clearBadgeOnBottomNavigation()
    }

    private fun hideContent() {
        binding.recyclerviewNotification.visibility = View.GONE
        binding.blankLayoutNotification.visibility = View.GONE
    }

    private fun clearBadgeOnBottomNavigation() {
        // 모든 알림이 삭제된 경우, BottomNaviFragment에 배지를 숨기라는 브로드캐스트를 전송합니다.
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent("ACTION_HIDE_NOTIFICATION_BADGE"))
    }

    private fun deleteNotification(notification: NotificationData) {
        lifecycleScope.launch {
            val userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)
            if (viewModel.deleteNotification(notification)) {
                viewModel.refreshNotifications(userIdx)// RecyclerView 갱신
                updateBadgeState()// 모든 알림 상태 업데이트
            }
        }
    }

    private fun updateBadgeState() {
        if (viewModel.notifications.value.none { !it.isRead }) {
            clearBadgeOnBottomNavigation()
        }
    }

    private fun fetchAndDisplayNotifications(){
        // 현재 사용자 ID 가져오기
        val userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)
        Log.d("NotificationFragment", "Refreshing notifications for userIdx: $userIdx")
        // ViewModel을 통해 알림 데이터를 다시 가져옵니다.
        viewModel.fetchNotifications(userIdx)
    }

    override fun onResume() {
        super.onResume()
        handleScreenVisibilityChange()
    }

    override fun onPause() {
        super.onPause()
        // 알림 화면을 벗어날 때 모든 알림을 읽음으로 표시
        handleScreenVisibilityChange()
    }

    private fun handleScreenVisibilityChange() {
        viewModel.markAllNotificationsAsRead()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 리시버 해제
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(dataRefreshReceiver)
    }

    private fun markNotificationAsRead(notification: NotificationData) {
        lifecycleScope.launch {
            viewModel.markNotificationAsRead(notification.notificationIdx) // 서버에 읽음 상태 업데이트

            // **알림 읽음 처리 후 isRead 값을 true로 업데이트**
            notification.isRead = true
            adapter.updateData(viewModel.notifications.value)
            updateBadgeState()
        }
    }
}
