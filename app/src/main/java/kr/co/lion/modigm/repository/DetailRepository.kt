package kr.co.lion.modigm.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kr.co.lion.modigm.db.detail.RemoteDetailDataSource
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData

class DetailRepository {
    private val remoteDetailDataSource = RemoteDetailDataSource()

    // 특정 studyIdx에 해당하는 스터디 데이터를 가져오는 메소드
    fun getStudyById(studyIdx: Int): Flow<StudyData?> = flow {
        emit(remoteDetailDataSource.getStudyById(studyIdx))
    }.flowOn(Dispatchers.IO)

    // 특정 studyIdx에 해당하는 스터디 멤버 수를 가져오는 메소드
    fun countMembersByStudyIdx(studyIdx: Int): Flow<Int> = flow {
        emit(remoteDetailDataSource.countMembersByStudyIdx(studyIdx))
    }.flowOn(Dispatchers.IO)

    // 특정 studyIdx에 해당하는 스터디 이미지를 가져오는 메소드
    fun getStudyPicByStudyIdx(studyIdx: Int): Flow<String?> = flow {
        emit(remoteDetailDataSource.getStudyPicByStudyIdx(studyIdx))
    }.flowOn(Dispatchers.IO)

    // 특정 studyIdx에 해당하는 userIdx 리스트를 가져오는 메소드
    suspend fun getUserIdsByStudyIdx(studyIdx: Int): List<Int> {
        return remoteDetailDataSource.getUserIdsByStudyIdx(studyIdx)
    }

    // 특정 userIdx에 해당하는 사용자 데이터를 가져오는 메소드
    fun getUserById(userIdx: Int): Flow<UserData?> = flow {
        emit(remoteDetailDataSource.getUserById(userIdx))
    }

    fun getTechIdxByStudyIdx(studyIdx: Int): Flow<List<Int>> = flow {
        val techList = remoteDetailDataSource.getTechIdxByStudyIdx(studyIdx)
        emit(techList)
    }.flowOn(Dispatchers.IO)

    // studyState 값을 업데이트하는 메소드 추가
    suspend fun updateStudyState(studyIdx: Int, newState: Boolean): Boolean {
        return remoteDetailDataSource.updateStudyState(studyIdx, newState)
    }

    // 스터디 데이터를 업데이트하는 메소드
    suspend fun updateStudy(studyData: StudyData): Boolean {
        return remoteDetailDataSource.updateStudy(studyData)
    }

    // 스킬 데이터를 삽입하는 메서드 추가
    suspend fun insertSkills(studyIdx: Int, skills: List<Int>) {
        remoteDetailDataSource.insertSkills(studyIdx, skills)
    }

    // 특정 studyIdx와 userIdx에 해당하는 사용자를 스터디에서 삭제하는 메소드
    suspend fun removeUserFromStudy(studyIdx: Int, userIdx: Int): Boolean {
        return remoteDetailDataSource.removeUserFromStudy(studyIdx, userIdx)
    }

    suspend fun addUserToStudy(studyIdx: Int, userIdx: Int): Boolean {
        return remoteDetailDataSource.addUserToStudy(studyIdx, userIdx)
    }

    suspend fun addUserToStudyRequest(studyIdx: Int, userIdx: Int): Boolean {
        return remoteDetailDataSource.addUserToStudyRequest(studyIdx, userIdx)
    }

    suspend fun getStudyRequestMembers(studyIdx: Int): List<UserData> {
        return remoteDetailDataSource.getStudyRequestMembers(studyIdx)
    }

    suspend fun acceptUser(studyIdx: Int, userIdx: Int): Boolean {
        val added = remoteDetailDataSource.addUserToStudyMember(studyIdx, userIdx)
        if (added) {
            return remoteDetailDataSource.removeUserFromStudyRequest(studyIdx, userIdx)
        }
        return false
    }

    // 특정 사용자를 tb_study_request에서 삭제하는 메소드
    suspend fun removeUserFromStudyRequest(studyIdx: Int, userIdx: Int): Boolean {
        return remoteDetailDataSource.removeUserFromStudyRequest(studyIdx, userIdx)
    }

    suspend fun updateStudyCanApplyField(studyIdx: Int, newState: String): Boolean {
        return remoteDetailDataSource.updateStudyCanApplyField(studyIdx, newState)
    }
}