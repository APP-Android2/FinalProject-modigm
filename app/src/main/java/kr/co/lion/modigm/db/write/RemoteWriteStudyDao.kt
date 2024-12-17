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
import kr.co.lion.modigm.db.FirestoreKeyProvider
import kr.co.lion.modigm.db.HikariCPDataSource
import kr.co.lion.modigm.model.StudyData
import java.io.File
import java.sql.PreparedStatement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RemoteWriteStudyDao {
    private val logTag by lazy { RemoteWriteStudyDao::class.simpleName }

    // 이미지 업로드 함수 (Amazon S3에 업로드)
    suspend fun uploadImageToS3(context: Context, uri: Uri): Result<String> {
        // URI로부터 실제 파일 경로를 얻음
        val filePath = getRealPathFromURI(context, uri) // URI로부터 실제 파일 경로를 얻음
        val file = File(filePath ?: throw IllegalArgumentException("Invalid file: $uri"))

        // Firestore에서 AWS 키 가져오기
        val keyProvider = FirestoreKeyProvider()
        val (accessKey, secretKey, bucketName) = keyProvider.getAwsKeys()

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
            var isResumed = false  // 이미 완료되었는지 체크할 플래그

            uploadObserver.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state == TransferState.COMPLETED && !isResumed) {
                        val url = s3Client.getUrl(bucketName, fileName).toString()
                        continuation.resume(Result.success(url))  // 성공 시 Result.success로 리턴
                        isResumed = true  // 코루틴 완료 상태로 설정
                    } else if (state == TransferState.FAILED && !isResumed) {
                        continuation.resume(Result.failure(Exception("Upload failed")))  // 실패 시 Result.failure로 리턴
                        isResumed = true  // 코루틴 완료 상태로 설정
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    // 업로드 진행 상황 업데이트 (필요 시 구현)
                }

                override fun onError(id: Int, ex: Exception) {
                    // 오류 발생 시 예외 처리
                    if (!isResumed) {
                        continuation.resume(Result.failure(ex))  // 실패 시 Result.failure로 리턴
                        isResumed = true  // 코루틴 완료 상태로 설정
                    }
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
                    Log.d(logTag, "Real path from URI: $path")
                }
            }
        }

        if (path == null) {
            Log.e(logTag, "Failed to get real path from URI: $uri")
        }
        return path
    }

    // study 테이블에 데이터를 삽입
    suspend fun insertStudyData(studyData: StudyData): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = """
                    INSERT INTO tb_study (studyTitle, studyContent, studyType, studyPeriod, studyOnOffline, 
                    studyDetailPlace, studyPlace, studyApplyMethod, studyCanApply, studyPic, studyMaxMember, 
                    studyState, studyChatLink, studyCreateDate, userIdx)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()

                    connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS).use { statement ->
                        // StudyData 객체의 데이터를 PreparedStatement에 매핑
                        StudyData.setPreparedStatement(statement, studyData)
                        // SQL 쿼리 실행
                        statement.executeUpdate()
                        // 생성된 스터디 데이터의 studyIdx를 반환
                        val resultSet = statement.generatedKeys
                        if (resultSet.next()) {
                            resultSet.getInt(1)
                        } else {
                            throw IllegalArgumentException("스터디 데이터 삽입 실패")
                        }
                    }
                }
            }.onFailure {
                Log.e(logTag, "스터디 데이터 삽입 중 오류 발생", it)
                Result.failure<Int>(it)
            }
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
            Log.e(logTag, "Error in executeBatchUpdate", e)
        } finally {
            preparedStatement?.close() // PreparedStatement 닫기
        }
    }
}