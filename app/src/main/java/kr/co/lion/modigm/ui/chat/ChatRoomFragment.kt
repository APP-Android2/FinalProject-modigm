package kr.co.lion.modigm.ui.chat

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChatBinding
import kr.co.lion.modigm.databinding.FragmentChatRoomBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.chat.adapter.MessageAdapter
import kr.co.lion.modigm.util.FragmentName
import java.text.SimpleDateFormat
import java.util.Date

data class Message(
    val userId: String = "",
    val text: String = "",
    val timestamp: String = "",
    val senderName: String,
)

class ChatRoomFragment : Fragment() {

    lateinit var fragmentChatRoomBinding: FragmentChatRoomBinding
    lateinit var mainActivity: MainActivity
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private val userId = "currentUser" // 현재 사용자의 ID를 설정하세요

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentChatRoomBinding = FragmentChatRoomBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

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

    // 툴바 세팅
    fun settingToolbar() {
        fragmentChatRoomBinding.apply {
            toolbarChatRoom.apply {
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
                    R.id.item1 -> {
                        mainActivity.removeFragment(FragmentName.CHAT_ROOM)
                        true
                    }
                    R.id.item2 -> {
                        true
                    }
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
            messageAdapter = MessageAdapter(userId, messages)
            adapter = messageAdapter
        }
    }

    // 테스트 메시지 추가 - (DB 연동하면 나중에 삭제해야함)
    private fun addTestMessages() {
        messages.add(Message(userId = userId, text = "안녕하세요~", timestamp = "00:01", senderName = "김원빈"))
        messages.add(Message(userId = "sonUser", text = "안녕하세요!", timestamp = "00:03", senderName = "손흥민"))
        messages.add(Message(userId = userId, text = "테스트 어때요?", timestamp = "01:04", senderName = "김원빈"))
        messages.add(Message(userId = "iuUser", text = "테스트 잘 되는거 같네요..?", timestamp = "02:13", senderName = "아이유"))
        messages.add(Message(userId = "ryuUser", text = "혹시 OOO님도 채팅 한번 쳐주세요!", timestamp = "02:21", senderName = "류현진"))

        // 어댑터에 데이터 변경을 알림
        messageAdapter.notifyDataSetChanged()
    }

    // 메시지 전송
    private fun sendMessage() {
        fragmentChatRoomBinding.apply {
            val text = editTextMessage.text.toString().trim()
            // 현재 시간
            val currentTimeText = SimpleDateFormat("HH:mm").format(Date())
            if (text.isNotEmpty()) {
                val message = Message(
                    userId = userId,
                    text = text,
                    timestamp = currentTimeText,
                    senderName = "김원빈"
                )
                messages.add(message)
                messageAdapter.notifyItemInserted(messages.size - 1)
                fragmentChatRoomBinding.recyclerView.scrollToPosition(messages.size - 1)
                editTextMessage.text.clear()
            }
        }
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
                sendMessage()
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
}