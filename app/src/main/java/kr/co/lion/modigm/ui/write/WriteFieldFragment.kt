package kr.co.lion.modigm.ui.write

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteBinding
import kr.co.lion.modigm.databinding.FragmentWriteFieldBinding
import kr.co.lion.modigm.ui.MainActivity

class WriteFieldFragment : Fragment() {

    private lateinit var fragmentWriteBinding: FragmentWriteBinding
    lateinit var fragmentWriteFieldBinding: FragmentWriteFieldBinding
    private lateinit var mainActivity: MainActivity
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Progress Bar 변경을 위한 바인딩 불러오기
        fragmentWriteBinding = FragmentWriteBinding.inflate(inflater)

        mainActivity = activity as MainActivity
        fragmentWriteFieldBinding = FragmentWriteFieldBinding.inflate(inflater)

        settingView()
        settingEvent()

        return fragmentWriteFieldBinding.root
    }

    fun settingView(){
        fragmentWriteFieldBinding.apply {

            // progress 게이지 설정
            fragmentWriteBinding.progressBarWriteFragment.progress = 20
        }
    }
    fun settingEvent(){
        val clickedStrokeColor = ContextCompat.getColor(mainActivity, R.color.pointColor)

        // 스터디 선택 시 클릭 리스너
        fragmentWriteFieldBinding.apply {
            cardviewWriteFieldStudy.setOnClickListener {
                cardviewWriteFieldStudy.apply {

                    // Stroke 색상 변경
                    cardviewWriteFieldStudy.strokeColor = clickedStrokeColor

                    // Elevation 추가
                    cardElevation = 20F

                }
            }

            // 공모전 선택 시 클릭 리스너
            cardviewWriteFieldContest.setOnClickListener {
                cardviewWriteFieldContest.apply {

                    // Stroke 색상 변경
                    cardviewWriteFieldStudy.strokeColor = clickedStrokeColor

                    // Elevation 추가
                    cardElevation = 20F

                }
            }

            // 프로젝트 선택 시 클릭 리스너
            cardviewWriteFieldProject.setOnClickListener {
                cardviewWriteFieldProject.apply {

                    // Stroke 색상 변경
                    cardviewWriteFieldStudy.strokeColor = clickedStrokeColor

                    // Elevation 추가
                    cardElevation = 20F
                }
            }

            buttonWriteFieldNext.setOnClickListener {
                // 다음 탭으로 이동
                fragmentWriteBinding.apply {
                    val currentItem = viewPagerWriteFragment.currentItem
                    if (currentItem < viewPagerWriteFragment.adapter!!.itemCount - 1){
                        viewPagerWriteFragment.currentItem = currentItem + 1
                    }
                }
            }
        }
    }
}