package kr.co.lion.modigm.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChatOnetoOneBinding
import kr.co.lion.modigm.model.ChatRoomData
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.chat.adapter.ChatRoomAdapter
import kr.co.lion.modigm.ui.chat.vm.ChatRoomViewModel
import kr.co.lion.modigm.util.FragmentName

class ChatOnetoOneFragment : Fragment() {

    lateinit var fragmentChatOnetoOneBinding: FragmentChatOnetoOneBinding
    lateinit var mainActivity: MainActivity
    
    // 어댑터
    private lateinit var chatRoomAdapter: ChatRoomAdapter

    // 뷰 모델
    private val chatRoomViewModel: ChatRoomViewModel by viewModels()

    // 내가 속한 1:1 채팅 방들을 담고 있을 리스트
    private var chatRoomDataList = mutableListOf<ChatRoomData>()
    // 해당 멤버의 유저 정보를 가지고 있을 리스트
    // private val usersDataList = mutableListOf<UserData>()

    // 현재 로그인 한 사용자 정보
    private val loginUserId = "rH82PMELb2TimapTRzownbZekd13" // 현재 사용자의 ID를 설정 (DB 연동 후 교체)
//    private val loginUserId = "swUser" // 현재 사용자의 ID를 설정 (DB 연동 후 교체)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentChatOnetoOneBinding = FragmentChatOnetoOneBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        return fragmentChatOnetoOneBinding.root
    }

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 초기화
        setupRecyclerView()

        // 실시간 채팅 방 데이터 업데이트
        getAndUpdateLiveChatRooms()

        // 데이터 변경 관찰
        observeData()
    }

    // 데이터 변경 관찰
    private fun observeData() {
        // 데이터 변경 관찰
        chatRoomViewModel.userChatRoomsList.observe(viewLifecycleOwner) { updatedChatRooms ->
            chatRoomDataList.clear()
            chatRoomDataList.addAll(updatedChatRooms)
            chatRoomAdapter.notifyDataSetChanged()
            Log.d("chatLog1", "1:1 - observeData() 데이터 변경")
        }
    }

    // 채팅 방 데이터 실시간 수신
    private fun getAndUpdateLiveChatRooms() {
        chatRoomViewModel.getUserChatRooms(loginUserId, false)
    }

    // RecyclerView 초기화
    private fun setupRecyclerView() {
        // 대화방 목록 RecyclerView 설정
        with(fragmentChatOnetoOneBinding.recyclerViewChatOnetoOne){
            layoutManager = LinearLayoutManager(requireContext())
            chatRoomAdapter = ChatRoomAdapter(chatRoomDataList, { roomItem ->
                Log.d("chatLog1", "1:1 - ${roomItem.chatIdx}번 ${roomItem.chatTitle}에 입장")

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

                parentFragmentManager.commit {
                    replace(R.id.containerMain , chatRoomFragment)
                    addToBackStack(FragmentName.CHAT_ROOM.str) // 뒤로가기 버튼으로 이전 상태로 돌아갈 수 있도록
                }
            }, loginUserId)
            adapter = chatRoomAdapter
        }
    }
}