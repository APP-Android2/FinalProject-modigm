package kr.co.lion.modigm.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChatBinding
import kr.co.lion.modigm.databinding.FragmentChatGroupBinding
import kr.co.lion.modigm.model.ChatRoomData
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.chat.dao.ChatRoomDao
import kr.co.lion.modigm.ui.chat.vm.ChatViewModel
import kr.co.lion.modigm.util.FragmentName

class ChatFragment : Fragment() {

    lateinit var fragmentChatBinding: FragmentChatBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentChatBinding = FragmentChatBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        // 옵션 메뉴가 있다는 것을 시스템에 알림
        setHasOptionsMenu(true)

        return fragmentChatBinding.root
    }

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 하단 바 체크 설정(채팅에 체크) 및 하단 바 이동 설정
        /*
        settingBottomTabs()
        bottomSheetSetting()
        */

        // 채팅 - (ViewPager) 세팅
        viewPagerActiviation()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_chat_toolbar, menu)

        // 현재 이게 실행이 안돼서 search 메뉴 개발 뒤로 미룸
        Log.d("test1234", "onCreateOptionsMenu 실행 됨")
    }

    // 하단 바 홈으로 체크 표시 설정
    /*
    fun settingBottomTabs() {
        fragmentChatBinding.apply {
            val menuItemId = R.id.bottomNaviChat
            fragmentChatBinding.mainBottomNavi.menu.findItem(menuItemId)?.isChecked = true
        }
    }

    // 하단 바 클릭 설정
    fun bottomSheetSetting() {
        fragmentChatBinding.apply {
            mainBottomNavi.setOnItemSelectedListener { item ->
                when(item.itemId) {
                    R.id.bottomNaviStudy -> {
                        mainActivity.replaceFragment(FragmentName.STUDY, false, false, null)
                    }
                    R.id.bottomNaviHeart -> {
                        mainActivity.replaceFragment(FragmentName.LIKE, false, false, null)
                    }
                    R.id.bottomNaviChat -> {
                        mainActivity.replaceFragment(FragmentName.CHAT, false, false, null)
                    }
                    else -> {
                        mainActivity.replaceFragment(FragmentName.PROFILE, false, false, null)
                    }
                }
                true
            }
        }
    }
    */

    // ViewPager 설정
    private fun viewPagerActiviation(){
        fragmentChatBinding.apply {
            // 1. 페이지 데이터를 로드
            val list = listOf(ChatGroupFragment(), ChatOnetoOneFragment())
            // 2. Adapter 생성
            val pagerAdapter = FragmentPagerAdapter(list, mainActivity)
            // 3. Adapater와 Pager연결
            viewPagerChat.adapter = pagerAdapter
            // 4. 탭 메뉴의 갯수만큼 제목을 목록으로 생성
            val titles = listOf("참여 중", "1:1")
            // 5. 탭 레이아웃과 뷰페이저 연결
            TabLayoutMediator(tabsChat, viewPagerChat) { tab, position ->
                tab.text = titles.get(position)
            }.attach()

            // ViewPager 드래그 비활성화
            viewPagerChat.isUserInputEnabled = false
        }
    }

    private inner class FragmentPagerAdapter(val fragmentList: List<Fragment>, fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }

    // 채팅 방 데이터 추가 (예시)
    fun addChatRoomData() {
        CoroutineScope(Dispatchers.Main).launch {

            val chatRoomSequence = ChatRoomDao.getChatRoomSequence()
            ChatRoomDao.updateChatRoomSequence(chatRoomSequence + 1)

            val chatIdx = chatRoomSequence + 1
            val chatTitle = "제 13회 해커톤 준비"
            val chatRoomImage = ""
            val chatMemberList = listOf("currentUser", "sonUser", "iuUser", "ryuUser")
            val participantCount = 4
            val groupChat = true
            val lastChatMessage = "마지막 메세지"
            val lastChatFullTime = 0L
            val lastChatTime = "00:00"

            val chatRoomData = ChatRoomData(chatIdx, chatTitle, chatRoomImage, chatMemberList, participantCount, groupChat, lastChatMessage, lastChatFullTime, lastChatTime)

            // 채팅 방 생성
            ChatRoomDao.insertChatRoomData(chatRoomData)
            Log.d("test1234", "${chatTitle} 채팅방 생성 완료")
        }
    }
}