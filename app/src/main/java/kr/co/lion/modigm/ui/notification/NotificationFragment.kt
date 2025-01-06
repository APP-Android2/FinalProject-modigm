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
            refreshNotifications()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        registerReceiver()
        loadData()
    }

    private fun setupUI() {
        initializeUI()
        observeViewModel()
    }

    private fun registerReceiver() {
        // 데이터 새로고침 브로드캐스트 리시버 등록
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(dataRefreshReceiver, IntentFilter("ACTION_REFRESH_DATA"))
    }

    private fun loadData() {
        val userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)
        viewModel.fetchNotifications(userIdx) // 알림 데이터 가져오기
    }

    private fun initializeUI() {
        setupToolbar()
        setupRecyclerView()
    }
    private fun setupToolbar() {
        with(binding.toolBarNotification) {
            title = "알림"
            inflateMenu(R.menu.menu_notification_toolbar) // 메뉴 설정
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.notification_toolbar_refresh -> {
                        refreshNotifications() // 새로고침 실행
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
                showLoading(isLoading)// 로딩 상태 업데이트
            }
        }
    }

    private fun updateNotificationUI(notifications: List<NotificationData>) {
        if (notifications.isNotEmpty()) {
            binding.blankLayoutNotification.visibility = View.GONE
            binding.recyclerviewNotification.visibility = View.VISIBLE
            adapter.updateData(notifications)
        } else {
            binding.blankLayoutNotification.visibility = View.VISIBLE
            binding.recyclerviewNotification.visibility = View.GONE
            clearBadgeOnBottomNavigation()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            binding.recyclerviewNotification.visibility = View.GONE
            binding.blankLayoutNotification.visibility = View.GONE
        } else {
            updateNotificationUI(viewModel.notifications.value)
        }
    }

    private fun clearBadgeOnBottomNavigation() {
        // 모든 알림이 삭제된 경우, BottomNaviFragment에 배지를 숨기라는 브로드캐스트를 전송합니다.
        val intent = Intent("ACTION_HIDE_NOTIFICATION_BADGE")
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    private fun deleteNotification(notification: NotificationData) {
        val userIdx = prefs.getInt("currentUserIdx", 0)

        lifecycleScope.launch {
            val result = viewModel.deleteNotification(notification) // MySQL에서 삭제
            if (result) {
                viewModel.refreshNotifications(userIdx) // RecyclerView 갱신
                checkAndUpdateBadge() // 모든 알림 삭제 후 상태 업데이트
            }
        }
    }

    // **배지 상태를 확인하는 메서드**
    private fun checkAndUpdateBadge() {
        // 남은 읽지 않은 알림이 있는지 확인
        val hasUnreadNotifications = viewModel.notifications.value.any { !it.isRead }

        // **읽지 않은 알림이 없으면 바텀 네비 배지를 숨김**
        if (!hasUnreadNotifications) {
            clearBadgeOnBottomNavigation()
        }
    }

    private fun refreshNotifications() {
        // 현재 사용자 ID 가져오기
        val userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)
        Log.d("NotificationFragment", "Refreshing notifications for userIdx: $userIdx")
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
    }

    private fun markNotificationAsRead(notification: NotificationData) {
        lifecycleScope.launch {
            viewModel.markNotificationAsRead(notification.notificationIdx) // 서버에 읽음 상태 업데이트

            // **알림 읽음 처리 후 isRead 값을 true로 업데이트**
            notification.isRead = true
            adapter.updateData(viewModel.notifications.value)
            checkAndUpdateBadge()
        }
    }
}
