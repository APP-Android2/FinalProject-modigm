package kr.co.lion.modigm.ui.login.social

import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentSocialLoginBinding
import kr.co.lion.modigm.ui.login.EmailLoginFragment
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.util.FragmentName

class SocialLoginViewInitializer(
    private val fragment: SocialLoginFragment,
    private val binding: FragmentSocialLoginBinding,
    private val viewModel: LoginViewModel
) {

    fun initBlurBackground() {
        Glide.with(fragment)
            .load(R.drawable.background_login2)
            .transform(
                CenterCrop(),
                BlurTransformation(5, 3),
                ColorFilterTransformation(0x60000000)
            )
            .into(binding.imageViewSocialLoginBackground)
    }

    fun initKakaoLoginButton() {
        binding.imageButtonLoginKakao.setOnClickListener {
            fragment.showLoginLoading()
            viewModel.loginKakao(fragment.requireContext())
        }
    }

    fun initGithubLoginButton() {
        binding.imageButtonLoginGithub.setOnClickListener {
            fragment.showLoginLoading()
            viewModel.githubLogin(fragment.requireActivity())
        }
    }

    fun initEmailLoginButton() {
        binding.textButtonLoginOther.setOnClickListener {
            fragment.parentFragmentManager.commit {
                replace<EmailLoginFragment>(R.id.containerMain)
                addToBackStack(FragmentName.EMAIL_LOGIN.str)
            }
        }
    }

    fun initScrollArrow() {
        with(binding) {
            // 화살표 바인딩
            with(imageViewLoginScrollArrow) {
                // 애니메이션 설정
                val floatAnimation =
                    AnimationUtils.loadAnimation(context, R.anim.breathing_up_down).apply {
                        setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(p0: Animation?) {}
                            override fun onAnimationEnd(animation: Animation?) {
                                // 애니메이션 끝나면 화살표 가시성 업데이트
                                visibility =
                                    if (scrollViewSocialLogin.canScrollVertically(1)) View.VISIBLE else View.GONE
                            }

                            override fun onAnimationRepeat(p0: Animation?) {}
                        })
                    }
                // 레이아웃이 완전히 초기화된 후에 가시성 업데이트
                scrollViewSocialLogin.viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        // 초기 상태에 따라 화살표 보이기/숨기기
                        visibility =
                            if (scrollViewSocialLogin.canScrollVertically(1)) View.VISIBLE else View.GONE
                        if (visibility == View.VISIBLE) startAnimation(floatAnimation)
                        // 리스너 제거
                        scrollViewSocialLogin.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
                // 스크롤할 때 화살표 상태 업데이트
                scrollViewSocialLogin.setOnScrollChangeListener { v, _, _, _, _ ->
                    visibility =
                        if (scrollViewSocialLogin.canScrollVertically(1)) View.VISIBLE else View.GONE
                    if (visibility == View.VISIBLE) startAnimation(floatAnimation) else clearAnimation()
                    // 스크롤이 맨 위에 도달하면 화살표 보이기
                    if (!v.canScrollVertically(-1)) visibility = View.VISIBLE
                }
            }
        }
    }
}