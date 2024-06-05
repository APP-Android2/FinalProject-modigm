package kr.co.lion.modigm.ui.write.more

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kr.co.lion.modigm.databinding.FragmentBottomSheetWriteProceedBinding
import kr.co.lion.modigm.ui.write.vm.WriteViewModel

class BottomSheetWriteProceedFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetWriteProceedBinding
    val viewModel: WriteViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentBottomSheetWriteProceedBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingEvent()
    }

    fun settingEvent() {
        binding.apply {
            imageButtonWriteProceedBottomSheetClose.setOnClickListener {
                // 닫기 종료
                dismiss()
            }


            textFieldWriteProceedBottomSheetSearch.apply {

                imeOptions = EditorInfo.IME_ACTION_DONE

                addTextChangedListener {
                    val location = textFieldWriteProceedBottomSheetSearch.text.toString()
                    // viewModel에 데이터 전송
                    viewModel.gettingStudyPlace(location)
                }

                // 엔터키 클릭 시
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val location = text.toString()
                        // viewModel에 데이터 전송
                        viewModel.gettingStudyPlace(location)
                        dismiss()
                        true
                    }
                    false
                }

            }

        }
    }

}