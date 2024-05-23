package kr.co.lion.modigm.model

data class ChatRoomData(
    val chatIdx: Int, // 채팅방 고유 ID
    val chatTitle: String, // 채팅방 이름
    val chatMemberList: List<String> = listOf() // 채팅방 참여 멤버 UID 목록
) {
    constructor(): this(0,"", listOf())
}