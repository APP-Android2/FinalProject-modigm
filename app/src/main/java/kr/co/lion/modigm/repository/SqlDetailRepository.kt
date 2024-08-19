package kr.co.lion.modigm.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kr.co.lion.modigm.db.detail.SqlRemoteDetailDataSource
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.SqlUserData

class SqlDetailRepository {
    private val sqlRemoteDetailDataSource = SqlRemoteDetailDataSource()

    // 특정 studyIdx에 해당하는 스터디 데이터를 가져오는 메소드
    fun getStudyById(studyIdx: Int): Flow<SqlStudyData?> = flow {
        emit(sqlRemoteDetailDataSource.getStudyById(studyIdx))
    }.flowOn(Dispatchers.IO)

    // 특정 studyIdx에 해당하는 스터디 멤버 수를 가져오는 메소드
    fun countMembersByStudyIdx(studyIdx: Int): Flow<Int> = flow {
        emit(sqlRemoteDetailDataSource.countMembersByStudyIdx(studyIdx))
    }.flowOn(Dispatchers.IO)

    // 특정 studyIdx에 해당하는 스터디 이미지를 가져오는 메소드
    fun getStudyPicByStudyIdx(studyIdx: Int): Flow<String?> = flow {
        emit(sqlRemoteDetailDataSource.getStudyPicByStudyIdx(studyIdx))
    }.flowOn(Dispatchers.IO)

    // 특정 studyIdx에 해당하는 userIdx 리스트를 가져오는 메소드
    suspend fun getUserIdsByStudyIdx(studyIdx: Int): List<Int> {
        return sqlRemoteDetailDataSource.getUserIdsByStudyIdx(studyIdx)
    }

    // 특정 userIdx에 해당하는 사용자 데이터를 가져오는 메소드
    fun getUserById(userIdx: Int): Flow<SqlUserData?> = flow {
        emit(sqlRemoteDetailDataSource.getUserById(userIdx))
    }

    fun getTechIdxByStudyIdx(studyIdx: Int): Flow<List<Int>> = flow {
        val techList = sqlRemoteDetailDataSource.getTechIdxByStudyIdx(studyIdx)
        emit(techList)
    }.flowOn(Dispatchers.IO)

    // studyState 값을 업데이트하는 메소드 추가
    suspend fun updateStudyState(studyIdx: Int, newState: Int): Boolean {
        return sqlRemoteDetailDataSource.updateStudyState(studyIdx, newState)
    }

    // 스터디 데이터를 업데이트하는 메소드
    suspend fun updateStudy(studyData: SqlStudyData): Boolean {
        return sqlRemoteDetailDataSource.updateStudy(studyData)
    }

    // 스킬 데이터를 삽입하는 메서드 추가
    suspend fun insertSkills(studyIdx: Int, skills: List<Int>) {
        sqlRemoteDetailDataSource.insertSkills(studyIdx, skills)
    }

    // 특정 studyIdx와 userIdx에 해당하는 사용자를 스터디에서 삭제하는 메소드
    suspend fun removeUserFromStudy(studyIdx: Int, userIdx: Int): Boolean {
        return sqlRemoteDetailDataSource.removeUserFromStudy(studyIdx, userIdx)
    }

    suspend fun addUserToStudy(studyIdx: Int, userIdx: Int): Boolean {
        return sqlRemoteDetailDataSource.addUserToStudy(studyIdx, userIdx)
    }

    suspend fun addUserToStudyRequest(studyIdx: Int, userIdx: Int): Boolean {
        return sqlRemoteDetailDataSource.addUserToStudyRequest(studyIdx, userIdx)
    }

}