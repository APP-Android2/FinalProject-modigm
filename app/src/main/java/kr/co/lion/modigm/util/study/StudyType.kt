package kr.co.lion.modigm.util.study

enum class StudyType (val num: Int, val str: String) {
    STUDY(1, "스터디"),
    PROJECT(2, "프로젝트"),
    COMPETITION(3, "공모전");

    companion object {
        fun fromNum(num: Int): StudyType? {
            return entries.find { it.num == num }
        }
    }
}