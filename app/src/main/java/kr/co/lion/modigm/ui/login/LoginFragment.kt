package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.firebase.auth.AuthCredential
import com.kakao.sdk.common.KakaoSdk
import jp.wasabeef.glide.transformations.BlurTransformation
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentLoginBinding
import kr.co.lion.modigm.ui.ViewBindingFragment
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.showLoginSnackBar

class LoginFragment : ViewBindingFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {

    private val viewModel: LoginViewModel by viewModels()  // LoginViewModel 인스턴스 생성

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Kakao SDK 초기화
        KakaoSdk.init(requireContext(), BuildConfig.KAKAO_NATIVE_APP_KEY)

        // 초기 뷰 설정
        initView()

        // Glide를 사용하여 이미지에 블러 효과 적용
        Glide.with(this)
            .load(R.drawable.background_login2)
            .transform(CenterCrop(), BlurTransformation(5, 3)) // 블러 반경과 샘플 크기 설정
            .into(binding.imageViewLoginBackground)


        // 자동 로그인 확인
        viewModel.attemptAutoLogin()



        // ViewModel의 데이터 변경 관찰
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.clearData() // ViewModel 데이터 초기화
    }

    // --------------------------------- LC END ---------------------------------

    /**
     * 초기 뷰 설정 메서드
     */
    private fun initView() {
        with(binding){

            // 스크롤 가능할 때
            showScrollArrow()

            // 카카오 로그인 버튼 클릭 리스너 설정
            imageButtonLoginKakao.setOnClickListener {
                Log.i("LoginFragment", "카카오 로그인 버튼 클릭됨")
                viewModel.loginWithKakao(requireContext())
            }

            // 깃허브 로그인 버튼 클릭 리스너 설정
            imageButtonLoginGithub.setOnClickListener {
                Log.i("LoginFragment", "깃허브 로그인 버튼 클릭됨")
                viewModel.loginWithGithub(requireActivity())
            }

            // 다른 방법으로 로그인 버튼 클릭 리스너 설정
            textButtonLoginOther.setOnClickListener {
                Log.i("LoginFragment", "다른 방법으로 로그인 버튼 클릭됨")
                parentFragmentManager.commit {
                    replace<OtherLoginFragment>(R.id.containerMain)
                    addToBackStack(FragmentName.OTHER_LOGIN.str)
                }
            }
        }
    }

    /**
     * ViewModel의 데이터 변경을 관찰하는 메서드
     */
    private fun observeViewModel() {

        // 깃허브 로그인 데이터 관찰
        viewModel.githubLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Log.i("LoginFragment", "깃허브 로그인 성공")
                requireActivity().showLoginSnackBar("깃허브 로그인 성공", R.drawable.icon_github_logo)
                navigateToBottomNaviFragment()
            }
        }
        // 이메일 로그인 실패 시 에러 처리
        viewModel.githubLoginError.observe(viewLifecycleOwner) { e ->
            handleLoginError(e)
        }

        // 카카오 로그인 데이터 관찰
        viewModel.kakaoLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Log.i("LoginFragment", "카카오 로그인 성공")
                requireActivity().showLoginSnackBar("카카오 로그인 성공", R.drawable.kakaotalk_sharing_btn_small)
                navigateToBottomNaviFragment()
            }
        }
        // 카카오 로그인 실패 시 에러 처리
        viewModel.kakaoLoginError.observe(viewLifecycleOwner) { e ->
            handleLoginError(e)
        }
    }

    /**
     * 로그인 오류 처리 메서드
     * @param e 발생한 오류
     */
    private fun handleLoginError(e: Throwable) {
        val message = if (e is LoginError) {
            e.getFullMessage()
        } else {
            "알 수 없는 오류!\n코드번호: 9999"
        }
        requireActivity().showLoginSnackBar(message, R.drawable.icon_error_24px)
    }

    /**
     * 회원가입 화면으로 이동하는 메서드
     * @param token 로그인 토큰
     * @param joinType 회원가입 타입
     */
    private fun navigateToJoinFragment(token: Any, joinType: JoinType) {
        // 회원가입으로 넘겨줄 데이터
        val bundle = Bundle().apply {
            when (token) {
                is String -> {
                    putString("customToken", token)
                }

                is AuthCredential -> {
                    putParcelable("credential", token)
                }
            }
            putString("joinType", joinType.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, JoinFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.JOIN.str)
        }
    }

    /**
     * BottomNaviFragment로 이동하는 메서드
     */
    private fun navigateToBottomNaviFragment() {
        parentFragmentManager.commit {
            replace(R.id.containerMain, BottomNaviFragment())
            addToBackStack(FragmentName.BOTTOM_NAVI.str)
        }
    }

    /**
     * 스크롤 가능할 때 화살표 보여주는 메서드
     */
    private fun showScrollArrow() {
        with(binding){
            // 화살표 바인딩
            with(imageViewLoginScrollArrow) {
                // 애니메이션 설정
                val floatAnimation = AnimationUtils.loadAnimation(context, R.anim.breathing_up_down).apply {
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(p0: Animation?) { }
                        override fun onAnimationEnd(animation: Animation?) {
                            // 애니메이션 끝나면 화살표 가시성 업데이트
                            visibility = if (scrollViewLogin.canScrollVertically(1)) View.VISIBLE else View.GONE
                        }
                        override fun onAnimationRepeat(p0: Animation?) { }
                    })
                }

                // 화살표 보이기/숨기기 업데이트 함수
                fun updateVisibility() {
                    visibility = if (scrollViewLogin.canScrollVertically(1)) View.VISIBLE else View.GONE
                }

                // 초기 상태에 따라 화살표 보이기/숨기기
                updateVisibility()
                if (visibility == View.VISIBLE) startAnimation(floatAnimation)

                // 스크롤할 때 화살표 상태 업데이트
                scrollViewLogin.setOnScrollChangeListener { v, _, _, _, _ ->
                    updateVisibility()
                    if (visibility == View.VISIBLE) startAnimation(floatAnimation) else clearAnimation()

                    // 스크롤이 맨 위에 도달하면 화살표 보이기
                    if (!v.canScrollVertically(-1)) visibility = View.VISIBLE
                }
            }
        }
    }
}