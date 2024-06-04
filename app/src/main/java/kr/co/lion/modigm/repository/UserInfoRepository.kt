package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.user.RemoteUserDataSource
import kr.co.lion.modigm.model.UserData

class UserInfoRepository {

    private val _joinUserDataSource = RemoteUserDataSource()

    // 회원가입
    suspend fun insetUserData(userInfoData: UserData): Boolean = _joinUserDataSource.insetUserData(userInfoData)
}