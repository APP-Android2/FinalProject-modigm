package kr.co.lion.modigm.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kr.co.lion.modigm.db.profile.RemoteProfileDao
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData

class ProfileRepository {
    private val _remoteProfileDao = RemoteProfileDao()

    // 유저 정보 불러오기
    suspend fun loadUserData(userIdx: Int?): Flow<UserData?> = flow {
        emit(_remoteProfileDao.loadUserDataByUserIdx(userIdx!!))
    }.flowOn(Dispatchers.IO)

    // 자기소개 링크 목록 불러오기
    suspend fun loadUserLinkData(userIdx: Int?): Flow<List<String>> = flow {
        emit(_remoteProfileDao.loadUserLinkDataByUserIdx(userIdx!!))
    }.flowOn(Dispatchers.IO)

    // 사용자가 진행한 스터디 목록 (3개만)
    suspend fun loadSmallHostStudyList(userIdx: Int?): Flow<List<StudyData>> = flow {
        emit(_remoteProfileDao.loadSmallHostStudyList(userIdx!!))
    }.flowOn(Dispatchers.IO)

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록 (3개만)
    suspend fun loadSmallPartStudyList(userIdx: Int?): Flow<List<StudyData>> = flow {
        emit(_remoteProfileDao.loadSmallPartStudyList(userIdx!!))
    }.flowOn(Dispatchers.IO)

    // 사용자가 진행한 스터디 목록 (전체)
    suspend fun loadHostStudyList(userIdx: Int?): Flow<List<StudyData>> = flow {
        emit(_remoteProfileDao.loadHostStudyList(userIdx!!))
    }.flowOn(Dispatchers.IO)

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록 (전체)
    suspend fun loadPartStudyList(userIdx: Int?): Flow<List<StudyData>> = flow {
        emit(_remoteProfileDao.loadPartStudyList(userIdx!!))
    }.flowOn(Dispatchers.IO)

    // 사용자 정보 업데이트
    suspend fun updateUserData(user: UserData?) = _remoteProfileDao.updateUserData(user!!)

    // 사용자 링크 목록 업데이트
    suspend fun updateUserLinkData(userIdx: Int, linkList: List<String>) = _remoteProfileDao.updateUserLinkData(userIdx, linkList)

    // 프로필 사진을 Amazon S3에 업로드
    suspend fun uploadProfilePic(uri: Uri, context: Context): Flow<String> = flow {
        emit(_remoteProfileDao.uploadProfilePic(uri, context))
    }.flowOn(Dispatchers.IO)

    // 회원 탈퇴
    suspend fun deleteUserData(userIdx: Int) = _remoteProfileDao.deleteUserData(userIdx)
}