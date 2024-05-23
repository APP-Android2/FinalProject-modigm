package kr.co.lion.modigm.ui.write

import android.os.Bundle
import android.util.Log
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

        // 카드 뷰 클릭시 이벤트
        cardViewEffect()
    }

    // 카드뷰 클릭시 효과 설정
    fun cardViewEffect(){
        val clickedStrokeColor = ContextCompat.getColor(mainActivity, R.color.pointColor)
        val unclickedStrokeColor = ContextCompat.getColor(mainActivity, R.color.textGray)

        // 스터디 선택 시 클릭 리스너
        fragmentWriteFieldBinding.apply {
            cardviewWriteFieldStudy.setOnClickListener {
                cardviewWriteFieldStudy.apply {
                    if (cardElevation == 20F && strokeColor == clickedStrokeColor){
                        cardElevation = 0F
                        strokeColor = unclickedStrokeColor
                    } else {

                        // Stroke 색상 변경
                        strokeColor = clickedStrokeColor
                        cardviewWriteFieldContest.strokeColor = unclickedStrokeColor
                        cardviewWriteFieldProject.strokeColor = unclickedStrokeColor

                        // Elevation 추가
                        cardElevation = 20F
                        cardviewWriteFieldContest.cardElevation = 0F
                        cardviewWriteFieldProject.cardElevation = 0F
                    }
                }
            }

            // 공모전 선택 시 클릭 리스너
            cardviewWriteFieldContest.setOnClickListener {
                cardviewWriteFieldContest.apply {
                    if (cardElevation == 20F && strokeColor == clickedStrokeColor){
                        cardElevation = 0F
                        strokeColor = unclickedStrokeColor
                    } else {
                        // Stroke 색상 변경
                        strokeColor = clickedStrokeColor
                        cardviewWriteFieldStudy.strokeColor = unclickedStrokeColor
                        cardviewWriteFieldProject.strokeColor = unclickedStrokeColor

                        // Elevation 추가
                        cardElevation = 20F
                        cardviewWriteFieldStudy.cardElevation = 0F
                        cardviewWriteFieldProject.cardElevation = 0F
                    }
                }
            }

            // 프로젝트 선택 시 클릭 리스너
            cardviewWriteFieldProject.setOnClickListener {
                cardviewWriteFieldProject.apply {
                    if (cardElevation == 20F && strokeColor == clickedStrokeColor){
                        cardElevation = 0F
                        strokeColor = unclickedStrokeColor
                    } else {

                        // Stroke 색상 변경
                        strokeColor = clickedStrokeColor
                        cardviewWriteFieldContest.strokeColor = unclickedStrokeColor
                        cardviewWriteFieldStudy.strokeColor = unclickedStrokeColor

                        // Elevation 추가
                        cardElevation = 20F
                        cardviewWriteFieldContest.cardElevation = 0F
                        cardviewWriteFieldStudy.cardElevation = 0F
                    }
                }
            }


            buttonWriteFieldNext.setOnClickListener {
                // 다음 탭으로 이동
                fragmentWriteBinding.apply {
                    Log.d("TedMoon", "다음 탭으로 이동1")
                    val currentItem = viewPagerWriteFragment.currentItem
                    Log.d("TedMoon", "다음 탭으로 이동2")
                    if (currentItem < viewPagerWriteFragment.adapter!!.itemCount - 1){
                        Log.d("TedMoon", "다음 탭으로 이동2-1")
                        viewPagerWriteFragment.currentItem = currentItem + 1
                        Log.d("TedMoon", "다음 탭으로 이동2-2")
                    }
                    Log.d("TedMoon", "다음 탭으로 이동 완료")
                }
            }
        }
    }
}