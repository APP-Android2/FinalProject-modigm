package kr.co.lion.modigm.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kr.co.lion.modigm.db.study.SqlRemoteDetailDataSource
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.SqlUserData

class SqlDetailRepository {
    private val sqlRemoteDetailDataSource = SqlRemoteDetailDataSource()

    // 특정 studyIdx에 해당하는 스터디 데이터를 가져오는 메소드
    fun getStudyById(studyIdx: Int): Flow<SqlStudyData?> = flow {
        emit(sqlRemoteDetailDataSource.getStudyById(studyIdx))
    }

    // 특정 studyIdx에 해당하는 스터디 멤버 수를 가져오는 메소드
    fun countMembersByStudyIdx(studyIdx: Int): Flow<Int> = flow {
        emit(sqlRemoteDetailDataSource.countMembersByStudyIdx(studyIdx))
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
}