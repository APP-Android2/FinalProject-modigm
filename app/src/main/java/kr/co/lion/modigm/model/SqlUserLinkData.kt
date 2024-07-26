package kr.co.lion.modigm.model

data class SqlUserLinkData(
    val linkIdx: Int = -1,              // 링크 고유번호
    val userIdx: Int = -1,              // 회원 고유번호
    val linkUrl: String = "",           // Url
    val linkOrder: Int = -1,         // 화면에서 보이는 순서
)