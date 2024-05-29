package kr.co.lion.modigm.model

import androidx.databinding.adapters.AutoCompleteTextViewBindingAdapter.IsValid

data class StudyData(
    val studyIdx: Int = -1,
    val studyTitle: String = "",
    val studyContent: String = "",
    val studyType: Int = 0,
    val studyPeriod: Int = 0,
    val studyMeet: Int = 0,
    val studyPlace: String = "",
    val studyApply: Int = 0,
    val studySkillList: List<Int> = listOf(),
    val studyState: Boolean = true,
    val studyPic: String = "",
    val studyUserCnt: Int = 0,
    val studyUidList: List<String> = listOf(),
    val chatIdx: Int = -1,
    val studyValid: Boolean = true,
)