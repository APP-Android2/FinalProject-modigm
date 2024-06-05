package kr.co.lion.modigm.ui.chat.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.ChatRoomData
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.repository.ChatRoomRepository

class ChatRoomViewModel : ViewModel() {

    // 채팅 Repository
    private val chatRoomRepository = ChatRoomRepository()

    // 채팅방 시퀀스 번호
    private val _chatRoomSequence = MutableLiveData<Int>()
    val chatRoomSequence: LiveData<Int> = _chatRoomSequence

    // 사용자에 맞는 채팅방 목록
    private val _userChatRoomsList = MutableLiveData<List<ChatRoomData>>()
    val userChatRoomsList: LiveData<List<ChatRoomData>> = _userChatRoomsList

    // 모든 채팅방 목록 (검색에서 사용)
    private val _allChatRoomsList = MutableLiveData<List<ChatRoomData>>()
    val allChatRoomsList: LiveData<List<ChatRoomData>> = _allChatRoomsList

    private val _userDataList = MutableLiveData<List<UserData>>()
    val userDataList: LiveData<List<UserData>> get() = _userDataList

    // 뷰모델 인스턴스가 생성될 때마다 가동
    init {
        // 초기화 시에 필요한 데이터 로드 수행
    }

    // 채팅 방 시퀀스 번호를 가져옴
    fun getChatRoomSequence() = viewModelScope.launch {
        try {
            val sequence = chatRoomRepository.getChatRoomSequence()
            _chatRoomSequence.postValue(sequence)
            Log.i("chatLog", "ChatRoomViewModel - 채팅 방 시퀀스 번호 가져옴: $sequence")
        } catch (e: Exception) {
            Log.e("chatLog", "Error - getChatRoomSequence: ${e.message}")
        }
    }

    // 채팅 방 시퀀스 번호를 업데이트
    fun updateChatRoomSequence(userSequence: Int) = viewModelScope.launch {
        try {
            chatRoomRepository.updateChatRoomSequence(userSequence)
            Log.i("chatLog", "ChatRoomViewModel - 채팅 방 시퀀스 번호 수정: $userSequence")
        } catch (e: Exception) {
            Log.e("chatLog", "Error - updateChatRoomSequence: ${e.message}")
        }
    }

    // 채팅 방 생성
    fun insertChatRoomData(chatRoomData: ChatRoomData) = viewModelScope.launch {
        try {
            chatRoomRepository.insertChatRoomData(chatRoomData)
            Log.i("chatLog", "ChatRoomViewModel - 채팅 방 데이터 추가: $chatRoomData")
        } catch (e: Exception) {
            Log.e("chatLog", "Error - insertChatRoomData: ${e.message}")
        }
    }

    // 특정 사용자에 맞는 그룹 또는 1:1 채팅 방을 가져옴
    fun getUserChatRooms(userUid: String, groupChat: Boolean) = viewModelScope.launch {
        try {
            chatRoomRepository.updateChatRoomsListener(userUid, groupChat) { chatRooms ->
                _userChatRoomsList.postValue(chatRooms)
                Log.i("chatLog", "ChatRoomViewModel - ${userUid}의 채팅 방: ${_userChatRoomsList.value}")
            }
        } catch (e: Exception) {
            Log.e("chatLog", "Error - getUserChatRooms: ${e.message}")
        }
    }

    // 특정 사용자에 맞는 모든 방을 가져옴 (검색에서 사용)
    fun getAllChatRooms(userUid: String) = viewModelScope.launch {
        try {
            chatRoomRepository.updateChatAllRoomsListener(userUid) { chatRooms ->
                _allChatRoomsList.postValue(chatRooms)
                Log.i("chatLog", "ChatRoomViewModel - ${userUid}의 모든 채팅 방: ${_allChatRoomsList.value}")
            }
        } catch (e: Exception) {
            Log.e("chatLog", "Error - getAllChatRooms: ${e.message}")
        }
    }

    // 해당 채팅방 데이터 [마지막 메세지, 마지막 메세지 시간] 변경함 (Update)
    fun updateChatRoomLastMessageAndTime(chatIdx: Int, chatMessage: String, chatFullTime: Long, chatTime: String) = viewModelScope.launch {
        try {
            chatRoomRepository.updateChatRoomLastMessageAndTime(chatIdx, chatMessage, chatFullTime, chatTime)
            Log.i("chatLog", "ChatRoomViewModel - ${chatIdx}번 채팅 방 [마지막 채팅, 시간] 수정")
        } catch (e: Exception) {
            Log.e("chatLog", "Error - updateChatRoomLastMessageAndTime: ${e.message}")
        }
    }

    // 채팅방 나가기 / chatMemberList 배열에서 내 ID를 제거 (Update)
    suspend fun removeUserFromChatMemberList(chatIdx: Int, userId: String) = viewModelScope.launch {
        try {
            chatRoomRepository.removeUserFromChatMemberList(chatIdx, userId)
            Log.i("chatLog", "ChatRoomViewModel - 해당 채팅 방에서 멤버 목록 제거 - ${chatIdx}번 $userId")
        } catch (e: Exception) {
            Log.e("chatLog", "Error - removeUserFromChatMemberList: ${e.message}")
        }
    }

    // 로그인 한 사용자 해당 채팅 방 메세지 읽음 처리 (Update)
    fun chatRoomMessageAsRead(chatIdx: Int, loginUserId: String) = viewModelScope.launch {
        try {
            chatRoomRepository.chatRoomMessageAsRead(chatIdx, loginUserId)
            Log.i("chatLog", "ChatRoomViewModel - ${chatIdx}번 채팅 방 ${loginUserId} 채팅 읽음 처리")
        } catch (e: Exception) {
            Log.e("chatLog", "Error - chatRoomMessageAsRead: ${e.message}")
        }
    }

    // 메세지 전송시 해당 채팅 방 사용자에 안읽은 메세지 카운트 증가
    fun increaseUnreadMessageCount(chatIdx: Int, senderId: String) = viewModelScope.launch {
        try {
            chatRoomRepository.increaseUnreadMessageCount(chatIdx, senderId)
            Log.i("chatLog", "ChatRoomViewModel - ${chatIdx}번 채팅 방 안읽은 메시지 카운트 증가")
        } catch (e: Exception) {
            Log.e("chatLog", "Error - increaseUnreadMessageCount: ${e.message}")
        }
    }

    // 사용자 정보를 가져오는 함수
    fun getUsersDataList(chatMemberList: List<String>) {
        viewModelScope.launch {
            try {
                chatRoomRepository.getUsersDataListListener(chatMemberList) { userDataList ->
                    _userDataList.postValue(userDataList)
                    Log.i("chatLog", "ChatRoomViewModel - 채팅 방 멤버 데이터: ${userDataList}")
                }
            } catch (e: Exception) {
                Log.e("chatLog", "Error - getUsersDataList: ${e.message}")
            }
        }
    }
}