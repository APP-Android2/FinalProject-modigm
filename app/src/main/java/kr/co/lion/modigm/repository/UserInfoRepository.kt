package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.remote.JoinUserDataSource
import kr.co.lion.modigm.model.UserInfoData

class UserInfoRepository {

    private val _joinUserDataSource = JoinUserDataSource()

    // 회원가입
    suspend fun insetUserData(userInfoData: UserInfoData): Boolean = _joinUserDataSource.insetUserData(userInfoData)
}