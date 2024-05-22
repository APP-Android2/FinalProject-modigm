package kr.co.lion.modigm.ui.write

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteFieldBinding
import kr.co.lion.modigm.ui.MainActivity

class WriteFieldFragment : Fragment() {

    lateinit var fragmentWriteFieldBinding: FragmentWriteFieldBinding
    private lateinit var mainActivity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mainActivity = activity as MainActivity
        fragmentWriteFieldBinding = FragmentWriteFieldBinding.inflate(layoutInflater)

        return fragmentWriteFieldBinding.root
    }

}