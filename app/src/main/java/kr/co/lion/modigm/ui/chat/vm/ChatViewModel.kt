package kr.co.lion.modigm.ui.chat.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {

    // ChatGroupFragment의 데이터 갱신을 감지하기 위한 LiveData
    private val _updateChatRoomData = MutableLiveData<Unit>()
    val updateChatRoomData: LiveData<Unit> = _updateChatRoomData

    // ChatRoomFragment에서 호출하여 ChatGroupFragment의 데이터를 갱신
    fun triggerChatRoomDataUpdate() {
        _updateChatRoomData.value = Unit
    }
}