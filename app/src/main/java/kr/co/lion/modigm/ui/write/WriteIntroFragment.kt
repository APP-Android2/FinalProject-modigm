package kr.co.lion.modigm.ui.write

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kr.co.lion.modigm.Manifest
import kr.co.lion.modigm.databinding.FragmentWriteIntroBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.write.more.CustomDialogWriteIntroExample

class WriteIntroFragment : Fragment() {

    lateinit var fragmentWriteIntroBinding: FragmentWriteIntroBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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
            imageViewWriteIntroCoverImage.setOnClickListener {

            }

            // 작성예시 클릭 리스너
            textViewWriteIntroWriteExample.setOnClickListener {
                // 다이얼로그를 띄워준다
                val context = requireContext()
                val dialog = CustomDialogWriteIntroExample(context)
                dialog.show()
            }
        }

    }

    // 사진 등록
    fun settingPicture(){

    }

    fun settingView(){
        // 필요 시 작성
    }
}