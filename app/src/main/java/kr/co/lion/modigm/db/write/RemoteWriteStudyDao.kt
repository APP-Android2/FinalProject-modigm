package kr.co.lion.modigm.db.write

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.db.HikariCPDataSource
import java.io.File
import java.sql.PreparedStatement
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RemoteWriteStudyDao {
    private val TAG = "RemoteWriteStudyDao"
    private var context: Context? = null

    // Context 설정
    fun setContext(context: Context) {
        this.context = context
    }

    // 이미지 업로드 함수 (Amazon S3에 업로드)
    suspend fun uploadImageToS3(context: Context, uri: Uri): String {
        // URI로부터 실제 파일 경로를 얻음
        val filePath = getRealPathFromURI(context, uri) // URI로부터 실제 파일 경로를 얻음
        val file = File(filePath ?: throw IllegalArgumentException("Invalid file: $uri"))

        // AWS 자격 증명
        val accessKey = BuildConfig.BK_ACCESSKEY
        val secretKey = BuildConfig.BK_SECRETKEY
        val bucketName = BuildConfig.BK_NAME

        // AWS S3 클라이언트 초기화
        val credentials = BasicAWSCredentials(accessKey, secretKey)
        val s3Client = AmazonS3Client(credentials)

        // 파일 업로드를 위한 TransferUtility 초기화
        val transferUtility = TransferUtility.builder()
            .context(context)
            .awsConfiguration(AWSConfiguration(context))
            .s3Client(s3Client)
            .build()

        // 파일명을 현재 시간 기준으로 설정
        val fileName = "${System.currentTimeMillis()}.jpg"
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

        // API 29 (Android Q) 이상에서는 openInputStream을 통해 파일을 저장
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val file = File(context.cacheDir, "tempImage.jpg")
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                return file.absolutePath
            }
        } else {
            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    path = it.getString(columnIndex)
                    Log.d(TAG, "Real path from URI: $path")
                }
            }
        }

        if (path == null) {
            Log.e(TAG, "Failed to get real path from URI: $uri")
        }
        return path
    }

    // study 데이터를 삽입하는 함수
    suspend fun insertStudyData(model: Map<String, Any>, studyPicUrl: String?): Int? {
        var preparedStatement: PreparedStatement? = null
        val columns = model.keys.toMutableList()
        val values = model.values.toMutableList()
        var idx: Int? = null

        // 이미지 URL을 studyPic 컬럼에 추가
        if (studyPicUrl != null) {
            if (columns.contains("studyPic")) {
                val index = columns.indexOf("studyPic")
                values[index] = studyPicUrl
            } else {
                columns.add("studyPic")
                values.add(studyPicUrl)
            }
        }

        try {
            // SQL 쿼리 문자열 생성
            val columnsString = columns.joinToString(",")
            val valuesString = values.joinToString(",") { "?" }
            val sql = "INSERT INTO tb_study ($columnsString) VALUES ($valuesString)"

            Log.d(TAG, "SQL: $sql")
            Log.d(TAG, "Values: $values")

            // 데이터베이스에 연결하여 데이터 삽입
            withContext(Dispatchers.IO) {
                HikariCPDataSource.getConnection().use { connection ->
                    preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
                    values.forEachIndexed { index, value ->
                        // 값의 타입에 따라 PreparedStatement 설정
                        when (value) {
                            is String -> preparedStatement?.setString(index + 1, value)
                            is Int -> preparedStatement?.setInt(index + 1, value)
                            is Boolean -> preparedStatement?.setBoolean(index + 1, value)
                            is ByteArray -> {
                                preparedStatement?.setBytes(index + 1, value)
                                Log.d(TAG, "Setting byte array for index ${index + 1}")
                                Log.d(TAG, "Byte array length: ${value.size}")
                            }
                        }
                    }
                    preparedStatement?.executeUpdate()
                    val resultSet = preparedStatement?.generatedKeys
                    if (resultSet?.next() == true) {
                        idx = resultSet.getInt(1) // 삽입된 데이터의 ID를 반환
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in insertStudyData", e)
        } finally {
            preparedStatement?.close() // PreparedStatement 닫기
        }
        return idx
    }

    // study 테이블에 tech stack 데이터를 삽입
    suspend fun insertStudyTechStack(studyIdx: Int, techStack: List<Int>) {
        val sql = "INSERT INTO tb_study_tech_stack (studyIdx, techIdx) VALUES (?, ?)"
        val paramsList = techStack.map { arrayOf<Any>(studyIdx, it) }
        executeBatchUpdate(sql, paramsList) // 배치 업데이트 실행
    }

    // study 테이블에 멤버 데이터를 삽입
    suspend fun insertStudyMember(studyIdx: Int, userIdxList: List<Int>) {
        val sql = "INSERT INTO tb_study_member (studyIdx, userIdx) VALUES (?, ?)"
        val paramsList = userIdxList.map { arrayOf<Any>(studyIdx, it) }
        executeBatchUpdate(sql, paramsList) // 배치 업데이트 실행
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
                        preparedStatement?.addBatch() // 배치에 추가
                    }
                    preparedStatement?.executeBatch() // 배치 실행
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in executeBatchUpdate", e)
        } finally {
            preparedStatement?.close() // PreparedStatement 닫기
        }
    }
}