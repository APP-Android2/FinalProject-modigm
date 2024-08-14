package kr.co.lion.modigm.repository

import android.content.Context
import android.net.Uri
import kr.co.lion.modigm.db.profile.RemoteProfileDataSource
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.model.SqlUserLinkData

class ProfileRepository {
    private val _remoteUserDataSource = RemoteProfileDataSource()

    // 유저 정보 불러오기
    suspend fun loadUserData(userIdx: Int?): SqlUserData? = _remoteUserDataSource.loadUserDataByUserIdx(userIdx!!)

    // 자기소개 링크 목록 불러오기
    suspend fun loadUserLinkData(userIdx: Int?): List<String> = _remoteUserDataSource.loadUserLinkDataByUserIdx(userIdx!!)

    // 사용자가 진행한 스터디 목록 (3개만)
    suspend fun loadSmallHostStudyList(userIdx: Int?): List<SqlStudyData> = _remoteUserDataSource.loadSmallHostStudyList(userIdx!!)

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록 (3개만)
    suspend fun loadSmallPartStudyList(userIdx: Int?): List<SqlStudyData> = _remoteUserDataSource.loadSmallPartStudyList(userIdx!!)

    // 사용자가 진행한 스터디 목록 (전체)
    suspend fun loadHostStudyList(userIdx: Int?): List<SqlStudyData> = _remoteUserDataSource.loadHostStudyList(userIdx!!)

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록 (전체)
    suspend fun loadPartStudyList(userIdx: Int?): List<SqlStudyData> = _remoteUserDataSource.loadPartStudyList(userIdx!!)

    // 사용자 정보 업데이트
    suspend fun updateUserData(user: SqlUserData?) = _remoteUserDataSource.updateUserData(user!!)

    // 사용자 링크 목록 업데이트
    suspend fun updateUserLinkData(userIdx: Int, linkList: List<String>) = _remoteUserDataSource.updateUserLinkData(userIdx, linkList)

    // 프로필 사진을 Amazon S3에 업로드
    suspend fun uploadProfilePic(uri: Uri, context: Context): String = _remoteUserDataSource.uploadProfilePic(uri, context)
}