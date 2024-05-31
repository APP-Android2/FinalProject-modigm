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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChatBinding
import kr.co.lion.modigm.model.ChatRoomData
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.chat.adapter.ChatRoomAdapter
import kr.co.lion.modigm.ui.chat.adapter.ChatSearchResultsAdapter
import kr.co.lion.modigm.ui.chat.dao.ChatRoomDao
import kr.co.lion.modigm.util.hideSoftInput

class ChatFragment : Fragment() {

    lateinit var fragmentChatBinding: FragmentChatBinding
    lateinit var mainActivity: MainActivity
    private lateinit var chatSearchResultsAdapter: ChatSearchResultsAdapter

    // 내가 속하며 검색 필터에 맞는 그룹 채팅 방들을 담고 있을 리스트
    var chatSearchRoomDataList = mutableListOf<ChatRoomData>()

    private val loginUserId = "currentUser" // 현재 사용자의 ID를 설정 (DB 연동 후 교체)
//    private val loginUserId = "swUser" // 현재 사용자의 ID를 설정 (DB 연동 후 교체)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentChatBinding = FragmentChatBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

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

        // 툴바 관련 세팅
        setupToolbar()

        // 채팅 - (ViewPager) 세팅
        viewPagerActiviation()
        
        // 리사이클러 뷰 세팅
        setupRecyclerView()

        // 실시간 채팅 방 데이터 업데이트
        getAndUpdateLiveChatRooms()
    }

    // 툴바 관련 기본 세팅
    private fun setupToolbar(){
        // Fragment 내에서 Toolbar 설정
        val toolbar = fragmentChatBinding.toolbarChat
        mainActivity.setSupportActionBar(toolbar)

        // 기본 제목을 제거
        mainActivity.supportActionBar?.setDisplayShowTitleEnabled(false)

        // 옵션 메뉴가 있다는 것을 시스템에 알림
        setHasOptionsMenu(true)
    }

    // 리사이클러 뷰 세팅
    private fun setupRecyclerView() {
        fragmentChatBinding.recyclerViewChatSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            chatSearchResultsAdapter = ChatSearchResultsAdapter(chatSearchRoomDataList, { roomItem ->
                Log.d("test1234", "${roomItem.chatIdx}번 ${roomItem.chatTitle}에 입장")
            }, mainActivity, loginUserId)
            adapter = chatSearchResultsAdapter
        }
    }

    // 툴바의 메뉴 세팅(검색)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_chat_toolbar, menu)

        val searchItem = menu.findItem(R.id.chat_toolbar_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = "검색어를 입력하세요"

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    // 검색어 제출 시의 로직 처리 - 이거 사용 X로 생각중
                    Log.d("test1234", "검색어 제출: $query")

                    // 검색 버튼 누르면 키보드 내리기
                    activity?.hideSoftInput()
                }
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                // 검색어 변경 시의 로직 처리 - 이거로 처리 하려고 함
                Log.d("test1234", "검색어 변경: $query")
                performSearch(query!!)
                return true
            }
        })
    }

    private fun performSearch(query: String) {
        // 검색 로직 처리 (필요에 따라 구현)
        // 예를 들어, 검색 결과를 가져와 RecyclerView에 표시
        if (query.isNullOrEmpty()){
            fragmentChatBinding.viewPagerContainer.visibility = View.VISIBLE
            fragmentChatBinding.recyclerViewChatSearchResults.visibility = View.GONE
        } else {
            fragmentChatBinding.viewPagerContainer.visibility = View.GONE
            fragmentChatBinding.recyclerViewChatSearchResults.visibility = View.VISIBLE
            chatSearchResultsAdapter.filter(query)
        }
    }

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

    // 실시간 채팅 방 데이터 업데이트
    private fun getAndUpdateLiveChatRooms() {
        // 내가 속한 그룹 채팅 방(RecyclerView)를 실시간으로 업데이트
        ChatRoomDao.updateChatAllRoomsListener(loginUserId) { updatedChatRooms ->
            chatSearchRoomDataList.clear()
            chatSearchRoomDataList.addAll(updatedChatRooms)

            // RecyclerView 갱신
            activity?.runOnUiThread {
                fragmentChatBinding.recyclerViewChatSearchResults.adapter?.notifyDataSetChanged()
            }
            Log.d("test1234", "실시간 Update - SearchList")
        }
    }

    // 채팅 방 데이터 추가 (예시)
    fun addChatRoomData() {
        CoroutineScope(Dispatchers.Main).launch {

            val chatRoomSequence = ChatRoomDao.getChatRoomSequence()
            ChatRoomDao.updateChatRoomSequence(chatRoomSequence + 1)

            val chatIdx = chatRoomSequence + 1
            val chatTitle = ""
            val chatRoomImage = ""
            val chatMemberList = listOf("currentUser", "swUser", "hwUser", "msUser", "shUser", "tjUser", "ryuUser", "sonUser", "iuUser")
            val participantCount = 4
            val groupChat = true
            val lastChatMessage = ""
            val lastChatFullTime = 0L
            val lastChatTime = ""

            val chatRoomData = ChatRoomData(chatIdx, chatTitle, chatRoomImage, chatMemberList, participantCount, groupChat, lastChatMessage, lastChatFullTime, lastChatTime)

            // 채팅 방 생성
            ChatRoomDao.insertChatRoomData(chatRoomData)
            Log.d("test1234", "${chatTitle} 채팅방 생성 완료")
        }
    }
}