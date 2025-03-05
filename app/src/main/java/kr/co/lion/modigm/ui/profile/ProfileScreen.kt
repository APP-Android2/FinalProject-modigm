package kr.co.lion.modigm.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kr.co.lion.modigm.R
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.ui.profile.vm.ProfileViewModel
import java.net.URL

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    changeToSettingsFragment: () -> Unit,
    changeToLinkWebView: (String) -> Unit,
    changeToDetailFragment: (Int) -> Unit
) {
    val profileName by viewModel.profileName.collectAsState(initial = "")
    val profileIntro by viewModel.profileIntro.collectAsState(initial = "")
    val profilePicUrl by viewModel.profileUserImage.collectAsState(initial = "")
    val profileInterests by viewModel.profileInterests.collectAsState(initial = "")
    val profileLinks by viewModel.profileLinkList.collectAsState(initial = emptyList())
    val profileHostStudies by viewModel.profileHostStudyList.collectAsState(initial = emptyList())
    val profilePartStudies by viewModel.profilePartStudyList.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp),
                title = { Text(text = "프로필") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = changeToSettingsFragment) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.icon_settings_24px),
                            contentDescription = "Settings"
                        )
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(name = profileName ?: "", profilePicUrl = profilePicUrl ?: "")
            IntroSection(intro = profileIntro ?: "", links = profileLinks, changeToLinkWebView = changeToLinkWebView)
            Spacer(modifier = Modifier.height(16.dp))

            InterestsSection(profileInterests ?: "")
            Spacer(modifier = Modifier.height(16.dp))

            StudiesSection(title = "진행한 스터디", studies = profileHostStudies, changeToDetailFragment = changeToDetailFragment)
            Spacer(modifier = Modifier.height(16.dp))

            StudiesSection(title = "참여한 스터디", studies = profilePartStudies, changeToDetailFragment = changeToDetailFragment)
        }
    }
}



@Composable
fun ProfileHeader(name: String, profilePicUrl: String?) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(profilePicUrl)
            .crossfade(true)
            .build(),
        contentScale = ContentScale.Crop,
        error = painterResource(id = R.drawable.image_default_profile)
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painter,
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
        Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
    }
}

@Composable
fun IntroSection(intro: String, links: List<String>, changeToLinkWebView: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,) {
        Row(modifier = Modifier.padding(start = 8.dp)) {
            links.forEach { link ->
                val domain = extractDomain(link)
                val iconRes = domainIcons[domain] ?: R.drawable.icon_link

                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "$domain icon",
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                        .clickable { changeToLinkWebView(link) }
                )
            }

        }

        if (intro.isNotBlank()) {
            Text(
                text = intro,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                color = Color(0xFF777777),
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 4.dp)
            )
        }
    }

}

private fun extractDomain(link: String): String {
    return try {
        val uri = URL(link)
        val domain = uri.host
        if (domain.startsWith("www.")) domain.substring(4) else domain
    } catch (e: Exception) {
        "invalid"
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestsSection(interests: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp)) // 모서리 둥글게
            .background(Color(0x66e3eeff)) // 배경색 변경
            .padding(16.dp) // 내부 패딩 추가
    ) {
        Text(text = "관심분야", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp), color = Color(0x55777777))
        FlowRow(
            verticalArrangement = Arrangement.spacedBy((-8).dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            interests.split(",").forEach { interest ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(interest, color = Color(0xff666666)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0x10888888)),
                    border = BorderStroke(1.dp, Color(0xff888888))
                )
            }
        }
    }
}

@Composable
fun StudiesSection(
    title: String,
    studies: List<StudyData>?,
    changeToDetailFragment: (Int) -> Unit
) {
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
                    StudyItem(study = study, changeToDetailFragment = changeToDetailFragment)
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun StudyItem(study: StudyData, changeToDetailFragment: (Int) -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .clickable { changeToDetailFragment(study.studyIdx) }) {
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
    ProfileScreen(
        viewModel = ProfileViewModel(),
        changeToSettingsFragment = {},
        changeToLinkWebView = {},
        changeToDetailFragment = {}
    )
}