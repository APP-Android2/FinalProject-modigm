package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentSettingsBinding
import kr.co.lion.modigm.ui.DBBaseFragment
import kr.co.lion.modigm.ui.profile.popup.LogoutAdDialog
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.Links
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class SettingsFragment: DBBaseFragment<FragmentSettingsBinding>(R.layout.fragment_settings) {
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
                    .replace(R.id.containerMain, EditProfileFragment())
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
                val logoutAdDialog = LogoutAdDialog()
                // 알림창이 띄워져있는 동안 배경 클릭 막기
                //logoutAdDialog.isCancelable = false
                logoutAdDialog.show(parentFragmentManager, "LogoutAdDialog")
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