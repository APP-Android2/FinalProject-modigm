package kr.co.lion.modigm.ui.join.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class JoinStep3ViewModel: ViewModel() {

    // 데이터는 추후 수정
    val interestList = MutableLiveData(
        arrayListOf(
            "웹", "서버", "임베디드", "프론트 엔드", "백 엔드",
            "iOS", "안드로이드", "C, C++", "파이썬", "하드웨어",
            "머신러닝", "빅데이터", "Node.js", ".NET", "블록체인",
            "크로스플랫폼", "그래픽스", "VR"
        )
    )

    private val _selectedInterestList = MutableLiveData<ArrayList<String>>(arrayListOf())
    val selectedInterestList: LiveData<ArrayList<String>> = _selectedInterestList

    fun addInterest(interest: String) {
        _selectedInterestList.value?.add(interest)
    }

    fun removeInterest(interest: String) {
        _selectedInterestList.value?.remove(interest)
    }

    // 유효성 검사
    fun validate(): Boolean {
        return selectedInterestList.value.isNullOrEmpty()
    }
}