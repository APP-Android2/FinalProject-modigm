package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.chat.ChatMessagesDataSource
import kr.co.lion.modigm.model.ChatMessagesData

class ChatMessagesRepository {

    private val chatMessagesDataSource = ChatMessagesDataSource()

    // Firestore 실시간 업데이트 리스너 - (메시지 데이터 Get)
    fun getChatMessagesListener(chatIdx: Int, onUpdate: (List<ChatMessagesData>) -> Unit) =
        chatMessagesDataSource.getChatMessagesListener(chatIdx, onUpdate)

    // 메세지 전송 db 추가
    suspend fun insertChatMessagesData(chatMessagesData: ChatMessagesData, chatIdx: Int, chatSenderName:String, chatFullTime:Long) =
        chatMessagesDataSource.insertChatMessagesData(chatMessagesData, chatIdx, chatSenderName, chatFullTime)
}