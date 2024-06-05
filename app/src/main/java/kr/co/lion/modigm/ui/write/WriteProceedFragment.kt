package kr.co.lion.modigm.ui.write

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
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
        settingEvent()
    }

    fun initData() {
        // 입력 초기화
        viewModel.userDidNotAnswer(tabName)

        // 전에 받은 입력이 있다면~
        if (viewModel.proceedClicked.value == true) {
            // 버튼을 활성화
            viewModel.activateButton()
        } else {
            // 버튼을 비활성화
            viewModel.deactivateButton()
        }

    }

    // 입력 유효성 검사
    fun validateInput(): Boolean {
        var result1 = false
        var result2 = false

        // 입력받은 스터디 장소 가져옴
        val location = fragmentWriteProceedBinding.textFieldWriteProceedLocation.text.toString()
        // 입력받은 최대 정원 수 가져옴
        val inputString = fragmentWriteProceedBinding.textFieldWriteProceedNumOfMember.text.toString()

        // 정원수가
        if (inputString.isEmpty()) {
            // 빈 문자열 에러 처리
            fragmentWriteProceedBinding.textFieldWriteProceedNumOfMember.error = "숫자를 입력하세요"
            return false
        }
        val maxMember: Int? = try {
            inputString.toInt()
        } catch (e: NumberFormatException) {
            // 변환 실패 에러 처리
            fragmentWriteProceedBinding.textFieldWriteProceedNumOfMember.error = "유효한 숫자가 아닙니다"
            null
        }

        // 멤버가 0명 || 멤버가 30명 이상
        if (maxMember != null) {
            if (maxMember > 30) {
                fragmentWriteProceedBinding.textFieldWriteProceedNumOfMember.error = "최대 정원은 30명입니다"
                viewModel.userDidNotAnswer(tabName)
                result1 = false
            } else {
                fragmentWriteProceedBinding.textFieldWriteProceedNumOfMember.error = null
                // ViewModel에 입력 정보 전달
                viewModel.gettingStudyMaxMember(maxMember.toInt())
                result1 = true
            }
        }

        // 오프라인 - 위치 정보
        viewModel.studyOnOffline.observe(viewLifecycleOwner) {
            if (it == 1 || it == 3) { // 오프라인 or 온오프 혼합
                if (location.isEmpty()) { // 미입력
                    fragmentWriteProceedBinding.textFieldWriteProceedLocation.error = "스터디 할 장소를 입력해주세요"
                    viewModel.gettingStudyPlace("")
                    result2 = false
                } else { // 입력
                    fragmentWriteProceedBinding.textFieldWriteProceedLocation.error = null
                    viewModel.gettingStudyPlace(location)
                    result2 = true
                }
            } else { // 온라인인 경우
                viewModel.gettingStudyPlace("")
                result2 = true
            }
        }

        if (result1 && result2) {
            // 모두 입력을 받은 경우 -> proceedClicked.value = true => 버튼 활성화
            viewModel.userDidAnswer(tabName)
        } else {
            viewModel.userDidNotAnswer(tabName)
        }

        return result1 && result2
    }


    fun settingEvent() {
        fragmentWriteProceedBinding.apply {

            // 오프라인 chip 클릭 이벤트
            chipWriteProceedOffline.setOnCheckedChangeListener { view, isChecked ->
                if (isChecked) { // checked
                    // 보여주기
                    textInputLayoutWriteProceedOfflineClicked.visibility = View.VISIBLE

                    // 체크 상태 조정
                    chipWriteProceedOnline.isChecked = false
                    chipWriteProceedMix.isChecked = false

                    // ViewModel에 정보 전달
                    viewModel.gettingStudyOnOffLine(1) // 1: 오프라인

                } else {
                    // 가리기
                    if (chipWriteProceedMix.isChecked) {
                        textInputLayoutWriteProceedOfflineClicked.visibility = View.VISIBLE
                    } else {
                        textInputLayoutWriteProceedOfflineClicked.visibility = View.GONE
                    }
                }
            }

            // 온라인 chip 클릭 이벤트
            chipWriteProceedOnline.setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) {
                    // 가리기
                    textInputLayoutWriteProceedOfflineClicked.visibility = View.GONE

                    // 체크 상태 조정
                    chipWriteProceedOffline.isChecked = false
                    chipWriteProceedMix.isChecked = false

                    // ViewModel에 정보 전달
                    viewModel.gettingStudyOnOffLine(2) // 2: 온라인
                }
            }

            // 온라인, 오프라인 혼합 chip 클릭 이벤트
            chipWriteProceedMix.setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) { // checked
                    // 보여주기
                    textInputLayoutWriteProceedOfflineClicked.visibility = View.VISIBLE

                    chipWriteProceedMix.setBackgroundColor(Color.parseColor("#1A51C5"))
                    // 체크 상태 조정
                    chipWriteProceedOffline.isChecked = false
                    chipWriteProceedOnline.isChecked = false

                    // ViewModel에 정보 전달
                    viewModel.gettingStudyOnOffLine(3) // 3: 온오프 혼합
                } else { // unchecked
                    // 가리기
                    if (chipWriteProceedOffline.isChecked) {
                        textInputLayoutWriteProceedOfflineClicked.visibility = View.VISIBLE
                    } else {
                        textInputLayoutWriteProceedOfflineClicked.visibility = View.GONE
                    }

                }
            }

            // 진행장소 - textField 클릭 이벤트
            textInputLayoutWriteProceedOfflineClicked.editText?.apply {

                // 클릭 시 바텀Sheet를 띄워준다
                textFieldWriteProceedLocation.setOnClickListener {
                    showBottomSheet()
                }
            }

            // BottomSheetWriteProceedFragment에서 스터디 장소를 입력받으면 작동
            viewModel.studyPlace.observe(viewLifecycleOwner) {
                // location이 바뀌면 적용함
                textFieldWriteProceedLocation.setText(it)
            }

            // 몇 명이서 진행할까요? textField 클릭 이벤트
            textFieldWriteProceedNumOfMember.apply {
                imeOptions = EditorInfo.IME_ACTION_DONE


                // 엔터키 클릭 시
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val maxMember = text.toString()
                        // 유효성 검사
                        validateInput()
                        true
                    }
                    false
                }

            }
        }
    }


    private fun showBottomSheet() {
        val modal = BottomSheetWriteProceedFragment()
        modal.setStyle(DialogFragment.STYLE_NORMAL, R.style.roundCornerBottomSheetDialogTheme)
        modal.show(parentFragmentManager, modal.tag)
    }
}
