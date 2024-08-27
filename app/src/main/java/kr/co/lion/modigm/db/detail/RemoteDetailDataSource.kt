package kr.co.lion.modigm.db.detail

import android.util.Log
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.SqlUserData

class RemoteDetailDataSource {
    private val TAG = "SqlRemoteDetailDataSource"
    private val studyDao = RemoteDetailDao()

    // 특정 studyIdx에 해당하는 스터디 데이터를 가져오는 메소드
    suspend fun getStudyById(studyIdx: Int): SqlStudyData? {
        return try {
            val studies = studyDao.getAllStudies()
            studies.find { it.studyIdx == studyIdx }
        } catch (e: Exception) {
            Log.e("RemoteStudyDataSource Error", "Error getStudyById: ${e.message}")
            null
        }
    }

    // 특정 studyIdx에 해당하는 스터디 멤버 수를 가져오는 메소드
    suspend fun countMembersByStudyIdx(studyIdx: Int): Int {
        return try {
            val studyMembers = studyDao.getAllStudyMembers()
            studyMembers.find { it.first == studyIdx }?.second ?: 0
        } catch (e: Exception) {
            Log.e("RemoteStudyDataSource Error", "Error countMembersByStudyIdx: ${e.message}")
            0
        }
    }

    // 특정 studyIdx에 해당하는 userIdx 리스트를 가져오는 메소드
    suspend fun getUserIdsByStudyIdx(studyIdx: Int): List<Int> {
        return studyDao.getUserIdsByStudyIdx(studyIdx)
    }

    // studyIdx에 해당하는 studyPic을 반환하는 메소드
    suspend fun getStudyPicByStudyIdx(studyIdx: Int): String? {
        return try {
            val studies = studyDao.getAllStudies()
            studies.find { it.studyIdx == studyIdx }?.studyPic
        } catch (e: Exception) {
            Log.e(TAG, "Error getStudyPicByStudyIdx: ${e.message}")
            null
        }
    }

    // 모든 스터디 데이터를 가져와서 상세 정보를 포함한 리스트로 반환하는 메소드
    suspend fun getAllStudyDetails(userIdx: Int): List<Triple<SqlStudyData, Int, Boolean>> {
        return try {
            val studies = studyDao.getAllStudies()
            val studyMembers = studyDao.getAllStudyMembers().toMap()
            val favorites = studyDao.getAllFavorites(userIdx).toMap()

            studies.map { study ->
                Triple(
                    study,
                    studyMembers[study.studyIdx] ?: 0,
                    favorites[study.studyIdx] ?: false
                )
            }
        } catch (e: Exception) {
            Log.e("RemoteStudyDataSource Error", "Error getAllStudyDetails: ${e.message}")
            emptyList()
        }
    }

    // 특정 userIdx에 해당하는 사용자 데이터를 가져오는 메소드
    suspend fun getUserById(userIdx: Int): SqlUserData? {
        return try {
            val users = studyDao.getAllUsers()
            Log.d("RemoteStudyDataSource", "Fetched users: $users")
            users.find { it.userIdx == userIdx }
        } catch (e: Exception) {
            Log.e("RemoteStudyDataSource Error", "Error getUserById: ${e.message}")
            null
        }
    }

    // 특정 studyIdx에 해당하는 techIdx 리스트를 가져오는 메소드
    suspend fun getTechIdxByStudyIdx(studyIdx: Int): List<Int> {
        return try {
            val studyTechStack = studyDao.getAllStudyTechStack()
            // studyIdx에 해당하는 techIdx 리스트 필터링
            studyTechStack.filter { it.first == studyIdx }.map { it.second }
        } catch (e: Exception) {
            Log.e("RemoteStudyDataSource Error", "Error getTechIdxByStudyIdx: ${e.message}")
            emptyList()
        }
    }

    // studyState 값을 업데이트하는 메소드 추가
    suspend fun updateStudyState(studyIdx: Int, newState: Boolean): Boolean {
        return try {
            studyDao.updateStudyState(studyIdx, newState) > 0
        } catch (e: Exception) {
            Log.e("RemoteStudyDataSource Error", "Error updateStudyState: ${e.message}")
            false
        }
    }

    // 스터디 데이터를 업데이트하는 메소드
    suspend fun updateStudy(studyData: SqlStudyData): Boolean {
        return try {
            studyDao.updateStudy(studyData) > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error updating study data", e)
            false
        }
    }

    // 스킬 데이터를 삽입하는 메서드 추가
    suspend fun insertSkills(studyIdx: Int, skills: List<Int>) {
        try {
            studyDao.insertStudyTechStack(studyIdx, skills)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting skills", e)
        }
    }

    // 스터디에서 특정 사용자를 삭제하는 메소드
    suspend fun removeUserFromStudy(studyIdx: Int, userIdx: Int): Boolean {
        return try {
            studyDao.removeUserFromStudy(studyIdx, userIdx)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing user from study", e)
            false
        }
    }

    suspend fun addUserToStudy(studyIdx: Int, userIdx: Int): Boolean {
        return studyDao.addUserToStudy(studyIdx, userIdx)
    }

    suspend fun addUserToStudyRequest(studyIdx: Int, userIdx: Int): Boolean {
        return studyDao.addUserToStudyRequest(studyIdx, userIdx)
    }

    suspend fun getStudyRequestMembers(studyIdx: Int): List<SqlUserData> {
        return studyDao.getStudyRequestMembers(studyIdx)
    }

    suspend fun addUserToStudyMember(studyIdx: Int, userIdx: Int): Boolean {
        return studyDao.addUserToStudyMember(studyIdx, userIdx)
    }

    suspend fun removeUserFromStudyRequest(studyIdx: Int, userIdx: Int): Boolean {
        return studyDao.removeUserFromStudyRequest(studyIdx, userIdx)
    }

    suspend fun updateStudyCanApplyField(studyIdx: Int, newState: String): Boolean {
        return try {
            studyDao.updateStudyCanApplyField(studyIdx, newState) > 0
        } catch (e: Exception) {
            Log.e("RemoteDetailDataSource Error", "Error updateStudyCanApplyField: ${e.message}")
            false
        }
    }

}