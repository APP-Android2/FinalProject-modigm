package kr.co.lion.modigm.model

data class ChatRoomData(
    val chatIdx: Int = 0, // 채팅방 고유 ID
    val chatTitle: String = "", // 채팅방 이름
    val chatRoomImage: String = "",
    val chatMemberList: List<String> = listOf(), // 채팅방 참여 멤버 UID 목록
    val participantCount: Int = 2, // 최소 참여자 수 2 (그룹 채팅방일 때만 해당)
    val groupChat: Boolean = false, // 그룹 채팅방 여부
    val lastChatMessage: String = "",
    val lastChatFullTime: Long = 0,
    val lastChatTime: String = "",
    val chatMemberState: MutableMap<String, Boolean> = mutableMapOf(), // 현재 채팅방 참여 상태
    val unreadMessageCount: MutableMap<String, Int> = mutableMapOf() // 사용자별 안 읽은 메시지 개수를 추적하는 필드
) {
    constructor(): this(
        0,"", "", listOf(), 2, false,
        "", 0, "",
    )
}