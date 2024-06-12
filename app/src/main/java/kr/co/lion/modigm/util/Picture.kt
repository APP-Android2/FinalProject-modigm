package kr.co.lion.modigm.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class Picture {
    companion object {
        ///////// 카메라 관련 /////////

        // 촬영된 사진이 저장될 경로를 구해서 반환하는 메서드
        // authorities : AndroidManifest.xml에 등록한 File Provider의 이름
        fun getPictureUri(context: Context, authorities:String): Uri {
            // 촬영한 사진이 저장될 경로
            // 외부 저장소 중에 애플리케이션 영역 경로를 가져온다.
            val rootPath = context.getExternalFilesDir(null).toString()
            // 이미지 파일명을 포함한 경로
            val picPath = "${rootPath}/tempImage.jpg"
            // File 객체 생성
            val file = File(picPath)
            // 사진이 저장된 위치를 관리할 Uri 생성
            val contentUri = FileProvider.getUriForFile(context, authorities, file)

            return contentUri
        }

        ///////// 카메라 관련 /////////


        ///// 카메라, 앨범 공통 ////////
        // 사진의 회전 각도값을 반환하는 메서드
        // ExifInterface : 사진, 영상, 소리 등의 파일에 기록한 정보
        // 위치, 날짜, 조리개값, 노출 정도 등등 다양한 정보가 기록된다.
        // ExifInterface 정보에서 사진 회전 각도값을 가져와서 그만큼 다시 돌려준다.
        fun getDegree(context:Context, uri:Uri) : Int {
            // 사진 정보를 가지고 있는 객체 가져온다.
            var exifInterface: ExifInterface? = null


            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                // 이미지 데이터를 가져올 수 있는 Content Provide의 Uri를 추출한다.
                // val photoUri = MediaStore.setRequireOriginal(uri)
                // ExifInterface 정보를 읽어올 스트림을 추출한다.

                val inputStream = context.contentResolver.openInputStream(uri)!!
                // ExifInterface 객체를 생성한다.
                exifInterface = ExifInterface(inputStream)
            } else {
                // ExifInterface 객체를 생성한다.
                exifInterface = ExifInterface(uri.path!!)
            }

            if(exifInterface != null){
                // 반환할 각도값을 담을 변수
                var degree = 0
                // ExifInterface 객체에서 회전 각도값을 가져온다.
                val ori = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)

                degree = when(ori){
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }

                return degree
            }

            return 0
        }

        // 회전시키는 메서드
        fun rotateBitmap(bitmap: Bitmap, degree:Float): Bitmap {
            // 회전 이미지를 생성하기 위한 변환 행렬
            val matrix = Matrix()
            matrix.postRotate(degree)

            // 회전 행렬을 적용하여 회전된 이미지를 생성한다.
            // 첫 번째 : 원본 이미지
            // 두 번째와 세번째 : 원본 이미지에서 사용할 부분의 좌측 상단 x, y 좌표
            // 네번째와 다섯번째 : 원본 이미지에서 사용할 부분의 가로 세로 길이
            // 여기에서는 이미지데이터 전체를 사용할 것이기 때문에 전체 영역으로 잡아준다.
            // 여섯번째 : 변환행렬. 적용해준 변환행렬이 무엇이냐에 따라 이미지 변형 방식이 달라진다.
            val rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)

            return rotateBitmap
        }

        // 이미지 사이즈를 조정하는 메서드
        fun resizeBitmap(bitmap: Bitmap, targetWidth:Int): Bitmap {
            // 이미지의 확대/축소 비율을 구한다.
            val ratio = targetWidth.toDouble() / bitmap.width.toDouble()
            // 세로 길이를 구한다.
            val targetHeight = (bitmap.height * ratio).toInt()
            // 크기를 조장한 Bitmap을 생성한다.
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false)

            return resizedBitmap
        }

        // 이미지뷰의 이미지를 추출해 로컬에 저장한다.
        fun saveImageViewData(context:Context, imageView: ImageView, fileName:String){
            // 외부 저장소까지의 경로를 가져온다.
            val filePath = context.getExternalFilesDir(null).toString()
            // 이미지 뷰에서 BitmapDrawable 객체를 추출한다.
            val bitmapDrawable = imageView.drawable as BitmapDrawable

            // 로컬에 저장할 경로
            val file = File("${filePath}/${fileName}")
            // 스트림 추출
            val fileOutputStream = FileOutputStream(file)
            // 이미지를 저장한다.
            // 첫 번째 : 이미지 데이터 포멧(JPEG, PNG, WEBP_LOSSLESS, WEBP_LOSSY)
            // 두 번째 : 이미지의 퀄리티
            // 세 번째 : 이미지 데이터를 저장할 파일과 연결된 스트림
            bitmapDrawable.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        }
    }
}