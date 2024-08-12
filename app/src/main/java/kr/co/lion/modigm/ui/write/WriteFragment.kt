package kr.co.lion.modigm.ui.write

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteBinding
import kr.co.lion.modigm.databinding.FragmentWriteFieldBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel


class WriteFragment : VBBaseFragment<FragmentWriteBinding>(FragmentWriteBinding::inflate) {

    private val viewModel: WriteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // tab
        setupTabs()

        // toolbar
        setupToolbar()

        // 다음 버튼
        setupNextButton()

        // 감시
        observe()

        // 뒤로가기 버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackPress()
        }
    }

    fun observe() {
        viewModel.isItemSelected.observe(viewLifecycleOwner) { isItemSelected ->
            if (isItemSelected) {
                binding.buttonWriteNext.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.pointColor)
                binding.buttonWriteNext.setTextColor(Color.WHITE) // 텍스트 색상을 흰색으로 설정
            } else {
                binding.buttonWriteNext.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.buttonGray)
                // 선택된 항목이 없다면 버튼을 원래의 색상으로 변경하고 텍스트 색상을 기본 색상으로 설정
                binding.buttonWriteNext.setTextColor(ContextCompat.getColor(requireContext(), R.color.textGray))
            }
        }
    }

    private fun setupTabs() {
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
            val tab = binding.tabLayoutWriteFragment.newTab().setText(title)
            binding.tabLayoutWriteFragment.addTab(tab)
            // 첫 번째 탭을 선택 상태로 설정
            if (index == 0) {
                binding.tabLayoutWriteFragment.selectTab(tab)
            }
        }

        binding.tabLayoutWriteFragment.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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

    private fun setupNextButton() {
        binding.buttonWriteNext.setOnClickListener {
            if (viewModel.validateCurrentTab()) {
                if (viewModel.currentTab == 4) {
                    val introFragment = childFragmentManager.fragments.find { it is WriteIntroFragment } as? WriteIntroFragment
                    introFragment?.uploadImageAndSaveData  { studyIdx ->
                        Log.d("WriteFragment", "Navigating to detail with studyIdx: $studyIdx")
                        navigateToDetailFragment(studyIdx)  // 데이터베이스에 저장된 studyIdx 값을 전달하여 상세 프래그먼트로 이동
                    }
                } else {
                    val currentTab = binding.tabLayoutWriteFragment.selectedTabPosition
                    val nextTabPosition = currentTab + 1
                    if (nextTabPosition < binding.tabLayoutWriteFragment.tabCount) {
                        val tab = binding.tabLayoutWriteFragment.getTabAt(nextTabPosition)
                        tab?.select()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "모든 필드를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

    private fun selectFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout_writeFragment, fragment)
        transaction.commit()
    }


    fun setupToolbar(){
        with(binding){
            toolbarWriteFragment.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun updateProgressBar(position: Int) {
        val progressPercentage = (position + 1) * 20 // 5개의 탭이므로 20%씩 증가
        binding.progressBarWriteFragment.progress = progressPercentage
    }

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