package kr.co.lion.modigm.util

enum class Interest(var str: String, var num: Int) {
    WEB("웹", 1),
    SERVER("서버", 2),
    FRONT_END("프론트엔드", 3),
    BACK_END("백엔드", 4),
    IOS("iOS", 5),
    AOS("Android", 6),
    C_C_PLUS_PLUS("C, C++", 7),
    PYTHON("Python", 8),
    HARDWARE("하드웨어", 9),
    MACHINE_LEARNING("머신러닝", 10),
    BIG_DATA("빅데이터", 11),
    NODE_JS("Node.js", 12),
    DOT_NET(".NET", 13),
    BLOCK_CHAIN("블록체인", 14),
    CROSS_PLATFORM("크로스플랫폼", 15),
    GRAPHICS("Graphics", 16),
    VR("VR", 17),
    ETC("기타", 18);

    companion object {
        fun fromNum(num: Int): Interest? {
            return entries.find { it.num == num }
        }
    }
}