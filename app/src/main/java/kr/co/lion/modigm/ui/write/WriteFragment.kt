package kr.co.lion.modigm.ui.write

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteBinding
import kr.co.lion.modigm.ui.chat.ChatFragment
import kr.co.lion.modigm.ui.detail.DetailEditFragment
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel
import kr.co.lion.modigm.util.FragmentName


class WriteFragment : Fragment() {

    lateinit var fragmentWriteBinding: FragmentWriteBinding
    private val writeViewModel: WriteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentWriteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_write, container, false)
        fragmentWriteBinding.lifecycleOwner = this
        fragmentWriteBinding.writeViewModel = writeViewModel

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

            // 다음 버튼 클릭 리스너
            buttonWriteNext.setOnClickListener {
                val currentItem = viewPagerWriteFragment.currentItem
                if (writeViewModel?.buttonState?.value == true) {
                    if (currentItem < viewPagerWriteFragment.adapter!!.itemCount - 1) {
                        viewPagerWriteFragment.currentItem += 1
                    }
                }
            }

            // 완료 버튼 클릭 리스너
            if (writeViewModel?.buttonText?.value == "완료") {
                // 내 글보기 화면으로 이동
                buttonWriteNext.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.containerMain, DetailFragment())
                        .addToBackStack(FragmentName.DETAIL.str)
                        .commit()
                }
            }
        }
    }

    fun settingView() {
        fragmentWriteBinding.apply {

            viewPagerWriteFragment.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    // progress Bar 게이지 변경 & 버튼 설정
                    progressBarWriteFragment.apply {
                        // progress Bar 게이지 설정
                        settingProgress(this, position)

                        // 버튼 설정
                        settingButton(position)
                    }
                }
            })
        }
    }

    // Tab Layout 활성화/비활성화 처리 -> 2차
    fun settingTabLayout() {

    }

    // 버튼 설정
    fun settingButton(position: Int) {
        when (position) {
            // 탭 - 분야
            0 -> {
                val tabName = "field"
                writeViewModel.fieldClicked.observe(viewLifecycleOwner) { didAnswer ->
                    writeViewModel.settingButton(
                        fragmentWriteBinding.buttonWriteNext,
                        tabName,
                        didAnswer
                    )
                }
            }
            // 탭 - 기간
            1 -> {
                val tabName = "period"
                writeViewModel.periodClicked.observe(viewLifecycleOwner) { didAnswer ->
                    writeViewModel.settingButton(
                        fragmentWriteBinding.buttonWriteNext,
                        tabName,
                        didAnswer
                    )
                }
            }
            // 탭 - 진행방식
            2 -> {
                val tabName = "proceed"
                writeViewModel.proceedClicked.observe(viewLifecycleOwner) { didAnswer ->
                    writeViewModel.settingButton(
                        fragmentWriteBinding.buttonWriteNext,
                        tabName,
                        didAnswer
                    )
                }
            }
            // 탭 - 기술
            3 -> {
                val tabName = "skill"
                writeViewModel.skillClicked.observe(viewLifecycleOwner) { didAnswer ->
                    writeViewModel.settingButton(
                        fragmentWriteBinding.buttonWriteNext,
                        tabName,
                        didAnswer
                    )
                }
            }
            // 탭 - 소개
            4 -> {
                val tabName = "intro"
                writeViewModel.introClicked.observe(viewLifecycleOwner) { didAnswer ->
                    writeViewModel.settingButton(
                        fragmentWriteBinding.buttonWriteNext,
                        tabName,
                        didAnswer
                    )
                }
            }

        }
    }

    // Progress Bar 설정
    fun settingProgress(progressBar: ProgressBar, position: Int){
        progressBar.apply {
            writeViewModel.settingProgressBar(position)
            writeViewModel.progressCount.observe(viewLifecycleOwner){ progress ->
                setProgress(progress, true)
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
