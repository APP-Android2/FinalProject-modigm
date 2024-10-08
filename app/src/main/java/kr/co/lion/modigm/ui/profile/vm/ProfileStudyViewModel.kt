package kr.co.lion.modigm.ui.profile.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.ProfileRepository

class ProfileStudyViewModel: ViewModel() {
    private val profileRepository = ProfileRepository()

    // 사용자가 참여한 스터디 리스트
    private val _profileStudyList = MutableLiveData<List<StudyData>>()
    val profileStudyList: MutableLiveData<List<StudyData>> = _profileStudyList

    // 사용자가 진행한 스터디 목록 (전체)
    fun loadHostStudyList(userIdx: Int) = viewModelScope.launch {
        try {
            profileRepository.loadHostStudyList(userIdx).collect { studyList ->
                _profileStudyList.value = studyList
            }
        } catch (e: Exception) {
            Log.e("profilevm", "loadPartStudyList(): ${e.message}")
        }
    }

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록 (전체)
    fun loadPartStudyList(userIdx: Int) = viewModelScope.launch {
        try {
            profileRepository.loadPartStudyList(userIdx).collect { studyList ->
                _profileStudyList.value = studyList
            }
        } catch (e: Exception) {
            Log.e("profilevm", "loadHostStudyList(): ${e.message}")
        }
    }
}