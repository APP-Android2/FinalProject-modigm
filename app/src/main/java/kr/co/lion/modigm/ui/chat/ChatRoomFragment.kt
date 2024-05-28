package kr.co.lion.modigm.ui.chat

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChatRoomBinding
import kr.co.lion.modigm.model.ChatMessagesData
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.chat.adapter.ChatRoomAdapter
import kr.co.lion.modigm.ui.chat.adapter.MessageAdapter
import kr.co.lion.modigm.ui.chat.dao.ChatMessagesDao
import kr.co.lion.modigm.ui.chat.dao.ChatRoomDao
import kr.co.lion.modigm.ui.chat.vm.ChatViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.hideSoftInput
import java.text.SimpleDateFormat
import java.util.Date

class ChatRoomFragment : Fragment() {

    lateinit var fragmentChatRoomBinding: FragmentChatRoomBinding
    lateinit var mainActivity: MainActivity
    private lateinit var messageAdapter: MessageAdapter

    // 보낼 메세지를 담고 있을 리스트
    private val messages = mutableListOf<ChatMessagesData>()
    private val loginUserId = "currentUser" // 현재 사용자의 ID를 설정하세요
    private val loginUserName = "김원빈" // 현재 사용자의 Name을 설정하세요

    private lateinit var chatViewModel: ChatViewModel

