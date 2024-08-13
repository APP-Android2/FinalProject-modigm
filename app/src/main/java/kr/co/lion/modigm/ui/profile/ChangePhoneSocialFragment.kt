package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChangePhoneSocialBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.profile.vm.ChangePhoneViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class ChangePhoneSocialFragment : VBBaseFragment<FragmentChangePhoneSocialBinding>(FragmentChangePhoneSocialBinding::inflate) {

    // 뷰모델 선언
    private val viewModel: ChangePhoneViewModel by viewModels()

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 초기화
        initView()
        // 뷰모델 관찰
        observeViewModel()
        // 소셜 로그인 재인증
        viewModel.socialLoginReAuthenticate(requireActivity(), getCurrentUserProvider())

    }

    // --------------------------------- LC END ---------------------------------

    // 뷰 초기화
    private fun initView() {
        with(binding) {

            // 툴바
            with(toolbarChangePhoneSocial) {
                title = "전화번호 변경"
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 재인증 버튼
            with(buttonChangePhoneNext) {
                isEnabled = false // 버튼을 처음에 비활성화
                setOnClickListener {
                    // 소셜 로그인 다시 인증
                    viewModel.socialLoginReAuthenticate(requireActivity(), getCurrentUserProvider())
                    progressBarChangePhoneSocial.visibility = View.VISIBLE
                    isEnabled = false
                }
            }
        }
    }

    // 뷰모델 관찰
    private fun observeViewModel() {
        // 인증 완료
        viewModel.isSocialReAuthComplete.observe(viewLifecycleOwner) { isSocialReAuthComplete ->
            if (isSocialReAuthComplete) {
                // 전화번호 변경 인증 화면으로 이동
                moveToNext()
            }
        }

        // 인증 에러
        viewModel.socialReAuthError.observe(viewLifecycleOwner) { error ->
            with(binding) {
                if (error != null) {
                    // 프로그래스바 숨기기
                    progressBarChangePhoneSocial.visibility = View.GONE
                    // 메세지 변경
                    textViewChangePhoneSocialMessage.text = "인증에 실패했습니다.\n다시 시도해주세요."
                    // 버튼 활성화
                    buttonChangePhoneNext.isEnabled = true
                }
            }
        }
    }



    // 전화번호 변경 인증 화면으로 이동
    private fun moveToNext() {
        // 완료 여부는 초기화해서 popStackBack으로 돌아와도 문제 없게
        viewModel.isSocialReAuthCompleteTo(false)
        val currentUserPhone = viewModel.currentUserPhone.value ?: ""
        val changePhoneAuthFragment = ChangePhoneAuthFragment().apply {
            arguments = Bundle().apply {
                putString("currentUserPhone", currentUserPhone)
            }
        }

        // 전화번호 변경 인증 화면으로 이동
        parentFragmentManager.commit {
            replace(R.id.containerMain, changePhoneAuthFragment)
            addToBackStack(FragmentName.CHANGE_PHONE_AUTH.str)
        }
    }

    // 현재 사용자의 소셜 로그인 제공자 가져오기
    private fun getCurrentUserProvider():String{
        return prefs.getString("currentUserProvider")
    }
}