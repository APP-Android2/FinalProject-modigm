package kr.co.lion.modigm.util

import kr.co.lion.modigm.R

enum class JoinType (var provider:String, var icon:Int) {
    KAKAO("kakao", R.drawable.kakaotalk_sharing_btn_small),
    GITHUB("github.com", R.drawable.icon_github_logo),
    EMAIL("password", R.drawable.email_login_logo),
    ERROR("error", 0);

    companion object{
        fun getType(str:String):JoinType{
            return when(str){
                "kakao" -> KAKAO
                "github.com" -> GITHUB
                "password" -> EMAIL
                else -> ERROR
            }
        }
    }
}