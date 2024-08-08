package kr.co.lion.modigm.db.profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.model.SqlUserLinkData
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

    // 프로필 사진을 Amazon S3에 업로드
    suspend fun uploadProfilePic(uri: Uri, context: Context): String {
        // URI로부터 실제 파일 경로를 얻음
        val filePath = getRealPathFromURI(context, uri) // URI로부터 실제 파일 경로를 얻음
        val file = File(filePath ?: throw IllegalArgumentException("Invalid file: $uri"))

        // 파일 접근 권한 확인 (안드로이드 6.0 이상)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        // AWS 자격 증명
        val accessKey = BuildConfig.BK_ACCESSKEY
        val secretKey = BuildConfig.BK_SECRETKEY
        val bucketName = BuildConfig.BK_NAME
//        val region = "AP_NORTHEAST_2"

        // AWS S3 클라이언트 초기화
        val credentials = BasicAWSCredentials(accessKey, secretKey)
        val s3Client = AmazonS3Client(credentials)

        // 파일 업로드를 위한 TransferUtility 초기화
        val transferUtility = TransferUtility.builder()
            .context(context)
            .awsConfiguration(AWSConfiguration(context))
            .s3Client(s3Client)
            .build()

        // TransferNetworkLossHandler 초기화
        TransferNetworkLossHandler.getInstance(context)

        // 파일명을 현재 시간 기준으로 설정
        val fileName = "${System.currentTimeMillis()}hw.jpg"
        val uploadObserver = transferUtility.upload(
            bucketName,
            fileName,
            file,
            CannedAccessControlList.PublicRead
        )

        // 코루틴을 사용하여 업로드 완료를 기다림
        return suspendCoroutine { continuation ->
            uploadObserver.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state == TransferState.COMPLETED) {
                        // 업로드 완료 시 URL 반환
                        val url = s3Client.getUrl(bucketName, fileName).toString()
                        continuation.resume(url)
                    } else if (state == TransferState.FAILED) {
                        // 업로드 실패 시 예외 발생
                        continuation.resumeWithException(Exception("Upload failed")) // 업로드 실패 시 예외 발생
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    // 업로드 진행 상황 업데이트 (필요 시 구현)
                }

                override fun onError(id: Int, ex: Exception) {
                    // 오류 발생 시 예외 처리
                    continuation.resumeWithException(ex)
                }
            })
        }
    }

    // URI로부터 실제 파일 경로를 얻는 함수
    private fun getRealPathFromURI(context: Context, uri: Uri): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
                Log.d("RemoteProfileDao", "Real path from URI: $path")
            }
        }
        if (path == null) {
            Log.e("RemoteProfileDao", "Failed to get real path from URI: $uri")
        }
        return path
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