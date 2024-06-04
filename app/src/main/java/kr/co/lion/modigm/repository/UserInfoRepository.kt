package kr.co.lion.modigm.repository

import android.app.Activity
import kr.co.lion.modigm.db.user.RemoteUserDataSource
import kr.co.lion.modigm.model.UserData

class UserInfoRepository {

    private val _joinUserDataSource = RemoteUserDataSource()

    // 회원가입
    suspend fun insetUserData(userInfoData: UserData): Boolean = _joinUserDataSource.insetUserData(userInfoData)


    // ----------------- 로그인 데이터 처리 -----------------

    // Firebase Functions를 통해 Custom Token 획득
    suspend fun getKakaoCustomToken(accessToken: String): String = _joinUserDataSource.getKakaoCustomToken(accessToken)

    // Firebase Custom Token으로 로그인
    suspend fun signInWithCustomToken(customToken: String) = _joinUserDataSource.signInWithCustomToken(customToken)

    // 깃허브 로그인
    suspend fun signInWithGithub(context: Activity) = _joinUserDataSource.signInWithGithub(context)

    // ----------------- 로그인 데이터 처리 끝-----------------
}
