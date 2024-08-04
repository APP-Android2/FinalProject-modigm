package kr.co.lion.modigm.ui.login

sealed class LoginError(private val code: String, override val message: String) : Throwable(message) {
    data object FirebaseEmailLoginError : LoginError("ERROR_INVALID_EMAIL", "이메일 로그인 실패") {
        private fun readResolve(): Any = FirebaseEmailLoginError
    }
    data object FirebasePasswordLoginError : LoginError("ERROR_WRONG_PASSWORD", "비밀번호 로그인 실패") {
        private fun readResolve(): Any = FirebasePasswordLoginError
    }
    data object FirebaseInvalidCredentials : LoginError("ERROR_INVALID_CREDENTIAL", "유효하지 않은 자격 증명") {
        private fun readResolve(): Any = FirebaseInvalidCredentials
    }
    data object FirebaseUserCollision : LoginError("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL", "사용자 충돌") {
        private fun readResolve(): Any = FirebaseUserCollision
    }
    data object FirebaseInvalidUser : LoginError("ERROR_USER_NOT_FOUND", "유효하지 않은 사용자") {
        private fun readResolve(): Any = FirebaseInvalidUser
    }
    data object FirebaseTooManyRequests : LoginError("ERROR_TOO_MANY_REQUESTS", "요청이 너무 많음") {
        private fun readResolve(): Any = FirebaseTooManyRequests
    }
    data object FirebaseNetworkError : LoginError("ERROR_NETWORK_REQUEST_FAILED", "네트워크 오류") {
        private fun readResolve(): Any = FirebaseNetworkError
    }
    data object FirebaseWeakPassword : LoginError("ERROR_WEAK_PASSWORD", "약한 비밀번호") {
        private fun readResolve(): Any = FirebaseWeakPassword
    }
    data object FirebaseVerificationError : LoginError("ERROR_INVALID_VERIFICATION_CODE", "전화 인증 실패") {
        private fun readResolve(): Any = FirebaseVerificationError
    }
    data object FirebaseUnknownError : LoginError("ERROR_INTERNAL_ERROR", "알 수 없는 Firebase 오류") {
        private fun readResolve(): Any = FirebaseUnknownError
    }

    data object KakaoAuthError : LoginError("-1", "카카오 인증 오류") {
        private fun readResolve(): Any = KakaoAuthError
    }
    data object KakaoServerError : LoginError("-2", "카카오 내부 서버 오류") {
        private fun readResolve(): Any = KakaoServerError
    }
    data object KakaoInvalidParameter : LoginError("-3", "잘못된 매개변수 오류") {
        private fun readResolve(): Any = KakaoInvalidParameter
    }
    data object KakaoPermissionError : LoginError("-5", "카카오 권한 오류") {
        private fun readResolve(): Any = KakaoPermissionError
    }
    data object KakaoAppKeyError : LoginError("-10", "앱키를 확인해주세요") {
        private fun readResolve(): Any = KakaoAppKeyError
    }
    data object KakaoClientError : LoginError("-301", "카카오 클라이언트 오류") {
        private fun readResolve(): Any = KakaoClientError
    }
    data object KakaoAuthError401 : LoginError("-401", "카카오 인증 오류") {
        private fun readResolve(): Any = KakaoAuthError401
    }
    data object KakaoPermissionError403 : LoginError("-403", "카카오 권한 오류") {
        private fun readResolve(): Any = KakaoPermissionError403
    }
    data object KakaoNotFoundError : LoginError("-404", "존재하지 않는 리소스 오류") {
        private fun readResolve(): Any = KakaoNotFoundError
    }
    data object KakaoServerError500 : LoginError("-500", "카카오 서버 오류") {
        private fun readResolve(): Any = KakaoServerError500
    }
    data object KakaoUnknownError : LoginError("-777", "알 수 없는 카카오 오류") {
        private fun readResolve(): Any = KakaoUnknownError
    }

