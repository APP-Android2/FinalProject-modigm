package kr.co.lion.modigm.ui.join

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinBinding
import kr.co.lion.modigm.ui.join.adapter.JoinViewPagerAdapter
import kr.co.lion.modigm.ui.join.vm.JoinStep1ViewModel
import kr.co.lion.modigm.ui.join.vm.JoinStep2ViewModel
import kr.co.lion.modigm.ui.join.vm.JoinStep3ViewModel
import kr.co.lion.modigm.ui.join.vm.JoinViewModel
import kr.co.lion.modigm.ui.login.OtherLoginFragment
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.hideSoftInput

class JoinFragment : Fragment() {

    private lateinit var binding: FragmentJoinBinding

    private val viewModel: JoinViewModel by viewModels()
    private val viewModelStep1: JoinStep1ViewModel by activityViewModels()
    private val viewModelStep2: JoinStep2ViewModel by activityViewModels()
    private val viewModelStep3: JoinStep3ViewModel by activityViewModels()

    private val joinType: JoinType? by lazy {
        JoinType.getType(arguments?.getString("joinType")?:"")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentJoinBinding.inflate(inflater)

        settingValuesFromBundle()
        settingToolBar()
        settingObservers()

        return binding.root
    }

    // 번들로 전달받은 값들을 뷰모델 라이브 데이터에 셋팅
    private fun settingValuesFromBundle(){
        if(joinType != null){
            // 프로바이더 셋팅
            viewModel.setUserProvider(joinType?.provider?:"")
            // email 셋팅
            viewModel.setUserEmail()
            // SNS계정인경우 uid 셋팅
            if(joinType != JoinType.EMAIL){
                viewModel.setUserUid()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingViewPagerAdapter()

        // 안드로이드 뒤로가기 기능에 뒤로가기 버튼 기능 추가
        lifecycleScope.launch {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                if(binding.viewPagerJoin.currentItem!=0){
                    binding.viewPagerJoin.currentItem -= 1
                }else{
                    // JoinFragment 회원가입 종료, 확인 알림창 띄우기?
                    showCancelJoinDialog()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 회원가입을 완료하지 않고 화면을 이탈한 경우 이미 등록되어있던 Auth 정보를 삭제한다.
        if(!viewModel.joinCompleted.value!!){
            viewModel.deleteCurrentUser()
        }
        viewModelStep1.reset()
        viewModelStep2.reset()
        viewModelStep3.reset()
    }

    private fun settingToolBar(){
        with(binding.toolbarJoin){
            title = "회원가입"
            setNavigationIcon(R.drawable.arrow_back_24px)
            setNavigationOnClickListener {
                if(binding.viewPagerJoin.currentItem!=0){
                    binding.viewPagerJoin.currentItem -= 1
                }else{
                    // JoinFragment 회원가입 종료, 확인 알림창 띄우기?
                    showCancelJoinDialog()
                }
            }
        }
    }

    // 회원가입 취소 다이얼로그
    private fun showCancelJoinDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog, null)
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.dialogColor)
            .setTitle("회원가입 취소")
            .setMessage("정말로 회원가입을 취소하시겠습니까?")
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.btnYes).text = "네"
        dialogView.findViewById<TextView>(R.id.btnYes).setOnClickListener {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btnNo).text = "아니오"
        dialogView.findViewById<TextView>(R.id.btnNo).setOnClickListener {
            // 아니요 버튼 로직
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun settingViewPagerAdapter(){
        val viewPagerAdapter = JoinViewPagerAdapter(this)

        // 뷰페이저에 보여줄 프래그먼트를 회원가입 유형에 따라 다르게 셋팅해준다.
        when(joinType){
            // 이메일로 회원가입할 때
            JoinType.EMAIL -> {
                viewPagerAdapter.addFragments(
                    arrayListOf(
                        JoinStep1Fragment(),
                        JoinStep2Fragment(),
                        JoinStep3Fragment()
                    )
                )
            }
            // SNS계정으로 회원가입할 때
            else -> {
                viewPagerAdapter.addFragments(
                    arrayListOf(
                        JoinStep2Fragment(),
                        JoinStep3Fragment()
                    )
                )
            }
        }

        with(binding){
            // 어댑터 설정
            viewPagerJoin.adapter = viewPagerAdapter
            // 전환 방향
            viewPagerJoin.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            // 터치로 스크롤 막기
            viewPagerJoin.isUserInputEnabled = false

            // 프로그래스바 설정
            progressBarJoin.max = 100

            viewPagerJoin.registerOnPageChangeCallback(
                object: ViewPager2.OnPageChangeCallback(){
                    override fun onPageSelected(position: Int) {
                        val progress = (position + 1) * 100 / viewPagerAdapter.itemCount
                        binding.progressBarJoin.setProgress(progress, true)
                    }
                }
            )

            // 다음 버튼 클릭 시 다음 화면으로 넘어가기
            buttonJoinNext.setOnClickListener {

                when(joinType){
                    // 이메일로 회원가입할 때
                    JoinType.EMAIL -> {
                        when(viewPagerJoin.currentItem){
                            // 이메일, 비밀번호 화면
                            0 -> step1Process()
                            // 이름, 전화번호 인증 화면
                            1 -> step2Process()
                            // 관심 분야 선택 화면
                            2 -> step3Process()
                        }
                    }
                    // SNS계정으로 회원가입할 때
                    else -> {
                        when(viewPagerJoin.currentItem){
                            // 이름, 전화번호 인증 화면
                            0 -> step2Process()
                            // 관심 분야 선택 화면
                            1 -> step3Process()
                        }
                    }
                }

            }
        }

    }

    private fun step1Process(){
        // 유효성 검사
        val validation = viewModelStep1.validate()
        if(!validation) return

        // 응답 받은 이메일, 비밀번호
        viewModel.setEmailAndPw(
            viewModelStep1.userEmail.value.toString(),
            viewModelStep1.userPassword.value.toString()
        )

        lifecycleScope.launch {
            showLoading()
            // 처음 화면인 경우
            if(viewModel.verifiedEmail.value.isNullOrEmpty()
                // 다음 화면으로 넘어갔다가 다시 돌아와서 이메일을 변경한 경우
                || (viewModelStep1.userEmail.value != viewModel.verifiedEmail.value && !viewModel.verifiedEmail.value.isNullOrEmpty())
                ){
                if(viewModelStep1.userEmail.value != viewModel.verifiedEmail.value && !viewModel.verifiedEmail.value.isNullOrEmpty()){
                    // 다음 화면으로 넘어갔다가 다시 돌아와서 이메일을 변경한 경우에는 기존에 등록한 이메일 계정을 삭제
                    viewModel.deleteCurrentUser()
                }
                // 계정 중복 확인
                val isDup = viewModel.createEmailUser()
                if(isDup.isNotEmpty()){
                    viewModelStep1.emailValidation.value = isDup
                    hideLoading()
                    return@launch
                }
            }
            hideLoading()
            // 다음 화면으로 이동
            binding.viewPagerJoin.currentItem += 1
        }
    }

    private fun step2Process(){
        // 유효성 검사
        val validation = viewModelStep2.validate()
        if(!validation) return
        // 응답 받은 이름, 전화번호
        viewModel.setUserNameAndPhoneNumber(
            viewModelStep2.userName.value.toString(),
            viewModelStep2.userPhone.value.toString()
        )

        lifecycleScope.launch {
            // 뒤로가기로 돌아왔을 때 이미 인증된 상태인 경우에는 바로 다음페이지로 넘어갈 수 있음
            if(viewModel.phoneVerification.value==true){
                // 전화번호를 변경하지 않은 경우에 넘어갈 수 있음
                if(viewModel.verifiedPhoneNumber.value == viewModelStep2.userPhone.value){
                    binding.viewPagerJoin.currentItem += 1
                    return@launch
                }
            }
            showLoading()

            val result = viewModelStep2.createPhoneUser()
            if(result.isEmpty()){
                // 인증 번호 확인 성공
                viewModelStep2.credential.value?.let { viewModel.setPhoneCredential(it) }
                viewModel.setPhoneVerified(true)
                viewModelStep2.userPhone.value?.let { viewModel.setVerifiedPhoneNumber(it) }
                viewModelStep2.cancelTimer()
            }else{
                // 인증 번호 확인 실패
                viewModel.setPhoneVerified(false)
            }
            if(!viewModel.phoneVerification.value!! && result=="이미 해당 번호로 가입한 계정이 있습니다."){
                viewModel.setAlreadyRegisteredUser(
                    viewModelStep2.alreadyRegisteredUserEmail.value?:"",
                    viewModelStep2.alreadyRegisteredUserProvider.value?:""
                )
                viewModel.isPhoneAlreadyRegistered.value = true
                viewModelStep2.cancelTimer()
                hideLoading()
            }
        }
    }

    private fun step3Process(){
        // 유효성 검사
        val validation = viewModelStep3.validate()
        if(!validation) return
        // 응답값
        viewModelStep3.selectedInterestList.value?.let { it1 ->
            viewModel.setInterests(it1)
        }

        lifecycleScope.launch {
            showLoading()
            try{
                when(joinType){
                    JoinType.EMAIL -> viewModel.completeJoinEmailUser()
                    else -> viewModel.completeJoinSnsUser()
                }
            }catch (e:Exception){
                Log.e("JoinError", "$e")
                showSnackBar()
                hideLoading()
            }

        }
    }

    private fun showSnackBar() {

        val snackbar =
            Snackbar.make(binding.root, "통신 에러, 잠시 후 다시 시도해주세요.", Snackbar.LENGTH_SHORT)

        // 스낵바의 뷰를 가져옵니다.
        val snackbarView = snackbar.view

        // 스낵바 텍스트 뷰 찾기
        val textView =
            snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

        // 텍스트 크기를 dp 단위로 설정
        val textSizeInPx = dpToPx(requireContext(), 16f)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx)

        snackbar.show()
    }

    // 스낵바 글시 크기 설정을 위해 dp를 px로 변환
    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    // 회원가입 절차 옵저버 세팅
    private fun settingObservers(){
        // 인증이 확인 되었을 때
        viewModel.phoneVerification.observe(viewLifecycleOwner){
            hideLoading()
            if(it){
                // 인증이 되었으면 다음으로 이동
                viewModelStep2.cancelTimer()
                binding.viewPagerJoin.currentItem += 1
            }
        }

        // 인증하기를 다시 했을 때 기존의 인증 완료 취소
        viewModelStep2.isVerifiedPhone.observe(viewLifecycleOwner){
            if(!it){
                viewModel.setPhoneVerified(false)
            }
        }

        // 전화번호가 기존에 등록된 번호인 것이 확인되었을 때
        viewModel.isPhoneAlreadyRegistered.observe(viewLifecycleOwner){
            hideLoading()
            if(it){
                // 중복인 경우 중복 알림 프래그먼트로 이동
                val bundle = Bundle()
                bundle.putString("email", viewModel.alreadyRegisteredUserEmail.value)
                bundle.putString("provider", viewModel.alreadyRegisteredUserProvider.value)
                val joinFragment = JoinDuplicateFragment()
                joinFragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, joinFragment)
                    .addToBackStack(FragmentName.JOIN_DUPLICATE.str)
                    .commit()
                viewModel.isPhoneAlreadyRegistered.value = false
            }
        }

        // 회원가입 완료 시 다음 화면으로 이동
        viewModel.joinCompleted.observe(viewLifecycleOwner){
            hideLoading()

            if(it){
                when(viewModel.userProvider.value){
                    // 이메일 계정 회원가입인 경우에는 로그인 화면으로 돌아오기
                    JoinType.EMAIL.provider ->{
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.containerMain, OtherLoginFragment())
                            .commit()
                    }
                    // SNS 계정인 경우에는 메인으로 넘어가기
                    else -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.containerMain, BottomNaviFragment())
                            .commit()
                    }
                }
                viewModelStep1.reset()
                viewModelStep2.reset()
                viewModelStep3.reset()
            }
        }
    }

    private fun showLoading(){
        requireActivity().hideSoftInput()
        binding.layoutLoadingJoin.visibility = View.VISIBLE
        requireActivity().window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun hideLoading(){
        binding.layoutLoadingJoin.visibility = View.GONE
        requireActivity().window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

}