package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDeleteUserBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.login.SocialLoginFragment
import kr.co.lion.modigm.ui.profile.vm.DeleteUserViewModel
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
import kr.co.lion.modigm.util.collectWhenStarted
import kr.co.lion.modigm.util.hideSoftInput

class DeleteUserFragment : VBBaseFragment<FragmentDeleteUserBinding>(FragmentDeleteUserBinding::inflate) {

    private val viewModel: DeleteUserViewModel by viewModels()

    private val userIdx by lazy {
        prefs.getInt("currentUserIdx")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inflate the layout for this fragment
        settingToolBar()
        settingDeleteUserButton()
    }

    private fun settingToolBar(){
        with(binding.toolbarDeleteUser){
            setNavigationIcon(R.drawable.arrow_back_24px)
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun settingDeleteUserButton(){
        binding.buttonDeleteUser.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog, null)
            val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.dialogColor)
                .setTitle("회원 탈퇴")
                .setMessage("정말로 회원 탈퇴를 진행하시겠습니까?")
                .setView(dialogView)
                .create()

            dialogView.findViewById<TextView>(R.id.btnYes).text = "네"
            dialogView.findViewById<TextView>(R.id.btnYes).setOnClickListener {
                // 회원 탈퇴 실행
                viewModel.deleteUserDate(userIdx)
                settingCollector()
                dialog.dismiss()
                showLoading()
            }

            dialogView.findViewById<TextView>(R.id.btnNo).text = "아니오"
            dialogView.findViewById<TextView>(R.id.btnNo).setOnClickListener {
                // 아니요 버튼 로직
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    private fun settingCollector() {
        // 회원 탈퇴 완료 시
        collectWhenStarted(viewModel.isDeleted){
            hideLoading()
            if(it){
                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, SocialLoginFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        // 회원 탈퇴 실패 시
        collectWhenStarted(viewModel.errorMessage){
            // 스낵바 띄우기
            it?.let {
                hideLoading()
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.resetErrorMessage()
            }
        }
    }

    private fun showLoading() {
        requireActivity().hideSoftInput()
        binding.layoutDeleteUserLoading.visibility = View.VISIBLE
        requireActivity().window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun hideLoading() {
        binding.layoutDeleteUserLoading.visibility = View.GONE
        requireActivity().window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}