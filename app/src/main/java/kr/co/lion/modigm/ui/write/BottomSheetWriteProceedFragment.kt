package kr.co.lion.modigm.ui.write

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kr.co.lion.modigm.databinding.FragmentBottomSheetWriteProceedBinding

class BottomSheetWriteProceedFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetWriteProceedBinding
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
            // 닫기 종료
            imageButtonWriteProceedBottomSheetClose.setOnClickListener {
                textFieldWriteProceedBottomSheetSearch.addTextChangedListener {

                }
                dismiss()
            }


            textFieldWriteProceedBottomSheetSearch.apply {

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

}