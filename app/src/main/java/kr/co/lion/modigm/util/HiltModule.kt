package kr.co.lion.modigm.util

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

/**
 *
 * Hilt를 사용하여 객체 주입이 필요한 경우 아래의 HiltModule 클래스에서
 * 객체를 반환하는 함수를 작성하면 됩니다.
 * 함수 이름 위에 @Provides 어노테이션을 꼭 붙여주셔야 사용할 수 있습니다.
 * 반환 객체가 싱글톤 객체인 경우에는 @Singleton을 붙여주면 됩니다.
 *
 */

@Module
@InstallIn(ActivityRetainedComponent ::class)
object HiltModule {

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

}