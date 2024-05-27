package kr.co.lion.modigm.model

data class UserInfoData(
    val userName: String,
    val userPhone: String,
    val userProfilePic: String? = null,
    val userInterestList: List<String>,
    val userIntro: String? = null,
    val userLinkMap: Map<String, String>? = null,
    val userNumber: String,
    // 회원 가입 시 True로, 회원 가입 탈퇴 처리 시 False로 변경
    val userState: Boolean,
)