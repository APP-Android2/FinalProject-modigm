package kr.co.lion.modigm.ui.favorite

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFavoriteBinding
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.favorite.adapter.FavoriteAdapter
import kr.co.lion.modigm.ui.favorite.vm.FavoriteViewModel
import kr.co.lion.modigm.util.FragmentName

class FavoriteFragment : Fragment() {

    private lateinit var binding: FragmentFavoriteBinding
    private val viewModel: FavoriteViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var favoriteAdapter: FavoriteAdapter

    // 프래그먼트의 뷰가 생성될 때 호출
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)

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
        Log.d("FavoriteFragment", "Loading Favorited studies for user: $uid")

        // 좋아요한 스터디 데이터를 로드
        viewModel.loadFavoriteStudies(uid)

        // 옵저버에서 클릭된 항목 처리
        favoriteAdapter.setOnItemClickListener { study ->
            // 클릭된 항목의 studyIdx를 bundle에 담아서 다음 화면으로 전달
            val detailFragment = DetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("studyIdx", study.studyIdx)
                    Log.d("Favoritefragment","${study.studyIdx}")
                }
            }

            requireActivity().supportFragmentManager.commit {
                replace(R.id.containerMain, detailFragment)
                addToBackStack(FragmentName.DETAIL.str)
            }
        }

    }

    fun settingToolbar() {
        binding.toolBarFavorite.title = "찜한 스터디"
    }

    fun setupRecyclerView() {
        binding.recyclerviewFavorite.layoutManager = LinearLayoutManager(context)
        favoriteAdapter = FavoriteAdapter(emptyList()) { study ->
            viewModel.toggleFavorite(uid, study.studyIdx)
        }
        binding.recyclerviewFavorite.adapter = favoriteAdapter
    }

    fun observeViewModel() {
        viewModel.favoritedStudies.observe(viewLifecycleOwner) { studies ->
            Log.d("FavoriteFragment", "Observed Favorited studies: $studies")
            if (studies.isNotEmpty()) {
                binding.recyclerviewFavorite.visibility = View.VISIBLE
                binding.blankLayoutFavorite.visibility = View.GONE
                favoriteAdapter.updateData(studies)
            } else {
                binding.recyclerviewFavorite.visibility = View.GONE
                binding.blankLayoutFavorite.visibility = View.VISIBLE
            }
        }
    }

}