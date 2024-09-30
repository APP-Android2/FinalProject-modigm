package kr.co.lion.modigm.db.detail

import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.db.HikariCPDataSource
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData
import java.sql.PreparedStatement
import java.sql.ResultSet

class RemoteDetailDao {
    private val TAG = "SqlRemoteDetailDao"

    // 쿼리를 실행하고 결과를 처리하는 공통 메소드
    suspend fun <T> executeQuery(query: String, vararg params: Any, block: (ResultSet) -> T?): List<T> = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<T>() // 결과를 저장할 리스트
            HikariCPDataSource.getConnection().use { connection -> // 데이터베이스 연결을 가져와 사용
                Log.d(TAG, "Executing query: $query with params: ${params.joinToString()}") // 쿼리 실행 전 로그
                connection.prepareStatement(query).use { statement -> // 쿼리 준비
                    // 쿼리 매개변수 설정
                    params.forEachIndexed { index, param ->
                        statement.setObject(index + 1, param)
                    }
                    // 쿼리 실행 및 결과 처리
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) { // 결과셋을 순회하며 결과를 처리
                        block(resultSet)?.let { results.add(it) } // 결과를 리스트에 추가
                    }
                }
            }
            Log.d(TAG, "Query executed successfully: $query with results: $results") // 쿼리 성공 로그
            results // 결과 리스트 반환
        } catch (e: Exception) {
            Log.e(TAG, "쿼리 실행 중 오류 발생", e) // 오류 로그
            emptyList() // 오류 발생 시 빈 리스트 반환
        }
    }

    // 스터디 데이터를 모두 가져오는 메소드
    suspend fun getAllStudies(): List<StudyData> {
        val query = "SELECT * FROM tb_study" // 쿼리문
        return executeQuery(query) { resultSet ->
            StudyData.getStudyData(resultSet) // 결과셋에서 스터디 데이터를 가져오는 메소드 호출
        }
    }

    // 모든 스터디 멤버 수를 가져오는 메소드
    suspend fun getAllStudyMembers(): List<Pair<Int, Int>> {
        val query = "SELECT studyIdx, COUNT(userIdx) AS memberCount FROM tb_study_member GROUP BY studyIdx" // 쿼리문
        return executeQuery(query) { resultSet ->
            Pair(resultSet.getInt("studyIdx"), resultSet.getInt("memberCount")) // 스터디 ID와 멤버 수를 쌍으로 반환
        }
    }

    // 특정 studyIdx에 해당하는 userIdx 리스트를 가져오는 메소드
    suspend fun getUserIdsByStudyIdx(studyIdx: Int): List<Int> {
        val query = "SELECT userIdx FROM tb_study_member WHERE studyIdx = ?"
        return executeQuery(query, studyIdx) { resultSet ->
            resultSet.getInt("userIdx")
        }
    }

    // 특정 사용자의 모든 좋아요 정보를 가져오는 메소드
    suspend fun getAllFavorites(userIdx: Int): List<Pair<Int, Boolean>> {
        val query = "SELECT studyIdx, IF(favoriteIdx IS NOT NULL, TRUE, FALSE) as isFavorite FROM tb_favorite WHERE userIdx = ?" // 쿼리문
        return executeQuery(query, userIdx) { resultSet ->
            Pair(resultSet.getInt("studyIdx"), resultSet.getBoolean("isFavorite")) // 스터디 ID와 좋아요 여부를 쌍으로 반환
        }
    }

    // 모든 사용자 데이터를 가져오는 메소드
    suspend fun getAllUsers(): List<UserData> {
        val query = "SELECT * FROM tb_user" // 쿼리문
        return executeQuery(query) { resultSet ->
            UserData.getUserData(resultSet) // 결과셋에서 사용자 데이터를 가져오는 메소드 호출
        }
    }

    // StudyTechStack 테이블의 모든 데이터를 가져오는 메소드
    suspend fun getAllStudyTechStack(): List<Pair<Int, Int>> {
        val query = "SELECT studyIdx, techIdx FROM tb_study_tech_stack" // 쿼리문
        return executeQuery(query) { resultSet ->
            Pair(resultSet.getInt("studyIdx"), resultSet.getInt("techIdx")) // studyIdx와 techIdx를 쌍으로 반환
        }
    }

    // 특정 스터디의 기술 스택을 조회하는 메소드
    suspend fun getStudyTechStack(studyIdx: Int): List<Int> {
        val query = "SELECT techIdx FROM tb_study_tech_stack WHERE studyIdx = ?"
        return executeQuery(query, studyIdx) { resultSet ->
            resultSet.getInt("techIdx")
        }
    }

    // studyState 값을 업데이트하는 메소드 추가
    suspend fun updateStudyState(studyIdx: Int, newState: Boolean): Int = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "UPDATE tb_study SET studyState = ? WHERE studyIdx = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, if (newState) 1 else 0)
                    statement.setInt(2, studyIdx)
                    return@withContext statement.executeUpdate()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating studyState", e)
            return@withContext 0
        }
    }

    // 데이터 업데이트 메소드
    suspend fun updateStudy(studyData: StudyData): Int = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = """
                    UPDATE tb_study SET 
                        studyTitle = ?, 
                        studyContent = ?, 
                        studyType = ?, 
                        studyPeriod = ?, 
                        studyOnOffline = ?, 
                        studyPlace = ?, 
                        studyDetailPlace = ?, 
                        studyApplyMethod = ?, 
                        studyCanApply = ?, 
                        studyPic = ?, 
                        studyMaxMember = ?, 
                        studyState = ?, 
                        studyChatLink = ?, 
                        userIdx = ? 
                    WHERE studyIdx = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, studyData.studyTitle)
                    statement.setString(2, studyData.studyContent)
                    statement.setString(3, studyData.studyType)
                    statement.setString(4, studyData.studyPeriod)
                    statement.setString(5, studyData.studyOnOffline)
                    statement.setString(6, studyData.studyPlace)
                    statement.setString(7, studyData.studyDetailPlace)
                    statement.setString(8, studyData.studyApplyMethod)
                    statement.setString(9, studyData.studyCanApply)
                    statement.setString(10, studyData.studyPic)
                    statement.setInt(11, studyData.studyMaxMember)
                    statement.setBoolean(12, studyData.studyState)
                    statement.setString(13, studyData.studyChatLink) // 새 필드 설정
                    statement.setInt(14, studyData.userIdx)
                    statement.setInt(15, studyData.studyIdx)
                    return@withContext statement.executeUpdate()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating study data", e)
            return@withContext 0
        }
    }

    // 스터디 테이블에 tech stack 데이터를 삽입하는 메서드
    suspend fun insertStudyTechStack(studyIdx: Int, techStack: List<Int>) {
        val existingTechStack = getStudyTechStack(studyIdx).toSet()
        val newTechStack = techStack.filter { it !in existingTechStack }

        if (newTechStack.isNotEmpty()) {
            val sql = "INSERT INTO tb_study_tech_stack (studyIdx, techIdx) VALUES (?, ?)"
            val paramsList = newTechStack.map { arrayOf<Any>(studyIdx, it) }
            executeBatchUpdate(sql, paramsList)
        }
    }

    // 여러 개의 SQL 업데이트를 한 번에 실행하는 배치 업데이트 함수
    private suspend fun executeBatchUpdate(sql: String, paramsList: List<Array<out Any>>) {
        var preparedStatement: PreparedStatement? = null
        try {
            withContext(Dispatchers.IO) {
                HikariCPDataSource.getConnection().use { connection ->
                    preparedStatement = connection.prepareStatement(sql)
                    paramsList.forEach { params ->
                        params.forEachIndexed { index, value ->
                            when (value) {
                                is String -> preparedStatement?.setString(index + 1, value)
                                is Int -> preparedStatement?.setInt(index + 1, value)
                            }
                        }
                        preparedStatement?.addBatch()
                    }
                    preparedStatement?.executeBatch()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in executeBatchUpdate", e)
        } finally {
            preparedStatement?.close()
        }
    }

    // 특정 studyIdx와 userIdx에 해당하는 사용자를 tb_study_member 테이블에서 삭제하는 메소드
    suspend fun removeUserFromStudy(studyIdx: Int, userIdx: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "DELETE FROM tb_study_member WHERE studyIdx = ? AND userIdx = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, studyIdx)
                    statement.setInt(2, userIdx)
                    return@withContext statement.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing user from study", e)
            return@withContext false
        }
    }

    // tb_study_member 테이블에 데이터 삽입
    suspend fun addUserToStudy(studyIdx: Int, userIdx: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "INSERT INTO tb_study_member (studyIdx, userIdx) VALUES (?, ?)"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, studyIdx)
                    statement.setInt(2, userIdx)
                    return@withContext statement.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding user to study", e)
            return@withContext false
        }
    }

    // tb_study_request 테이블에 데이터 삽입
    suspend fun addUserToStudyRequest(studyIdx: Int, userIdx: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "INSERT INTO tb_study_request (studyIdx, userIdx) VALUES (?, ?)"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, studyIdx)
                    statement.setInt(2, userIdx)
                    return@withContext statement.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding user to study request", e)
            return@withContext false
        }
    }

    // 특정 studyIdx에 해당하는 신청자 정보를 tb_study_request 에서 가져오는 메소드
    suspend fun getStudyRequestMembers(studyIdx: Int): List<UserData> = withContext(Dispatchers.IO) {
        try {
            val members = mutableListOf<UserData>()
            HikariCPDataSource.getConnection().use { connection ->
                val query = """
                    SELECT u.* FROM tb_study_request sr
                    JOIN tb_user u ON sr.userIdx = u.userIdx
                    WHERE sr.studyIdx = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, studyIdx)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        members.add(UserData.getUserData(resultSet))
                    }
                }
            }
            return@withContext members
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching study request members", e)
            return@withContext emptyList<UserData>()
        }
    }

    // 사용자를 tb_study_member에 추가하는 메서드
    suspend fun addUserToStudyMember(studyIdx: Int, userIdx: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "INSERT INTO tb_study_member (studyIdx, userIdx) VALUES (?, ?)"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, studyIdx)
                    statement.setInt(2, userIdx)
                    return@withContext statement.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding user to study member", e)
            return@withContext false
        }
    }

    // 사용자를 tb_study_request에서 삭제하는 메서드
    suspend fun removeUserFromStudyRequest(studyIdx: Int, userIdx: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "DELETE FROM tb_study_request WHERE studyIdx = ? AND userIdx = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, studyIdx)
                    statement.setInt(2, userIdx)
                    return@withContext statement.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing user from study request", e)
            return@withContext false
        }
    }

    suspend fun updateStudyCanApplyField(studyIdx: Int, newState: String): Int = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "UPDATE tb_study SET studyCanApply = ? WHERE studyIdx = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, newState)
                    statement.setInt(2, studyIdx)
                    return@withContext statement.executeUpdate()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating studyCanApply field", e)
            return@withContext 0
        }
    }

    // RemoteDetailDao에 이미 참여 중인지 확인하는 메서드 추가
    suspend fun isUserAlreadyMember(studyIdx: Int, userIdx: Int): Flow<Boolean> = flow {
        val query = "SELECT COUNT(*) FROM tb_study_member WHERE studyIdx = ? AND userIdx = ?"
        var isMember = false
        withContext(Dispatchers.IO) {
            try {
                HikariCPDataSource.getConnection().use { connection ->
                    connection.prepareStatement(query).use { statement ->
                        statement.setInt(1, studyIdx)
                        statement.setInt(2, userIdx)
                        val resultSet = statement.executeQuery()
                        if (resultSet.next()) {
                            isMember = resultSet.getInt(1) > 0 // 사용자가 스터디에 참여 중인지 확인
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RemoteDetailDao", "Error checking if user is already a member", e)
            }
        }
        emit(isMember) // 결과를 Flow로 반환

    }


    // 사용자 FCM 토큰을 가져오는 메서드
    suspend fun getUserFcmToken(userIdx: Int): String? = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "SELECT fcmToken FROM tb_user_fcm WHERE userIdx = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, userIdx)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        return@withContext resultSet.getString("fcmToken")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RemoteDetailDao", "Error fetching FCM token", e)
        }
        return@withContext null
    }


    // 알림 데이터를 데이터베이스에 삽입하는 메서드
    suspend fun insertNotification(userIdx: Int, title: String, content: String, coverPhotoUrl: String, studyIdx: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "INSERT INTO tb_notification (userIdx, notificationTitle, notificationContent, cover_photo_url, studyIdx) VALUES (?, ?, ?, ?, ?)"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, userIdx)
                    statement.setString(2, title)
                    statement.setString(3, content)
                    statement.setString(4, coverPhotoUrl)
                    statement.setInt(5, studyIdx) // studyIdx 저장
                    val rowsAffected = statement.executeUpdate()
                    Log.d(TAG, "insertNotification: rowsAffected=$rowsAffected")
                    return@withContext rowsAffected > 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting notification", e)
            return@withContext false
        }
    }



    // 사용자 FCM 토큰을 삽입하거나 업데이트하는 메서드
    suspend fun insertUserFcmToken(userIdx: Int, fcmToken: String): Boolean = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                Log.d("RemoteDetailDao", "Preparing to insert FCM token for userIdx: $userIdx with token: $fcmToken")
                val query = """
                INSERT INTO tb_user_fcm (userIdx, fcmToken)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE fcmToken = VALUES(fcmToken)
            """
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, userIdx)
                    statement.setString(2, fcmToken)
                    val rowsUpdated = statement.executeUpdate()
                    Log.d("RemoteDetailDao", "Rows updated/inserted: $rowsUpdated")
                    return@withContext rowsUpdated > 0
                }
            }
        } catch (e: Exception) {
            Log.e("RemoteDetailDao", "Error inserting FCM token", e)
            return@withContext false
        }
    }

    // 특정 사용자가 이미 스터디에 신청했는지 확인하는 메서드
    suspend fun checkExistingApplication(userIdx: Int, studyIdx: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "SELECT COUNT(*) AS count FROM tb_study_request WHERE userIdx = ? AND studyIdx = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, userIdx)
                    statement.setInt(2, studyIdx)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        return@withContext resultSet.getInt("count") > 0
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking existing application", e)
        }
        return@withContext false
    }

    // studyPic 업데이트하는 메서드
    suspend fun updateStudyPic(studyIdx: Int, imageUrl: String): Int = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "UPDATE tb_study SET studyPic = ? WHERE studyIdx = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, imageUrl)
                    statement.setInt(2, studyIdx)
                    return@withContext statement.executeUpdate()
                }
            }
        } catch (e: Exception) {
            Log.e("RemoteDetailDao", "Error updating studyPic", e)
            return@withContext 0
        }
    }

    // S3에 저장된 이미지를 삭제하는 메서드
    suspend fun deleteImageFromS3(fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // AWS 자격 증명 (BuildConfig에서 관리)
            val accessKey = BuildConfig.BK_ACCESSKEY
            val secretKey = BuildConfig.BK_SECRETKEY
            val bucketName = BuildConfig.BK_NAME

            // AWS S3 클라이언트 초기화
            val awsCredentials = BasicAWSCredentials(accessKey, secretKey)
            val s3Client = AmazonS3Client(awsCredentials)

            // 파일 경로에서 실제 파일 이름만 추출 (URL로부터 파일 경로 추출)
            val fileKey = fileName.substringAfterLast("/") // 파일명 추출 (URL이든 경로든 마지막 슬래시 이후 파일명만 추출)

            // S3에서 파일 삭제
            s3Client.deleteObject(bucketName, fileKey)
            Log.d("RemoteDetailDao", "Image deleted successfully from S3: $fileKey")
            return@withContext true
        } catch (e: Exception) {
            Log.e("RemoteDetailDao", "Error deleting image from S3: ${e.message}")
            return@withContext false
        }
    }

}