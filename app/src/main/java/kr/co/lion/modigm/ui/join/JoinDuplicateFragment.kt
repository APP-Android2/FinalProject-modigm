package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinDuplicateBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.login.LoginFragment
import kr.co.lion.modigm.util.FragmentName

class JoinDuplicateFragment : Fragment() {

    val binding by lazy {
        FragmentJoinDuplicateBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        settingToolBar()
        settingButtonJoinDupLogin()

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                // 추후 수정
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun settingToolBar(){
        binding.toolbarJoinDup.title = ""
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbarJoinDup)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun settingButtonJoinDupLogin(){
        // 로그인 화면으로 돌아가거나 추후 가능할 경우 sns로그인 api 연동 예정
        binding.buttonJoinDupLogin.setOnClickListener {
            (requireActivity() as MainActivity).removeFragment(FragmentName.JOIN)
            (requireActivity() as MainActivity).replaceFragment(FragmentName.LOGIN, false, true, null)
        }
    }

}