package kr.co.lion.modigm.ui.profile

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChangePwBinding
import kr.co.lion.modigm.ui.profile.vm.ChangePwViewModel
import kr.co.lion.modigm.ui.profile.vm.ChangePwErrorMessage
import kr.co.lion.modigm.util.hideSoftInput

class ChangePwFragment : Fragment() {

     lateinit var binding: FragmentChangePwBinding
     private val viewModel: ChangePwViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_change_pw, container, false)
        binding.changePwViewModel = viewModel
        binding.lifecycleOwner = this

        settingToolbar()
        settingTextInputLayoutError()
        settingChangePWButtonDone()
        settingChangePwResultObserver()

        return binding.root
    }

    // 뒤로가기 버튼
    private fun settingToolbar(){
        binding.changePWToolbar.apply {
            title = "설정"
            setNavigationIcon(R.drawable.arrow_back_24px)
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

    }

    // 유효성 검사
    private fun settingTextInputLayoutError(){
        viewModel.oldPwError.observe(viewLifecycleOwner){
            binding.changePWTextFieldPW.error = it
        }
        viewModel.newPwError.observe(viewLifecycleOwner){
            binding.changePWTextFieldNewPW.error = it
        }
        viewModel.newPwCheckError.observe(viewLifecycleOwner){
            binding.changePWTextFieldCheck.error = it
        }
    }

    // 확인 버튼 클릭 이벤트
    private fun settingChangePWButtonDone(){
        binding.changePWButtonDone.setOnClickListener {
            // 버튼 클릭 방지
            it.isClickable = false
            requireActivity().hideSoftInput()
            lifecycleScope.launch {
                viewModel.changePw()
            }
        }
    }

    // 비밀번호 변경 결과 옵저버
    private fun settingChangePwResultObserver(){
        viewModel.changePwResult.observe(viewLifecycleOwner){
            when(it){
                ChangePwErrorMessage.CHANGE_PW_SUCCESS -> parentFragmentManager.popBackStack()
                else -> showSnackBar(it.str)
            }
            // 버튼 클릭 방지 해제
            binding.changePWButtonDone.isClickable = true
        }
    }

    private fun showSnackBar(result:String) {

        val snackbar =
            Snackbar.make(binding.root, result, Snackbar.LENGTH_SHORT)

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
    private fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

}