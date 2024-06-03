package kr.co.lion.modigm.ui.chat.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.ChatMessagesData
import kr.co.lion.modigm.repository.ChatMessagesRepository

class ChatMessagesViewModel : ViewModel() {

    // 채팅 Repository
    private val chatMessagesRepository = ChatMessagesRepository()

    private val _chatMessages = MutableLiveData<List<ChatMessagesData>>()
    val chatMessages: LiveData<List<ChatMessagesData>> = _chatMessages

    // 특정 사용자에 맞는 그룹 또는 1:1 채팅 방을 가져옴
    fun getChatMessagesListener(chatIdx: Int) = viewModelScope.launch {
        try {
            chatMessagesRepository.getChatMessagesListener(chatIdx) { messages ->
                _chatMessages.postValue(messages)
            }
        } catch (e: Exception) {
            Log.e("chatLog", "Error - getChatMessagesListener: ${e.message}")
        }
    }

    // 메세지 전송 데이터 -> Firebase 추가
    fun insertChatMessagesData(chatMessagesData: ChatMessagesData, chatIdx: Int, chatSenderName: String, chatFullTime: Long) = viewModelScope.launch {
        try {
            chatMessagesRepository.insertChatMessagesData(chatMessagesData, chatIdx, chatSenderName, chatFullTime)
            Log.i("chatLog", "채팅 데이터 추가: $chatMessagesData")
        } catch (e: Exception) {
            Log.e("chatLog", "Error - insertChatMessagesData: ${e.message}")
        }
    }
}