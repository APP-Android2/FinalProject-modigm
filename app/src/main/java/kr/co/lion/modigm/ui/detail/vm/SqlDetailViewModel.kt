package kr.co.lion.modigm.ui.detail.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.repository.SqlDetailRepository

class SqlDetailViewModel: ViewModel() {

    private val sqlDetailRepository = SqlDetailRepository()

    private val _studyData = MutableStateFlow<SqlStudyData?>(null)
    val studyData: StateFlow<SqlStudyData?> = _studyData

    private val _memberCount = MutableStateFlow(0)
    val memberCount: StateFlow<Int> = _memberCount

    private val _allStudyDetails = MutableStateFlow<List<Triple<SqlStudyData, Int, Boolean>>>(emptyList())
    val allStudyDetails: StateFlow<List<Triple<SqlStudyData, Int, Boolean>>> = _allStudyDetails

    private val _userData = MutableStateFlow<SqlUserData?>(null)
    val userData: StateFlow<SqlUserData?> = _userData

    private val _studyTechList = MutableStateFlow<List<Int>>(emptyList())
    val studyTechList: StateFlow<List<Int>> get() = _studyTechList

    private val _updateResult = MutableStateFlow<Boolean?>(null)
    val updateResult: StateFlow<Boolean?> get() = _updateResult

    private val _studyPic = MutableStateFlow<String?>(null)
    val studyPic: StateFlow<String?> = _studyPic

    private val _studySkills = MutableStateFlow<List<Int>>(emptyList())
    val studySkills: StateFlow<List<Int>> get() = _studySkills

    fun clearData() {
        _studyData.value = null
        _memberCount.value = 0
        _userData.value = null
        _studyTechList.value = emptyList()
        _studyPic.value = null
    }

    // 특정 studyIdx에 대한 스터디 데이터를 가져오는 메소드
    fun getStudy(studyIdx: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                sqlDetailRepository.getStudyById(studyIdx).collect { data ->
                    _studyData.value = data
                    data?.let { getStudyPic(it.studyIdx) } // 데이터 가져온 후 이미지 로드
                }
            } catch (throwable: Throwable) {
                Log.e("DetailViewModel", "Error fetching study data", throwable)
            }
        }
    }

    // 특정 studyIdx에 대한 스터디 멤버 수를 가져오는 메소드
    fun countMembersByStudyIdx(studyIdx: Int) {
        viewModelScope.launch {
            try {
                sqlDetailRepository.countMembersByStudyIdx(studyIdx).collect { count ->
                    _memberCount.value = count
                }
            } catch (throwable: Throwable) {
                Log.e("DetailViewModel", "Error counting members", throwable)
            }
        }
    }

    // 특정 studyIdx에 대한 스터디 이미지를 가져오는 메소드
//    fun getStudyPic(studyIdx: Int) {
//        viewModelScope.launch {
//            try {
//                sqlDetailRepository.getStudyPicByStudyIdx(studyIdx).collect { pic ->
//                    _studyPic.value = pic
//                }
//            } catch (throwable: Throwable) {
//                Log.e("DetailViewModel", "Error fetching study pic", throwable)
//            }
//        }
//    }
    // 특정 studyIdx에 대한 스터디 이미지를 가져오는 메소드
    fun getStudyPic(studyIdx: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            sqlDetailRepository.getStudyPicByStudyIdx(studyIdx).collect { pic ->
                _studyPic.value = pic
            }
        }
    }

    // 특정 userIdx에 대한 사용자 데이터를 가져오는 메소드
    fun getUserById(userIdx: Int) {
        viewModelScope.launch {
            try {
                sqlDetailRepository.getUserById(userIdx).collect { user ->
                    _userData.value = user
                    Log.d("DetailViewModel", "Fetched user data: $user")
                }
            } catch (throwable: Throwable) {
                Log.e("DetailViewModel", "Error fetching user data", throwable)
            }
        }
    }

    fun getTechIdxByStudyIdx(studyIdx: Int) {
        viewModelScope.launch {
            sqlDetailRepository.getTechIdxByStudyIdx(studyIdx).collect { techList ->
                _studyTechList.value = techList
            }
        }
    }

    // studyState 값을 업데이트하는 메소드 추가
    fun updateStudyState(studyIdx: Int, newState: Int) {
        viewModelScope.launch {
            val result = sqlDetailRepository.updateStudyState(studyIdx, newState)
            _updateResult.emit(result)
        }
    }

    // 스터디 데이터를 업데이트하는 함수
    fun updateStudyData(studyData: SqlStudyData) {
        viewModelScope.launch {
            val result = sqlDetailRepository.updateStudy(studyData)
            _updateResult.value = result
        }
    }

    fun insertSkills(studyIdx: Int, skills: List<Int>) {
        viewModelScope.launch {
            sqlDetailRepository.insertSkills(studyIdx, skills)
        }
    }

    // 업데이트 결과 초기화 함수
    fun clearUpdateResult() {
        _updateResult.value = null
    }


    // ViewModel이 파괴될 때 호출되는 메서드
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            sqlDetailRepository.close()
        }
    }
}