package kr.co.lion.modigm.ui.write

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayout
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel

class WriteFragment : VBBaseFragment<FragmentWriteBinding>(FragmentWriteBinding::inflate) {
    // 뷰모델
    private val viewModel: WriteViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 뷰
        initView()
        // 뷰모델 관찰
        observeViewModel()
        // 뒤로가기 버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackPress()
        }
    }

    private fun initView(){
        with(binding) {

            // 탭 레이아웃 설정
            with(tabLayoutWriteFragment){
                val fragments = listOf(
                    WriteFieldFragment(),
                    WritePeriodFragment(),
                    WriteProceedFragment(),
                    WriteSkillFragment(),
                    WriteIntroFragment()
                )

                val fragmentManager = childFragmentManager
                selectFragment(fragmentManager, fragments.first())  // 처음 탭의 프래그먼트를 불러옵니다.

                // 탭에 제목을 설정하고 추가하는 부분
                val tabTitles = listOf("분야", "기간", "진행방식", "기술", "소개")
                tabTitles.forEachIndexed { index, title ->
                    val tab = newTab().setText(title)
                    addTab(tab)
                    // 첫 번째 탭을 선택 상태로 설정
                    if (index == 0) {
                        selectTab(tab)
                    }
                }

                addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        viewModel.currentTab = tab.position
                        selectFragment(fragmentManager, fragments[tab.position])
                        updateProgressBar(tab.position)
                    }
                    override fun onTabUnselected(tab: TabLayout.Tab) {}
                    override fun onTabReselected(tab: TabLayout.Tab) {
                        selectFragment(fragmentManager, fragments[tab.position])
                        updateProgressBar(tab.position)
                    }
                })
            }

            // 다음 버튼
            with(buttonWriteNext){
                setOnClickListener {
                    if (viewModel.validateCurrentTab()) {
                        if (viewModel.currentTab == 4) {
                            val introFragment = childFragmentManager.fragments.find { it is WriteIntroFragment } as? WriteIntroFragment
                            introFragment?.uploadImageAndSaveData  { studyIdx ->
                                Log.d("WriteFragment", "Navigating to detail with studyIdx: $studyIdx")
                                navigateToDetailFragment(studyIdx)  // 데이터베이스에 저장된 studyIdx 값을 전달하여 상세 프래그먼트로 이동
                            }
                        } else {
                            val currentTab = tabLayoutWriteFragment.selectedTabPosition
                            val nextTabPosition = currentTab + 1
                            if (nextTabPosition < tabLayoutWriteFragment.tabCount) {
                                val tab = tabLayoutWriteFragment.getTabAt(nextTabPosition)
                                tab?.select()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "모든 필드를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // 툴바 설정
            with(toolbarWriteFragment){
                // 뒤로가기 버튼
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    // 뷰모델 관찰
    private fun observeViewModel() {
        with(binding){
            // 뷰모델 관찰자 등록
            viewModel.isItemSelected.observe(viewLifecycleOwner) { isItemSelected ->
                if (isItemSelected) {
                    buttonWriteNext.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.pointColor)
                    buttonWriteNext.setTextColor(Color.WHITE) // 텍스트 색상을 흰색으로 설정
                } else {
                    buttonWriteNext.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.buttonGray)
                    // 선택된 항목이 없다면 버튼을 원래의 색상으로 변경하고 텍스트 색상을 기본 색상으로 설정
                    buttonWriteNext.setTextColor(ContextCompat.getColor(requireContext(), R.color.textGray))
                }
            }
        }
    }

    // 글상세 프래그먼트로 이동하는 메서드
    private fun navigateToDetailFragment(studyIdx: Int) {
        val detailFragment = DetailFragment().apply {
            arguments = Bundle().apply {
                putInt("studyIdx", studyIdx)
            }
        }
        Log.d("WriteFragment", "DetailFragment created with studyIdx: $studyIdx")
        parentFragmentManager.beginTransaction()
            .replace(R.id.containerMain,detailFragment)
            .commit()
        Log.d("WriteFragment", "DetailFragment transaction committed with studyIdx: $studyIdx")
    }

    // 프래그먼트 선택 메서드
    private fun selectFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout_writeFragment, fragment)
        transaction.commit()
    }

    // 프로그레스바 설정
    private fun updateProgressBar(position: Int) {
        val progressPercentage = (position + 1) * 20 // 5개의 탭이므로 20%씩 증가
        binding.progressBarWriteFragment.progress = progressPercentage
    }

    // 뒤로가기 버튼 처리
    private fun handleBackPress() {
        val currentTab = binding.tabLayoutWriteFragment.selectedTabPosition
        if (currentTab > 0) {
            val previousTabPosition = currentTab - 1
            val tab = binding.tabLayoutWriteFragment.getTabAt(previousTabPosition)
            tab?.select()
        } else {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}