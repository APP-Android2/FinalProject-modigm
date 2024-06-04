package kr.co.lion.modigm.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
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
import kr.co.lion.modigm.ui.chat.adapter.ChatSearchResultsAdapter
import kr.co.lion.modigm.db.chat.ChatRoomDataSource
import kr.co.lion.modigm.ui.chat.vm.ChatRoomViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.hideSoftInput

class ChatFragment : Fragment() {

    lateinit var fragmentChatBinding: FragmentChatBinding
    lateinit var mainActivity: MainActivity
    
    // 어댑터
    private lateinit var chatSearchResultsAdapter: ChatSearchResultsAdapter

    // 뷰 모델
    private val chatRoomViewModel: ChatRoomViewModel by viewModels()

    // 내가 속하며 검색 필터에 맞는 그룹 채팅 방들을 담고 있을 리스트
    var chatSearchRoomDataList = mutableListOf<ChatRoomData>()

    // 현재 로그인 한 사용자 정보
    private val loginUserId = "currentUser" // 현재 사용자의 ID를 설정 (DB 연동 후 교체)
    // private val loginUserId = "swUser" // 현재 사용자의 ID를 설정 (DB 연동 후 교체)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentChatBinding = FragmentChatBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        return fragmentChatBinding.root
    }

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.v("chatLog1", "BackStackEntryCount: ${parentFragmentManager.backStackEntryCount}")

        // 툴바 관련 세팅
        setupToolbar()

        // 채팅 - (ViewPager) 세팅
        viewPagerActiviation()
        
        // 리사이클러 뷰 세팅
        setupRecyclerView()

        // 실시간 채팅 방 데이터 업데이트
        getAndUpdateLiveChatRooms()

        // 데이터 변경 관찰
        observeData()
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
                    // 검색 완료 시의 로직 처리 - 이거 사용 X로 생각중
                    Log.d("chatLog1", "검색어 완료: $query")

                    // 검색 버튼 누르면 키보드 내리기
                    activity?.hideSoftInput()
                }
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                // 검색어 변경 시의 로직 처리 - 이거로 처리 하려고 함
                Log.d("chatLog1", "검색어 변경: $query")
                searchResult(query!!)
                return true
            }
        })
    }

    // 검색 로직 처리
    private fun searchResult(query: String) {
        // 검색 결과를 가져와 RecyclerView에 표시
        if (query.isNullOrEmpty()){
            with(fragmentChatBinding){
                viewPagerContainer.visibility = View.VISIBLE
                recyclerViewChatSearchResults.visibility = View.GONE
            }
        }
        else {
            with(fragmentChatBinding){
                viewPagerContainer.visibility = View.GONE
                recyclerViewChatSearchResults.visibility = View.VISIBLE
            }
            chatSearchResultsAdapter.filter(query)
        }
    }

    // ViewPager 설정
    private fun viewPagerActiviation(){
        with(fragmentChatBinding){
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

    // 데이터 변경 관찰
    private fun observeData() {
        // 데이터 변경 관찰
        chatRoomViewModel.allChatRoomsList.observe(viewLifecycleOwner) { updatedChatRooms ->
            chatSearchRoomDataList.clear()
            chatSearchRoomDataList.addAll(updatedChatRooms)
            chatSearchResultsAdapter.notifyDataSetChanged()
        }
    }

    // 채팅 방 데이터 실시간 수신
    private fun getAndUpdateLiveChatRooms() {
        chatRoomViewModel.getAllChatRooms(loginUserId)
    }

    // 리사이클러 뷰 세팅
    private fun setupRecyclerView() {
        with(fragmentChatBinding) {
            with(recyclerViewChatSearchResults){
                layoutManager = LinearLayoutManager(requireContext())
                // onItemClick 시
                chatSearchResultsAdapter = ChatSearchResultsAdapter(chatSearchRoomDataList, { roomItem ->
                    Log.d("chatLog1", "${loginUserId}가 ${roomItem.chatIdx}번 ${roomItem.chatTitle}에 입장")

                    // ChatRoomFragment로 이동
                    val chatRoomFragment = ChatRoomFragment().apply {
                        arguments = Bundle().apply {
                            putInt("chatIdx", roomItem.chatIdx)
                            putString("chatTitle", roomItem.chatTitle)
                            putStringArrayList("chatMemberList", ArrayList(roomItem.chatMemberList))
                            putInt("participantCount", roomItem.participantCount)
                            putBoolean("groupChat", roomItem.groupChat)
                        }
                    }

                    requireActivity().supportFragmentManager.commit {
                        replace(R.id.containerMain, chatRoomFragment)
                        addToBackStack(FragmentName.CHAT_ROOM.str)
                    }

                }, loginUserId)
                adapter = chatSearchResultsAdapter
            }
        }
    }

    // 채팅 방 데이터 추가 (예시)
    fun addChatRoomData() {

        CoroutineScope(Dispatchers.Main).launch {

            val chatRoomSequence = ChatRoomDataSource.getChatRoomSequence()
            ChatRoomDataSource.updateChatRoomSequence(chatRoomSequence + 1)

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
            ChatRoomDataSource.insertChatRoomData(chatRoomData)
            Log.d("test1234", "${chatTitle} 채팅방 생성 완료")
        }
    }
}