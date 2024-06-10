package kr.co.lion.modigm.ui.like.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.LikeRepository

class LikeViewModel : ViewModel() {
    private val likeRepository = LikeRepository()

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

}