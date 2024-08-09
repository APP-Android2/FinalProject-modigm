package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentSettingsBinding
import kr.co.lion.modigm.ui.login.LoginFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class SettingsFragment(private val profileFragment: ProfileFragment) : Fragment() {
    lateinit var fragmentSettingsBinding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentSettingsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)

        return fragmentSettingsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        setupBackButton()
        setupToolbar()
        setupButtons()
    }

    private fun setupBackButton() {
        // 뒤로 가기 물리키
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().supportFragmentManager.popBackStack()
            }
        })
    }

    private fun setupToolbar() {
        fragmentSettingsBinding.apply {
            toolbarSettings.apply {
                // title
                title = "설정"

                // 뒤로 가기
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun setupButtons() {
        fragmentSettingsBinding.apply {
            // 회원 정보 수정
            layoutSettingsEditInfo.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .add(R.id.containerMain, EditProfileFragment(profileFragment))
                    .addToBackStack(FragmentName.EDIT_PROFILE.str)
                    .commit()
            }

            // 비밀번호 변경 메뉴
            with(layoutSettingsEditPw) {
                // 로그인 방식
                val currentUserProvider = prefs.getString("currentUserProvider")
                // 로그인 방식에 따른 메뉴 표시
                visibility = when (currentUserProvider) {
                    // 이메일 로그인인 경우
                    JoinType.EMAIL.provider -> {
                        View.VISIBLE
                    }
                    // 소셜 로그인인 경우
                    else -> {
                        View.GONE
                    }
                }
                // 버튼 클릭 시
                setOnClickListener {
                    parentFragmentManager.commit {
                        add(R.id.containerMain, ChangePasswordEmailFragment())
                        addToBackStack(FragmentName.CHANGE_PASSWORD_EMAIL.str)
                    }
                }
            }

            // 로그아웃 (오류뜸, clearBackStack 수정 필요)
            layoutSettingsLogout.setOnClickListener {
                // 로그아웃 처리
                Firebase.auth.signOut()
                // SharedPreferences 초기화
                prefs.clearAllPrefs()
                // Backstack 모두 제거하고 로그인 화면으로 돌아간다
                clearBackStack()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, LoginFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    // Backstack 의 모든 fragment 제거
    private fun clearBackStack() {
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}