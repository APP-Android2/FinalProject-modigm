package kr.co.lion.modigm.util

enum class Interest(var num: Int, var str: String) {
    KOTLIN(1, "Kotlin"),
    JAVA(2, "JAVA"),
    ANDROID(3, "Android");

    companion object {
        fun fromNum(num: Int): Interest? {
            return values().find { it.num == num }
        }
    }

}