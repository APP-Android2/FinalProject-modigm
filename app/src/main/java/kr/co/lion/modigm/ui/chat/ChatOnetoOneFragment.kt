package kr.co.lion.modigm.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.databinding.FragmentChatOnetoOneBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.chat.adapter.ChatRoomAdapter

// 대화방 아이템을 나타내는 데이터 클래스
data class ChatRoomItem(
    val chatIdx: Int, // 채팅방 고유 ID
    val chatTitle: String, // 채팅방 이름
    val chatMemberList: List<String>, // 채팅방 참여 멤버 UID 목록
    val participantCount: Int = 0, // 참여자 수 (그룹 채팅방일 때만 해당)
    val isGroupChat: Boolean = false // 그룹 채팅방 여부
)

class ChatOnetoOneFragment : Fragment() {

    lateinit var fragmentChatOnetoOneBinding: FragmentChatOnetoOneBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentChatOnetoOneBinding = FragmentChatOnetoOneBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        // RecyclerView 초기화
        setupRecyclerView()

        return fragmentChatOnetoOneBinding.root
    }

    // RecyclerView 초기화
    private fun setupRecyclerView() {
        // 대화방 목록 데이터 생성 (임시)
        val roomList = listOf(
            ChatRoomItem(
                chatIdx = 1,
                chatTitle = "류현진",
                chatMemberList = listOf("currentUser", "ryuUser"),
                participantCount = 2,
                isGroupChat = false
            ),
            ChatRoomItem(
                chatIdx = 2,
                chatTitle = "아이유",
                chatMemberList = listOf("currentUser", "iuUser"),
                participantCount = 2,
                isGroupChat = false
            ),
            ChatRoomItem(
                chatIdx = 3,
                chatTitle = "손흥민",
                chatMemberList = listOf("currentUser", "sonUser"),
                participantCount = 2,
                isGroupChat = false
            )
        )

        // 대화방 목록 RecyclerView 설정
        fragmentChatOnetoOneBinding.recyclerViewChatOnetoOne.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ChatRoomAdapter(roomList, { roomItem ->
                // 대화방 선택 시 동작
                Log.d("test1234", "Selected Room: ${roomItem.chatTitle}")
            }, mainActivity)
        }
    }
}