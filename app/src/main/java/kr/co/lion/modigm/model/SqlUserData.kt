package kr.co.lion.modigm.model

import java.sql.ResultSet

data class SqlUserData(
    val userIdx: Int = -1,              // 회원 고유번호
    val userUid: String = "",           // 회원 UID
    val userName: String = "",          // 이름
    val userPhone: String = "",         // 전화번호
    val userProfilePic: String = "",    // 프로필 사진
    val userIntro: String = "",         // 자기소개
    val userEmail: String = "",         // 사용자 이메일
    val userProvider: String = "",      // Firebase Auth에 등록된 계정 Provider
    val userInterests: String = "",     // 관심 분야 목록
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["userUid"] = this.userUid
        map["userName"] = this.userName
        map["userPhone"] = this.userPhone
        map["userProfilePic"] = this.userProfilePic
        map["userIntro"] = this.userIntro
        map["userEmail"] = this.userEmail
        map["userProvider"] = this.userProvider
        map["userInterests"] = this.userInterests
        return map
    }

    companion object {
        fun getUserData(resultSet: ResultSet): SqlUserData {
            return SqlUserData(
                resultSet.getInt("userIdx"),
                resultSet.getString("userUid") ?: "",
                resultSet.getString("userName") ?: "",
                resultSet.getString("userPhone") ?: "",
                resultSet.getString("userProfilePic") ?: "",
                resultSet.getString("userIntro") ?: "",
                resultSet.getString("userEmail") ?: "",
                resultSet.getString("userProvider") ?: "",
                resultSet.getString("userInterests") ?: ""
            )
        }
    }
}