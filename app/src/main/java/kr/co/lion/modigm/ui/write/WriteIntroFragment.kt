package kr.co.lion.modigm.ui.write

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.co.lion.modigm.databinding.FragmentWriteIntroBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.write.more.CustomDialogWriteIntroExample

class WriteIntroFragment : Fragment() {

    lateinit var fragmentWriteIntroBinding: FragmentWriteIntroBinding
    lateinit var mainActivity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mainActivity = activity as MainActivity
        fragmentWriteIntroBinding = FragmentWriteIntroBinding.inflate(inflater)
        return fragmentWriteIntroBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingView()
        settingEvent()
    }

    fun settingEvent(){
        fragmentWriteIntroBinding.apply {
            // image 버튼 클릭 리스너
            imageButtonWriteIntroCoverImage.setOnClickListener {
                // 파일을 불러들인다
            }

            // 작성예시 클릭 리스너
            textViewWriteIntroWriteExample.setOnClickListener {
                // 다이얼로그를 띄워준다
                val dialog = CustomDialogWriteIntroExample(mainActivity)
                dialog.show()
            }
        }

    }

    fun settingView(){
        // 필요 시 작성
    }
}