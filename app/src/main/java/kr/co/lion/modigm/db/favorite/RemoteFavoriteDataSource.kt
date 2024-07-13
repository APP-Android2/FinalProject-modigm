package kr.co.lion.modigm.db.favorite

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.StudyData

class RemoteFavoriteDataSource {

    private val studyCollection = FirebaseFirestore.getInstance().collection("Study")

    suspend fun fetchFavoriteStudies(uid: String): List<Int> {
        val documentReference = FirebaseFirestore.getInstance().collection("favorite").document(uid)
        val snapshot = documentReference.get().await()
        if (snapshot.exists()) {
            val favorites = snapshot.get("favorites") as? List<Map<String, Any>> ?: listOf()
            Log.d("FavoriteDataSource", "Fetched favorites for user $uid: $favorites")
            favorites.forEach {
                Log.d("FavoriteDataSource", "Favorite entry: $it")
            }
            val studyIdxs = favorites.mapNotNull {
                val studyIdx = it["studyIdx"] as? Long // Firestore에서 숫자는 Long으로 저장됨
                Log.d("FavoriteDataSource", "Extracted studyIdx: $studyIdx")
                studyIdx?.toInt() // Int로 변환하여 반환
            }
            Log.d("FavoriteDataSource", "Parsed study indices: $studyIdxs")
            return studyIdxs
        }
        Log.d("FavoriteDataSource", "No favorites found for user $uid")
        return listOf()
    }

    suspend fun selectContentData(studyIdx: Int): StudyData? {
        return try {
            Log.d("FavoriteDataSource", "Fetching content data for studyIdx: $studyIdx")
            // Firestore 쿼리 실행
            val querySnapshot = studyCollection
                .whereEqualTo("studyIdx", studyIdx)
                .get()
                .await()
            Log.d("FavoriteDataSource", "Query result for studyIdx $studyIdx: ${querySnapshot.documents}")

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

    suspend fun fetchStudyDetails(studyIdxs: List<Int>): List<StudyData> {
        val studies = mutableListOf<StudyData>()

        for (idx in studyIdxs) {
            Log.d("FavoriteDataSource", "Fetching study data for studyIdx: $idx")
            val studyData = selectContentData(idx)
            if (studyData != null) {
                studies.add(studyData)
                Log.d("FavoriteDataSource", "Fetched study for index $idx: $studyData")
            } else {
                Log.d("FavoriteDataSource", "No study data found for index $idx")
            }
        }

        Log.d("FavoriteDataSource", "Fetched study details: $studies")
        return studies
    }
}