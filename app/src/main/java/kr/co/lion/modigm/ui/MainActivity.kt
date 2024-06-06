package kr.co.lion.modigm.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.LoginFragment

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.commit {
            replace(R.id.containerMain, LoginFragment())
        }
    }
}