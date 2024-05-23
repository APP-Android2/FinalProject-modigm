package kr.co.lion.modigm.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChatOnetoOneBinding
import kr.co.lion.modigm.databinding.RowChatroomFiledBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.chat.adapter.ChatRoomAdapter
import kr.co.lion.modigm.util.MainFragmentName

// 대화방 아이템을 나타내는 데이터 클래스
data class ChatRoomItem(
    val chatIdx: Int, // 채팅방 고유 ID
    val chatTitle: String, // 채팅방 이름
    val chatMemberList: List<String> = listOf("currentUser", "iuUser"), // 채팅방 참여 멤버 UID 목록
    val participantCount: Int = 0, // 참여자 수 (그룹 채팅방일 때만 해당)
    val isGroupChat: Boolean = false // 그룹 채팅방 여부
)
class ChatOnetoOneFragment : Fragment() {

    lateinit var fragmentChatOnetoOneBinding: FragmentChatOnetoOneBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentChatOnetoOneBinding = FragmentChatOnetoOneBinding.inflate(layoutInflater)
        val view = fragmentChatOnetoOneBinding.root
        mainActivity = activity as MainActivity

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewChatOnetoOne)

        // 대화방 목록 데이터 생성 (임시)
        val roomList = listOf(
            ChatRoomItem(
                chatIdx = 1,
                chatTitle = "그룹 채팅방 1",
                chatMemberList = listOf("uid1", "uid2", "uid3"),
                participantCount = 3,
                isGroupChat = true
            ),
            ChatRoomItem(
                chatIdx = 2,
                chatTitle = "그룹 채팅방 2",
                chatMemberList = listOf("uid4", "uid5"),
                participantCount = 2,
                isGroupChat = true
            ),
            ChatRoomItem(
                chatIdx = 3,
                chatTitle = "1:1 채팅방 (사용자 1)",
                chatMemberList = listOf("uid1"),
                participantCount = 2,
                isGroupChat = false
            ),
            ChatRoomItem(
                chatIdx = 4,
                chatTitle = "1:1 채팅방 (사용자 2)",
                chatMemberList = listOf("uid2"),
                participantCount = 2,
                isGroupChat = false
            )
        )

        // 대화방 목록 RecyclerView 설정
        fragmentChatOnetoOneBinding.recyclerViewChatOnetoOne.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ChatRoomAdapter(roomList) { roomItem ->
                // 대화방 선택 시 동작
                // 여기서는 간단하게 로그를 출력하도록 하겠습니다.
                // Log.d("ChatRoomListFragment", "Selected Room: ${roomItem.chatTitle}")
            }
        }

        // Recycler 뷰
//        settingRecyclerViewChatRoom()

        return fragmentChatOnetoOneBinding.root
    }

    // Recycler 뷰 설정
//    fun settingRecyclerViewChatRoom() {
//        fragmentChatOnetoOneBinding.apply {
//            recyclerViewChatGroup.apply {
//                // 어뎁터 및 레이아웃 매니저 설정
//                adapter = ChatOnetoOneRecyclerViewAdapter()
//                layoutManager = LinearLayoutManager(mainActivity)
//            }
//        }
//    }

    // 그룹 채팅(참여 중 탭) Recycler 뷰 어댑터 세팅
//    inner class ChatOnetoOneRecyclerViewAdapter: RecyclerView.Adapter<ChatOnetoOneRecyclerViewAdapter.ChatOnetoOneViewHolder>(){
//        inner class ChatOnetoOneViewHolder(rowChatroomFiledBinding: RowChatroomFiledBinding): RecyclerView.ViewHolder(rowChatroomFiledBinding.root){
//            val rowChatroomFiledBinding: RowChatroomFiledBinding
//            init {
//                this.rowChatroomFiledBinding = rowChatroomFiledBinding
//
//                this.rowChatroomFiledBinding.root.layoutParams = ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT
//                )
//            }
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatOnetoOneViewHolder {
//            val rowChatroomFiledBinding = RowChatroomFiledBinding.inflate(layoutInflater)
//            val chatOnetoOneViewHolder = ChatOnetoOneViewHolder(rowChatroomFiledBinding)
//
//            return chatOnetoOneViewHolder
//        }
//
//        override fun getItemCount(): Int {
//            return 2
//        }
//
//        override fun onBindViewHolder(holder: ChatOnetoOneViewHolder, position: Int) {
//            holder.rowChatroomFiledBinding.root.setOnClickListener {
//                mainActivity.replaceFragment(MainFragmentName.CHAT_ROOM, true, true, null)
//            }
//
//            holder.rowChatroomFiledBinding.textViewRowChatroomFiledRoomTitle.text = "홍길동"
//        }
//    }
}