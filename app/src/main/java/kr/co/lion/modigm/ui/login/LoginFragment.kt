package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.kakao.sdk.common.KakaoSdk
import jp.wasabeef.glide.transformations.BlurTransformation
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentLoginBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class LoginFragment : VBBaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {

    private val viewModel: LoginViewModel by viewModels()  // LoginViewModel 인스턴스 생성

    // --------------------------------- LC START ---------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kakao SDK 초기화
        KakaoSdk.init(requireContext(), BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 초기 뷰 설정
        initView()
        // Glide를 사용하여 이미지에 블러 효과 적용
        Glide.with(this)
            .load(R.drawable.background_login2)
            .transform(CenterCrop(), BlurTransformation(5, 3))
            .into(binding.imageViewLoginBackground)
        // 자동 로그인 확인
        val autoLogin = prefs.getBoolean("autoLogin")
        if(autoLogin){
            viewModel.tryAutoLogin()
        }
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
                Log.i(tag, "카카오 로그인 버튼 클릭됨")
                viewModel.loginKakao(requireContext())
            }
            // 깃허브 로그인 버튼 클릭 리스너 설정
            imageButtonLoginGithub.setOnClickListener {
                Log.i(tag, "깃허브 로그인 버튼 클릭됨")
                viewModel.githubLogin(requireActivity())
            }
            // 다른 방법으로 로그인 버튼 클릭 리스너 설정
            textButtonLoginOther.setOnClickListener {
                Log.i(tag, "다른 방법으로 로그인 버튼 클릭됨")
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
        // 카카오 로그인 데이터 관찰
        viewModel.kakaoLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Log.i(tag, "카카오 로그인 성공")
                val joinType = JoinType.KAKAO
                navigateToBottomNaviFragment(joinType)
            }
        }
        // 깃허브 로그인 데이터 관찰
        viewModel.githubLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Log.i(tag, "깃허브 로그인 성공")
                val joinType = JoinType.GITHUB
                navigateToBottomNaviFragment(joinType)
            }
        }
        // 카카오 회원가입 데이터 관찰
        viewModel.kakaoJoinResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Log.i(tag, "카카오 회원가입으로 이동")
                val joinType = JoinType.KAKAO
                navigateToJoinFragment(joinType)
            }
        }
        // 깃허브 회원가입 데이터 관찰
        viewModel.githubJoinResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Log.i(tag, "깃허브 회원가입으로 이동")
                val joinType = JoinType.GITHUB
                navigateToJoinFragment(joinType)
            }
        }
        // 이메일 자동로그인 데이터 관찰
        viewModel.emailAutoLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Log.i("LoginFragment", "이메일 로그인 성공")
                val joinType = JoinType.EMAIL
                navigateToBottomNaviFragment(joinType)
            }
        }
        // 카카오 로그인 실패 시 에러 처리
        viewModel.kakaoLoginError.observe(viewLifecycleOwner) { e ->
            if (e != null) {
                showLoginErrorDialog(e)
            }
        }
        // 깃허브 로그인 실패 시 에러 처리
        viewModel.githubLoginError.observe(viewLifecycleOwner) { e ->
            if (e != null) {
                showLoginErrorDialog(e)
            }
        }
    }

    /**
     * 로그인 오류 처리 메서드
     * @param e 발생한 오류
     */
    private fun showLoginErrorDialog(e: Throwable) {
        val message = if (e.message != null) {
            e.message.toString()
        } else {
            "알 수 없는 오류!"
        }
        showLoginErrorDialog(message)
    }

    /**
     * 회원가입 화면으로 이동하는 메서드
     * @param joinType 회원가입 타입
     */
    private fun navigateToJoinFragment(joinType: JoinType) {
        // 회원가입으로 넘겨줄 데이터
        val bundle = Bundle().apply {
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
    private fun navigateToBottomNaviFragment(joinType: JoinType) {

        val bundle = Bundle().apply {
            putString("joinType", joinType.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, BottomNaviFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.BOTTOM_NAVI.str)
        }
    }

    // 오류 다이얼로그 표시
    private fun showLoginErrorDialog(message: String) {
        val dialog = CustomLoginErrorDialog(requireContext())
        dialog.setTitle("오류")
        dialog.setMessage(message)
        dialog.setPositiveButton("확인") {
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * 스크롤 가능할 때 화살표 보여주는 메서드
     */
    private fun showScrollArrow() {
        with(binding){
            // 화살표 바인딩
            with(imageViewLoginScrollArrow) {
                // 화살표 보이기/숨기기 업데이트 함수
                fun updateVisibility() {
                    visibility = if (scrollViewLogin.canScrollVertically(1)) View.VISIBLE else View.GONE
                }
                // 애니메이션 설정
                val floatAnimation = AnimationUtils.loadAnimation(context, R.anim.breathing_up_down).apply {
                    setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(p0: Animation?) { }
                        override fun onAnimationEnd(animation: Animation?) {
                            // 애니메이션 끝나면 화살표 가시성 업데이트
                            updateVisibility() // 애니메이션 끝난 후에도 가시성 업데이트
                        }
                        override fun onAnimationRepeat(p0: Animation?) { }
                    })
                }
                // 레이아웃이 완전히 초기화된 후에 가시성 업데이트
                scrollViewLogin.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        // 초기 상태에 따라 화살표 보이기/숨기기
                        updateVisibility()
                        if (visibility == View.VISIBLE) startAnimation(floatAnimation)
                        // 리스너 제거
                        scrollViewLogin.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
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