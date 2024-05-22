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
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChatBinding
import kr.co.lion.modigm.databinding.FragmentChatRoomBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.util.MainFragmentName

class ChatRoomFragment : Fragment() {

    lateinit var fragmentChatRoomBinding: FragmentChatRoomBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentChatRoomBinding = FragmentChatRoomBinding.inflate(layoutInflater)
        mainActivity = activity as MainActivity

        // 채팅 방 - (툴바) 세팅
        settingToolbar()

        setupEditTextListener()

        return fragmentChatRoomBinding.root
    }

    // 툴바 세팅
    fun settingToolbar() {
        fragmentChatRoomBinding.apply {
            toolbarChatRoom.apply {
                // 왼쪽 네비게이션 버튼(Back)
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainFragmentName.CHAT_ROOM)
                }
            }
        }
    }

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
        }
    }

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