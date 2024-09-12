package kr.co.lion.modigm.util.study

enum class StudyOnOffline (val num: Int, val str: String) {
    ONLINE(1, "온라인"),
    OFFLINE(2, "오프라인"),
    ONOFFLINE(3, "온/오프 혼합");

    companion object {
        fun fromNum(num: Int): StudyOnOffline? {
            return StudyOnOffline.entries.find { it.num == num }
        }
    }
}