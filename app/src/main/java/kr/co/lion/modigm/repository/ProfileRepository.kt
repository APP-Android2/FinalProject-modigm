package kr.co.lion.modigm.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kr.co.lion.modigm.db.profile.RemoteProfileDataSource
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.model.SqlUserLinkData

class ProfileRepository {
    private val _remoteUserDataSource = RemoteProfileDataSource()

    // 유저 정보 불러오기
    suspend fun loadUserData(userIdx: Int?): Flow<SqlUserData?> = flow {
        emit(_remoteUserDataSource.loadUserDataByUserIdx(userIdx!!))
    }.flowOn(Dispatchers.IO)

    // 자기소개 링크 목록 불러오기
    suspend fun loadUserLinkData(userIdx: Int?): Flow<List<String>> = flow {
        emit(_remoteUserDataSource.loadUserLinkDataByUserIdx(userIdx!!))
    }.flowOn(Dispatchers.IO)

    // 사용자가 진행한 스터디 목록 (3개만)
    suspend fun loadSmallHostStudyList(userIdx: Int?): Flow<List<SqlStudyData>> = flow {
        emit(_remoteUserDataSource.loadSmallHostStudyList(userIdx!!))
    }.flowOn(Dispatchers.IO)

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록 (3개만)
    suspend fun loadSmallPartStudyList(userIdx: Int?): Flow<List<SqlStudyData>> = flow {
        emit(_remoteUserDataSource.loadSmallPartStudyList(userIdx!!))
    }.flowOn(Dispatchers.IO)

    // 사용자가 진행한 스터디 목록 (전체)
    suspend fun loadHostStudyList(userIdx: Int?): Flow<List<SqlStudyData>> = flow {
        emit(_remoteUserDataSource.loadHostStudyList(userIdx!!))
    }.flowOn(Dispatchers.IO)

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록 (전체)
    suspend fun loadPartStudyList(userIdx: Int?): Flow<List<SqlStudyData>> = flow {
        emit(_remoteUserDataSource.loadPartStudyList(userIdx!!))
    }.flowOn(Dispatchers.IO)

    // 사용자 정보 업데이트
    suspend fun updateUserData(user: SqlUserData?) = _remoteUserDataSource.updateUserData(user!!)

    // 사용자 링크 목록 업데이트
    suspend fun updateUserLinkData(userIdx: Int, linkList: List<String>) = _remoteUserDataSource.updateUserLinkData(userIdx, linkList)

    // 프로필 사진을 Amazon S3에 업로드
    suspend fun uploadProfilePic(uri: Uri, context: Context): Flow<String> = flow {
        emit(_remoteUserDataSource.uploadProfilePic(uri, context))
    }.flowOn(Dispatchers.IO)
}