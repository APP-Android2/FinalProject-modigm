package kr.co.lion.modigm.repository

import android.content.Context
import android.widget.ImageView
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
}