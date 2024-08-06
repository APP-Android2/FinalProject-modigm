package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.util.Log
import android.view.View
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
        viewModel.tryAutoLogin()



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

        // 깃허브 회원가입 데이터 관찰
        viewModel.githubJoinResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Log.i(tag, "깃허브 회원가입으로 이동")
                val joinType = JoinType.GITHUB
                navigateToJoinFragment(joinType)
            }
        }

        viewModel.kakaoJoinResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Log.i(tag, "카카오 회원가입으로 이동")
                val joinType = JoinType.KAKAO
                navigateToJoinFragment(joinType)
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
}