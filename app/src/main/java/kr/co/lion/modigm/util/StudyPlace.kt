package kr.co.lion.modigm.util

enum class StudyPlace (var num: Int, var str: String) {
    ONLINE(1, "온라인"),
    OFFLINE(2, "오프라인"),
    ONOFFLINE(3, "온/오프 혼합");

    companion object {
        fun fromNum(num: Int): StudyPlace? {
            return StudyPlace.entries.find { it.num == num }
        }
    }
}