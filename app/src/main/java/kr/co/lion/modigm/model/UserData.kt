package kr.co.lion.modigm.model

data class UserData(
    var userName: String = "",                                  // 이름
    var userPhone: String = "",                                 // 전화번호
    var userProfilePic: String = "",                            // 프로필 사진
    var userIntro: String = "",                                 // 자기소개
    var userInterestList: MutableList<Int> = mutableListOf(),   // 관심 분야 목록
    var userLinkList: MutableList<String> = mutableListOf(),    // 소개 링크 목록
    var userUid: String = "",                                   // 사용자 UID
    var userEmail: String = "",                                 // 사용자 이메일
    var userProvider: String = ""                               // Firebase Auth에 등록된 계정 Provider
)