package kr.co.lion.modigm.ui

import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.transition.MaterialSharedAxis
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.ActivityMainBinding
import kr.co.lion.modigm.ui.chat.ChatFragment
import kr.co.lion.modigm.ui.chat.ChatGroupFragment
import kr.co.lion.modigm.ui.chat.ChatOnetoOneFragment
import kr.co.lion.modigm.ui.chat.ChatRoomFragment
import kr.co.lion.modigm.ui.detail.DetailEditFragment
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.detail.DetailMemberFragment
import kr.co.lion.modigm.ui.join.JoinDuplicateFragment
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.like.LikeFragment
import kr.co.lion.modigm.ui.login.LoginFragment
import kr.co.lion.modigm.ui.login.OtherLoginFragment
import kr.co.lion.modigm.ui.profile.EditProfileFragment
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.ui.profile.ProfileWebFragment
import kr.co.lion.modigm.ui.profile.SettingsFragment
import kr.co.lion.modigm.ui.profile.vm.EditProfileViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.ui.study.FilterSortFragment
import kr.co.lion.modigm.ui.study.StudyAllFragment
import kr.co.lion.modigm.ui.study.StudyFragment
import kr.co.lion.modigm.ui.study.StudyMyFragment
import kr.co.lion.modigm.ui.write.WriteFragment
import kr.co.lion.modigm.util.FragmentName

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.commit {
            replace(R.id.containerMain, LoginFragment())
        }
    }
}