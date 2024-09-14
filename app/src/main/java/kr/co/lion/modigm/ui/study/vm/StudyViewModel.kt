package kr.co.lion.modigm.ui.study.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.FilterStudyData
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.TechStackData
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class StudyViewModel : ViewModel() {

    // --------------------------------- 초기화 --------------------------------
    // 태그
    private val logTag by lazy { StudyViewModel::class.simpleName }

    // 스터디 레포지토리
    private val studyRepository by lazy { StudyRepository() }
    // --------------------------------- 초기화 --------------------------------

    // --------------------------------- 공통 --------------------------------
    // 로딩 상태를 나타내는 LiveData
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 스와이프 리프레시 상태를 나타내는 LiveData
    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    // 현재 사용자의 인덱스를 가져오는 함수
    private fun getCurrentUserIdx(): Int {
        return prefs.getInt("currentUserIdx")
    }
    // --------------------------------- 공통 --------------------------------

    // --------------------------------- 전체 스터디 --------------------------------
    // 전체 스터디 목록 중 모집중인 스터디 리스트
    private val _allStudyData = MutableLiveData<List<Triple<StudyData, Int, Boolean>>>()
    val allStudyData: LiveData<List<Triple<StudyData, Int, Boolean>>> = _allStudyData

    private val _allStudyError = MutableLiveData<Throwable?>()
    val allStudyError: LiveData<Throwable?> = _allStudyError

    /**
     * 전체 스터디 목록 중 모집중인 스터디 가져오기 (홈화면 '전체 스터디' 접근 시)
     */
    fun getAllStudyData() {
        viewModelScope.launch {
            _isLoading.postValue(true) // 로딩 시작
            val result = studyRepository.getAllStudyData(getCurrentUserIdx())
            result.onSuccess {
                _allStudyData.postValue(it)
            }.onFailure {
                Log.e(logTag, "Error getAllStudyData", it)
                _allStudyError.postValue(it)
            }
            _isLoading.postValue(false) // 로딩 종료
        }
    }

    /**
     * 스와이프 리프레시용 전체 스터디 데이터 가져오기
     */
    fun refreshAllStudyData() {
        viewModelScope.launch {
            _isRefreshing.postValue(true) // 스와이프 리프레시 로딩 시작
            val result = studyRepository.getAllStudyData(getCurrentUserIdx())
            result.onSuccess {
                _allStudyData.postValue(it)
            }.onFailure {
                Log.e(logTag, "Error refreshAllStudyData", it)
                _allStudyError.postValue(it)
            }
            _isRefreshing.postValue(false) // 스와이프 리프레시 로딩 종료
        }
    }
    // --------------------------------- 전체 스터디 --------------------------------

    // --------------------------------- 내 스터디 --------------------------------
    // 내 스터디 리스트
    private val _myStudyData = MutableLiveData<List<Triple<StudyData, Int, Boolean>>>()
    val myStudyData: LiveData<List<Triple<StudyData, Int, Boolean>>> = _myStudyData

    private val _myStudyError = MutableLiveData<Throwable?>()
    val myStudyError: LiveData<Throwable?> = _myStudyError

    /**
     * 전체 스터디 목록 중 내 스터디 목록 가져오기 (홈화면 '내 스터디' 접근 시)
     */
    fun getMyStudyData() {
        viewModelScope.launch {
            _isLoading.postValue(true) // 로딩 시작
            val result = studyRepository.getMyStudyData(getCurrentUserIdx())
            result.onSuccess {
                _myStudyData.postValue(it)
            }.onFailure { e ->
                Log.e(logTag, "Error getMyStudyData", e)
                _myStudyError.postValue(e)
            }
            _isLoading.postValue(false) // 로딩 종료
        }
    }

    /**
     * 스와이프 리프레시용 내 스터디 데이터 가져오기
     */
    fun refreshMyStudyData() {
        viewModelScope.launch {
            _isRefreshing.postValue(true) // 스와이프 리프레시 로딩 시작
            val result = studyRepository.getMyStudyData(getCurrentUserIdx())
            result.onSuccess {
                _myStudyData.postValue(it)
            }.onFailure {
                Log.e(logTag, "Error refreshMyStudyData", it)
                _myStudyError.postValue(it)
            }
            _isRefreshing.postValue(false) // 스와이프 리프레시 로딩 종료
        }
    }
    // --------------------------------- 내 스터디 --------------------------------

    // --------------------------------- 스터디 좋아요 --------------------------------
    // 좋아요 상태
    private val _isFavorite = MutableLiveData<Pair<Int, Boolean>>()
    val isFavorite: LiveData<Pair<Int, Boolean>> = _isFavorite

    private val _isFavoriteError = MutableLiveData<Throwable?>()
    val isFavoriteError: LiveData<Throwable?> = _isFavoriteError

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
    // --------------------------------- 스터디 좋아요 --------------------------------

    // --------------------------------- 스터디 필터 --------------------------------
    // 필터링된 전체 스터디 목록 LiveData
    private val _filterAllStudyData = MutableLiveData<List<Triple<StudyData, Int, Boolean>>>()
    val filterAllStudyData: LiveData<List<Triple<StudyData, Int, Boolean>>> = _filterAllStudyData

    // 필터 적용 여부를 관리하는 LiveData
    private val _isFilterApplied = MutableLiveData<Boolean>(false)
    val isFilterApplied: LiveData<Boolean> = _isFilterApplied

    /**
     * 필터링된 전체 스터디 목록 가져오기
     */
    fun getFilteredAllStudyList(newFilterData: FilterStudyData) {
        viewModelScope.launch {
            _isLoading.postValue(true) // 로딩 시작
            newFilterData.let { filter ->
                val result = studyRepository.getFilteredAllStudyList(getCurrentUserIdx(), filter)
                result.onSuccess {
                    _filterAllStudyData.postValue(it)
                    _isFilterApplied.postValue(true) // 필터가 적용되었음을 표시
                }.onFailure { e ->
                    Log.e(logTag, "필터링된 스터디 목록 가져오기 실패", e)
                    _allStudyError.postValue(e)
                }
            }
            _isLoading.postValue(false) // 로딩 종료
        }
    }

    // 필터링된 내 스터디 목록 LiveData
    private val _filteredMyStudyData = MutableLiveData<List<Triple<StudyData, Int, Boolean>>>()
    val filteredMyStudyData: LiveData<List<Triple<StudyData, Int, Boolean>>> = _filteredMyStudyData

    /**
     * 필터링된 내 스터디 목록 가져오기
     */
    fun getFilteredMyStudyList(newFilterData: FilterStudyData) {
        viewModelScope.launch {
            _isLoading.postValue(true) // 로딩 시작
            newFilterData.let { filter ->
                val result = studyRepository.getFilteredMyStudyList(getCurrentUserIdx(), filter)
                result.onSuccess {
                    _filteredMyStudyData.postValue(it)
                    _isFilterApplied.postValue(true) // 필터가 적용되었음을 표시
                }.onFailure { e ->
                    Log.e(logTag, "필터링된 스터디 목록 가져오기 실패", e)
                    _myStudyError.postValue(e)
                }
            }
            _isLoading.postValue(false) // 로딩 종료
        }
    }
    // --------------------------------- 스터디 필터 --------------------------------

    // --------------------------------- 필터용 기술 스택 --------------------------------
    // 기술 스택 데이터 LiveData
    private val _techStackData = MutableLiveData<List<TechStackData>>()
    val techStackData: LiveData<List<TechStackData>> = _techStackData

    /**
     * 기술 스택 데이터를 가져오는 함수
     */
    fun getTechStackData() {
        viewModelScope.launch {
            val result = studyRepository.getTechStackData()
            result.onSuccess {
                _techStackData.postValue(it)
            }.onFailure { e ->
                Log.e(logTag, "기술 스택 데이터 조회 실패", e)
            }
        }
    }
    // --------------------------------- 필터용 기술 스택 --------------------------------

    /**
     * 데이터 초기화 메서드
     */
    fun clearData() {
        _allStudyData.postValue(emptyList())
        _myStudyData.postValue(emptyList())
        _isFavorite.postValue(Pair(-1, false))
        _isFavoriteError.postValue(null)
        _myStudyError.postValue(null)
        _allStudyError.postValue(null)
        _isLoading.postValue(false)
        _isFilterApplied.postValue(false)
        _filterAllStudyData.postValue(emptyList())
        _filteredMyStudyData.postValue(emptyList())
    }
}