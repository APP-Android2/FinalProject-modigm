package kr.co.lion.modigm.ui.chat

import android.os.Bundle
import android.view.View
import kr.co.lion.modigm.databinding.FragmentChatBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class ChatFragment : VBBaseFragment<FragmentChatBinding>(FragmentChatBinding::inflate) {

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    // SharedPreferences에 저장된 모든 값을 가져오는 함수
    private fun getAllPrefs(): String {
        return prefs.getAllPrefs()
    }

    fun initView(){
        with(binding) {
            // SharedPreferences에 저장된 모든 값을 출력
            textViewChat.text = "현재 SharedPrefs에 저장된 값 : \n${getAllPrefs()}"
        }
    }

}