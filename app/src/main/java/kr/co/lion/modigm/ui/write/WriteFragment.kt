package kr.co.lion.modigm.ui.write

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.google.android.material.tabs.TabLayout
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.login.CustomCancelDialog
import kr.co.lion.modigm.ui.write.vm.WriteViewModel
import kr.co.lion.modigm.util.FragmentName

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
        backButton()

    }

    private fun initView(){
        with(binding) {

            // 탭 레이아웃 설정
            with(tabLayoutWrite){
                addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        // 선택된 탭에 따라 프래그먼트 교체
                        val fragment = when (tab.position) {
                            0 -> WriteFieldFragment()
                            1 -> WritePeriodFragment()
                            2 -> WriteProceedFragment()
                            3 -> WriteSkillFragment()
                            4 -> WriteIntroFragment()
                            else -> WriteFieldFragment()
                        }

                        // 프로그래스바 20%씩 증가
                        val progressPercentage = (tab.position + 1) * 20
                        progressBarWrite.progress = progressPercentage

                        // 프래그먼트 교체
                        childFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace(R.id.containerWrite, fragment)
                        }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {

                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {

                    }
                })
            }

            // 툴바 설정
            with(toolbarWriteFragment){
                // 뒤로가기 버튼
                setNavigationOnClickListener {
                    showCancelDialog()
                }
            }
        }
    }

    // 뷰모델 관찰
    private fun observeViewModel() {
        with(binding){
        }
    }

    // 프래그먼트 선택 메서드
    private fun selectFragment(fragment: Fragment) {
        childFragmentManager.commit {
            replace(R.id.containerWrite, fragment)
        }
    }

    // 뒤로가기 버튼 처리
    private fun backButton() {
        with(binding) {
            // 백버튼 처리
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

                val currentTab = tabLayoutWrite.selectedTabPosition
                if (currentTab > 0) {
                    val previousTabPosition = currentTab - 1
                    val tab = tabLayoutWrite.getTabAt(previousTabPosition)
                    tab?.select()
                } else {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    // 뒤로가기 다이얼로그 표시
    private fun showCancelDialog() {
        val dialog = CustomCancelDialog(requireContext())
        with(dialog){
            setTitle("뒤로가기")
            setPositiveButton("확인") {
                parentFragmentManager.popBackStack(FragmentName.BOTTOM_NAVI.str,0)
            }
            setNegativeButton("취소") {
                dismiss()
            }
            show()
        }
    }
}