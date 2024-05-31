package kr.co.lion.modigm.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.databinding.FragmentChatOnetoOneBinding
import kr.co.lion.modigm.model.ChatRoomData
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.chat.adapter.ChatRoomAdapter
import kr.co.lion.modigm.ui.chat.dao.ChatRoomDao
import kr.co.lion.modigm.ui.chat.vm.ChatViewModel

class ChatOnetoOneFragment : Fragment() {

    lateinit var fragmentChatOnetoOneBinding: FragmentChatOnetoOneBinding
    lateinit var mainActivity: MainActivity

    // 내가 속한 1:1 채팅 방들을 담고 있을 리스트
    var chatRoomDataList = mutableListOf<ChatRoomData>()

    private val loginUserId = "currentUser" // 현재 사용자의 ID를 설정 (DB 연동 후 교체)

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
    }

    // 실시간 채팅 방 데이터 업데이트
    private fun getAndUpdateLiveChatRooms(){
        // 내가 속한 그룹 채팅 방(RecyclerView)를 실시간으로 업데이트
        ChatRoomDao.updateChatRoomsListener(loginUserId, groupChat = false) { updatedChatRooms ->
            chatRoomDataList.clear()
            chatRoomDataList.addAll(updatedChatRooms)

            // RecyclerView 갱신
            activity?.runOnUiThread {
                fragmentChatOnetoOneBinding.recyclerViewChatOnetoOne.adapter?.notifyDataSetChanged()
            }
            Log.d("test1234", "실시간 Update - 1:1 List")
        }
    }

    // RecyclerView 초기화
    private fun setupRecyclerView() {

        // 대화방 목록 RecyclerView 설정
        fragmentChatOnetoOneBinding.recyclerViewChatOnetoOne.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ChatRoomAdapter(chatRoomDataList, { roomItem ->
                // 대화방 선택 시 동작
                Log.d("test1234", "Selected Room: ${roomItem.chatTitle}")
            }, mainActivity, loginUserId)
        }
    }
}