package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentLoginBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.util.FragmentName


class LoginFragment : Fragment() {

    lateinit var binding : FragmentLoginBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.inflate(inflater,container,false)
        mainActivity = activity as MainActivity

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
            // 배경

            // 로고

            // 카카오 로그인 버튼
            with(imageButtonLoginKakao){
                setOnClickListener{

                }
            }

            // 다른 방법으로 로그인 텍스트 버튼
            with(textButtonLoginOther){
                setOnClickListener{

                    // 다른 방법으로 로그인 화면으로 이동
//                    val supportFragmentManager = parentFragmentManager.beginTransaction()
//                    supportFragmentManager.replace(R.id.containerMain, OtherLoginFragment())
//                        .addToBackStack(FragmentName.OTHER_LOGIN.str)
//                        .commit()
                }
            }
        }
    }
}