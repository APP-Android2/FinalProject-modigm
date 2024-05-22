package kr.co.lion.modigm.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.FragmentChatOnetoOneBinding
import kr.co.lion.modigm.databinding.RowChatroomFiledBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.util.MainFragmentName

class ChatOnetoOneFragment : Fragment() {

    lateinit var fragmentChatOnetoOneBinding: FragmentChatOnetoOneBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentChatOnetoOneBinding = FragmentChatOnetoOneBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        // Recycler 뷰
        settingRecyclerViewChatRoom()

        return fragmentChatOnetoOneBinding.root
    }

    // Recycler 뷰 설정
    fun settingRecyclerViewChatRoom() {
        fragmentChatOnetoOneBinding.apply {
            recyclerViewChatGroup.apply {
                // 어뎁터 및 레이아웃 매니저 설정
                adapter = ChatOnetoOneRecyclerViewAdapter()
                layoutManager = LinearLayoutManager(mainActivity)
            }
        }
    }

    // 그룹 채팅(참여 중 탭) Recycler 뷰 어댑터 세팅
    inner class ChatOnetoOneRecyclerViewAdapter: RecyclerView.Adapter<ChatOnetoOneRecyclerViewAdapter.ChatOnetoOneViewHolder>(){
        inner class ChatOnetoOneViewHolder(rowChatroomFiledBinding: RowChatroomFiledBinding): RecyclerView.ViewHolder(rowChatroomFiledBinding.root){
            val rowChatroomFiledBinding: RowChatroomFiledBinding
            init {
                this.rowChatroomFiledBinding = rowChatroomFiledBinding

                this.rowChatroomFiledBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatOnetoOneViewHolder {
            val rowChatroomFiledBinding = RowChatroomFiledBinding.inflate(layoutInflater)
            val chatOnetoOneViewHolder = ChatOnetoOneViewHolder(rowChatroomFiledBinding)

            return chatOnetoOneViewHolder
        }

        override fun getItemCount(): Int {
            return 5
        }

        override fun onBindViewHolder(holder: ChatOnetoOneViewHolder, position: Int) {
            holder.rowChatroomFiledBinding.root.setOnClickListener {
                mainActivity.replaceFragment(MainFragmentName.CHAT_ROOM, true, true, null)
            }
        }
    }
}