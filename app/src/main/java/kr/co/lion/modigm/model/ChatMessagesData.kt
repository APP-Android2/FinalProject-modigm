package kr.co.lion.modigm.model

data class ChatMessagesData(
    val chatIdx : Int, // 채팅방 고유 ID
    val chatSenderId: String, // 채팅 전송자 UID
    val chatMessage: String, // 채팅 메세지 내용
    val chatTime: String, // 채팅 전송한 시간
) {
    constructor(): this(0,"", "", "")
}