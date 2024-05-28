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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentChatOnetoOneBinding = FragmentChatOnetoOneBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        val chatViewModel = ViewModelProvider(requireActivity()).get(ChatViewModel::class.java)
        chatViewModel.updateChatRoomData.observe(viewLifecycleOwner) {
            gettingOneToOneChatRoomData()
        }

        gettingOneToOneChatRoomData()

        // RecyclerView 초기화
        setupRecyclerView()

        return fragmentChatOnetoOneBinding.root
    }

    override fun onResume() {
        super.onResume()
        // 프래그먼트가 다시 활성화될 때 데이터 갱신
        gettingOneToOneChatRoomData()
    }

    // RecyclerView 초기화
    private fun setupRecyclerView() {

        // 대화방 목록 RecyclerView 설정
        fragmentChatOnetoOneBinding.recyclerViewChatOnetoOne.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ChatRoomAdapter(chatRoomDataList, { roomItem ->
                // 대화방 선택 시 동작
                Log.d("test1234", "Selected Room: ${roomItem.chatTitle}")
            }, mainActivity)
        }
    }

    // 내가 속한 모든 그룹 채팅 방을 가져와 화면의 RecyclerView를 갱신한다.
    fun gettingOneToOneChatRoomData() {
        CoroutineScope(Dispatchers.Main).launch {
            // 대화방 목록 데이터 가져오기
            val newChatRoomDataList = ChatRoomDao.getOneToOneChatRooms("currentUser")

            // 새로운 목록으로 업데이트
            chatRoomDataList.clear()
            chatRoomDataList.addAll(newChatRoomDataList)

            // UI 스레드에서 RecyclerView를 갱신
            activity?.runOnUiThread {
                fragmentChatOnetoOneBinding.recyclerViewChatOnetoOne.adapter?.notifyDataSetChanged()
            }
        }
    }
}