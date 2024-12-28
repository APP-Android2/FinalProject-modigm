package kr.co.lion.modigm.ui.login.email

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentEmailLoginBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.CustomLoginErrorDialog
import kr.co.lion.modigm.ui.login.FindEmailFragment
import kr.co.lion.modigm.ui.login.FindPasswordFragment
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.hideSoftInput
import kr.co.lion.modigm.util.shake
import kr.co.lion.modigm.util.showSoftInput

class EmailLoginFragment : VBBaseFragment<FragmentEmailLoginBinding>(FragmentEmailLoginBinding::inflate) {

    // 뷰모델
    private val viewModel: EmailLoginViewModel by viewModels()  // LoginViewModel 인스턴스 생성

    // 태그
    private val logTag by lazy { EmailLoginFragment::class.simpleName }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val isLoading by viewModel.isLoading.observeAsState(false)
                val emailLoginResult by viewModel.emailLoginResult.observeAsState(false)
                val emailLoginError by viewModel.emailLoginError.observeAsState()

                EmailLoginScreen(
                    isLoading = isLoading,
                    emailLoginResult = emailLoginResult,
                    emailLoginError = emailLoginError,
                    onNavigateToBottomNaviFragment = { joinType -> navigateToBottomNaviFragment(joinType) },
                    onNavigateToFindEmailFragment = { navigateToFindEmailFragment() },
                    onNavigateToFindPasswordFragment = { navigateToFindPasswordFragment() },
                    onNavigateToJoinFragment = { joinType -> navigateToJoinFragment(joinType) },
                    showLoginErrorDialog = { error -> showErrorDialog(error) },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView() // 초기 UI 설정
        observeViewModel() // ViewModel의 데이터 변경 관찰
    }

