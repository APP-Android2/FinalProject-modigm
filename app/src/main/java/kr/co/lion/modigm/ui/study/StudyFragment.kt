package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudyBinding
import kr.co.lion.modigm.ui.write.WriteFragment

class StudyFragment : Fragment() {

    // 바인딩
    private lateinit var binding : FragmentStudyBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // 바인딩
        binding = FragmentStudyBinding.inflate(inflater,container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 프래그먼트 설정
        if (savedInstanceState == null) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerStudy, StudyAllFragment())
                .commit()
        }

        // 초기 뷰 세팅
        initView()

    }

    // 초기 뷰 세팅
    fun initView(){

        // 바인딩
        with(binding){

            // 탭 레이아웃 설정
            val tabLayout: TabLayout = tabLayoutStudy

            // 탭 선택 리스너 설정
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val fragment = when (tab.position) {
                        0 -> StudyAllFragment()
                        1 -> StudyMyFragment()
                        else -> StudyAllFragment()
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerStudy, fragment)
                        .commit()
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    // 필요시 구현
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    // 필요시 구현
                }

            })

            // FAB 설정
            with(fabStudyWrite){
                setOnClickListener{
                    requireActivity().supportFragmentManager.beginTransaction().replace(R.id.containerMain, WriteFragment()).commit()
                }
            }
        }
    }
}