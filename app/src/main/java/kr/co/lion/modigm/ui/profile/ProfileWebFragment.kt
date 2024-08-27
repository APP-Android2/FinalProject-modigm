package kr.co.lion.modigm.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentProfileWebBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.util.FragmentName


class ProfileWebFragment : VBBaseFragment<FragmentProfileWebBinding>(FragmentProfileWebBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar()
        initWebView()
    }

    private fun initToolbar() {
        binding.toolbarProfileWeb.apply {
            // 툴바 메뉴
            inflateMenu(R.menu.menu_profile_web)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item_profile_web_finish -> {
                        // 이전 프래그먼트로 돌아간다
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }
                true
            }
        }
    }

    fun initWebView() {
        val link = arguments?.getString("link")

        val webView = binding.profileWebView // FragmentProfileWebBinding을 통해 webView에 접근
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()

                if (url.startsWith("intent://")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        if (intent != null) {
                            view.context.startActivity(intent)
                            return true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url)
                    return false
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("intent://")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        if (intent != null) {
                            view.context.startActivity(intent)
                            return true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url)
                    return false
                }
                return super.shouldOverrideUrlLoading(view, url)
            }
        }

        webView.settings.javaScriptEnabled = true

        // 로컬 스토리지를 사용하는 페이지일 경우 domStorageEnabled을 true로 셋팅
        webView.settings.domStorageEnabled = true

        webView.loadUrl(link!!)
    }
}