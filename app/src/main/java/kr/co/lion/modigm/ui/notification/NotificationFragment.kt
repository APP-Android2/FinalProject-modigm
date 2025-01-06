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
        initializeUI() // 뷰 초기화
        observeViewModel() // viewmodel 관찰

        val userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)
        viewModel.fetchNotifications(userIdx) // 알림 데이터 가져오기

        // 데이터 새로고침 브로드캐스트 리시버 등록
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(dataRefreshReceiver, IntentFilter("ACTION_REFRESH_DATA"))
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
        // Fragment에서 lifecycleScope을 사용하여 StateFlow 구독
        lifecycleScope.launchWhenStarted {
            viewModel.notifications.collect { notifications ->
                Log.d("NotificationFragment", "Notifications: $notifications")
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
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            // 로딩 중에는 알림 목록 및 빈 레이아웃 모두 숨김
            binding.recyclerviewNotification.visibility = View.GONE
            binding.blankLayoutNotification.visibility = View.GONE
        } else {
            // 로딩이 끝나면 알림 상태에 따라 레이아웃 가시성 조정 (isLoading이 끝난 후 조정)
            if (viewModel.notifications.value.isEmpty()) {
                binding.blankLayoutNotification.visibility = View.VISIBLE
            } else {
                binding.recyclerviewNotification.visibility = View.VISIBLE
            }
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
        adapter.onDestroy() // Adapter의 리시버 해제
    }

    private fun markNotificationAsRead(notification: NotificationData) {
        lifecycleScope.launch {
            viewModel.markNotificationAsRead(notification.notificationIdx) // 서버에 읽음 상태 업데이트

            // **알림 읽음 처리 후 isRead 값을 true로 업데이트**
            notification.isRead = true

            // **읽음 처리한 알림의 배지만 사라지게 함**
            adapter.updateData(viewModel.notifications.value)

            // **남은 읽지 않은 알림이 없다면 바텀 네비 배지 숨기기**
            checkAndUpdateBadge()
        }
    }

}
