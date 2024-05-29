package kr.co.lion.modigm.util

// MainActivity에서 보여줄 프레그먼트들의 이름
enum class FragmentName (var str: String){


    // 채팅
    CHAT("ChatFragment"),
    CHAT_GROUP("ChatGroupFragment"),
    CHAT_ONE_TO_ONE("ChatOnetoOneFragment"),
    CHAT_ROOM("ChatRoomFragment"),

    // 글 상세보기
    DETAIL("DetailFragment"),
    DETAIL_MEMBER("DetailMemberFragment"),
    DETAIL_EDIT("DetailEditFragment"),


    // 회원가입
    JOIN("JoinFragment"),
    JOIN_DUPLICATE("JoinDuplicateFragment"),

    // 찜
    LIKE("LikeFragment"),

    // 로그인
    LOGIN("LoginFragment"),
    OTHER_LOGIN("OtherLoginFragment"),

    // 프로필
    PROFILE("ProfileFragment"),
    PROFILE_WEB("ProfileWebFragment"),
    SETTINGS("SettingsFragment"),

    // 스터디
    STUDY("StudyFragment"),
    STUDY_ALL("StudyAllFragment"),
    STUDY_MY("StudyMyFragment"),
    FILTER_SORT("FilterSortFragment"),
    BOTTOM_NAVI("BottomNaviFragment"),

    // 글 작성
    WRITE("WriteFragment"),



}