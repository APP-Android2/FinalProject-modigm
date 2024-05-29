package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import kr.co.lion.modigm.databinding.FragmentProfileWebBinding
import kr.co.lion.modigm.ui.MainActivity


class ProfileWebFragment : Fragment() {
    lateinit var fragmentProfileWebBinding: FragmentProfileWebBinding
    lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentProfileWebBinding = FragmentProfileWebBinding.inflate(inflater,container,false)
        mainActivity = activity as MainActivity

        initWebView()

        return fragmentProfileWebBinding.root
    }

    fun initWebView() {
        val link = arguments?.getString("link")

        val webView = fragmentProfileWebBinding.profileWebView // FragmentProfileWebBinding을 통해 webView에 접근
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(link!!)
    }
}