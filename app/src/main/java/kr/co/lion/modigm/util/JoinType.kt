package kr.co.lion.modigm.util

import kr.co.lion.modigm.R

enum class JoinType (val provider: String, val icon: Int) {
    KAKAO("kakao", R.drawable.kakaotalk_sharing_btn_small),
    GITHUB("github", R.drawable.icon_github_logo),
    EMAIL("email", R.drawable.email_login_logo),
    ERROR("error", R.drawable.icon_error_24px);

    companion object{
        fun getType(str:String):JoinType{
            return when(str){
                "kakao" -> KAKAO
                "github" -> GITHUB
                "email" -> EMAIL
                else -> ERROR
            }
        }
    }
}