package kr.co.lion.modigm.repository

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import com.google.firebase.auth.AuthResult
import kr.co.lion.modigm.db.user.RemoteUserDataSource
import kr.co.lion.modigm.model.UserData

class UserInfoRepository {
    private val _remoteUserDataSource = RemoteUserDataSource()

    // 회원가입
    suspend fun insetUserData(userInfoData: UserData): Boolean = _remoteUserDataSource.insetUserData(userInfoData)

    // 유저 정보 불러오기
    suspend fun loadUserData(uid: String): UserData? = _remoteUserDataSource.loadUserDataByUid(uid)

    // 유저 프로필 사진 불러오기
    suspend fun loadUserProfilePic(context: Context, imageFileName: String, imageView: ImageView) = _remoteUserDataSource.loadUserProfilePic(context, imageFileName, imageView)

    // 해당 전화 번호의 계정이 있는지 확인 (중복 확인)
    suspend fun checkUserByPhone(phoneNumber: String): Map<String, String>? = _remoteUserDataSource.checkUserByPhone(phoneNumber)


    // ----------------- 로그인 데이터 처리 -----------------

    // 이메일과 비밀번호로 로그인하는 메소드
    suspend fun loginWithEmailPassword(email: String, password: String) = _remoteUserDataSource.loginWithEmailPassword(email, password)

    // Firebase Functions를 통해 Custom Token 획득
    suspend fun getKakaoCustomToken(accessToken: String): String = _remoteUserDataSource.getKakaoCustomToken(accessToken)

    // Firebase Custom Token으로 로그인
    suspend fun signInWithCustomToken(customToken: String): String = _remoteUserDataSource.signInWithCustomToken(customToken)

    // 깃허브 로그인
    suspend fun signInWithGithub(context: Activity): AuthResult = _remoteUserDataSource.signInWithGithub(context)

    // 사용자 UID를 통해 사용자가 이미 가입된 계정인지 확인
    suspend fun isUserAlreadyRegistered(uid: String): Boolean = _remoteUserDataSource.isUserAlreadyRegistered(uid)

    // ----------------- 로그인 데이터 처리 끝-----------------
}