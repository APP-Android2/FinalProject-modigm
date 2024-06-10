package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChangePhoneBinding
import kr.co.lion.modigm.ui.profile.vm.ChangePhoneViewModel

class ChangePhoneFragment : Fragment() {

    lateinit var binding: FragmentChangePhoneBinding
    private val changePhoneViewModel: ChangePhoneViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_change_phone, container, false)
        binding.changePhoneViewModel = changePhoneViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

}