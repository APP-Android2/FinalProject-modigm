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
    suspend fun loadUserDataByUserIdx(userIdx: Int): SqlUserData? {
        // 사용자 정보 객체를 담을 변수
        var user: SqlUserData? = null

        try {
            getConnection().use { connection ->
                val query = """
                    SELECT * FROM User
                    WHERE userUid = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, userIdx)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        // 결과를 NewUserData 객체에 매핑
                        user = SqlUserData(
                            userIdx = resultSet.getInt("userIdx"),
                            userName = resultSet.getString("userName"),
                            userPhone = resultSet.getString("userPhone"),
                            userProfilePic = resultSet.getString("userProfilePic"),
                            userIntro = resultSet.getString("userIntro"),
                            userEmail = resultSet.getString("userEmail"),
                            userProvider = resultSet.getString("userProvider"),
                            userInterests = resultSet.getString("userInterestList"),
                        )
                    }
                }
            }
        } catch (error: Exception){
            Log.e("RemoteProfileDao","loadUserDataByUserIdx(): $error")
        }

        return user
    }

    // 사용자 프로필 사진을 받아오는 메서드
    suspend fun loadUserProfilePic(context: Context, imageFileName: String, imageView: ImageView){
        // 이미지가 등록되어 있지 않으면 불러오지 않는다
        if (imageFileName.isNotEmpty()) {
            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // 이미지에 접근할 수 있는 객체를 가져온다.
                val storageRef = Firebase.storage.reference.child("userProfile/$imageFileName")
                // 이미지의 주소를 가지고 있는 Uri 객체를 받아온다.
                val imageUri = storageRef.downloadUrl.await()
                // 이미지 데이터를 받아와 이미지 뷰에 보여준다.
                CoroutineScope(Dispatchers.Main).launch {
                    Glide.with(context).load(imageUri).into(imageView)
                }
            }
            job1.join()
            // 이미지는 용량이 매우 클 수 있다. 즉 이미지 데이터를 내려받는데 시간이 오래걸릴 수도 있다.
            // 이에, 이미지 데이터를 받아와 보여주는 코루틴을 작업이 끝날 때 까지 대기 하지 않는다.
            // 그 이유는 데이터를 받아오는데 걸리는 오랜 시간 동안 화면에 아무것도 나타나지 않을 수 있기 때문이다.
            // 따라서 이 메서드는 제일 마지막에 호출해야 한다.(다른 것들을 모두 보여준 후에...)
        }
    }

    // 사용자 정보를 수정하는 메서드
    suspend fun updateUserData(user: SqlUserData) {
        try {
            getConnection().use { connection ->
                val query = """
                    UPDATE User
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
                    DELETE FROM UserList
                    WHERE userIdx = ?
                """
                connection.prepareStatement(userQuery).use { statement ->
                    statement.setInt(1, userIdx)
                    statement.executeUpdate()
                }

                // 입력된 링크 저장
                linkList.forEachIndexed { index, link ->
                    val insertQuery = """
                    INSERT INTO UserList (userIdx, linkUrl, linkOrder)
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

    // 사용자 프로필 사진을 업로드하고 Firestore에 저장하는 메서드
    suspend fun addProfilePic(newImageUri: Uri, fileName: String, profileFragment: ProfileFragment) {
        try {
            val storageReference = FirebaseStorage.getInstance().reference.child("userProfile/$fileName")
            storageReference.putFile(newImageUri).addOnSuccessListener {
                profileFragment.updateViews()
            }.addOnFailureListener {
                Log.e("RemoteUserDataSource", "addProfilePic(): ${it.message}")
            }
        } catch (e: Exception) {
            Log.e("RemoteUserDataSource", "addProfilePic(): ${e.message}")
        }
    }
}