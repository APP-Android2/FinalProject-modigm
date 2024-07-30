package kr.co.lion.modigm.ui.login

sealed class LoginError(private val code: Int, override val message: String) : Throwable(message) {
    data object FirebaseEmailLoginError : LoginError(1001, "이메일 로그인 실패") {
        private fun readResolve(): Any = FirebaseEmailLoginError
    }
    data object FirebasePasswordLoginError : LoginError(1002, "비밀번호 로그인 실패") {
        private fun readResolve(): Any = FirebasePasswordLoginError
    }
    data object FirebaseInvalidCredentials : LoginError(1003, "유효하지 않은 자격 증명") {
        private fun readResolve(): Any = FirebaseInvalidCredentials
    }
    data object FirebaseUserCollision : LoginError(1004, "사용자 충돌") {
        private fun readResolve(): Any = FirebaseUserCollision
    }
    data object FirebaseInvalidUser : LoginError(1005, "유효하지 않은 사용자") {
        private fun readResolve(): Any = FirebaseInvalidUser
    }
    data object FirebaseTooManyRequests : LoginError(1006, "요청이 너무 많음") {
        private fun readResolve(): Any = FirebaseTooManyRequests
    }
    data object FirebaseNetworkError : LoginError(1007, "네트워크 오류") {
        private fun readResolve(): Any = FirebaseNetworkError
    }
    data object FirebaseWeakPassword : LoginError(1008, "약한 비밀번호") {
        private fun readResolve(): Any = FirebaseWeakPassword
    }
    data object FirebaseUnknownError : LoginError(1009, "알 수 없는 Firebase 오류") {
        private fun readResolve(): Any = FirebaseUnknownError
    }

    data object KakaoAuthError : LoginError(2001, "카카오 인증 오류") {
        private fun readResolve(): Any = KakaoAuthError
    }
    data object KakaoClientError : LoginError(2002, "카카오 클라이언트 오류") {
        private fun readResolve(): Any = KakaoClientError
    }
    data object KakaoServerError : LoginError(2003, "카카오 서버 오류") {
        private fun readResolve(): Any = KakaoServerError
    }
    data object KakaoNetworkError : LoginError(2004, "카카오 네트워크 오류") {
        private fun readResolve(): Any = KakaoNetworkError
    }
    data object KakaoCanceledError : LoginError(2005, "카카오 로그인 취소됨") {
        private fun readResolve(): Any = KakaoCanceledError
    }
    data object KakaoUnknownError : LoginError(2006, "알 수 없는 카카오 오류") {
        private fun readResolve(): Any = KakaoUnknownError
    }

    data object GithubOAuthError : LoginError(3001, "깃허브 OAuth 오류") {
        private fun readResolve(): Any = GithubOAuthError
    }
    data object GithubNetworkError : LoginError(3002, "깃허브 네트워크 오류") {
        private fun readResolve(): Any = GithubNetworkError
    }
    data object GithubCanceledError : LoginError(3003, "깃허브 로그인 취소됨") {
        private fun readResolve(): Any = GithubCanceledError
    }
    data object GithubUnknownError : LoginError(3004, "알 수 없는 깃허브 오류") {
        private fun readResolve(): Any = GithubUnknownError
    }

    data object DatabaseConnectionError : LoginError(4001, "데이터베이스 연결 오류") {
        private fun readResolve(): Any = DatabaseConnectionError
    }
    data object DatabaseSyntaxError : LoginError(4002, "SQL 문법 오류") {
        private fun readResolve(): Any = DatabaseSyntaxError
    }
    data object DatabaseIntegrityError : LoginError(4003, "데이터 무결성 오류") {
        private fun readResolve(): Any = DatabaseIntegrityError
    }
    data object DatabaseTimeoutError : LoginError(4004, "데이터베이스 시간 초과") {
        private fun readResolve(): Any = DatabaseTimeoutError
    }
    data object DatabaseLockError : LoginError(4005, "데이터베이스 잠금 오류") {
        private fun readResolve(): Any = DatabaseLockError
    }
    data object DatabaseUnknownError : LoginError(4006, "알 수 없는 데이터베이스 오류") {
        private fun readResolve(): Any = DatabaseUnknownError
    }

    data object UnknownError : LoginError(9999, "알 수 없는 오류") {
        private fun readResolve(): Any = UnknownError
    }

    fun getFullMessage(): String {
        return when (this) {
            is FirebaseEmailLoginError,
            is FirebasePasswordLoginError,
            is FirebaseInvalidCredentials,
            is FirebaseUserCollision,
            is FirebaseInvalidUser,
            is FirebaseTooManyRequests,
            is FirebaseNetworkError,
            is FirebaseWeakPassword,
            is FirebaseUnknownError -> "이메일 로그인 오류! \n코드번호: $code"

            is KakaoAuthError,
            is KakaoClientError,
            is KakaoServerError,
            is KakaoNetworkError,
            is KakaoCanceledError,
            is KakaoUnknownError -> "카카오 로그인 오류! \n코드번호: $code"

            is GithubOAuthError,
            is GithubNetworkError,
            is GithubCanceledError,
            is GithubUnknownError -> "깃허브 로그인 오류! \n코드번호: $code"

            is DatabaseConnectionError -> "서버 연결이 불안정합니다. 잠시 후 다시 시도해주세요! \n코드번호: $code"

            else -> "알 수 없는 오류! \n코드번호: $code"
        }
    }
}
