package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.profile.RemoteProfileDataSource
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.model.SqlUserLinkData

class ProfileRepository {
    private val _remoteUserDataSource = RemoteProfileDataSource()

    // 유저 정보 불러오기
    suspend fun loadUserData(userIdx: Int?): SqlUserData? = _remoteUserDataSource.loadUserDataByUserIdx(userIdx!!)

    // 자기소개 링크 목록 불러오기
    suspend fun loadUserLinkData(userIdx: Int?): List<SqlUserLinkData> = _remoteUserDataSource.loadUserLinkDataByUserIdx(userIdx!!)

    // 사용자가 진행한 스터디 목록
    suspend fun loadHostStudyList(userIdx: Int?): List<SqlStudyData> = _remoteUserDataSource.loadHostStudyList(userIdx!!)

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록
    suspend fun loadPartStudyList(userIdx: Int?): List<SqlStudyData> = _remoteUserDataSource.loadPartStudyList(userIdx!!)

    // 해당 유저의 전화번호 업데이트
    suspend fun updateUserData(user: SqlUserData?) = _remoteUserDataSource.updateUserData(user!!)

    // 해당 유저의 전화번호 업데이트
    suspend fun updateUserListData(userIdx: Int, linkList: List<String>) = _remoteUserDataSource.updateUserListData(userIdx, linkList)
}