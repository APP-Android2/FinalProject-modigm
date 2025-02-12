package kr.co.lion.modigm.util

// MainActivity에서 보여줄 프레그먼트들의 이름
enum class FragmentName (var str: String){

    // 알림
    NOTI("NotificationFragment"),

    // 글 상세보기
    DETAIL("DetailFragment"),
    DETAIL_MEMBER("DetailMemberFragment"),
    DETAIL_EDIT("DetailEditFragment"),

    // 회원가입
    JOIN("JoinFragment"),
    JOIN_DUPLICATE("JoinDuplicateFragment"),

    // 찜
    FAVORITE("FavoriteFragment"),

    // 로그인
    SOCIAL_LOGIN("SocialLoginFragment"),
    EMAIL_LOGIN("EmailLoginFragment"),
    FIND_EMAIL("FindEmailFragment"),
    FIND_EMAIL_AUTH("FindEmailAuthFragment"),
    FIND_PW("FindPwFragment"),
    FIND_PW_AUTH("FindPwAuthFragment"),
    RESET_PW("ResetPwFragment"),

    // 프로필
    PROFILE("ProfileFragment"),
    PROFILE_WEB("ProfileWebFragment"),
    PROFILE_STUDY("ProfileStudyFragment"),
    EDIT_PROFILE("EditProfileFragment"),
    SETTINGS("SettingsFragment"),
    CHANGE_PASSWORD_EMAIL("ChangePasswordEmailFragment"),
    CHANGE_PASSWORD_AUTH("ChangePasswordAuthFragment"),
    CHANGE_PHONE_EMAIL("ChangePhoneEmailFragment"),
    CHANGE_PHONE_SOCIAL("ChangePhoneSocialFragment"),
    CHANGE_PHONE_AUTH("ChangePhoneAuthFragment"),
    DELETE_USER("DeleteUserFragment"),

    // 스터디
    STUDY("StudyFragment"),
    STUDY_ALL("StudyAllFragment"),
    STUDY_MY("StudyMyFragment"),
    FILTER_SORT("FilterSortFragment"),
    BOTTOM_NAVI("BottomNaviFragment"),
    STUDY_SEARCH("StudySearchFragment"),

    // 글 작성
    WRITE("WriteFragment"),

}