package kr.co.lion.modigm.model

import java.sql.ResultSet

data class UserLinkData(
    val linkIdx: Int = -1,              // 링크 고유번호
    val userIdx: Int = -1,              // 회원 고유번호
    val linkUrl: String = "",           // Url
    val linkOrder: Int = -1,         // 화면에서 보이는 순서
) {
    companion object {
        fun getUserLinkData(resultSet: ResultSet): UserLinkData {
            return UserLinkData(
                resultSet.getInt("linkIdx"),
                resultSet.getInt("userIdx"),
                resultSet.getString("linkUrl") ?: "",
                resultSet.getInt("linkOrder")
            )
        }
    }
}