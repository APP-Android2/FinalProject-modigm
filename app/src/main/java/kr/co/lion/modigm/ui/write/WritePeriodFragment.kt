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


class WritePeriodFragment :
    VBBaseFragment<FragmentWritePeriodBinding>(FragmentWritePeriodBinding::inflate) {
    // 뷰모델
    private val viewModel: WriteViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        observeViewModel()
    }

    // 선택된 기간
    private var selectPeriod: String? = null

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
                        if (position == 0) {
                            selectPeriod = null
                        } else {
                            selectPeriod = spinnerItemList[position]
                            selectPeriod?.let {
                                val selectPeriodItem = it.replace(" ", "")
                                // 선택된 값을 ViewModel에 저장
                                viewModel.updateWriteData("studyPeriod", selectPeriodItem)
                            }
                        }
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
                    if (selectPeriod == "기간 선택"){
                        return@setOnClickListener
                    }
                    viewModel.updateSelectedTab(2)
                }
            }
        }
    }

    // 버튼의 색상을 업데이트하는 함수
    private fun updateButtonColor() {
        with(binding){
            val colorResId = if (selectPeriod != null) R.color.pointColor else R.color.buttonGray
            buttonWritePeriodNext.apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }

        }

    }

    // ViewModel의 데이터를 관찰하는 함수
    private fun observeViewModel() {
        // studyPeriod 데이터가 변경되면 스피너의 선택값을 갱신
        val savedPeriod = viewModel.getUpdateData("studyPeriod")?.toString()
        selectPeriod = when (savedPeriod) {
            "1개월이하" -> "1개월 이하"
            "2개월이하" -> "2개월 이하"
            "3개월이하" -> "3개월 이하"
            "4개월이하" -> "4개월 이하"
            "5개월이하" -> "5개월 이하"
            "6개월미만" -> "6개월 미만"
            "6개월이상" -> "6개월 이상"
            else -> null
        }

        // 스피너를 ViewModel에서 가져온 데이터로 업데이트
        updateSpinnerSelection(selectPeriod)
    }

    // 스피너 선택값을 업데이트하는 함수
    private fun updateSpinnerSelection(period: String?) {
        with(binding) {
            spinnerWritePeriod.apply {
                when (period) {
                    "1개월 이하" -> setSelection(1)
                    "2개월 이하" -> setSelection(2)
                    "3개월 이하" -> setSelection(3)
                    "4개월 이하" -> setSelection(4)
                    "5개월 이하" -> setSelection(5)
                    "6개월 미만" -> setSelection(6)
                    "6개월 이상" -> setSelection(7)
                    else -> setSelection(0)
                }
            }
        }
    }
}