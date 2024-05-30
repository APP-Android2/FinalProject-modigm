package kr.co.lion.modigm.db.remote

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.StudyData

class StudyDataSource {
    // 스터디 '컬렉션' 접근
    private val studyCollection = Firebase.firestore.collection("Study")
    // 시퀀스 '컬렉션' 접근
    private val sequenceCollection = Firebase.firestore.collection("Sequence")


    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    // 고유 StudyIdx 를 얻기 위한 시퀀스 값을 가져온다.
    suspend fun getStudySequence(): Int {
        try {

            var studySequence = -1

            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // 스터디 시퀀스 '문서' 로 접근
                val documentReference = sequenceCollection.document("StudySequence")
                // 문서 내에 있는 데이터를 가져올 수 있는 객체를 가져온다.
                val documentSnapShot = documentReference.get().await()

                studySequence = documentSnapShot.getLong("value")?.toInt() ?: -1
            }
            job1.join()

            return studySequence
        } catch (e: Exception) {
            Log.e("Firebase Error", "Error dbGetStudySequence : ${e.message}")
            return 0
        }
    }

    // 고유 StudyIdx 를 얻기 위한 시퀀스 값을 업데이트 한다.
    suspend fun updateStudySequence(studySequence: Int) {
        try {

            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // 스터디 시퀀스 '문서' 로 접근
                val documentReference = sequenceCollection.document("StudySequence")
                // 저장할 데이터를 담을 HashMap
                val map = mutableMapOf<String, Long>()
                // "value"라는 이름의 필드가 있다면 값이 덮어 씌워지고 필드가 없다면 필드가 새로 생성된다.
                map["value"] = studySequence.toLong()
                // 저장한다.
                documentReference.set(map)
            }
            job1.join()
        } catch (e: Exception) {
            Log.e("Firebase Error", "Error dbUpdateStudySequence : ${e.message}")
        }
    }


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
            // 현재 사용자 uid를 가져옵니다.(테스트용)
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