    override fun onResume() {
        super.onResume()
        // 이메일 텍스트 필드 포커싱 및 소프트키보드 보여주기
        with(binding) {
            requireActivity().showSoftInput(textInputEditOtherEmail)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.clearViewModelData() // ViewModel 데이터 초기화
    }

    private fun initView() {
        with(binding){

            // 실시간 텍스트 변경 감지 설정
            textInputEditOtherEmail.apply{
                // 텍스트 변경 시
                addTextChangedListener(inputWatcher)

                // 포커스 변경 시
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        // 이메일 필드가 포커스를 받을 때 해당 필드로 스크롤
                        otherLoginScrollView.smoothScrollTo(0, textViewOtherTitle.top)
                        requireActivity().showSoftInput(this)
                    } else {
                        // 이메일 필드가 포커스를 잃으면 키보드 숨기기
                        requireActivity().hideSoftInput()
                    }
                }
            }
            // 비밀번호 입력
            textInputEditOtherPassword.apply {
                // 텍스트 변경 시
                addTextChangedListener(inputWatcher)
                // 에디터 액션 설정
                setOnEditorActionListener { _, actionId, _ ->
                    // 엔터키 입력 시 로그인 시도
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        buttonOtherLogin.performClick() // 로그인 버튼 클릭
                        true
                    } else {
                        false
                    }
                }
                // 포커스 변경 시
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        // 비밀번호 필드가 포커스를 받을 때 해당 필드로 스크롤
                        otherLoginScrollView.smoothScrollTo(0, textViewOtherSecondTitle.top)
                        requireActivity().showSoftInput(this)
                    }
                    if(!hasFocus){
                        // 비밀번호 필드가 포커스를 잃으면 키보드 숨기기
                        requireActivity().hideSoftInput()
                    }
                }
            }

            // 로그인 버튼 초기값을 비활성화 상태로 설정
            buttonOtherLogin.isEnabled = false

            // 로그인 버튼 클릭 시 로그인 시도
            buttonOtherLogin.setOnClickListener {
                if(!checkAllInput()) {
                    return@setOnClickListener
                }
                requireActivity().hideSoftInput()

                val email = textInputEditOtherEmail.text.toString()
                val password = textInputEditOtherPassword.text.toString()
                val autoLogin = checkBoxOtherAutoLogin.isChecked
                viewModel.emailLogin(email, password, autoLogin)
            }

            // 회원가입 버튼 클릭 시 회원가입 화면으로 이동
            buttonOtherJoin.setOnClickListener {
                val joinType = JoinType.EMAIL
                navigateToJoinFragment(joinType)
            }
            // 돌아가기 버튼 클릭 시
            buttonOtherBack.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    // ViewModel의 데이터 변경을 관찰하여 UI 업데이트
    private fun observeViewModel() {
        // 이메일 로그인 데이터 관찰
        viewModel.emailLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Log.i(logTag, "이메일 로그인 성공")
                val joinType = JoinType.EMAIL
                navigateToBottomNaviFragment(joinType)
            }
        }
        // 이메일 로그인 실패 시 에러 처리
        viewModel.emailLoginError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showErrorDialog(error)
            }
        }
    }

    private fun showErrorDialog(e: Throwable) {
        val message = if (e.message != null) {
            e.message.toString()
        } else {
            "알 수 없는 오류!\n코드번호: 9999"
        }
        showLoginErrorDialog(message)
    }

    private fun navigateToFindEmailFragment() {
        parentFragmentManager.commit {
            replace(R.id.containerMain, FindEmailFragment())
            addToBackStack(FragmentName.FIND_EMAIL.str)
        }
    }

    private fun navigateToFindPasswordFragment() {
        parentFragmentManager.commit {
            replace(R.id.containerMain, FindPasswordFragment())
            addToBackStack(FragmentName.FIND_PASSWORD.str)
        }
    }

    // 회원가입 화면으로 이동하는 메소드
    private fun navigateToJoinFragment(joinType: JoinType) {
        Log.d(logTag, "navigateToJoinFragment - joinType: ${joinType.provider}")
        val bundle = Bundle().apply {
            putString("joinType", joinType.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, JoinFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.JOIN.str)
        }
    }

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
        // 다이얼로그 생성
        val dialog = CustomLoginErrorDialog(requireContext())
        with(dialog){
            // 다이얼로그 제목
            setTitle("오류")
            // 다이얼로그 메시지
            setMessage(message)
            // 확인 버튼
            setPositiveButton("확인") {
                // 확인 버튼 클릭 시 다이얼로그 닫기
                dismiss()
            }
            // 다이얼로그 표시
            show()
        }

    }

    // 유효성 검사
    private fun checkAllInput(): Boolean {
        return checkEmail() && checkPassword()
    }

    private fun checkEmail(): Boolean {
        with(binding){
            // 에러 메시지를 설정하고 포커스와 흔들기 동작을 수행하는 함수
            fun showError(message: String) {
                textInputLayoutOtherEmail.error = message
                textInputEditOtherEmail.requestFocus()
                textInputEditOtherEmail.shake()
            }
            return when {
                textInputEditOtherEmail.text.toString().isEmpty() -> {
                    showError("이메일을 입력해주세요.")
                    false
                }
                !isEmailValid(textInputEditOtherEmail.text.toString()) -> {
                    showError("올바른 이메일을 입력해주세요.")
                    false
                }
                else -> {
                    textInputLayoutOtherEmail.error = null
                    true
                }
            }
        }
    }

    private fun checkPassword(): Boolean {
        with(binding) {

            // 에러 메시지를 설정하고 포커스와 흔들기 동작을 수행하는 함수
            fun showError(message: String) {
                textInputLayoutOtherPassword.error = message
                textInputEditOtherPassword.requestFocus()
                textInputEditOtherPassword.shake()
            }
            return when {
                textInputEditOtherPassword.text.toString().isEmpty() -> {
                    showError("비밀번호를 입력해주세요.")
                    false
                }
                !isPasswordValid(textInputEditOtherPassword.text.toString()) -> {
                    showError("올바른 비밀번호를 입력해주세요.")
                    false
                }
                else -> {
                    textInputLayoutOtherPassword.error = null
                    true
                }
            }
        }
    }

    // 이메일 유효성을 검사하는 함수
    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    // 비밀번호 유효성을 검사하는 함수
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    // 유효성 검사 및 버튼 활성화/비활성화 업데이트
    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            with(binding){
                buttonOtherLogin.isEnabled =
                    !textInputEditOtherEmail.text.isNullOrEmpty() && !textInputEditOtherPassword.text.isNullOrEmpty()
            }
        }
        override fun afterTextChanged(p0: Editable?) {
        }
    }
}
