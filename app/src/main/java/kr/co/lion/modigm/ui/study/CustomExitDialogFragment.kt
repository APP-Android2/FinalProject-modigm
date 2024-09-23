package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import kr.co.lion.modigm.databinding.FragmentCustomDialogExitBinding
import kr.co.lion.modigm.ui.VBBaseDialogFragment
import kr.co.lion.modigm.util.ModigmApplication
import kotlin.system.exitProcess

class CustomExitDialogFragment : VBBaseDialogFragment<FragmentCustomDialogExitBinding>(FragmentCustomDialogExitBinding::inflate) {

    override fun onStart() {
        super.onStart()

        // 다이얼로그의 크기 설정
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent) // 투명 배경 설정 (필요한 경우)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()


    }

    private fun initView() {
        with(binding) {
            // 사전 로드된 AdView를 설정
            val adView = ModigmApplication.preloadedAdView

            // 광고가 이미 다른 부모 뷰에 추가되어 있을 경우 제거
            adView.parent?.let { parent ->
                (parent as ViewGroup).removeView(adView)
            }

            // 광고가 로드되어 있으면 바로 다이얼로그에 표시
            exitDialogAdView.addView(adView)
            adView.visibility = View.VISIBLE

            // 긍정 버튼 (돌아가기)
            buttonDialogPositive.apply {
                setOnClickListener {
                    dismiss() // 다이얼로그 닫기
                }
            }

            // 부정 버튼 (종료)
            buttonDialogNegative.apply {
                setOnClickListener {
                    activity?.finishAffinity() // 앱 종료
                    exitProcess(0) // 앱 프로세스 종료
                }
            }
        }
    }
}