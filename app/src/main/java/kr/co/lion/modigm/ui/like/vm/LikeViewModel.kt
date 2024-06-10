package kr.co.lion.modigm.ui.like.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.LikeRepository
import kr.co.lion.modigm.repository.StudyRepository

class LikeViewModel : ViewModel() {
    private val likeRepository = LikeRepository()
    private val studyRepository = StudyRepository()

    private val _likedStudies = MutableLiveData<List<StudyData>>()
    val likedStudies: LiveData<List<StudyData>> = _likedStudies

    fun loadLikedStudies(uid: String) {
        viewModelScope.launch {
            try {
                Log.d("LikeViewModel", "Loading liked studies for user: $uid")
                val likedStudies = likeRepository.getLikedStudies(uid)
                Log.d("LikeViewModel", "Fetched liked studies: $likedStudies")
                _likedStudies.postValue(likedStudies)
            } catch (e: Exception) {
                Log.e("LikeViewModel", "Error loading liked studies", e)
                _likedStudies.postValue(emptyList())
            }
        }
    }

    fun toggleLike(uid: String, studyIdx: Int) {
        viewModelScope.launch {
            try {
                val studyCollection = FirebaseFirestore.getInstance().collection("Study")
                val query = studyCollection.whereEqualTo("studyIdx", studyIdx)
                val querySnapshot = query.get().await()

                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val currentlyLiked = document.getBoolean("studyLikeState") ?: false
                    val newLikeState = !currentlyLiked

                    document.reference.update("studyLikeState", newLikeState).await()

                    if (newLikeState) {
                        studyRepository.addLike(uid, studyIdx)
                    } else {
                        studyRepository.removeLike(uid, studyIdx)
                    }

                    // 좋아요 상태 업데이트 후 목록 새로고침
                    loadLikedStudies(uid)
                } else {
                    Log.e("LikeViewModel", "No matching document found for studyIdx: $studyIdx")
                }
            } catch (e: Exception) {
                Log.e("LikeViewModel", "Error toggling like", e)
            }
        }
    }

}