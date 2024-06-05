package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.chat.ChatRoomDataSource
import kr.co.lion.modigm.model.ChatRoomData
import kr.co.lion.modigm.model.UserData

class ChatRoomRepository {

    private val chatRoomDataSource = ChatRoomDataSource()

    // 채팅 방 시퀀스 번호를 가져옴
    suspend fun getChatRoomSequence() = chatRoomDataSource.getChatRoomSequence()

    // 채팅 방 시퀀스 번호를 업데이트
    suspend fun updateChatRoomSequence(userSequence:Int) = chatRoomDataSource.updateChatRoomSequence(userSequence)

    // 채팅 방 생성
    suspend fun insertChatRoomData(chatRoomData: ChatRoomData) = chatRoomDataSource.insertChatRoomData(chatRoomData)

    // userUid에 맞는 그룹 또는 1:1 채팅 방을 가져옴
    fun updateChatRoomsListener(userUid: String, groupChat: Boolean , onUpdate: (List<ChatRoomData>) -> Unit) =
        chatRoomDataSource.updateChatRoomsListener(userUid, groupChat, onUpdate)

    // userUid에 맞는 모든 방을 가져옴 (검색에서 사용)
    fun updateChatAllRoomsListener(userUid: String, onUpdate: (List<ChatRoomData>) -> Unit) =
        chatRoomDataSource.updateChatAllRoomsListener(userUid, onUpdate)

    // 해당 채팅방 데이터 [마지막 메세지, 마지막 메세지 시간] 변경함 (Update)
    suspend fun updateChatRoomLastMessageAndTime(chatIdx: Int, chatMessage: String, chatFullTime: Long, chatTime: String) =
        chatRoomDataSource.updateChatRoomLastMessageAndTime(chatIdx, chatMessage, chatFullTime, chatTime)

    // 채팅방에 사용자 추가 / chatMemberList 배열에 ID 추가 (Update)
    suspend fun addUserToChatMemberList(chatIdx: Int, userId: String) =
        chatRoomDataSource.addUserToChatMemberList(chatIdx, userId)

    // 채팅방 나가기 / chatMemberList 배열에서 내 ID를 제거 (Update)
    suspend fun removeUserFromChatMemberList(chatIdx: Int, userId: String) =
        chatRoomDataSource.removeUserFromChatMemberList(chatIdx, userId)

    // 로그인 한 사용자 해당 채팅 방 메세지 읽음 처리 (Update)
    suspend fun chatRoomMessageAsRead(chatIdx: Int, loginUserId: String) =
        chatRoomDataSource.chatRoomMessageAsRead(chatIdx, loginUserId)

    // 메세지 전송시 해당 채팅 방 사용자에 안읽은 메세지 카운트 증가 (Update)
    suspend fun increaseUnreadMessageCount(chatIdx: Int, senderId: String) =
        chatRoomDataSource.increaseUnreadMessageCount(chatIdx, senderId)

    // 실시간으로 여러 사용자 정보를 가져오는 함수
    fun getUsersDataListListener(chatMemberList: List<String>, onUpdate: (List<UserData>) -> Unit) =
        chatRoomDataSource.getUsersDataListListener(chatMemberList, onUpdate)
}