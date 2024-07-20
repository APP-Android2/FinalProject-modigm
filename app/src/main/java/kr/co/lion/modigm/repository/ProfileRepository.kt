package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.profile.RemoteProfileDataSource
import kr.co.lion.modigm.model.SqlUserData

class ProfileRepository {
    private val _remoteUserDataSource = RemoteProfileDataSource()

    // 유저 정보 불러오기
    suspend fun loadUserData(userIdx: Int?): SqlUserData? = _remoteUserDataSource.loadUserDataByUserIdx(userIdx!!)

    // 해당 유저의 전화번호 업데이트
    suspend fun updateUserData(user: SqlUserData?) = _remoteUserDataSource.updateUserData(user!!)

    // 해당 유저의 전화번호 업데이트
    suspend fun updateUserListData(userIdx: Int, linkList: List<String>) = _remoteUserDataSource.updateUserListData(userIdx, linkList)
}