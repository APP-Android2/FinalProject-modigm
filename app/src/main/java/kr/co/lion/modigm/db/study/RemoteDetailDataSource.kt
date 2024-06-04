package kr.co.lion.modigm.db.study

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData

class RemoteDetailDataSource {

    // 스터디 '컬렉션' 접근
    private val studyCollection = FirebaseFirestore.getInstance().collection("Study")

    //유저 컬렉션 접근
    private val userCollection = FirebaseFirestore.getInstance().collection("User")

    suspend fun selectContentData(studyIdx: Int): StudyData? {
        return try {
            // Firestore 쿼리 실행
            val querySnapshot = studyCollection
                .whereEqualTo("studyIdx", studyIdx)
                .get()
                .await()

            // 결과 문서가 있을 경우 첫 번째 문서의 데이터를 StudyData 객체로 변환
            if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents[0].toObject(StudyData::class.java)
            } else {
                null // 쿼리 결과가 없을 경우 null 반환
            }
        } catch (e: Exception) {
            // 오류 처리: 로깅이나 사용자에게 피드백 제공
            Log.e("Firestore Error", "Error fetching study data: ${e.message}")
            null
        }
    }

    suspend fun loadUserDetailsByUid(uid: String): UserData? {
        return try {
            val querySnapshot = userCollection
                .whereEqualTo("userUid", uid)
                .get()
                .await()
            // 결과 문서가 있을 경우 첫 번째 문서의 데이터를 UserData 객체로 변환
            if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents[0].toObject(UserData::class.java)
            } else {
                null // 쿼리 결과가 없을 경우 null 반환
            }
        } catch (e: Exception) {
            Log.e("LoadUser", "Error fetching user data: ${e.message}")
            null
        }
    }

    suspend fun updateStudyCanApplyByStudyIdx(studyIdx: Int, canApply: Boolean) {
        try {
            val querySnapshot = studyCollection
                .whereEqualTo("studyIdx", studyIdx)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                studyCollection.document(document.id).update("studyCanApply", canApply).await()
            }
        } catch (e: Exception) {
            throw Exception("Failed to update study status: ${e.message}")
        }
    }
}