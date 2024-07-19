package kr.co.lion.modigm.ui.favorite.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.repository.FavoriteStudyRepository
import kr.co.lion.modigm.repository.StudyListRepository

class FavoriteViewModel : ViewModel() {
    private val favoriteStudyRepository = FavoriteStudyRepository()
    private val studyListRepository = StudyListRepository()

    private val _favoritedStudyList = MutableLiveData<List<Triple<SqlStudyData, Int, Boolean>>>()
    val favoritedStudyList: LiveData<List<Triple<SqlStudyData, Int, Boolean>>> = _favoritedStudyList

    private val _isFavorite = MutableLiveData<Pair<Int, Boolean>>()
    val isFavorite: LiveData<Pair<Int, Boolean>> get() = _isFavorite


    fun getMyFavoriteStudyDataList(userIdx: Int) {
        viewModelScope.launch {
            try {
                val favoriteStudies = favoriteStudyRepository.getMyFavoriteStudyDataList(userIdx)

                _favoritedStudyList.postValue(favoriteStudies.values.toList())
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error loading favorited studies", e)
                _favoritedStudyList.postValue(emptyList())
            }
        }
    }

    fun toggleFavorite(userIdx: Int, studyIdx: Int) {
        viewModelScope.launch {
            try {
                val currentState = studyListRepository.toggleFavorite(userIdx, studyIdx)
                // 좋아요 상태 업데이트 후 목록 새로고침
                _isFavorite.value = Pair(studyIdx, currentState)
                getMyFavoriteStudyDataList(userIdx)
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Error toggling favorite", e)
            }
        }
    }

    // 데이터 초기화 메서드
    fun clearData() {
        _favoritedStudyList.value = emptyList()
    }

    // Dao 코루틴 및 히카리CP 자원 해제하기
    private fun close() {
        viewModelScope.launch {
            favoriteStudyRepository.close()
        }
    }
    // 뷰모델에서 Dao 코루틴 및 히카리CP 자원 해제
    override fun onCleared() {
        super.onCleared()
        close()
    }
}
