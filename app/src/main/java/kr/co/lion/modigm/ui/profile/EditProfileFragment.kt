package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentEditProfileBinding
import kr.co.lion.modigm.ui.profile.vm.EditProfileViewModel
import kr.co.lion.modigm.util.FragmentName

class EditProfileFragment : Fragment() {
    lateinit var fragmentEditProfileBinding: FragmentEditProfileBinding
    private val editProfileViewModel: EditProfileViewModel by viewModels()

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentEditProfileBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)

        // Bind ViewModel and lifecycle owner
        fragmentEditProfileBinding.editProfileViewModel = editProfileViewModel
        fragmentEditProfileBinding.lifecycleOwner = this

        firebaseAuth = Firebase.auth
        // 임시 로그인
        firebaseAuth.signInWithEmailAndPassword("test@naver.com", "test1234!").addOnCompleteListener {
            if (it.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("editprofile", "signInWithEmail:success")
                user = firebaseAuth.currentUser!!
                Log.d("editprofile", user.uid)
                initView()
            } else {
                // If sign in fails, display a message to the user.
                Log.w("editprofile", "signInWithEmail:failure", it.exception)

            }
        }

        return fragmentEditProfileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        initView()
    }

    private fun initView() {
        setupToolbar()
        setupUserInfo()
//        setupRecyclerViewLink()
//        setupRecyclerViewPartStudy()
//        setupRecyclerViewHostStudy()
//
//        observeData()
    }

    private fun setupToolbar() {
        fragmentEditProfileBinding.apply {
            toolbarEditProfile.apply {
                // title
                title = "프로필 수정"

                // 뒤로 가기
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack(FragmentName.EDIT_PROFILE.str, 0)
                }
            }
        }
    }

    private fun setupUserInfo() {
        editProfileViewModel.loadUserData(user, requireContext(), fragmentEditProfileBinding.imageProfilePic, fragmentEditProfileBinding.chipGroupProfile)
//        editProfileViewModel.loadPartStudyList(uid)
//        editProfileViewModel.loadHostStudyList(uid)
    }
}