package kr.co.lion.modigm.db.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.StudyData

class StudyDataSource {
    // 스터디 '컬렉션' 접근
    private val studyCollection = FirebaseFirestore.getInstance().collection("Study")
    // 시퀀스 '컬렉션' 접근
    private val sequenceCollection = FirebaseFirestore.getInstance().collection("Sequence")

    private val currentUserUid: String? = FirebaseAuth.getInstance().currentUser?.uid

    // 고유 StudyIdx 를 얻기 위한 시퀀스 값을 가져온다.
    suspend fun getStudySequence(): Int {
        return try {
            val documentReference = sequenceCollection.document("StudySequence")
            val documentSnapShot = documentReference.get().await()
            documentSnapShot.getLong("value")?.toInt() ?: -1
        } catch (e: Exception) {
            Log.e("Firebase Error", "Error dbGetStudySequence : ${e.message}")
            -1
        }
    }

    // 고유 StudyIdx 를 얻기 위한 시퀀스 값을 업데이트 한다.
    suspend fun updateStudySequence(studySequence: Int) {
        try {
            val documentReference = sequenceCollection.document("StudySequence")
            val map = mutableMapOf<String, Long>()
            map["value"] = studySequence.toLong()
            documentReference.set(map).await()
        } catch (e: Exception) {
            Log.e("Firebase Error", "Error dbUpdateStudySequence : ${e.message}")
        }
    }


    // 전체 스터디 목록을 가져온다.
    suspend fun getStudyAllData(): List<StudyData> {
        return try {
            val querySnapshot = studyCollection.get().await()
            querySnapshot.map { it.toObject(StudyData::class.java) }
        } catch (e: Exception) {
            Log.e("Firebase Error", "Error dbGetStudyAllData : ${e.message}")
            emptyList()
        }
    }

    // 전체 스터디 목록 중 모집중인 스터디만 가져온다.
    suspend fun getStudyStateTrueData(): List<Pair<StudyData, Int>> {
        return try {
            val query = studyCollection.whereEqualTo("studyState", true)
            val querySnapshot = query.get().await()
            querySnapshot.map { documentSnapshot ->
                val studyData = documentSnapshot.toObject(StudyData::class.java)
                val studyUidList = documentSnapshot.get("studyUidList") as? List<*>
                val participantCount = studyUidList?.size ?: 0
                Pair(studyData, participantCount)
            }
        } catch (e: Exception) {
            Log.e("Firebase Error", "Error dbGetStudyStateTrueData : ${e.message}")
            emptyList()
        }
    }

    // 내 스터디 목록을 가져온다.
    suspend fun getStudyMyData(): List<Pair<StudyData, Int>> {
        return try {
            // 현재 사용자 uid를 가져옵니다.(테스트용)
            val currentUserUid = "3DAiWpgwoZShL21ehAQcgolHYRA3"

            if (currentUserUid != null) {
                // studyUidList에 현재 사용자 uid가 포함된 스터디를 가져옵니다.
                val query = studyCollection.whereArrayContains("studyUidList", currentUserUid)
                val querySnapshot = query.get().await()
                querySnapshot.map { documentSnapshot ->
                    val studyData = documentSnapshot.toObject(StudyData::class.java)
                    val studyUidList = documentSnapshot.get("studyUidList") as? List<*>
                    val participantCount = studyUidList?.size ?: 0
                    Pair(studyData, participantCount)
                }
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