package kr.co.lion.modigm.ui.study

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudyMyBinding
import kr.co.lion.modigm.ui.study.adapter.StudyMyAdapter
import kr.co.lion.modigm.ui.study.vm.StudyViewModel


class StudyMyFragment : Fragment() {

    private lateinit var binding: FragmentStudyMyBinding
    private val viewModel: StudyViewModel by viewModels()

    val studyAllAdapter: StudyMyAdapter = StudyMyAdapter(
        rowClickListener = {

        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // 바인딩
        binding = FragmentStudyMyBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 뷰 세팅
        initView()
    }

    // 초기 뷰 세팅
    fun initView(){

        with(binding){


            // 리사이클러뷰
            with(recyclerViewStudyMy) {
                // 리사이클러뷰 어답터


                adapter = studyAllAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(requireActivity())
            }

        }
    }
}