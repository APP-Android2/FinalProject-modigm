package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.profile.vm.ProfileViewModel
import kr.co.lion.modigm.util.CustomColor
import kr.co.lion.modigm.util.FragmentName
import java.net.URL

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()

    // onCreateView에서 초기화
    var userIdx: Int? = null
    var isBottomNavi: Boolean? = null

    private val domainIcons = mapOf(
        "youtube.com" to R.drawable.icon_youtube_logo,
        "github.com" to R.drawable.icon_github_logo,
        "linkedin.com" to R.drawable.icon_linkedin_logo,
        "velog.io" to R.drawable.icon_velog_logo,
        "instagram.com" to R.drawable.icon_instagram_logo,
        "notion.com" to R.drawable.icon_notion_logo,
        "facebook.com" to R.drawable.icon_facebook_logo,
        "twitter.com" to R.drawable.icon_twitter_logo,
        "open.kakao.com" to R.drawable.kakaotalk_sharing_btn_small,
        "default" to R.drawable.icon_link
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        userIdx = arguments?.getInt("userIdx")
        isBottomNavi = arguments?.getBoolean("isBottomNavi")

        return ComposeView(requireContext()).apply {
            setContent {
                ProfileScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupUserInfo()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
        val profileName by viewModel.profileName.collectAsState(initial = "")
        val profileIntro by viewModel.profileIntro.collectAsState(initial = "")
        val profileInterests by viewModel.profileInterests.collectAsState(initial = "")
        val profileLinks by viewModel.profileLinkList.collectAsState(initial = emptyList())
        val profileHostStudies by viewModel.profileHostStudyList.collectAsState(initial = emptyList())
        val profilePartStudies by viewModel.profilePartStudyList.collectAsState(initial = emptyList())

        Scaffold(
            modifier = Modifier.background(Color.White),
            topBar = {
                TopAppBar(
                    title = { Text(text = "프로필") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                    actions = {
                        IconButton(onClick = { /* TODO: Handle settings */ }) {
                            Icon(painterResource(id = R.drawable.icon_settings_24px), contentDescription = "Settings")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                ProfileHeader(name = profileName ?: "Default Name", intro = profileIntro ?: "Default Intro")
                Spacer(modifier = Modifier.height(16.dp))
                InterestsSection(profileInterests ?: "")
                Spacer(modifier = Modifier.height(16.dp))
                LinksSection(profileLinks)
                Spacer(modifier = Modifier.height(16.dp))
                StudiesSection("진행한 스터디", profileHostStudies)
                Spacer(modifier = Modifier.height(16.dp))
                StudiesSection("참여한 스터디", profilePartStudies)
            }
        }
    }

    @Composable
    fun ProfileHeader(name: String, intro: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.image_loading_gray),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(100.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = intro, fontSize = 14.sp, color = CustomColor.TEXTGRAY)
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun InterestsSection(interests: String) {
        Column {
            Text(text = "관심분야", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow {
                interests.split(",").forEach { interest ->
                    SuggestionChip(onClick = { /*TODO*/ }, label = { Text(interest) })
                }
            }
        }
    }

    @Composable
    fun LinksSection(links: List<String>) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "링크", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            links.forEach { link ->
                Row(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    val domain = extractDomain(link)
                    val iconRes = domainIcons[domain] ?: R.drawable.icon_link

                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = "$domain icon",
                        modifier = Modifier.size(30.dp).padding(end = 8.dp).clickable {
                            viewLifecycleOwner.lifecycleScope.launch {
                                // bundle 에 필요한 정보를 담는다
                                val bundle = Bundle()
                                bundle.putString("link", link)

                                // 이동할 프래그먼트로 bundle을 넘긴다
                                val profileWebFragment = ProfileWebFragment()
                                profileWebFragment.arguments = bundle

                                // Fragment 교체
                                requireActivity().supportFragmentManager.commit {
                                    setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                                    replace(R.id.containerMain, profileWebFragment)
                                    addToBackStack(FragmentName.PROFILE_WEB.str)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun StudiesSection(title: String, studies: List<StudyData>?) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (studies.isNullOrEmpty()) {
                Text(
                    text = "데이터가 없습니다",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Column {
                    studies.forEach { study ->
                        StudyItem(study)
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun StudyItem(study: StudyData) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp).clickable {
            viewLifecycleOwner.lifecycleScope.launch {
                val detailFragment = DetailFragment()

                // Bundle 생성 및 현재 사용자 uid 담기
                val bundle = Bundle()
                bundle.putInt("studyIdx", study.studyIdx)

                // Bundle을 ProfileFragment에 설정
                detailFragment.arguments = bundle

                requireActivity().supportFragmentManager.commit {
                    setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out)
                    replace(R.id.containerMain, detailFragment)
                    addToBackStack(FragmentName.DETAIL.str)
                }
            }
        }) {
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(70.dp)
            ) {
                GlideImage(
                    model = study.studyPic,
                    contentDescription = "Study Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = study.studyTitle,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_category_24px),
                        contentDescription = "Category Icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = study.studyType, fontSize = 14.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.icon_location_on_24px),
                        contentDescription = "Location Icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = study.studyOnOffline, fontSize = 14.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.icon_person_24px),
                        contentDescription = "Member Icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = study.studyCanApply, fontSize = 14.sp)
                }
            }
        }
    }

    @Preview
    @Composable
    fun ProfileScreenPreview() {
        ProfileScreen()
    }

    private fun setupUserInfo() {
        viewModel.profileUserIdx.value = userIdx
        viewModel.loadUserData()
        viewModel.loadUserLinkListData()
        viewModel.loadHostStudyList(userIdx!!)
        viewModel.loadPartStudyList(userIdx!!)
    }

    private fun extractDomain(url: String): String {
        return try {
            val uri = URL(url)
            val domain = uri.host
            if (domain.startsWith("www.")) domain.substring(4) else domain
        } catch (e: Exception) {
            "invalid"
        }
    }
}