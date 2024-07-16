package kr.co.lion.modigm.ui.favorite.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.FavoriteRepository
import kr.co.lion.modigm.repository.StudyRepository

class FavoriteViewModel : ViewModel() {
    private val favoriteRepository = FavoriteRepository()
    private val studyRepository = StudyRepository()

    private val _favoritedStudies = MutableLiveData<List<StudyData>>()
    val favoritedStudies: LiveData<List<StudyData>> = _favoritedStudies

    fun loadFavoriteStudies(uid: String) {
        viewModelScope.launch {
            try {
                Log.d("FavoriteViewModel", "Loading favorited studies for user: $uid")
                val favoriteStudies = favoriteRepository.getFavoriteStudies(uid)
                Log.d("FavoriteViewModel", "Fetched favorited studies: $favoritedStudies")
                _favoritedStudies.postValue(favoriteStudies)
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error loading favorited studies", e)
                _favoritedStudies.postValue(emptyList())
            }
        }
    }

    fun toggleFavorite(uid: String, studyIdx: Int) {
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
                    loadFavoriteStudies(uid)
                } else {
                    Log.e("FavoriteViewModel", "No matching document found for studyIdx: $studyIdx")
                }
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error toggling favorite", e)
            }
        }
    }

}