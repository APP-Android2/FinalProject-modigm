package kr.co.lion.modigm.ui.write

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWritePeriodBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.write.adapter.OptionSpinnerAdapter
import kr.co.lion.modigm.ui.write.vm.WriteViewModel
import kr.co.lion.modigm.util.showLoginSnackBar


class WritePeriodFragment :
    VBBaseFragment<FragmentWritePeriodBinding>(FragmentWritePeriodBinding::inflate) {
    // 뷰모델
    private val viewModel: WriteViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        observeViewModel()
    }

    private fun initView() {
        with(binding) {
            // 스피너 설정
            spinnerWritePeriod.apply {
                val spinnerItemList = listOf(
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
                    spinnerItemList
                )
                adapter = spinnerAdapter

                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedPeriod = if (position == 0) {
                            // "기간 선택"일 경우 ViewModel에서 데이터를 비우고 버튼 비활성화
                            viewModel.updateWriteData("studyPeriod", null)
                            null
                        } else {
                            spinnerItemList[position]
                        }

                        // ViewModel에 선택한 기간 저장
                        selectedPeriod?.let {
                            val selectPeriodItem = it.replace(" ", "")
                            viewModel.updateWriteData("studyPeriod", selectPeriodItem)
                        }
                        // 버튼 색상 업데이트
                        updateButtonColor()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // 아무것도 선택되지 않은 경우 처리
                    }
                }
            }

            // 버튼 클릭 리스너
            with(buttonWritePeriodNext) {
                setOnClickListener {
                    // 유효성 검사
                    val studyPeriod = viewModel.getUpdateData("studyPeriod") as? String

                    if (studyPeriod == null || studyPeriod == "기간 선택") {
                        requireActivity().showLoginSnackBar("기간을 입력해주세요.", null)
                        return@setOnClickListener
                    }
                    viewModel.updateSelectedTab(2)
                }
            }
        }
    }

    // 버튼의 색상을 업데이트하는 함수
    private fun updateButtonColor() {
        val selectedPeriod = viewModel.getUpdateData("studyPeriod") as? String
        val isPeriodSelected = !selectedPeriod.isNullOrEmpty()

        // 선택한 기간에 따라 버튼 색상 및 활성화 상태 설정
        val colorResId = if (isPeriodSelected) R.color.pointColor else R.color.buttonGray

        with(binding) {
            buttonWritePeriodNext.apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                isEnabled = isPeriodSelected  // 기간이 선택되었을 때만 버튼 활성화
            }
        }
    }

    // ViewModel의 데이터를 관찰하는 함수
    private fun observeViewModel() {

        viewModel.writeDataMap.observe(viewLifecycleOwner) { dataMap ->
            val savedPeriod = dataMap?.get("studyPeriod")?.toString()
            val period = when (savedPeriod) {
                "1개월이하" -> "1개월 이하"
                "2개월이하" -> "2개월 이하"
                "3개월이하" -> "3개월 이하"
                "4개월이하" -> "4개월 이하"
                "5개월이하" -> "5개월 이하"
                "6개월미만" -> "6개월 미만"
                "6개월이상" -> "6개월 이상"
                else -> null
            }
            // 스피너 값을 ViewModel 데이터에 따라 초기화
            updateSpinnerSelection(period)
        }
    }

    // 스피너 선택값을 업데이트하는 함수
    private fun updateSpinnerSelection(period: String?) {
        val periodList = listOf(
            "기간 선택",
            "1개월 이하",
            "2개월 이하",
            "3개월 이하",
            "4개월 이하",
            "5개월 이하",
            "6개월 미만",
            "6개월 이상"
        )
        with(binding) {
            val selectedPosition = periodList.indexOf(period).takeIf { it >= 0 } ?: 0
            spinnerWritePeriod.setSelection(selectedPosition)
        }
    }
}