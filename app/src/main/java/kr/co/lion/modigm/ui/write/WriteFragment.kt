package kr.co.lion.modigm.ui.write

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        initData()
        settingView()
        settingEvent()
        viewPagerActivation()
    }

    fun initData() {
        writeViewModel.initField()
        writeViewModel.initPeriod()
        writeViewModel.initProceed()
        writeViewModel.initSkill()
        writeViewModel.initIntro()
    }

    fun settingEvent() {
        fragmentWriteBinding.apply {

            // Toolbar
            toolbarWriteFragment.apply {
                setNavigationOnClickListener {
                    // 뒤로가기
                    parentFragmentManager.popBackStack()
                }
            }

            // 버튼 리스너
            buttonWriteNext.setOnClickListener {
                val currentItem = viewPagerWriteFragment.currentItem
                // 다음 버튼 클릭 리스너
                if (writeViewModel?.buttonState?.value == true && writeViewModel?.buttonFinalStateActivation() == false) {
                    if (currentItem < viewPagerWriteFragment.adapter!!.itemCount - 1) {
                        viewPagerWriteFragment.currentItem += 1
                    }
                }
                // 완료 버튼 클릭 리스너
                else if (writeViewModel?.buttonFinalStateActivation() == true) {
                    // 입력된 정보를 DB에 저장
                    uploadStudyData()

                    // 데이터를 저장한 후, DetailFragment로 이동하면서 studyId를 전달
                    val studyIdx = (writeViewModel as WriteViewModel).returnStudyIdx()

                    // DetailFragment에 Bundle 객체로 studyIdx를 전달
                    val detailFragment = DetailFragment().apply {
                        val bundle = Bundle().apply {
                            // studyIdx를 Bundle 객체에 포함
                            putInt("studyIdx", studyIdx)
                        }
                        // argument에 전달
                        arguments = bundle
                    }
                    // 저장 후 내 글 보기 화면으로 이동
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.containerMain, detailFragment)
                        .addToBackStack(FragmentName.DETAIL.str)
                        .commit()

                } else if (writeViewModel?.buttonFinalStateActivation() == false) {
                    Log.d("TedMoon", "Deactivated Button!!")
                    noticeUserDidNotAnswer()
                }
                getLog()
            }

        }
    }

    // 글 작성처리 메서드
    fun uploadStudyData() {
        viewLifecycleOwner.lifecycleScope.launch {
            writeViewModel.uploadStudyData()
            try {
                // 스터디 정보 업로드
                Log.d("TedMoon", "정보 업로드")
            } catch (e: Exception) {
                Log.e("Finish Button", "Firebase Error ${e}")
            }
        }
    }

    fun settingView() {
        fragmentWriteBinding.apply {
            // ViewPager의 화면이 바뀔 때마다 적용
            viewPagerWriteFragment.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    // progress Bar 게이지 설정
                    settingProgress(progressBarWriteFragment, position)
                    // 버튼 설정
                    settingButton(buttonWriteNext, position)
                }
            })
        }
    }

    // 어디서 입력을 안 했는지 알려줌
    fun noticeUserDidNotAnswer() {
        val context = requireContext()
        val tab0 = writeViewModel.fieldClicked.value!!
        val tab1 = writeViewModel.periodClicked.value!!
        val tab2 = writeViewModel.proceedClicked.value!!
        val tab3 = writeViewModel.skillClicked.value!!
        val tab4 = writeViewModel.introClicked.value!!

        val toast = if (!tab0) {
            // 분야
            Toast.makeText(context, "분야를 입력해주세요", Toast.LENGTH_LONG)
        } else if (!tab1) {
            Toast.makeText(context, "기간을 입력해주세요", Toast.LENGTH_LONG)
        } else if (!tab2) {
            Toast.makeText(context, "진행방식을 입력해주세요", Toast.LENGTH_LONG)
        } else if (!tab3) {
            Toast.makeText(context, "기술을 입력해주세요", Toast.LENGTH_LONG)
        } else if (!tab4) {
            Toast.makeText(context, "소개를 입력해주세요", Toast.LENGTH_LONG)
        } else null

        toast?.show()
    }

    // Tab Layout 활성화/비활성화 처리 -> (2차)
    fun settingTabLayout() {

    }

    // 버튼 설정
    fun settingButton(button: Button, position: Int) {
        when (position) {
            0 -> { // 탭 - 분야
                // text설정
                button.setText("다음")

                // 사용자 입력 여부에 따라 UI 변경
                writeViewModel.fieldClicked.observe(viewLifecycleOwner) { didAnswer ->
                    settingButtonView(button, didAnswer)
                }
            }

            1 -> { // 탭 - 기간
                // text설정
                button.setText("다음")

                // 사용자 입력 여부에 따라 UI 변경
                writeViewModel.periodClicked.observe(viewLifecycleOwner) { didAnswer ->
                    settingButtonView(button, didAnswer)
                }
            }

            2 -> { // 탭 - 진행방식
                // text설정
                button.setText("다음")

                // 사용자 입력 여부에 따라 UI 변경
                writeViewModel.proceedClicked.observe(viewLifecycleOwner) { didAnswer ->
                    settingButtonView(button, didAnswer)
                }
            }

            3 -> { // 탭 - 기술
                // text설정
                button.setText("다음")

                // 사용자 입력 여부에 따라 UI 변경
                writeViewModel.skillClicked.observe(viewLifecycleOwner) { didAnswer ->
                    settingButtonView(button, didAnswer)
                }
            }

            4 -> { // 탭 - 소개
                // text설정
                button.setText("완료")

                // 사용자 입력 여부에 따라 UI 변경
                writeViewModel.introClicked.observe(viewLifecycleOwner) { didAnswer ->
                    settingButtonView(button, didAnswer)
                }
            }

        }
    }

    fun settingButtonView(button: Button, didAnswer: Boolean) {
        if (didAnswer) { // 버튼 활성화

            // 버튼 배경색 설정
            button.setBackgroundColor(Color.parseColor("#1A51C5"))
            // 버튼 글자색 설정
            button.setTextColor(Color.parseColor("#FFFFFF"))
            // 버튼 활성화
            writeViewModel.activateButton()
        } else { // 버튼 비활성화

            // 버튼 배경색 설정
            button.setBackgroundColor(Color.parseColor("#bbbbbb"))
            // 버튼 글자색 설정
            button.setTextColor(Color.parseColor("#777777"))
            // 버튼 비활성화
            writeViewModel.deactivateButton()
        }
    }

    // Progress Bar 설정
    fun settingProgress(progressBar: ProgressBar, position: Int) {
        when (position) {
            0 -> progressBar.setProgress(20, true)
            1 -> progressBar.setProgress(40, true)
            2 -> progressBar.setProgress(60, true)
            3 -> progressBar.setProgress(80, true)
            4 -> progressBar.setProgress(100, true)
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

            return fragment
        }
    }

    // Log를 찍어준다 - 테스트용
    private fun getLog() {
        Log.d(
            "TedMoon",
            "ViewPager currentItem : ${fragmentWriteBinding.viewPagerWriteFragment.currentItem}"
        )
        Log.d(
            "TedMoon",
            "ViewPager ItemSize : ${fragmentWriteBinding.viewPagerWriteFragment.adapter!!.itemCount}"
        )

        Log.d(
            "TedMoon",
            "다음 / 완료 Button상태\n" +
                    "buttonState : ${writeViewModel.buttonState.value}\n" +
                    "buttonFinalState : ${writeViewModel.buttonFinalStateActivation()}"
        )
        Log.d(
            "TedMoon",
            "탭별 Clicked 상태\n" +
                    "fieldClicked : ${writeViewModel?.fieldClicked?.value}\n " +
                    "periodClicked : ${writeViewModel?.periodClicked?.value}\n " +
                    "proceedClicked : ${writeViewModel?.proceedClicked?.value}\n " +
                    "skillClicked : ${writeViewModel?.skillClicked?.value}\n" +
                    "introClicked : ${writeViewModel?.introClicked?.value}"
        )
        Log.d(
            "TedMoon",
            "각 데이터 조회\n" +
                    "글 고유번호 : ${writeViewModel.studyIdx.value}\n" +
                    "글 제목 : ${writeViewModel?.studyTitle?.value}\n" +
                    "글 내용 : ${writeViewModel?.studyContent?.value}\n" +
                    "활동타입 : ${writeViewModel?.studyType?.value},\n" +
                    "진행기간: ${writeViewModel?.studyPeriod?.value},\n" +
                    "진행방식: ${writeViewModel?.studyOnOffline?.value},\n" +
                    "스터디 장소 : ${writeViewModel?.studyPlace?.value},\n" +
                    "스터디 장소 세부정보 : ${writeViewModel?.studyDetailPlace?.value}\n" +
                    "신청방식 : ${writeViewModel?.studyApplyMethod?.value}\n" +
                    "필요기술 스택 목록 : ${writeViewModel?.studySkillList?.value}\n" +
                    "모집상태 : ${writeViewModel?.studyCanApply?.value}\n" +
                    "썸네일 사진 : ${writeViewModel?.studyPic?.value}\n" +
                    "스터디 인원 : ${writeViewModel?.studyMaxMember?.value}\n" +
                    "현재 참여자 목록 : ${writeViewModel?.studyUIdList?.value}\n" +
                    "연결된 현재 채팅방 고유 번호 : ${writeViewModel?.chatIdx?.value}\n" +
                    "글 삭제여부 : ${writeViewModel?.studyState?.value}\n"
        )
    }
}
