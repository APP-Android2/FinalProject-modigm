package kr.co.lion.modigm.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.FragmentChatGroupBinding
import kr.co.lion.modigm.databinding.RowChatroomFiledBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.chat.adapter.ChatRoomAdapter
import kr.co.lion.modigm.util.FragmentName

class ChatGroupFragment : Fragment() {

    lateinit var fragmentChatGroupBinding: FragmentChatGroupBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentChatGroupBinding = FragmentChatGroupBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        // Recycler 뷰
        setupRecyclerView()

        return fragmentChatGroupBinding.root
    }

    // RecyclerView 초기화
    private fun setupRecyclerView() {
        // 대화방 목록 데이터 생성 (임시)
        val roomList = listOf(
            ChatRoomItem(
                chatIdx = 4,
                chatTitle = "Kotlin 스터디 모집",
                chatMemberList = listOf("currentUser", "sonUser", "iuUser", "ryuUser"),
                participantCount = 4,
                isGroupChat = true
            ),
            ChatRoomItem(
                chatIdx = 5,
                chatTitle = "Modigm 멤버 모임",
                chatMemberList = listOf("currentUser", "swUser", "tjUser", "hwUser", "msUser", "shUser"),
                participantCount = 6,
                isGroupChat = true
            ),
            ChatRoomItem(
                chatIdx = 6,
                chatTitle = "제 11회 해커톤 준비반",
                chatMemberList = listOf("currentUser", "sonUser", "iuUser", "ryuUser"),
                participantCount = 4,
                isGroupChat = true
            ),
        )

        // 대화방 목록 RecyclerView 설정
        fragmentChatGroupBinding.recyclerViewChatGroup.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ChatRoomAdapter(roomList, { roomItem ->
                // 대화방 선택 시 동작
                Log.d("test1234", "Selected Room: ${roomItem.chatTitle}")
            }, mainActivity)
        }
    }
}