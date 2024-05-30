package kr.co.lion.modigm.db.remote

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.StudyData

class StudyDataSource {
    private val studyCollection = Firebase.firestore.collection("Study")

    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    // 전체 스터디 목록을 가져온다.
    suspend fun getStudyAllData(): List<StudyData> {

        return try {
            val query = studyCollection
            val querySnapshot = query.get().await()
            querySnapshot.map { it.toObject(StudyData::class.java) }

        } catch (e: Exception) {
            Log.e("Firebase Error", "Error dbGetStudyAllData : ${e.message}")
            emptyList()
        }
    }

    // 전체 스터디 목록 중 모집중인 스터디만 가져온다.
    suspend fun getStudyStateTrueData(): List<StudyData> {

        return try {
            val query = studyCollection.whereEqualTo("studyState",true)
            val querySnapshot = query.get().await()
            querySnapshot.map { it.toObject(StudyData::class.java) }

        } catch (e: Exception) {
            Log.e("Firebase Error", "Error dbGetStudyAllData : ${e.message}")
            emptyList()
        }
    }

    // 내 스터디 목록을 가져온다.
    suspend fun getStudyMyData(): List<StudyData> {

        return try {
            // 현재 사용자 uid를 가져옵니다.
            val currentUserUid = "3DAiWpgwoZShL21ehAQcgolHYRA3"

            if (currentUserUid != null) {
                // studyUidList에 현재 사용자 uid가 포함된 스터디를 가져옵니다.
                val query = studyCollection.whereArrayContains("studyUidList", currentUserUid)
                val querySnapshot = query.get().await()
                querySnapshot.map { it.toObject(StudyData::class.java) }
            } else {
                Log.e("Firebase Error", "User is not authenticated")
                emptyList()
            }

        } catch (e: Exception) {
            Log.e("Firebase Error", "Error dbGetStudyMyData : ${e.message}")
            emptyList()
        }
    }

}