    // 깃허브 로그인 시 발생할 수 있는 오류
    data object GithubOAuthError : LoginError("401", "깃허브 인증 오류") {
        private fun readResolve(): Any = GithubOAuthError
    }
    data object GithubForbiddenError : LoginError("403", "깃허브 권한 오류") {
        private fun readResolve(): Any = GithubForbiddenError
    }
    data object GithubNotFoundError : LoginError("404", "존재하지 않는 깃허브 리소스") {
        private fun readResolve(): Any = GithubNotFoundError
    }
    data object GithubServerError : LoginError("500", "깃허브 서버 오류") {
        private fun readResolve(): Any = GithubServerError
    }
    data object GithubBadGateway : LoginError("502", "잘못된 게이트웨이") {
        private fun readResolve(): Any = GithubBadGateway
    }
    data object GithubServiceUnavailable : LoginError("503", "서비스 이용 불가") {
        private fun readResolve(): Any = GithubServiceUnavailable
    }
    data object GithubUnknownError : LoginError("-1", "알 수 없는 깃허브 오류") {
        private fun readResolve(): Any = GithubUnknownError
    }

    // 데이터베이스 연결 오류 세분화
    data object DatabaseConnectionError : LoginError("4001", "데이터베이스 연결 오류") {
        private fun readResolve(): Any = DatabaseConnectionError
    }
    data object DatabaseSyntaxError : LoginError("42000", "SQL 문법 오류") {
        private fun readResolve(): Any = DatabaseSyntaxError
    }
    data object DatabaseIntegrityError : LoginError("23000", "데이터 무결성 오류") {
        private fun readResolve(): Any = DatabaseIntegrityError
    }
    data object DatabaseTimeoutError : LoginError("4004", "데이터베이스 시간 초과") {
        private fun readResolve(): Any = DatabaseTimeoutError
    }
    data object DatabaseLockError : LoginError("4005", "데이터베이스 잠금 오류") {
        private fun readResolve(): Any = DatabaseLockError
    }
    data object DatabaseUnknownError : LoginError("4006", "알 수 없는 데이터베이스 오류") {
        private fun readResolve(): Any = DatabaseUnknownError
    }
    data object DatabaseNetworkError : LoginError("08S01", "데이터베이스 네트워크 오류") {
        private fun readResolve(): Any = DatabaseNetworkError
    }
    data object DatabaseAuthenticationError : LoginError("28000", "데이터베이스 인증 오류") {
        private fun readResolve(): Any = DatabaseAuthenticationError
    }
    data object DatabasePermissionError : LoginError("42000", "데이터베이스 권한 오류") {
        private fun readResolve(): Any = DatabasePermissionError
    }
    data object DatabaseDiskError : LoginError("4009", "데이터베이스 디스크 오류") {
        private fun readResolve(): Any = DatabaseDiskError
    }
    data object DatabaseConstraintError : LoginError("4003", "데이터베이스 제약 조건 위반") {
        private fun readResolve(): Any = DatabaseConstraintError
    }
    data object DatabaseDataCorruptionError : LoginError("4007", "데이터베이스 데이터 손상") {
        private fun readResolve(): Any = DatabaseDataCorruptionError
    }
    data object DatabaseShutdownError : LoginError("4008", "데이터베이스 셧다운 오류") {
        private fun readResolve(): Any = DatabaseShutdownError
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
            is FirebaseUnknownError,
            is FirebaseVerificationError -> "이메일 로그인 오류! \n코드번호: $code"

            is KakaoAuthError,
            is KakaoServerError,
            is KakaoInvalidParameter,
            is KakaoPermissionError,
            is KakaoAppKeyError,
            is KakaoClientError,
            is KakaoAuthError401,
            is KakaoPermissionError403,
            is KakaoNotFoundError,
            is KakaoServerError500,
            is KakaoUnknownError -> "카카오 로그인 오류! \n코드번호: $code"

            is GithubOAuthError,
            is GithubForbiddenError,
            is GithubNotFoundError,
            is GithubServerError,
            is GithubBadGateway,
            is GithubServiceUnavailable,
            is GithubUnknownError -> "깃허브 로그인 오류! \n코드번호: $code"

            is DatabaseConnectionError,
            is DatabaseSyntaxError,
            is DatabaseIntegrityError,
            is DatabaseTimeoutError,
            is DatabaseLockError,
            is DatabaseNetworkError,
            is DatabaseAuthenticationError,
            is DatabasePermissionError,
            is DatabaseDiskError,
            is DatabaseConstraintError,
            is DatabaseDataCorruptionError,
            is DatabaseShutdownError-> "서버 연결 오류! \n코드번호: $code"

            else -> "알 수 없는 오류! \n코드번호: $code"
        }
    }
}
