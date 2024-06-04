package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentEditProfileBinding
import kr.co.lion.modigm.ui.profile.vm.EditProfileViewModel

class EditProfileFragment : Fragment() {
    lateinit var fragmentEditProfileBinding: FragmentEditProfileBinding
    private val editProfileViewModel: EditProfileViewModel by viewModels()

    // arguments에서 불러옴
    val uid = "fKdVSYNodxYgYJHq8MYKlAC2GCk1"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentEditProfileBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)

        // Bind ViewModel and lifecycle owner
        fragmentEditProfileBinding.editProfileViewModel = editProfileViewModel
        fragmentEditProfileBinding.lifecycleOwner = this

        return fragmentEditProfileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        setupToolbar()
        setupFab()
        setupUserInfo()
        setupRecyclerViewLink()
        setupRecyclerViewPartStudy()
        setupRecyclerViewHostStudy()

        observeData()
    }
}