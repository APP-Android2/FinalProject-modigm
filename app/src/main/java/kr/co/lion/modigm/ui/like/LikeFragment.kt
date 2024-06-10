package kr.co.lion.modigm.ui.like

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentLikeBinding
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.ui.like.adapter.LikeAdapter
import kr.co.lion.modigm.ui.like.vm.LikeViewModel

class LikeFragment : Fragment() {

    private lateinit var binding: FragmentLikeBinding
    private val viewModel: LikeViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var likeAdapter: LikeAdapter

    // 프래그먼트의 뷰가 생성될 때 호출
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLikeBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

        return binding.root

    }

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 툴바 설정
        settingToolbar()

        setupRecyclerView()

        observeViewModel()

        // 좋아요한 스터디 데이터를 로드
        Log.d("LikeFragment", "Loading liked studies for user: $uid")

        // 좋아요한 스터디 데이터를 로드
        viewModel.loadLikedStudies(uid)
    }

    fun settingToolbar() {
        binding.toolBarLike.title = "찜한 스터디"
    }

    fun setupRecyclerView() {
        binding.recyclerviewLike.layoutManager = LinearLayoutManager(context)
        likeAdapter = LikeAdapter(emptyList()) { study ->
            viewModel.toggleLike(uid, study.studyIdx)
        }
        binding.recyclerviewLike.adapter = likeAdapter
    }

    fun observeViewModel() {
        viewModel.likedStudies.observe(viewLifecycleOwner) { studies ->
            Log.d("LikeFragment", "Observed liked studies: $studies")
            if (studies.isNotEmpty()) {
                binding.recyclerviewLike.visibility = View.VISIBLE
                binding.blankLayoutLike.visibility = View.GONE
                likeAdapter.updateData(studies)
            } else {
                binding.recyclerviewLike.visibility = View.GONE
                binding.blankLayoutLike.visibility = View.VISIBLE
            }
        }
    }

}