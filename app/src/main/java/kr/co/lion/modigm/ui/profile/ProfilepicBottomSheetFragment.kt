package kr.co.lion.modigm.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentProfilepicBottomSheetBinding
import kr.co.lion.modigm.ui.VBBaseBottomSheetFragment
import kr.co.lion.modigm.ui.profile.vm.EditProfileViewModel
import kr.co.lion.modigm.util.Picture
import java.io.File

class ProfilepicBottomSheetFragment(val editProfileFragment: EditProfileFragment): VBBaseBottomSheetFragment<FragmentProfilepicBottomSheetBinding>(FragmentProfilepicBottomSheetBinding::inflate) {
    private val editProfileViewModel: EditProfileViewModel by activityViewModels()

    // 촬영된 사진이 저장된 경로 정보를 가지고 있는 Uri 객체
    private lateinit var photoUri: Uri

    // 카메라 실행을 위한 런처
    private var cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // 사진을 사용하겠다고 한 다음에 돌아왔을 경우
        if(it.resultCode == AppCompatActivity.RESULT_OK) {
            // 사진 객체를 생성한다.
            val bitmap = BitmapFactory.decodeFile(photoUri.path)
            editProfileFragment.binding.imageProfilePic.setImageBitmap(bitmap)

            // 회전 각도값을 구한다.
            val degree = Picture.getDegree(requireContext(), photoUri)
            // 회전된 이미지를 구한다.
            val bitmap2 = Picture.rotateBitmap(bitmap, degree.toFloat())
            // 크기를 조정한 이미지를 구한다.
            val bitmap3 = Picture.resizeBitmap(bitmap2, 1024)

            editProfileFragment.binding.imageProfilePic.setImageBitmap(bitmap3)
            editProfileViewModel.editProfilePicUri.value = photoUri
            editProfileViewModel.picChanged = true

            dismiss()
        }
    }

    // 앨범 실행을 위한 런처
    private var albumLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
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

                editProfileFragment.binding.imageProfilePic.setImageBitmap(bitmap3)
                editProfileViewModel.editProfilePicUri.value = imageUri
                editProfileViewModel.picChanged = true
                dismiss()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
        // bottomSheet 배경 설정
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.style_bottom_sheet_background)

        // 오른쪽 위 아이콘 클릭 시 BottomSheet 닫기
        binding.iconProfilepicClose.setOnClickListener {
            dismiss()
        }

        // 사진 촬영
        binding.cardViewProfilepicPhoto.setOnClickListener {
            startCameraLauncher()
        }

        // 앨범에서 선택
        binding.cardViewProfilepicAlbum.setOnClickListener {
            startAlbumLauncher()
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
}