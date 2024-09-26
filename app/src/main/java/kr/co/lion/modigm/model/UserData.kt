package kr.co.lion.modigm.model

import java.sql.ResultSet
import org.threeten.bp.LocalDateTime;
import java.sql.Timestamp

data class UserData(
    val userIdx: Int = -1,              // 회원 고유번호
    val userUid: String = "",           // 회원 UID
    val userName: String = "",          // 이름
    val userPhone: String = "",         // 전화번호
    val userProfilePic: String = "",    // 프로필 사진
    val userIntro: String = "",         // 자기소개
    val userEmail: String = "",         // 사용자 이메일
    val userProvider: String = "",      // Firebase Auth에 등록된 계정 Provider
    val userInterests: String = "",     // 관심 분야 목록
    val userJoinDate: LocalDateTime = LocalDateTime.now(), // 회원가입 일자
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
        map["userJoinDate"] = this.userJoinDate
        return map
    }

    companion object {
        fun getUserData(resultSet: ResultSet): UserData {
            return UserData(
                resultSet.getInt("userIdx"),
                resultSet.getString("userUid") ?: "",
                resultSet.getString("userName") ?: "",
                resultSet.getString("userPhone") ?: "",
                resultSet.getString("userProfilePic") ?: "",
                resultSet.getString("userIntro") ?: "",
                resultSet.getString("userEmail") ?: "",
                resultSet.getString("userProvider") ?: "",
                resultSet.getString("userInterests") ?: "",
                getLocalDate(resultSet.getTimestamp("userJoinDate"))
            )
        }

        private fun getLocalDate(timestamp:Timestamp): LocalDateTime {
            return LocalDateTime.ofEpochSecond(
                timestamp.time / 1000, // 밀리초를 초 단위로 변환
                (timestamp.time % 1000 * 1000000).toInt(), // 나머지 밀리초를 나노초로 변환
                org.threeten.bp.ZoneOffset.UTC // 필요에 따라 ZoneOffset 설정
            )
        }
    }
}