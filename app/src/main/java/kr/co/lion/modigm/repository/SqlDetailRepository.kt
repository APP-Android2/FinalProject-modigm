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

    // 리소스를 해제하는 메서드 추가
    suspend fun close() {
        sqlRemoteDetailDataSource.close()
    }
}