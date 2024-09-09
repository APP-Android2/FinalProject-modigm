package kr.co.lion.modigm.ui.notification

import NotificationViewModel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
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
                }
            }
        }
    }

    private fun deleteNotification(notification: NotificationData) {
        val userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)

        CoroutineScope(Dispatchers.IO).launch {
            val result = viewModel.deleteNotification(notification) // MySQL에서 삭제

            if (result) {
                viewModel.refreshNotifications(userIdx) // RecyclerView 갱신
            }
        }
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
        val userIdx = ModigmApplication.prefs.getInt("currentUserIdx", 0)

        CoroutineScope(Dispatchers.IO).launch {
            val result = viewModel.markNotificationAsRead(notification.notificationIdx) // 서버에 읽음 상태 업데이트

            if (result) {
                viewModel.refreshNotifications(userIdx) // RecyclerView 갱신
            }
        }
    }

}