    // 현재 방 번호, 제목, 그룹 채팅방 여부
    var chatIdx = 0
    var chatTitle = "채팅방 제목"
    var chatMemberList = listOf<String>()
    var isGroupChat = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentChatRoomBinding = FragmentChatRoomBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        chatViewModel = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)

        // Bundle로부터 데이터 가져오기
        arguments?.let {
            chatIdx = it.getInt("chatIdx")
            chatTitle = it.getString("chatTitle").toString()
            chatMemberList = it.getStringArrayList("chatMemberList")!!
            isGroupChat = it.getBoolean("groupChat")
        }

        // 채팅 방 - (툴바) 세팅
        settingToolbar()

        // 채팅 입력 칸 (입력 여부에 따른) 버튼 세팅
        setupEditTextListener()

        // RecyclerView 초기화
        setupRecyclerView()

        // 테스트 메시지 추가
        addTestMessages()

        return fragmentChatRoomBinding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("test1234", "ChatRoomFragment - onDestroy")
        chatViewModel.triggerChatRoomDataUpdate()
    }

    // 툴바 세팅
    fun settingToolbar() {
        fragmentChatRoomBinding.apply {
            toolbarChatRoom.apply {
                title = "$chatTitle"
                if (isGroupChat == true) subtitle = "현재인원 ${chatMemberList.size}명"
                // 왼쪽 네비게이션 버튼(Back)
                setNavigationOnClickListener {
                    mainActivity.removeFragment(FragmentName.CHAT_ROOM)
                }
                // 오른쪽 툴바 버튼(More_Vert, 수직 점 세개)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        // 점 세개 클릭 시
                        R.id.chatroom_toolbar_more_dot -> {
                            showPopupMenu()
                        }
                    }
                    true
                }
            }
        }
    }

    // 팝업 메뉴 세팅 - 툴바의 점 세개 버튼 누르면 나오는 팝업 메뉴
    private fun showPopupMenu() {
        fragmentChatRoomBinding.apply {
            // 툴바의 점 세개 버튼 위치에 팝업 메뉴를 표시
            val view = toolbarChatRoom.findViewById<View>(R.id.chatroom_toolbar_more_dot) ?: return
            val popupMenu = PopupMenu(requireContext(), view)
            popupMenu.menuInflater.inflate(R.menu.popup_menu_chatroom_more_vert, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    // 채팅방 나가기
                    R.id.item1 -> {
                        outChatRoom()
                        mainActivity.removeFragment(FragmentName.CHAT_ROOM)
                        true
                    }
                    // 멤버 보기
                    R.id.item2 -> {
                        true
                    }
                    // 공지
                    R.id.item3 -> {
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    // RecyclerView 초기화
    private fun setupRecyclerView() {
        fragmentChatRoomBinding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            messageAdapter = MessageAdapter(loginUserId, messages)
            adapter = messageAdapter
        }
    }

    // 메세지 전송
    fun addChatMessagesData() {
        CoroutineScope(Dispatchers.Main).launch {
            fragmentChatRoomBinding.apply {

                val text = editTextMessage.text.toString().trim()
                // 현재 시간
                val now = System.currentTimeMillis()
                val currentTimeText = SimpleDateFormat("HH:mm").format(Date())

                val chatIdx = chatIdx
                // 현재 로그인 한 계정의 아이디
                val chatSenderId = loginUserId
                val chatSenderName = loginUserName
                val chatMessage = text
                val chatFullTime = now
                val chatTime = currentTimeText.toString()

                if (text.isNotEmpty()) {
                    val message = ChatMessagesData(
                        chatIdx,
                        chatSenderId,
                        chatSenderName,
                        chatMessage,
                        chatFullTime,
                        chatTime,
                    )
                    messages.add(message)
                    messageAdapter.notifyItemInserted(messages.size - 1)
                    fragmentChatRoomBinding.recyclerView.scrollToPosition(messages.size - 1)
                    editTextMessage.text.clear()

                    // 메세지 전송 후 저장
                    ChatMessagesDao.insertChatMessagesData(message, chatIdx, chatSenderName, chatFullTime)
                    // 메세지 전송 후 해당 채팅 방 마지막 메세지 및 시간 변경
                    ChatRoomDao.updateChatRoomLastMessageAndTime(chatIdx, chatMessage, chatFullTime, chatTime)

                    // 전송 후 키보드 숨기기
                    activity?.hideSoftInput()
                }
            }
        }
    }

    // 테스트 메시지 추가 - (DB 연동하면 나중에 삭제해야함)
    private fun addTestMessages() {
        /*
        CoroutineScope(Dispatchers.Main).launch {
            val messagesList = ChatMessagesDao.getChatMessages(chatIdx)

            messages.clear()
            messages.addAll(messagesList)

            // 어댑터에 데이터 변경을 알림
            messageAdapter.notifyDataSetChanged()
        }
        */
        // Firestore 실시간 업데이트 리스너 등록
        ChatMessagesDao.addChatMessagesListener(chatIdx) { updatedMessages ->
            // 업데이트된 메시지로 어댑터의 메시지 리스트 업데이트
            messageAdapter.updateMessages(updatedMessages)
            // RecyclerView 최하단 표시
            scrollToBottom()
        }
    }

    // RecyclerView (대화 맨 마지막 기준)으로 설정
    private fun scrollToBottom() {
        // 만약 메시지가 하나도 없다면 스크롤 할 필요가 없음 -> 함수 바로 종료
        if (messages.isEmpty()) return

        // 메시지가 하나 이상 있다면 마지막 아이템의 인덱스를 구한다
        val lastIndex = messages.size - 1

        // RecyclerView를 마지막 아이템으로 스크롤합니다.
        fragmentChatRoomBinding.recyclerView.scrollToPosition(lastIndex)
    }

    // 채팅 입력 칸 - 변경 관련 Listener
    fun setupEditTextListener() {
        fragmentChatRoomBinding.apply {
            editTextMessage.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 텍스트 변경 전 호출
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 텍스트가 변경될 때 호출
                    updateEditTextInText()
                }

                override fun afterTextChanged(s: Editable?) {
                    // 텍스트 변경 후 호출
                }
            })
            // 전송 버튼 클릭 시 메시지 전송
            imageButtonChatRoomSend.setOnClickListener {
                addChatMessagesData()
            }
        }
    }

    // 채팅 입력 칸 - 입력 상태 여부에 따라 설정
    fun updateEditTextInText() {
        fragmentChatRoomBinding.apply {
            if (editTextMessage.text.toString().isEmpty()){
                imageButtonChatRoomSend.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#999999"))
            } else {
                imageButtonChatRoomSend.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1A51C5"))
            }
        }
    }

    // 대화방 나가기
    fun outChatRoom() {
        CoroutineScope(Dispatchers.Main).launch {
            ChatRoomDao.removeUserFromChatMemberList(chatIdx, loginUserId)
        }
    }
}