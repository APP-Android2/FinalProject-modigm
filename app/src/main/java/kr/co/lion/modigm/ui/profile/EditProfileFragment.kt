package kr.co.lion.modigm.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentEditProfileBinding
import kr.co.lion.modigm.ui.DBBaseFragment
import kr.co.lion.modigm.ui.profile.adapter.ItemTouchHelperCallback
import kr.co.lion.modigm.ui.profile.adapter.LinkAddAdapter
import kr.co.lion.modigm.ui.profile.popup.InterestBottomSheetFragment
import kr.co.lion.modigm.ui.profile.vm.EditProfileViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
import kr.co.lion.modigm.util.Picture
import java.io.File

class EditProfileFragment: DBBaseFragment<FragmentEditProfileBinding>(R.layout.fragment_edit_profile) {
    private val editProfileViewModel: EditProfileViewModel by activityViewModels()

    // 어댑터 선언
    private lateinit var linkAddAdapter: LinkAddAdapter

    // 카메라 실행을 위한 런처
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    // 앨범 실행을 위한 런처
    private lateinit var albumLauncher: ActivityResultLauncher<Intent>
    // 촬영된 사진이 저장된 경로 정보를 가지고 있는 Uri 객체
    private lateinit var photoUri: Uri

    // 확인할 권한 목록
    private val permissionList = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_MEDIA_LOCATION
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding.editProfileViewModel = editProfileViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    private fun setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // 사진을 사용하겠다고 한 다음에 돌아왔을 경우
            if(it.resultCode == AppCompatActivity.RESULT_OK) {
                // 사진 객체를 생성한다.
                val bitmap = BitmapFactory.decodeFile(photoUri.path)
                binding.imageProfilePic.setImageBitmap(bitmap)

                // 회전 각도값을 구한다.
                val degree = Picture.getDegree(requireContext(), photoUri)
                // 회전된 이미지를 구한다.
                val bitmap2 = Picture.rotateBitmap(bitmap, degree.toFloat())
                // 크기를 조정한 이미지를 구한다.
                val bitmap3 = Picture.resizeBitmap(bitmap2, 1024)

                binding.imageProfilePic.setImageBitmap(bitmap3)
                editProfileViewModel.editProfilePicUri.value = photoUri
                editProfileViewModel.picChanged = true
            }
        }
    }

    private fun setupAlbumLauncher() {
        albumLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            // 사진 선택을 완료한 후 돌아왔다면
            if(it.resultCode == AppCompatActivity.RESULT_OK) {
                // 선택한 이미지의 경로 데이터를 관리하는 Uri 객체를 추출한다.
                val imageUri = it.data?.data
                if(imageUri != null){
                    // 안드로이드 Q(10) 이상이라면
                    val bitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                        // 이미지를 생성할 수 있는 객체를 생성한다.
                        val source = ImageDecoder.createSource(requireContext().contentResolver, imageUri)
                        // Bitmap을 생성한다.
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        // 컨텐츠 프로바이더를 통해 이미지 데이터에 접근한다.
                        val cursor = requireContext().contentResolver.query(imageUri, null, null, null, null)
                        if(cursor != null){
                            cursor.moveToNext()

                            // 이미지의 경로를 가져온다.
                            val idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                            val source = cursor.getString(idx)

                            // 이미지를 생성한다
                            BitmapFactory.decodeFile(source)
                        }  else {
                            null
                        }
                    }

                    // 회전 각도값을 가져온다.
                    val degree = Picture.getDegree(requireContext(), imageUri)
                    // 회전 이미지를 가져온다
                    val bitmap2 = Picture.rotateBitmap(bitmap!!, degree.toFloat())
                    // 크기를 줄인 이미지를 가져온다.
                    val bitmap3 = Picture.resizeBitmap(bitmap2, 1024)

                    binding.imageProfilePic.setImageBitmap(bitmap3)
                    editProfileViewModel.editProfilePicUri.value = imageUri
                    editProfileViewModel.picChanged = true
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions(permissionList, 0)
        initView()
        setupCameraLauncher()
        setupAlbumLauncher()
    }

    private fun initView() {
        setupToolbar()
        setupUserInfo()
        setupButtonChangePic()
        setupButtonChangePhone()
        setupRecyclerViewLink()
        setupButtonLinkAdd()
        setupButtonDone()

        observeData()
    }

    private fun setupToolbar() {
        binding.toolbarEditProfile.apply {
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun setupUserInfo() {
        // 데이터베이스로부터 데이터를 불러와 뷰모델에 담기
        editProfileViewModel.loadUserData()
        editProfileViewModel.loadUserLinkData()
    }

    private fun setupButtonChangePic() {
        binding.imageEditProfileChangePic.setOnClickListener { view ->
            // 드롭다운 표시
            showDropdownPhotoOrAlbum(view)
        }
    }

    private fun showDropdownPhotoOrAlbum(anchorView: View) {
        // 팝업 윈도우의 레이아웃을 설정
        val popupView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_profilepic_dropdown, null)

        // 팝업 윈도우 객체 생성
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 배경을 설정해야 그림자가 적용됨 (반드시 배경이 있어야 함)
        popupWindow.setBackgroundDrawable(AppCompatResources.getDrawable(requireContext(), android.R.drawable.dialog_holo_light_frame))

        // 팝업 윈도우 외부를 터치하면 닫히도록 설정
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true

        // 팝업 윈도우를 anchorView 아래에 표시
        popupWindow.showAsDropDown(anchorView, -20, 0)
        popupWindow.elevation = 10f

        // 팝업 안의 아이템 클릭 리스너 설정
        val item1: LinearLayout = popupView.findViewById(R.id.layoutDropdownPhoto)
        val item2: LinearLayout = popupView.findViewById(R.id.layoutDropdownAlbum)

        item1.setOnClickListener {
            // 아이템 1이 클릭되었을 때의 처리
            startCameraLauncher()
            popupWindow.dismiss()
        }

        item2.setOnClickListener {
            // 아이템 2가 클릭되었을 때의 처리
            startAlbumLauncher()
            popupWindow.dismiss()
        }
    }

    // 카메라 런처를 실행하는 메서드
    // step1) res/xml 폴더에 xml 파일을 만들어주고 이 파일에 사진이 저장될 외부저장소까지의 경로를 기록해준다.
    // step2) AndroidMenifest.xml 에 1에서 만든 파일의 경로를 지정해 준다.
    fun startCameraLauncher() {
        // 촬영한 사진이 저장될 경로
        val file = File(requireContext().getExternalFilesDir(null), "tempImage.jpg")
        photoUri = FileProvider.getUriForFile(requireContext(), "kr.co.lion.modigm.file_provider", file)

        // 카메라 인텐트에서 MediaStore.EXTRA_OUTPUT을 통해 이미지가 저장된 파일 경로를 지정하면
        // 실제 이미지 파일은 지정한 경로에 저장되고, 인텐트의 반환 데이터로 이미지가 직접 포함되지 않는다
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

        cameraLauncher.launch(cameraIntent)
    }

    // 앨범 런처를 실행하는 메서드
    fun startAlbumLauncher(){
        // 앨범에서 사진을 선택할 수 있도록 셋팅된 인텐트를 생성한다.
        val albumIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // 실행할 액티비티의 타입을 설정(이미지를 선택할 수 있는 것이 뜨게 한다)
        albumIntent.setType("image/*")
        // 선택할 수 있는 파일들의 MimeType을 설정한다.
        // 여기서 선택한 종류의 파일만 선택이 가능하다. 모든 이미지로 설정한다.
        val mimeType = arrayOf("image/*")
        albumIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        // 액티비티를 실행한다.
        albumLauncher.launch(albumIntent)
    }

    private fun setupButtonChangePhone() {
        // 바인딩
        with(binding) {
            // 전화번호 변경 버튼
            with(buttonEditProfilePhone) {
                // 로그인 방식
                val currentUserProvider = prefs.getString("currentUserProvider")

                // 버튼 클릭 시
                setOnClickListener {
                    // 로그인 방식에 따라 다른 화면으로 이동
                    when (currentUserProvider) {
                        // 이메일 로그인인 경우
                        JoinType.EMAIL.provider -> {
                            parentFragmentManager.commit {
                                add(R.id.containerMain, ChangePhoneEmailFragment())
                                addToBackStack(FragmentName.CHANGE_PHONE_EMAIL.str)
                            }
                        }
                        // 소셜 로그인인 경우
                        else -> {
                            parentFragmentManager.commit {
                                add(R.id.containerMain, ChangePhoneSocialFragment())
                                addToBackStack(FragmentName.CHANGE_PHONE_SOCIAL.str)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerViewLink() {
        // 리사이클러뷰 어댑터와 뷰모델을 함께 초기화
        linkAddAdapter = LinkAddAdapter(mutableListOf(), requireContext(), editProfileViewModel)

        // 리스너를 구현한 Adapter 클래스를 Callback 클래스의 생성자로 지정
        val itemTouchHelperCallback = ItemTouchHelperCallback(linkAddAdapter)

        // ItemTouchHelper의 생성자로 ItemTouchHelper.Callback 객체 셋팅
        val helper = ItemTouchHelper(itemTouchHelperCallback)

        binding.recyclerViewEditProfileLink.apply {
            adapter = linkAddAdapter
            layoutManager = LinearLayoutManager(requireContext())

            // RecyclerView에 ItemTouchHelper 연결
            helper.attachToRecyclerView(this)
        }
    }

    private fun setupButtonLinkAdd() {
        binding.buttonEditProfileLink.setOnClickListener {
            // 리스트에 링크 추가
            editProfileViewModel.addLinkToList()
            // 링크 텍스트필드 비우기
            editProfileViewModel.editProfileNewLink.value = ""
        }
    }

    private fun setupButtonDone() {
        binding.buttonEditProfileDone.setOnClickListener {
            // 프로그래스바 띄우기
            binding.layoutEditProfileProgressBar.visibility = View.VISIBLE

            // 데이터베이스 업데이트
            lifecycleScope.launch {
                editProfileViewModel.updateUserData(editProfileViewModel.picChanged, requireContext())
                editProfileViewModel.updateUserLinkData()

                // 스낵바 띄우기
                val snackbar = Snackbar.make(binding.root, "정보가 업데이트되었습니다.", Snackbar.LENGTH_LONG)
                snackbar.show()

                // 이전 프래그먼트로 돌아간다
                parentFragmentManager.popBackStack()
            }
        }
    }

    // 데이터 변경 관찰
    fun observeData() {
        // 프로필 사진
        lifecycleScope.launch {
            editProfileViewModel.editProfilePicUrl.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { image ->
                if (!image.isNullOrEmpty()) {
                    val requestOptions = RequestOptions()
                        .placeholder(R.drawable.image_loading_gray) // 필요 시 기본 플레이스홀더 설정
                        .error(R.drawable.image_detail_1) // 이미지 로딩 실패 시 표시할 이미지

                    Glide.with(requireContext())
                        .load(image)
                        .apply(requestOptions)
                        .into(object : CustomViewTarget<ImageView, Drawable>(binding.imageProfilePic) {
                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                // 로딩 실패 시 기본 이미지를 보여줌
                                binding.imageProfilePic.setImageResource(R.drawable.image_default_profile)
                            }

                            override fun onResourceCleared(placeholder: Drawable?) {
                                // 리소스가 클리어 될 때
                            }

                            override fun onResourceReady(resource: Drawable, transition: com.bumptech.glide.request.transition.Transition<in Drawable>?) {
                                // 로딩 성공 시
                                binding.imageProfilePic.setImageDrawable(resource)
                            }
                        })
                } else {
                    // Handle the case where the image string is null (e.g., show a default image)
                    binding.imageProfilePic.setImageResource(R.drawable.image_default_profile)
                }
            }
        }

        // 로그인 방식
        lifecycleScope.launch {
            editProfileViewModel.editProfileProvider.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { provider ->
                when (provider) {
                    "kakao" -> binding.textFieldEditProfileEmail.helperText = "카카오로 로그인된 계정입니다."
                    "github" -> binding.textFieldEditProfileEmail.helperText = "깃허브로 로그인된 계정입니다."
                    "email" -> binding.textFieldEditProfileEmail.helperText = "이메일로 로그인된 계정입니다."
                    "naver" -> binding.textFieldEditProfileEmail.helperText = "네이버로 로그인된 계정입니다."
                    "google" -> binding.textFieldEditProfileEmail.helperText = "구글로 로그인된 계정입니다."
                    else -> binding.textFieldEditProfileEmail.helperText = "로그인 정보를 불러올 수 없습니다."
                }
            }
        }

        // 관심 분야 chipGroup
        lifecycleScope.launch {
            editProfileViewModel.editProfileInterests.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { interests ->
                // 기존 칩들 제거
                binding.chipGroupProfile.removeAllViews()

                val interestList = interests?.split(",")?.map { it.trim() }

                // 리스트가 변경될 때마다 for 문을 사용하여 아이템을 처리
                if (interestList != null) {
                    for (interest in interestList) {
                        // 아이템 처리 코드
                        binding.chipGroupProfile.addView(Chip(context).apply {
                            text = interest
                            setTextAppearance(R.style.ChipTextStyle)
                            // 자동 padding 없애기
                            setEnsureMinTouchTargetSize(false)
                            // 배경 흰색으로 지정
                            setChipBackgroundColorResource(android.R.color.white)
                            // 클릭 불가
                            isClickable = false
                            // chip에서 X 버튼 보이게 하기
                            isCloseIconVisible = true
                            // X버튼 누르면 chip 없어지게 하기
                            setOnCloseIconClickListener {
                                binding.chipGroupProfile.removeView(this)

                                // 선택된 칩들 텍스트를 콤마로 연결한 문자열 생성
                                val remainingChips = binding.chipGroupProfile.children
                                    .filterIsInstance<Chip>()
                                    .map { it.text.toString() }
                                    .filter { it != "+" } // '+' 버튼 제외
                                    .joinToString(",")

                                // ViewModel의 interest 문자열 업데이트
                                editProfileViewModel.editProfileInterests.value = remainingChips
                            }
                        })
                    }
                }
                // 마지막 칩은 칩을 추가하는 버튼으로 사용
                binding.chipGroupProfile.addView(Chip(context).apply {
                    // chip 텍스트 설정
                    text = "+"

                    setTextAppearance(R.style.ChipTextStyle)

                    // 자동 padding 없애기
                    setEnsureMinTouchTargetSize(false)
                    // 배경 회색으로 지정
                    setChipBackgroundColorResource(R.color.dividerView)
                    // 클릭하면 바텀시트 올라옴
                    setOnClickListener {
                        val bottomSheet = InterestBottomSheetFragment()
                        bottomSheet.show(childFragmentManager, bottomSheet.tag)
                    }
                })
            }
        }


        // 링크 리스트
        lifecycleScope.launch {
            editProfileViewModel.editProfileLinkList.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { linkList ->
                linkAddAdapter.updateData(linkList.toMutableList())
            }
        }
    }
}