package kr.co.lion.modigm.ui.chat

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChatRoomBinding
import kr.co.lion.modigm.model.ChatMessagesData
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.chat.adapter.ChatRoomAdapter
import kr.co.lion.modigm.ui.chat.adapter.ChatRoomMemberAdapter
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
    private lateinit var chatRoomMemberAdapter: ChatRoomMemberAdapter


    // 보낼 메세지를 담고 있을 리스트
    private val messages = mutableListOf<ChatMessagesData>()
    private val loginUserId = "currentUser" // 현재 사용자의 ID를 설정 (DB 연동 후 교체)
    private val loginUserName = "김원빈" // 현재 사용자의 Name을 설정 (DB 연동 후 교체)

    // 테스트 아이디 바꾸기
//    private val loginUserId = "iuUser" // 현재 사용자의 ID를 설정 (DB 연동 후 교체)
//    private val loginUserName = "아이유" // 현재 사용자의 Name을 설정 (DB 연동 후 교체)

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

        return fragmentChatRoomBinding.root
    }

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 입장시 -> 메시지 읽음 처리
        readMessage()

        // 채팅 방 - (툴바) 세팅
        settingToolbar()

        // 채팅 입력 칸 (입력 여부에 따른) 버튼 세팅
        setupEditTextListener()

        // RecyclerView 초기화
        setupRecyclerView()
        setupMemberRecyclerView()

        // 메시지 가져오기 및 업데이트
        getAndUpdateMessages()

        // 사이드 네비게이션 클릭 Event
        sideNavigationTextViewClickEvent()
    }


    // Pause 상태 - 채팅방 나갈때도 포함되며 Destory에 안쓴 이유는 (onDestory 보다 먼저 실행되서...) 어차피 readMessage 처리 해야해서
    override fun onPause() {
        super.onPause()
        Log.d("test1234", "ChatRoomFragment - onPause 실행")
        readMessage()
    }

    // 툴바 세팅
    fun settingToolbar() {
        fragmentChatRoomBinding.apply {
            toolbarChatRoom.apply {
                setNavigationViewWidth()
                title = "$chatTitle"
                if (isGroupChat == true) subtitle = "현재인원 ${chatMemberList.size}명"
                // 왼쪽 네비게이션 버튼(Back)
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                    // mainActivity.removeFragment(FragmentName.CHAT_ROOM)
                }
                // 오른쪽 툴바 버튼(Menu)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.chatroom_toolbar_menu -> {
                            drawerLayoutContent.openDrawer(GravityCompat.END)
                        }
                    }
                    true
                }
            }
        }
    }

    // 네비게이션 뷰의 너비를 동적으로 설정하는 함수
    private fun setNavigationViewWidth() {
        fragmentChatRoomBinding.apply {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            val params = navigationViewContent.layoutParams
            params.width = (width * 0.8).toInt() // 화면 폭의 80%로 설정
            navigationViewContent.layoutParams = params
        }
    }

    // 네비게이션 뷰의 멤버 RecyclerView 초기화
    private fun setupMemberRecyclerView() {
        fragmentChatRoomBinding.recyclerViewChatRoomMemeberList.apply {
            layoutManager = LinearLayoutManager(context)
            chatRoomMemberAdapter = ChatRoomMemberAdapter(chatMemberList)
            adapter = chatRoomMemberAdapter
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

                    ChatRoomDao.increaseUnreadMessageCount(chatIdx, chatSenderId)
                }
            }
        }
    }

    // 해당 채팅 방의 메시지 추가 및 가져오기 - (DB 연동하면 나중에 삭제해야함)
    private fun getAndUpdateMessages() {
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

    // 사이드 네비게이션 클릭 Event
    fun sideNavigationTextViewClickEvent() {
        fragmentChatRoomBinding.apply {

            // 공지 클릭 시
            textViewSpeaker.setOnClickListener {
                // 눌렸을 때의 효과
                it.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.textViewClickGray))

                // 잠시 후 기본 상태로 돌아가기
                it.postDelayed({
                    it.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                }, 30)
            }

            // 대화방 나가기 클릭 시
            textViewLeaveChat.setOnClickListener {
                // 눌렸을 때의 효과
                it.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.textViewClickGray))

                // 잠시 후 기본 상태로 돌아가기
                it.postDelayed({
                    it.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                }, 30)
                
                // 해당 채팅방 멤버 리스트 제거 및 나가기
                outChatRoom()
            }
        }
    }

    // 대화방 나가기
    fun outChatRoom() {
        CoroutineScope(Dispatchers.Main).launch {
            ChatRoomDao.removeUserFromChatMemberList(chatIdx, loginUserId)
        }
        parentFragmentManager.popBackStack()
    }

    // 메세지 읽음 처리
    fun readMessage() {
        CoroutineScope(Dispatchers.Main).launch {
            ChatRoomDao.chatRoomMessageAsRead(chatIdx, loginUserId)
        }
    }
}