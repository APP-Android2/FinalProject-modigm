package kr.co.lion.modigm.ui.write

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.activityViewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWritePeriodBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.write.adapter.OptionSpinnerAdapter
import kr.co.lion.modigm.ui.write.vm.WriteViewModel


class WritePeriodFragment : VBBaseFragment<FragmentWritePeriodBinding>(FragmentWritePeriodBinding::inflate) {

    private val viewModel: WriteViewModel by activityViewModels()
    val tabName = "period"

    var selectPeriod :String = ""

    // 선택된 기간에 해당하는 숫자를 저장하는 배열
    val periodTags = listOf("0", "1개월 이하", "2개월 이하", "3개월 이하","4개월 이하", "5개월 이하", "6개월 미만", "6개월 이상")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingWritePeriodSpinner()
    }

    fun settingWritePeriodSpinner() {
        val periodRequests = listOf(
            "기간 선택",
            "1개월 이하",
            "2개월 이하",
            "3개월 이하",
            "4개월 이하",
            "5개월 이하",
            "6개월 미만",
            "6개월 이상"
        )
        val spinnerAdapter = OptionSpinnerAdapter(
            requireContext(),
            R.layout.spinner,
            periodRequests
        )
        binding.SpinnerWritePeriod.adapter = spinnerAdapter


        binding.SpinnerWritePeriod.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    // 선택된 위치의 태그를 가져오기
                    val tag = periodTags[position]
                    selectPeriod = periodRequests[position]
                    if (position == 0) {
                        viewModel.validatePeriod(false)
                    } else {
                        val tagWithoutSpaces = tag.replace(" ", "")  // 띄어쓰기를 제거
                        viewModel.selectedPeriodTag.value = tagWithoutSpaces
                        viewModel.validatePeriod(true)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // 아무것도 선택되지 않은 경우 처리
                    viewModel.validatePeriod(false)
                }
            }
    }
}