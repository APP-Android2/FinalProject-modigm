package kr.co.lion.modigm.util

import android.app.Application
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ModigmApplication : Application() {

    companion object {
        val prefs: PreferenceUtil by lazy {
            PreferenceUtil(instance.applicationContext)
        }

        lateinit var instance: ModigmApplication
            private set

        lateinit var preloadedAdView: AdView // 사전 로딩된 광고를 저장할 변수
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // AdMob 초기화
        MobileAds.initialize(this) {
            // 초기화가 완료되면 배너 광고를 사전 로드
            preloadAdBanner()
        }
    }

    // 배너 광고를 미리 로드하는 함수
    private fun preloadAdBanner() {
        // AdView 생성 및 설정
        val adSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(this, 320)
        preloadedAdView = AdView(this).apply {
            adUnitId = "ca-app-pub-3940256099942544/9214589741" // 실제 광고 단위 ID로 변경
            setAdSize(adSize) // adSize는 여기서 설정
        }

        // 광고 요청을 생성하고 로드
        val adRequest = AdRequest.Builder().build()
        preloadedAdView.loadAd(adRequest)
    }
}
