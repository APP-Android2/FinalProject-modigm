package kr.co.lion.modigm.util

import android.app.Application
import android.content.ComponentCallbacks2
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.db.HikariCPDataSource

@HiltAndroidApp
class ModigmApplication : Application() {

    companion object {
        val prefs: PreferenceUtil by lazy {
            PreferenceUtil(instance.applicationContext)
        }

        lateinit var instance: ModigmApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // 히카리CP 자원 해제
        CoroutineScope(Dispatchers.IO).launch {
            HikariCPDataSource.closeDataSource()
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            // 히카리CP 자원 해제
            CoroutineScope(Dispatchers.IO).launch {
                HikariCPDataSource.closeDataSource()
            }
        }
    }
}