package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.remote.JoinUserDataSource
import kr.co.lion.modigm.model.UserData

class UserInfoRepository {

    private val _joinUserDataSource = JoinUserDataSource()

    // 회원가입
    suspend fun insetUserData(userInfoData: UserData): Boolean = _joinUserDataSource.insetUserData(userInfoData)
}