package kr.co.lion.modigm.util

import android.app.Application

class ModigmApplication: Application(){
    companion object {
        lateinit var prefs: PreferenceUtil
    }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate()
    }
}