package kr.co.lion.modigm.ui.study.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class FavoriteViewModel : ViewModel(){
    // --------------------------------- MySQL 적용 ---------------------------------
    // --------------------------------- 초기화 시작 --------------------------------

    // 태그
    private val logTag by lazy { FavoriteViewModel::class.simpleName }

    // 스터디 레포지토리
    private val studyRepository by lazy { StudyRepository() }



    // --------------------------------- 초기화 끝 --------------------------------
    // --------------------------------- 라이브데이터 시작 --------------------------------


    // 로딩 상태를 나타내는 LiveData
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading


    // 좋아요한 스터디 목록
    private val _favoriteStudyData = MutableLiveData<List<Triple<StudyData, Int, Boolean>>>()
    val favoriteStudyData: LiveData<List<Triple<StudyData, Int, Boolean>>> = _favoriteStudyData

    // 좋아요 상태
    private val _isFavorite = MutableLiveData<Pair<Int, Boolean>>()
    val isFavorite: LiveData<Pair<Int, Boolean>> = _isFavorite


    private val _favoriteStudyError = MutableLiveData<Throwable?>()
    val favoriteStudyError: LiveData<Throwable?> = _favoriteStudyError

    private val _isFavoriteError = MutableLiveData<Throwable?>()
    val isFavoriteError: LiveData<Throwable?> = _isFavoriteError

    // --------------------------------- 라이브데이터 끝 --------------------------------

    // 현재 사용자의 인덱스를 가져오는 함수
    private fun getCurrentUserIdx(): Int {
        return prefs.getInt("currentUserIdx")
    }

    /**
     * 좋아요한 스터디 목록 가져오기 (찜화면 접근 시)
     */
    fun getFavoriteStudyData() {
        viewModelScope.launch {
            _isLoading.postValue(true) // 로딩 시작
            val result = studyRepository.getFavoriteStudyData(getCurrentUserIdx())
            result.onSuccess {
                _favoriteStudyData.postValue(it)
            }.onFailure { e ->
                Log.e(logTag, "Error getFavoriteStudyData", e)
                _favoriteStudyError.postValue(e)
            }
            _isLoading.postValue(false) // 로딩 종료
        }
    }

    /**
     * 좋아요 상태 변경
     * @param studyIdx 스터디 인덱스
     * @param currentState 현재 좋아요 상태
     */
    fun changeFavoriteState(studyIdx: Int, currentState: Boolean) {
        viewModelScope.launch {
            // 좋아요 상태 변경
            val result = if (currentState) {
                studyRepository.removeFavorite(getCurrentUserIdx(), studyIdx)
            } else {
                studyRepository.addFavorite(getCurrentUserIdx(), studyIdx)
            }

            // 결과에 따른 UI 업데이트
            result.onSuccess {
                _isFavorite.postValue(Pair(studyIdx, !currentState))
            }.onFailure { e ->
                Log.e(logTag, "Error changing favorite state", e)
                _isFavoriteError.postValue(e)
            }
        }
    }

    /**
     * 데이터 초기화 메서드
     */
    fun clearData() {
        _favoriteStudyData.postValue(emptyList())
        _isFavorite.postValue(Pair(-1, false))
        _favoriteStudyError.postValue(null)
        _isFavoriteError.postValue(null)
        _isLoading.postValue(false)
    }

    // ------------------MySQL 적용 끝-----------------------
}