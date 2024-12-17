package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.KakaoSdk
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentLoginBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.ui.study.CustomExitDialogFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
import kr.co.lion.modigm.util.showLoginSnackBar

class SocialLoginFragment : VBBaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {

    // 뷰모델
    private val viewModel: LoginViewModel by viewModels()

    // 태그
    private val logTag by lazy { SocialLoginFragment::class.simpleName }

    // 백버튼 콜백
    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            private var doubleBackToExitPressedOnce = false

            override fun handleOnBackPressed() {
                // 백버튼을 두 번 눌렀을 때 앱 종료
                if (doubleBackToExitPressedOnce) {
                    // 종료 다이얼로그 표시
                    showExitDialog()
                } else {
                    doubleBackToExitPressedOnce = true
                    // Snackbar를 표시하여 사용자에게 알림
                    requireActivity().showLoginSnackBar("한 번 더 누르면 앱이 종료됩니다.", null)
                    // 2초 후에 doubleBackToExitPressedOnce 플래그 초기화
                    view?.postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kakao SDK 초기화
        KakaoSdk.init(requireContext(), BuildConfig.KAKAO_NATIVE_APP_KEY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 초기 뷰 설정
        initView()

        // 자동 로그인
        autoLogin()

        // ViewModel의 데이터 변경 관찰
        observeViewModel()

        // 백버튼 동작 설정
        backButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 백버튼 콜백 제거
        backPressedCallback.remove()

        viewModel.clearData() // ViewModel 데이터 초기화
    }

    private fun initView() {
        with(binding){

            // Glide를 사용하여 이미지에 블러 효과 적용
            Glide.with(this@SocialLoginFragment)
                .load(R.drawable.background_login2)
                .transform(CenterCrop(), BlurTransformation(5, 3), ColorFilterTransformation(0x60000000))
                .into(imageViewLoginBackground)

            // 스크롤 가능할 때 화살표 표시
            showScrollArrow()
            // 카카오 로그인 버튼 클릭 리스너 설정
            imageButtonLoginKakao.setOnClickListener {
                showLoginLoading()
                Log.i(logTag, "카카오 로그인 버튼 클릭됨")
                viewModel.loginKakao(requireContext())
            }
            // 깃허브 로그인 버튼 클릭 리스너 설정
            imageButtonLoginGithub.setOnClickListener {
                showLoginLoading()
                Log.i(logTag, "깃허브 로그인 버튼 클릭됨")
                viewModel.githubLogin(requireActivity())
            }
            // 다른 방법으로 로그인 버튼 클릭 리스너 설정
            textButtonLoginOther.setOnClickListener {
                Log.i(logTag, "다른 방법으로 로그인 버튼 클릭됨")
                parentFragmentManager.commit {
                    replace<EmailLoginFragment>(R.id.containerMain)
                    addToBackStack(FragmentName.EMAIL_LOGIN.str)
                }
            }
        }
    }

    private fun autoLogin() {
        // 자동 로그인 확인
        val autoLogin = prefs.getBoolean("autoLogin")
        if(autoLogin){
            showLoginLoading()
            viewModel.tryAutoLogin()
        }
    }

    private fun observeViewModel() {
        // 카카오 로그인 데이터 관찰
        viewModel.kakaoLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                hideLoginLoading()
                Log.i(logTag, "카카오 로그인 성공")
                val joinType = JoinType.KAKAO

                // FCM 토큰 등록
                val userIdx = prefs.getInt("currentUserIdx", 0)
                if (userIdx > 0) {
                    registerFcmTokenToServer(userIdx)
                }

                goToBottomNaviFragment(joinType)
            }
        }
        // 깃허브 로그인 데이터 관찰
        viewModel.githubLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                hideLoginLoading()
                Log.i(logTag, "깃허브 로그인 성공")
                val joinType = JoinType.GITHUB

                // FCM 토큰 등록
                val userIdx = prefs.getInt("currentUserIdx", 0)
                if (userIdx > 0) {
                    registerFcmTokenToServer(userIdx)
                }

                goToBottomNaviFragment(joinType)
            }
        }
        // 카카오 회원가입 데이터 관찰
        viewModel.kakaoJoinResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                hideLoginLoading()
                Log.i(logTag, "카카오 회원가입으로 이동")
                val joinType = JoinType.KAKAO
                goToJoinFragment(joinType)
            }
        }
        // 깃허브 회원가입 데이터 관찰
        viewModel.githubJoinResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                hideLoginLoading()
                Log.i(logTag, "깃허브 회원가입으로 이동")
                val joinType = JoinType.GITHUB

                goToJoinFragment(joinType)
            }
        }
        // 이메일 자동로그인 데이터 관찰
        viewModel.emailAutoLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                hideLoginLoading()
                Log.i(logTag, "이메일 로그인 성공")

                val userIdx = prefs.getInt("currentUserIdx", 0)
                Log.d("SocialLoginFragment", "UserIdx after login: $userIdx")  // UserIdx 로그 추가

                // FCM 토큰 등록
                if (userIdx > 0) {
                    Log.d("SocialLoginFragment", "Calling registerFcmTokenToServer")  // 로그 추가
                    registerFcmTokenToServer(userIdx)
                } else {
                    Log.e("SocialLoginFragment", "UserIdx is not valid")
                }

                val joinType = JoinType.EMAIL
                goToBottomNaviFragment(joinType)
            }
        }
        // 카카오 로그인 실패 시 에러 처리
        viewModel.kakaoLoginError.observe(viewLifecycleOwner) { e ->
            if (e != null) {
                hideLoginLoading()
                showLoginErrorDialog(e)
            }
        }
        // 깃허브 로그인 실패 시 에러 처리
        viewModel.githubLoginError.observe(viewLifecycleOwner) { e ->
            if (e != null) {
                hideLoginLoading()
                showLoginErrorDialog(e)
            }
        }
        viewModel.autoLoginError.observe(viewLifecycleOwner) { e ->
            if (e != null) {
                hideLoginLoading()
                requireActivity().showLoginSnackBar(e.message.toString(), null)
            }
        }
    }

    private fun showLoginErrorDialog(e: Throwable) {
        val message = if (e.message != null) {
            e.message.toString()
        } else {
            "알 수 없는 오류!"
        }
        showLoginErrorDialog(message)
    }

    private fun goToJoinFragment(joinType: JoinType) {
        // 회원가입으로 넘겨줄 데이터
        val bundle = Bundle().apply {
            putString("joinType", joinType.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, JoinFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.JOIN.str)
        }
    }

    private fun goToBottomNaviFragment(joinType: JoinType) {

        val bundle = Bundle().apply {
            putString("joinType", joinType.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, BottomNaviFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.BOTTOM_NAVI.str)
        }
    }

    private fun showLoginErrorDialog(message: String) {
        val dialog = CustomLoginErrorDialog(requireContext())
        with(dialog){
            setTitle("오류")
            setMessage(message)
            setPositiveButton("확인") {
                dismiss()
            }
            show()
        }
    }

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

    // 로딩 화면 표시
    private fun showLoginLoading() {
        with(binding){
            layoutLoginLoadingBackground.visibility = View.VISIBLE
        }
    }
    // 로딩 화면 숨기기
    private fun hideLoginLoading() {
        with(binding){
            layoutLoginLoadingBackground.visibility = View.GONE
        }
    }

    // FCM 토큰을 가져와 서버에 등록
    private fun registerFcmTokenToServer(userIdx: Int) {
        Log.d("SocialLoginFragment", "Attempting to fetch FCM Token...")
        FirebaseMessaging.getInstance().deleteToken() // 기존 토큰 삭제 (필요한 경우)
            .addOnCompleteListener { deleteTask ->
                if (!deleteTask.isSuccessful) {
                    Log.e("SocialLoginFragment", "FCM 토큰 삭제 실패", deleteTask.exception)
                    return@addOnCompleteListener
                }

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        // 여기에서 실패 원인을 로그로 찍음
                        Log.e(
                            "SocialLoginFragment",
                            "Fetching FCM registration token failed",
                            task.exception
                        )
                        return@addOnCompleteListener
                    }

                    val token = task.result
                    Log.d("SocialLoginFragment", "FCM Token: $token")

                    // 토큰이 null이 아닌지 확인하고 서버에 등록하는 로직
                    if (token != null) {
                        Log.d("SocialLoginFragment", "FCM Token: $token")
                        // FCM 토큰을 ViewModel을 통해 서버에 등록
                        viewModel.registerFcmToken(userIdx, token)
                    } else {
                        Log.e("SocialLoginFragment", "FCM Token is null")
                    }
                }
            }
    }

    // 백버튼 종료 동작
    private fun backButton() {
        // 백버튼 콜백을 안전하게 추가
        backPressedCallback.let { callback ->
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        }
    }

    private fun showExitDialog() {
        val dialog = CustomExitDialogFragment()
        dialog.show(parentFragmentManager, "CustomExitDialog")
    }
}