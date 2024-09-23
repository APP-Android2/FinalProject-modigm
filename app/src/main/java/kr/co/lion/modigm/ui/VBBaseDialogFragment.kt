package kr.co.lion.modigm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

typealias InflateDialog<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

/**
 * 다이얼로그 프래그먼트의 바인딩 처리를 위한 베이스 클래스
 *
 * @param inflate 뷰 바인딩을 위한 inflate 함수를 전달받습니다.
 */
abstract class VBBaseDialogFragment<VB : ViewBinding>(
    private val inflate: InflateDialog<VB>
) : DialogFragment() {

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