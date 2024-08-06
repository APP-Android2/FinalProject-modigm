package kr.co.lion.modigm.db.profile

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.model.SqlUserLinkData
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class RemoteProfileDao {
    private val dbUrl = BuildConfig.DB_URL
    private val dbUser = BuildConfig.DB_USER
    private val dbPassword = BuildConfig.DB_PASSWORD

    // 데이터베이스 연결 생성
    private suspend fun getConnection(): Connection = withContext(Dispatchers.IO) {
        Class.forName("com.mysql.jdbc.Driver") // 최신 드라이버 클래스명 사용
        DriverManager.getConnection(dbUrl, dbUser, dbPassword)
    }

    // userIdx를 통해 사용자 정보를 가져오는 메서드
    suspend fun loadUserDataByUserIdx(userIdx: Int): SqlUserData? = withContext(Dispatchers.IO)  {
        // 사용자 정보 객체를 담을 변수
        var user: SqlUserData? = null

        try {
            getConnection().use { connection ->
                val query = """
                    SELECT * FROM tb_user
                    WHERE userIdx = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, userIdx)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        // 결과를 NewUserData 객체에 매핑
                        user = SqlUserData.getUserData(resultSet)
                    }
                }
            }
        } catch (sqlError: SQLException) {
            Log.e("RemoteProfileDao", "loadUserDataByUserIdx(): SQL error - $sqlError")
        } catch (error: Exception) {
            Log.e("RemoteProfileDao", "loadUserDataByUserIdx(): General error - $error")
        }

        return@withContext user
    }

    // userIdx를 통해 등록된 링크 목록을 가져오는 메서드
    suspend fun loadUserLinkDataByUserIdx(userIdx: Int): List<String> = withContext(Dispatchers.IO) {
        val linkList = mutableListOf<SqlUserLinkData>()

        try {
            getConnection().use { connection ->
                val query = """
                    SELECT * FROM tb_user_link
                    WHERE userIdx = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, userIdx)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val link = SqlUserLinkData.getUserLinkData(resultSet)

                        linkList.add(link)
                    }
                }
            }
        } catch (sqlError: SQLException) {
            Log.e("RemoteProfileDao", "loadUserLinkDataByUserIdx(): SQL error - $sqlError")
        } catch (error: Exception) {
            Log.e("RemoteProfileDao", "loadUserLinkDataByUserIdx(): General error - $error")
        }

        return@withContext linkList.sortedBy { it.linkOrder }.map { it.linkUrl }
    }

    // 사용자 정보를 수정하는 메서드
    suspend fun updateUserData(user: SqlUserData) = withContext(Dispatchers.IO) {
        try {
            getConnection().use { connection ->
                val query = """
                    UPDATE tb_user
                    SET userProfilePic = ?,
                        userIntro = ?,
                        userInterests = ?
                    WHERE userIdx = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, user.userProfilePic)
                    statement.setString(2, user.userIntro)
                    statement.setString(3, user.userInterests)
                    statement.setInt(4, user.userIdx)

                    statement.executeUpdate()
                }
            }
        } catch (error: Exception) {
            Log.e("RemoteProfileDao", "updateUserData(): $error")
        }
    }

    // 사용자 링크 목록 정보를 수정하는 메서드
    suspend fun updateUserLinkData(userIdx: Int, linkList: List<String>) = withContext(Dispatchers.IO) {
        try {
            getConnection().use { connection ->
                val userQuery = """
                    DELETE FROM tb_user_link
                    WHERE userIdx = ?
                """
                connection.prepareStatement(userQuery).use { statement ->
                    statement.setInt(1, userIdx)
                    statement.executeUpdate()
                }

                // 입력된 링크 저장
                linkList.forEachIndexed { index, link ->
                    val insertQuery = """
                    INSERT INTO tb_user_link (userIdx, linkUrl, linkOrder)
                    VALUES (?, ?, ?)
                """
                    connection.prepareStatement(insertQuery).use { statement ->
                        statement.setInt(1, userIdx)
                        statement.setString(2, link)
                        statement.setInt(3, index) // Assuming linkOrder is 1-based
                        statement.executeUpdate()
                    }
                }
            }
        } catch (error: Exception) {
            Log.e("RemoteProfileDao", "updateUserLinkData(): $error")
        }
    }

    // 사용자가 진행한 스터디 목록 (3개만)
    suspend fun loadSmallHostStudyList(userIdx: Int): List<SqlStudyData> = withContext(Dispatchers.IO) {
        val studyList = mutableListOf<SqlStudyData>()

        try {
            getConnection().use { connection ->
                val query = """
                    SELECT * FROM tb_study
                    WHERE userIdx = ?
                    AND studyState = ?
                    ORDER BY studyIdx DESC
                    LIMIT 3
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, userIdx)
                    statement.setBoolean(2, true)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val study = SqlStudyData.getStudyData(resultSet)

                        studyList.add(study)
                    }
                }
            }
        } catch (error: Exception) {
            Log.e("RemoteProfileDao", "loadHostStudyList(): $error")
        }

        return@withContext studyList
    }

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록 (3개만)
    suspend fun loadSmallPartStudyList(userIdx: Int): List<SqlStudyData> = withContext(Dispatchers.IO) {
        val studyList = mutableListOf<SqlStudyData>()

        try {
            getConnection().use { connection ->
                val query = """
                    SELECT s.*
                    FROM tb_study s
                    JOIN tb_study_member sm ON s.studyIdx = sm.studyIdx
                    WHERE sm.userIdx = ?
                    AND s.userIdx != ?
                    AND s.studyState = ?
                    ORDER BY studyIdx DESC
                    LIMIT 3
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, userIdx)
                    statement.setInt(2, userIdx)
                    statement.setBoolean(3, true)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val study = SqlStudyData.getStudyData(resultSet)

                        studyList.add(study)
                    }
                }
            }
        } catch (error: Exception) {
            Log.e("RemoteProfileDao", "loadHostStudyList(): $error")
        }

        return@withContext studyList
    }

    // 사용자가 진행한 스터디 목록 (전체)
    suspend fun loadHostStudyList(userIdx: Int): List<SqlStudyData> = withContext(Dispatchers.IO) {
        val studyList = mutableListOf<SqlStudyData>()

        try {
            getConnection().use { connection ->
                val query = """
                    SELECT * FROM tb_study
                    WHERE userIdx = ?
                    AND studyState = ?
                    ORDER BY studyIdx DESC
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, userIdx)
                    statement.setBoolean(2, true)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val study = SqlStudyData.getStudyData(resultSet)

                        studyList.add(study)
                    }
                }
            }
        } catch (error: Exception) {
            Log.e("RemoteProfileDao", "loadHostStudyList(): $error")
        }

        return@withContext studyList
    }

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록 (전체)
    suspend fun loadPartStudyList(userIdx: Int): List<SqlStudyData> = withContext(Dispatchers.IO) {
        val studyList = mutableListOf<SqlStudyData>()

        try {
            getConnection().use { connection ->
                val query = """
                    SELECT s.*
                    FROM tb_study s
                    JOIN tb_study_member sm ON s.studyIdx = sm.studyIdx
                    WHERE sm.userIdx = ?
                    AND s.userIdx != ?
                    AND s.studyState = ?
                    ORDER BY studyIdx DESC
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, userIdx)
                    statement.setInt(2, userIdx)
                    statement.setBoolean(3, true)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val study = SqlStudyData.getStudyData(resultSet)

                        studyList.add(study)
                    }
                }
            }
        } catch (error: Exception) {
            Log.e("RemoteProfileDao", "loadHostStudyList(): $error")
        }

        return@withContext studyList
    }
}