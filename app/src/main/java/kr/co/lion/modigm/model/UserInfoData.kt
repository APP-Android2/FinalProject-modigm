package kr.co.lion.modigm.model

data class UserInfoData(
    val userName: String,
    val userPhone: String,
    val userProfilePic: String,
    val userInterestList: MutableList<Int>,
    val userIntro: String,
    val userLinkMap: MutableList<String>,
    val userNumber: String,
    // 회원 가입 시 True로, 회원 가입 탈퇴 처리 시 False로 변경
    val userState: Boolean,
)