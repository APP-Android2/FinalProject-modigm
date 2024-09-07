package kr.co.lion.modigm.ui.profile

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.CustomLogoutDialogBinding
import kr.co.lion.modigm.databinding.FragmentSettingsBinding
import kr.co.lion.modigm.ui.DBBaseFragment
import kr.co.lion.modigm.ui.login.LoginFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.Links
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class SettingsFragment(private val profileFragment: ProfileFragment): DBBaseFragment<FragmentSettingsBinding>(R.layout.fragment_settings) {

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
                parentFragmentManager.popBackStack()
            }
        })
    }

    private fun setupToolbar() {
        binding.toolbarSettings.apply {
            // title
            title = "설정"

            // 뒤로 가기
            setNavigationIcon(R.drawable.icon_arrow_back_24px)
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun setupButtons() {
        binding.apply {
            // 회원 정보 수정
            layoutSettingsEditInfo.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, EditProfileFragment(profileFragment))
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
                        replace(R.id.containerMain, ChangePasswordEmailFragment())
                        addToBackStack(FragmentName.CHANGE_PASSWORD_EMAIL.str)
                    }
                }
            }

            // 공지사항
            layoutSettingsNotice.setOnClickListener {
                openWebView(Links.NOTICE.url)
            }

            // 고객센터
            layoutSettingsService.setOnClickListener {
                openWebView(Links.SERVICE.url)
            }

            // 이용약관
            layoutSettingsTerms.setOnClickListener {
                openWebView(Links.TERMS.url)
            }

            // 로그아웃
            layoutSettingsLogout.setOnClickListener {
                // 로그아웃 확인 다이얼로그
                val customDialogBinding = CustomLogoutDialogBinding.inflate(layoutInflater)
                val builder = MaterialAlertDialogBuilder(requireContext(), R.style.dialogColor)
                    .setView(customDialogBinding.root)
                    .setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
                        // SharedPreferences 초기화
                        prefs.clearAllPrefs()
                        prefs.setBoolean("autoLogin", false)

                        // 로그아웃 처리
                        Firebase.auth.signOut()

                        // 로그인 화면으로 돌아간다
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.containerMain, LoginFragment())
                            .addToBackStack(null)
                            .commit()
                    }.setNegativeButton("취소") { dialogInterface: DialogInterface, i: Int -> }
                builder.show()
            }
        }
    }

    private fun openWebView(url: String){
        viewLifecycleOwner.lifecycleScope.launch {
            // bundle 에 필요한 정보를 담는다
            val bundle = Bundle()
            bundle.putString("link", url)

            // 이동할 프래그먼트로 bundle을 넘긴다
            val profileWebFragment = ProfileWebFragment()
            profileWebFragment.arguments = bundle

            // Fragment 교체
            parentFragmentManager.commit {
                add(R.id.containerMain, profileWebFragment)
                addToBackStack(FragmentName.PROFILE_WEB.str)
            }
        }
    }
}