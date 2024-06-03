package kr.co.lion.modigm.ui.write

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import kr.co.lion.modigm.databinding.FragmentWriteBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.write.vm.WriteViewModel


class WriteFragment : Fragment() {

    lateinit var fragmentWriteBinding: FragmentWriteBinding
    private val viewModel: WriteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentWriteBinding = FragmentWriteBinding.inflate(inflater)

        return fragmentWriteBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingView()
        settingEvent()
        viewPagerActivation()
    }

    fun settingEvent() {
        fragmentWriteBinding.apply {

            // Toolbar
            toolbarWriteFragment.apply {
                setNavigationOnClickListener {
                    // 뒤로가기
                    parentFragmentManager
                        .beginTransaction()
                        .remove(this@WriteFragment)
                        .commit()

                    parentFragmentManager.popBackStack()
                }
            }

            buttonWriteNext.apply {

            }
            buttonWriteNext.setOnClickListener {
                // 버튼의 상태가 실시간으로 반영되지 않고 탭 간의 이동이 발생해야 반영이 되는 문제가 발생함 -> 해결
                val currentItem = viewPagerWriteFragment.currentItem
                if (viewModel.buttonState.value == true) {
                    Log.d("TedMoon", "Button Activated")
                    if (currentItem < viewPagerWriteFragment.adapter!!.itemCount - 1) {
                        viewPagerWriteFragment.currentItem += 1
                    }

                } else {
                    Log.d("TedMoon", "Button not Activated")
                }
            }
        }
    }

    fun settingView() {
        fragmentWriteBinding.apply {

            viewPagerWriteFragment.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    // progress Bar 게이지 변경 & 버튼 색상 설정
                    progressBarWriteFragment.apply {
                        when (position) {
                            0 -> {
                                setProgress(20, true)
                                settingButtonView(buttonWriteNext, position)
                            }

                            1 -> {
                                setProgress(40, true)
                                settingButtonView(buttonWriteNext, position)
                            }

                            2 -> {
                                setProgress(60, true)
                                settingButtonView(buttonWriteNext, position)
                            }

                            3 -> {
                                setProgress(80, true)
                                settingButtonView(buttonWriteNext, position)
                            }

                            4 -> {
                                setProgress(100, true)
                                settingButtonView(buttonWriteNext, position)
                            }
                        }
                    }
                }
            })
        }
    }

    // Tab Layout 활성화/비활성화 처리 -> 2차
    fun settingProgressBar() {

    }

    fun settingButtonView(btn: Button, position: Int) {
        btn.apply {
            when (position) {
                // 탭 - 분야
                0 -> {
                    // 버튼 텍스트 설정
                    btn.text = "다음"

                    // 버튼 색상 설정 - 임시본
                    viewModel.fieldClicked.observe(viewLifecycleOwner) { didAnswer ->
                        if (didAnswer) {
                            btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                            btn.setTextColor(Color.parseColor("#FFFFFF"))
                            viewModel.activateButton() // 버튼 활성화
                        } else {
                            btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                            btn.setTextColor(Color.parseColor("#777777"))
                            viewModel.deactivateButton() // 버튼 비활성화
                        }
                    }
                }
                // 탭 - 기간
                1 -> {
                    // 버튼 텍스트 설정
                    btn.text = "다음"

                    // 버튼 색상 설정 - 임시본
                    viewModel.periodClicked.observe(viewLifecycleOwner) { didAnswer ->
                        if (didAnswer) {
                            btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                            btn.setTextColor(Color.parseColor("#FFFFFF"))
                            viewModel.activateButton() // 버튼 활성화
                        } else {
                            btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                            btn.setTextColor(Color.parseColor("#777777"))
                            viewModel.deactivateButton() // 버튼 비활성화
                        }
                    }
                }
                // 탭 - 진행방식
                2 -> {
                    // 버튼 텍스트 설정
                    btn.text = "다음"

                    // 버튼 색상 설정 - 임시본
                    viewModel.proceedClicked.observe(viewLifecycleOwner) { didAnswer ->
                        if (didAnswer) {
                            Log.d("TedMoon", "Did Clicked? : ${viewModel.proceedClicked}")
                            btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                            btn.setTextColor(Color.parseColor("#FFFFFF"))
                            viewModel.activateButton() // 버튼 활성화
                        } else {
                            Log.d("TedMoon", "Did Clicked? : ${viewModel.proceedClicked}")
                            btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                            btn.setTextColor(Color.parseColor("#777777"))
                            viewModel.deactivateButton() // 버튼 비활성화
                        }
                    }
                }
                // 탭 - 기술
                3 -> {
                    // 버튼 텍스트 설정
                    btn.text = "다음"

                    // 버튼 색상 설정 - 임시본
                    viewModel.skillClicked.observe(viewLifecycleOwner) { didAnswer ->
                        if (didAnswer) {
                            btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                            btn.setTextColor(Color.parseColor("#FFFFFF"))
                            viewModel.activateButton() // 버튼 활성화
                        } else {
                            btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                            btn.setTextColor(Color.parseColor("#777777"))
                            viewModel.deactivateButton() // 버튼 비활성화
                        }
                    }
                }
                // 탭 - 소개
                4 -> {
                    // 버튼 텍스트 설정
                    btn.text = "완료"

                    // 버튼 색상 설정 - 임시본
                    viewModel.introClicked.observe(viewLifecycleOwner) { didAnswer ->
                        if (didAnswer) {
                            btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                            btn.setTextColor(Color.parseColor("#FFFFFF"))
                            viewModel.activateButton() // 버튼 활성화
                        } else {
                            btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                            btn.setTextColor(Color.parseColor("#777777"))
                            viewModel.deactivateButton() // 버튼 비활성화
                        }
                    }
                }
            }
        }
    }

    // ViewPager 설정
    fun viewPagerActivation() {
        fragmentWriteBinding.apply {

            // ViewPager 스와이프(제거)
            this.viewPagerWriteFragment.isUserInputEnabled = false

            // 1. 페이지 데이터를 로드
            val fragmentList = listOf(
                WriteFieldFragment(),
                WritePeriodFragment(),
                WriteProceedFragment(),
                WriteSkillFragment(),
                WriteIntroFragment()
            )

            val context = this@WriteFragment
            // 2. Adapter 생성
            val pagerAdapter = FragmentPagerAdapter(fragmentList, context)

            // 3. Adapter와 ViewPager 연결
            viewPagerWriteFragment.adapter = pagerAdapter

            // 4. Tab Layout과 ViewPager 연결
            TabLayoutMediator(tabLayoutWriteFragment, viewPagerWriteFragment) { tab, position ->
                // tab 제목 설정
                tab.text = when (position) {
                    0 -> "분야"
                    1 -> "기간"
                    2 -> "진행방식"
                    3 -> "기술"
                    4 -> "소개"
                    else -> throw IllegalArgumentException("Invalid postion : $position")
                }
            }.attach()
        }
    }

    private inner class FragmentPagerAdapter(
        val fragmentList: List<Fragment>,
        writeFragment: WriteFragment
    ) : FragmentStateAdapter(writeFragment) {
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            val fragment = fragmentList[position]


            // 각 fragment에 전달할 데이터를 설정해준다
            val bundle = Bundle().apply {

            }
            fragment.arguments = bundle

            return fragment
        }
    }
}
