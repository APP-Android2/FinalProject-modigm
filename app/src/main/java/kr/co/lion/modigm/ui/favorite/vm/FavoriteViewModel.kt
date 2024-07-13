package kr.co.lion.modigm.ui.favorite.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.repository.FavoriteRepository

class FavoriteViewModel : ViewModel() {
    private val favoriteRepository = FavoriteRepository()

    private val _favoritedStudies = MutableLiveData<List<Triple<SqlStudyData, Int, Boolean>>>()
    val favoritedStudies: LiveData<List<Triple<SqlStudyData, Int, Boolean>>> = _favoritedStudies

    private val _isFavorite = MutableLiveData<Pair<Int, Boolean>>()
    val isFavorite: LiveData<Pair<Int, Boolean>> get() = _isFavorite


    fun loadFavoriteStudies(userIdx: Int) {
        viewModelScope.launch {
            try {
                Log.d("FavoriteViewModel", "Loading favorited studies for user: $userIdx")
                val favoriteStudies = favoriteRepository.getFavoriteStudies(userIdx)
                Log.d("FavoriteViewModel", "Fetched favorited studies: $favoriteStudies")
                _favoritedStudies.postValue(favoriteStudies.values.toList())
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error loading favorited studies", e)
                _favoritedStudies.postValue(emptyList())
            }
        }
    }

    fun toggleFavorite(userIdx: Int, studyIdx: Int) {
        viewModelScope.launch {
            try {
                val currentState = favoriteRepository.toggleFavorite(userIdx, studyIdx)
                // 좋아요 상태 업데이트 후 목록 새로고침
                loadFavoriteStudies(userIdx)
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error toggling favorite", e)
            }
        }
    }
}
