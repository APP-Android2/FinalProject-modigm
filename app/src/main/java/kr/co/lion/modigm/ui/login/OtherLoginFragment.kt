package kr.co.lion.modigm.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentLoginBinding
import kr.co.lion.modigm.databinding.FragmentOtherLoginBinding

class OtherLoginFragment : Fragment() {

    private lateinit var binding : FragmentOtherLoginBinding



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 바인딩
        binding = FragmentOtherLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    // view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 뷰 세팅
        initView()
    }


    // 초기 뷰 세팅
    fun initView(){

        // 바인딩
        with(binding){


            // 로고

            // 버튼


        }
    }

}