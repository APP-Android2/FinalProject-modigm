package kr.co.lion.modigm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * 이 클래스는 뷰 바인딩을 위한 베이스 프래그먼트입니다.
 * 각 프래그먼트에서 이 클래스를 상속받아 데이터 바인딩을 간편하게 사용할 수 있습니다.
 *
 * @param inflate 뷰 바인딩을 위한 inflate 함수를 전달하여 해당 레이아웃을 바인딩합니다.
 *
 * 작성 예시 : class ExampleFragment : VBBaseFragment<FragmentExampleBinding>(FragmentExampleBinding::inflate)
 */

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class VBBaseFragment<VB : ViewBinding>(
    private val inflate: Inflate<VB>
) : Fragment() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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