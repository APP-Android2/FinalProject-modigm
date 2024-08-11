package kr.co.lion.modigm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

/**
 * 이 클래스는 데이터 바인딩을 위한 베이스 프래그먼트입니다.
 * 각 프래그먼트에서 이 클래스를 상속받아 데이터 바인딩을 간편하게 사용할 수 있습니다.
 *
 * @param layoutResId 레이아웃 리소스 ID를 전달하여 해당 레이아웃을 바인딩합니다.
 *
 * 상속 시 작성 예시 : class ExampleFragment : DBBaseFragment<FragmentExampleBinding>(R.layout.fragment_example)
 */

abstract class DBBaseFragment<VB : ViewDataBinding>(
    private val layoutResId: Int
) : Fragment() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutResId, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}