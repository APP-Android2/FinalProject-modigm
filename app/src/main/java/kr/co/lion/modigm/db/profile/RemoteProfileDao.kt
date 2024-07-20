package kr.co.lion.modigm.db.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.ui.profile.ProfileFragment
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
                        user = SqlUserData(
                            userIdx = resultSet.getInt("userIdx"),
                            userUid = resultSet.getString("userUid"),
                            userName = resultSet.getString("userName"),
                            userPhone = resultSet.getString("userPhone"),
                            userProfilePic = resultSet.getString("userProfilePic"),
                            userIntro = resultSet.getString("userIntro"),
                            userEmail = resultSet.getString("userEmail"),
                            userProvider = resultSet.getString("userProvider"),
                            userInterests = resultSet.getString("userInterests"),
                        )
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

    // 사용자 정보를 수정하는 메서드
    suspend fun updateUserData(user: SqlUserData) {
        try {
            getConnection().use { connection ->
                val query = """
                    UPDATE tb_user
                    SET userProfilePic = ?,
                        userIntro = ?,
                        userEmail = ?,
                        userInterestList = ?
                    WHERE userIdx = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, user.userProfilePic)
                    statement.setString(2, user.userIntro)
                    statement.setString(3, user.userEmail)
                    statement.setString(4, user.userInterests)
                    statement.setInt(5, user.userIdx)

                    statement.executeUpdate()
                }
            }
        } catch (error: Exception) {
            Log.e("RemoteProfileDao", "updateUserData(): $error")
        }
    }

    // 사용자 정보를 수정하는 메서드
    suspend fun updateUserListData(userIdx: Int, linkList: List<String>) {
        try {
            getConnection().use { connection ->
                // 먼저 링크를 모두 삭제
                val userQuery = """
                    DELETE FROM tb_user_list
                    WHERE userIdx = ?
                """
                connection.prepareStatement(userQuery).use { statement ->
                    statement.setInt(1, userIdx)
                    statement.executeUpdate()
                }

                // 입력된 링크 저장
                linkList.forEachIndexed { index, link ->
                    val insertQuery = """
                    INSERT INTO tb_user_list (userIdx, linkUrl, linkOrder)
                    VALUES (?, ?, ?)
                """
                    connection.prepareStatement(insertQuery).use { statement ->
                        statement.setInt(1, userIdx)
                        statement.setString(2, link)
                        statement.setInt(3, index + 1) // Assuming linkOrder is 1-based
                        statement.executeUpdate()
                    }
                }
            }
        } catch (error: Exception) {
            Log.e("RemoteProfileDao", "updateUserData(): $error")
        }
    }
}