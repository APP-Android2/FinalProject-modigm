package kr.co.lion.modigm.ui.like

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentLikeBinding
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.ui.like.adapter.LikeAdapter

class LikeFragment : Fragment() {

    private lateinit var binding: FragmentLikeBinding

    // 프래그먼트의 뷰가 생성될 때 호출
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLikeBinding.inflate(inflater, container, false)

        return binding.root

    }

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 툴바 설정
        settingToolbar()

        setupRecyclerView()
    }

    fun settingToolbar() {
        binding.toolBarLike.title = "찜한 스터디"
    }

    fun setupRecyclerView() {
        // 비어있는 화면 확인 임시 데이터
//        val dummyData = listOf<StudyData>()

        // 임시 확인 데이터 (StudyData 기반으로)
        val dummyData = listOf(
            StudyData(
                studyIdx = 1,
                studyTitle = "Java 스터디 모집합니다!",
                studyContent = "자바 기초부터 시작하는 스터디",
                studyType = 0,
                studyPeriod = 12,
                studyOnOffline = 0,
                studyPlace = "서울",
                studyApplyMethod = 1,
                studyMaxMember = 20,
                studySkillList = listOf(1, 2),
                studyState = true,
                studyPic = "image_detail_1",
                studyUidList = listOf("1", "2", "3")
            ),
            StudyData(
                studyIdx = 2,
                studyTitle = "Python 스터디 모집합니다!",
                studyContent = "데이터 사이언스를 위한 파이썬",
                studyType = 1,
                studyPeriod = 10,
                studyOnOffline = 1,
                studyPlace = "온라인",
                studyApplyMethod = 0,
                studyMaxMember = 10,
                studySkillList = listOf(3, 4),
                studyState = false,
                studyPic = "image_detail_2",
                studyUidList = listOf("4","5")
            ),
            StudyData(
                studyIdx = 3,
                studyTitle = "JavaScript 프로젝트!",
                studyContent = "웹 개발 프로젝트 참가자 모집",
                studyType = 2,
                studyPeriod = 15,
                studyOnOffline = 2,
                studyPlace = "온오프 혼합",
                studyApplyMethod = 1,
                studyMaxMember = 15,
                studySkillList = listOf(5, 6),
                studyState = true,
                studyPic = "image_detail_1",
                studyUidList = listOf("6", "7", "8", "9", "10")
            )
        )

        if (dummyData.isNotEmpty()) {
            binding.recyclerviewLike.visibility = View.VISIBLE
            binding.blankLayoutLike.visibility = View.GONE
            binding.recyclerviewLike.layoutManager = LinearLayoutManager(context)
            binding.recyclerviewLike.adapter = LikeAdapter(dummyData)
        } else {
            binding.recyclerviewLike.visibility = View.GONE
            binding.blankLayoutLike.visibility = View.VISIBLE
        }
    }

}