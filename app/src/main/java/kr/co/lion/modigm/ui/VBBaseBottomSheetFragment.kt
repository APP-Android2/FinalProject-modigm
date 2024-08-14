package kr.co.lion.modigm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 이 클래스는 바텀시트 뷰 바인딩을 위한 베이스 프래그먼트입니다.
 * 각 프래그먼트에서 이 클래스를 상속받아 데이터 바인딩을 간편하게 사용할 수 있습니다.
 *
 * @param inflate 뷰 바인딩을 위한 inflate 함수를 전달하여 해당 레이아웃을 바인딩합니다.
 *
 * 작성 예시 : class ExampleBottomSheetFragment : VBBaseBottomSheetFragment<FragmentExampleBottomSheetBinding>(FragmentExampleBottomSheetBinding::inflate)
 */

typealias InflateBottomSheet<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class VBBaseBottomSheetFragment<VB : ViewBinding>(
    private val inflate: InflateBottomSheet<VB>
) : BottomSheetDialogFragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}