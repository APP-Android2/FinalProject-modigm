package kr.co.lion.modigm.db.study

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData

class RemoteStudyDataSource {

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

    // 사용자 프로필 사진을 받아오는 메서드
    suspend fun loadStudyThumbnail(
        context: Context,
        imageFileName: String,
        imageView: ImageView
    ) {
        try {
            // 이미지가 등록되어 있지 않으면 불러오지 않는다
            if (imageFileName.isNotEmpty()) {
                val job1 = CoroutineScope(Dispatchers.IO).launch {
                    // 이미지에 접근할 수 있는 객체를 가져온다.
                    val storageRef = Firebase.storage.reference.child("studyPic/$imageFileName")
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
        } catch (e: Exception) {
            Log.e("studyds", "loadStudyThumbnail: ${e.message}")
        }
    }


    // 사용자가 참여한 스터디 목록을 가져온다.
    suspend fun loadStudyPartData(uid: String): MutableList<StudyData> {
        // 사용자 정보를 담을 리스트
        val studyList = mutableListOf<StudyData>()

        try {
            // studyUidList에 해당 uid가 포함된 문서들을 가져온다.
            val querySnapshot =
                studyCollection.whereArrayContains("studyUidList", uid).get().await()

            // 가져온 문서의 수 만큼 반복한다.
            querySnapshot.forEach {
                // StudyData 객체에 담는다.
                val study = it.toObject(StudyData::class.java)
                // 리스트에 담는다.
                studyList.add(study)
            }
        } catch (e: Exception) {
            Log.e("studyds", "loadStudyPartData: ${e.message}")
        }

        return studyList
    }

    // 사용자가 진행한 스터디 목록을 가져온다.
    suspend fun loadStudyHostData(uid: String): MutableList<StudyData> {
        // 사용자 정보를 담을 리스트
        val studyList = mutableListOf<StudyData>()

        try {
            // studyUidList에 해당 uid가 포함된 문서들을 가져온다.
            val querySnapshot =
                studyCollection.whereArrayContains("studyUidList", uid).get().await()

            // 가져온 문서의 수 만큼 반복한다.
            querySnapshot.forEach { documentSnapshot ->
                // StudyData 객체에 담는다.
                val study = documentSnapshot.toObject(StudyData::class.java)
                // 첫 번째 요소(스터디 진행자)가 uid와 일치하는지 확인한다.
                if (study.studyUidList.isNotEmpty() && study.studyUidList[0] == uid) {
                    // 리스트에 담는다.
                    studyList.add(study)
                }
            }
        } catch (e: Exception) {
            Log.e("studyds", "loadStudyHostData: ${e.message}")
        }

        return studyList
    }
}