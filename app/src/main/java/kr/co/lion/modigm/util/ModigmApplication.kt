package kr.co.lion.modigm.util

import android.app.Application

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
}