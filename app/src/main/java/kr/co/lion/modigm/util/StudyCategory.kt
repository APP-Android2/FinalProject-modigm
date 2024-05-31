package kr.co.lion.modigm.util

enum class StudyCategory (var num: Int, var str: String) {
    STUDY(1, "스터디"),
    PROJECT(2, "프로젝트"),
    COMPETITION(3, "공모전");

    companion object {
        fun fromNum(num: Int): StudyCategory? {
            return entries.find { it.num == num }
        }
    }
}