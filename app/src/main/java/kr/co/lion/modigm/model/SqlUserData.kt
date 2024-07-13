package kr.co.lion.modigm.model

data class SqlUserData(
    var userIdx: Int,                   // 회원 고유번호
    var userName: String = "",          // 이름
    var userPhone: String = "",         // 전화번호
    var userProfilePic: String = "",    // 프로필 사진
    var userIntro: String = "",         // 자기소개
    var userEmail: String = "",         // 사용자 이메일
    var userProvider: String = "",      // Firebase Auth에 등록된 계정 Provider
    var userInterests: String = "",     // 관심 분야 목록
)