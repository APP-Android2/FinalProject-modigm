package kr.co.lion.modigm.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.databinding.FragmentChatGroupBinding
import kr.co.lion.modigm.databinding.RowChatroomFiledBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.util.FragmentName
import java.text.NumberFormat
import java.util.Locale

class ChatGroupFragment : Fragment() {

    lateinit var fragmentChatGroupBinding: FragmentChatGroupBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentChatGroupBinding = FragmentChatGroupBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        // Recycler 뷰
        settingRecyclerViewChatRoom()

        return fragmentChatGroupBinding.root
    }

    // Recycler 뷰 설정
    fun settingRecyclerViewChatRoom() {
        fragmentChatGroupBinding.apply {
            recyclerViewChatGroup.apply {
                // 어뎁터 및 레이아웃 매니저 설정
                adapter = ChatGroupRecyclerViewAdapter()
                layoutManager = LinearLayoutManager(mainActivity)
            }
        }
    }

    // 그룹 채팅(참여 중 탭) Recycler 뷰 어댑터 세팅
    inner class ChatGroupRecyclerViewAdapter: RecyclerView.Adapter<ChatGroupRecyclerViewAdapter.ChatGroupViewHolder>(){
        inner class ChatGroupViewHolder(rowChatroomFiledBinding: RowChatroomFiledBinding): RecyclerView.ViewHolder(rowChatroomFiledBinding.root){
            val rowChatroomFiledBinding: RowChatroomFiledBinding
            init {
                this.rowChatroomFiledBinding = rowChatroomFiledBinding

                this.rowChatroomFiledBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatGroupViewHolder {
            val rowChatroomFiledBinding = RowChatroomFiledBinding.inflate(layoutInflater)
            val chatGroupViewHolder = ChatGroupViewHolder(rowChatroomFiledBinding)

            return chatGroupViewHolder
        }

        override fun getItemCount(): Int {
            return 6
        }

        override fun onBindViewHolder(holder: ChatGroupViewHolder, position: Int) {
            holder.rowChatroomFiledBinding.root.setOnClickListener {
                mainActivity.replaceFragment(FragmentName.CHAT_ROOM, true, true, null)
            }
        }
    }
}