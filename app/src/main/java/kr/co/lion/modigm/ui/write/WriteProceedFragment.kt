package kr.co.lion.modigm.ui.write

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteProceedBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.write.more.BottomSheetWriteProceedFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel

class WriteProceedFragment : Fragment() {

    lateinit var fragmentWriteProceedBinding: FragmentWriteProceedBinding
    private val viewModel: WriteViewModel by activityViewModels()
    val tabName = "proceed"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        fragmentWriteProceedBinding = FragmentWriteProceedBinding.inflate(inflater)
        return fragmentWriteProceedBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initData()
        settingView()
        settingEvent()
    }

    fun initData(){
        // 입력 초기화
        viewModel.userDidNotAnswer(tabName)

        // 전에 받은 입력이 있다면~
        if (viewModel.proceedClicked.value == true){
            // 버튼을 활성화
            viewModel.activateButton()
        } else {
            // 버튼을 비활성화
            viewModel.deactivateButton()
        }

    }



    fun settingEvent(){
        fragmentWriteProceedBinding.apply {

            // 오프라인 chip 클릭 이벤트
            chipWriteProceedOffline.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){ // checked
                    // 보여주기
                    textInputLayoutWriteProceedOfflineClicked.visibility = View.VISIBLE

                    // 체크 상태 조정
                    chipWriteProceedOnline.isChecked = false
                    chipWriteProceedMix.isChecked = false

                }
                else {
                    // 가리기
                    if (chipWriteProceedMix.isChecked){
                        textInputLayoutWriteProceedOfflineClicked.visibility = View.VISIBLE
                    }
                    else {
                        textInputLayoutWriteProceedOfflineClicked.visibility = View.GONE
                    }
                }
            }

            // 온라인 chip 클릭 이벤트
            chipWriteProceedOnline.setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked){
                    // 가리기
                    textInputLayoutWriteProceedOfflineClicked.visibility = View.GONE

                    // 체크 상태 조정
                    chipWriteProceedOffline.isChecked = false
                    chipWriteProceedMix.isChecked = false
                }
            }

            // 온라인, 오프라인 혼합 chip 클릭 이벤트
            chipWriteProceedMix.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){ // checked
                    // 보여주기
                    textInputLayoutWriteProceedOfflineClicked.visibility = View.VISIBLE

                    // 체크 상태 조정
                    chipWriteProceedOffline.isChecked = false
                    chipWriteProceedOnline.isChecked = false
                }
                else { // unchecked
                    // 가리기
                    if (chipWriteProceedOffline.isChecked){
                        textInputLayoutWriteProceedOfflineClicked.visibility = View.VISIBLE
                    }
                    else {
                        textInputLayoutWriteProceedOfflineClicked.visibility = View.GONE
                    }

                }
            }

            // 어디서 진행할까요? textField 클릭 이벤트
            textInputLayoutWriteProceedOfflineClicked.editText?.apply {

                // 클릭 시 바텀Sheet를 띄워준다
                textFieldWriteProceedLocation.setOnClickListener {
                    showBottomSheet()
                }
            }

            // 몇 명이서 진행할까요? textField 클릭 이벤트
            textFieldWriteProceedNumOfMember.apply {

                // 키보드에서 return 클릭 시 키보드 없애기
                setOnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE){
                        // 키보드 숨기기
                        v.clearFocus()
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0 )
                        true
                    } else {
                        false
                    }
                }
            }
        }
    }

    fun settingView(){
        // 입력을 받았으면 버튼 세팅
        isThereInput()
    }


    private fun showBottomSheet(){
        val modal = BottomSheetWriteProceedFragment()
        modal.setStyle(DialogFragment.STYLE_NORMAL, R.style.roundCornerBottomSheetDialogTheme)
        modal.show(parentFragmentManager, modal.tag)
    }

    fun isThereInput(){
        fragmentWriteProceedBinding.apply {
            if (!textFieldWriteProceedNumOfMember.text.isNullOrBlank() && textFieldWriteProceedLocation.text.isNullOrBlank()){
                viewModel.userDidAnswer(tabName)
            } else {
                viewModel.userDidNotAnswer(tabName)
            }
        }
    }

}
