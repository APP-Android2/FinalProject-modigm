package kr.co.lion.modigm.util

import kr.co.lion.modigm.ui.like.LikeFragment

// MainActivity에서 보여줄 프레그먼트들의 이름
enum class FragmentName (var str: String){


    // 채팅
    CHAT("ChatFragment"),
    CHAT_GROUP("ChatGroupFragment"),
    CHAT_ONE_TO_ONE("ChatOnetoOneFragment"),
    CHAT_ROOM("ChatRoomFragment"),

    // 글 상세보기
    DETAIL("DetailFragment"),


    // 회원가입
    JOIN("JoinFragment"),

    // 찜
    LIKE("LikeFragment"),

    // 로그인
    LOGIN("LoginFragment"),

    // 프로필
    PROFILE("ProfileFragment"),

    // 스터디
    STUDY("StudyFragment"),

    // 글 작성
    WRITE("WriteFragment"),



}