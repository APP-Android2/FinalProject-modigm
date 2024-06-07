package kr.co.lion.modigm.ui.study.adapter

import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowStudyMyBinding
import kr.co.lion.modigm.model.StudyData

class StudyMyViewHolder(
    private val binding: RowStudyMyBinding,
    private val rowClickListener: (Int) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    // 전체 스터디 항목별 세팅
    fun bind(studyData: Pair<StudyData, Int>) {
        with(binding) {
            // 루트 뷰의 레이아웃 설정 및 클릭 리스너 설정
            setupRootView(studyData)

            // 스터디 사진 설정
            loadStudyImage(studyData.first.studyPic)

            // 스터디 상태 설정 (모집중/모집완료)
            setStudyState(studyData.first.studyState)

            // 스터디 진행 방식 설정 (온라인/오프라인/혼합)
            setStudyOnOffline(studyData.first.studyOnOffline)

            // 스터디 제목 설정
            textViewStudyMyTitle.text = studyData.first.studyTitle

            // 스터디 기간 설정 (스터디/프로젝트/공모전)
            setStudyPeriod(studyData.first.studyPeriod)

            // 스터디 인원 설정
            setStudyMembers(studyData)

            // 스터디 신청 방식 설정
            setStudyApplyMethod(studyData)

            // 찜 버튼 설정
            setupFavoriteButton()
        }
    }

    // 루트 뷰의 레이아웃 설정 및 클릭 리스너 설정
    private fun setupRootView(studyData: Pair<StudyData, Int>) {
        with(binding.root) {
            // 루트 뷰의 레이아웃 파라미터 설정
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // 루트 뷰 클릭 리스너 설정
            setOnClickListener {
                rowClickListener.invoke(studyData.first.studyIdx)
            }

            // 스터디 이미지 클릭 리스너 설정 (현재는 빈 구현)
            binding.imageViewStudyMyPic.setOnClickListener {
                // 클릭 시 실행될 코드 (현재는 빈 구현)
            }
        }
    }

    // Firebase Storage에서 스터디 이미지 로드
    private fun loadStudyImage(imageFileName: String) {
        if (imageFileName.isNotEmpty()) {
            val storageRef = FirebaseStorage.getInstance().reference.child("studyPic/$imageFileName")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(itemView.context)
                    .load(uri)
                    .into(binding.imageViewStudyMyPic)
            }.addOnFailureListener {
                // 실패 시 기본 이미지 설정 또는 에러 처리
                binding.imageViewStudyMyPic.setImageResource(R.drawable.image_detail_1)
            }
        } else {
            binding.imageViewStudyMyPic.setImageResource(R.drawable.image_detail_2)
        }
    }

    // 스터디 상태 설정 (모집중/모집완료)
    private fun setStudyState(studyState: Boolean) {
        // 스터디 상태에 따라 텍스트를 설정
        binding.textViewStudyMyCanApply.text = if (studyState) "모집중" else "모집 완료"
    }

    // 스터디 진행 방식 설정 (온라인/오프라인/혼합)
    private fun setStudyOnOffline(studyOnOffline: Int) {
        with(binding.textViewStudyMyOnOffline) {
            // 스터디 진행 방식에 따라 텍스트와 텍스트 색상을 설정
            when (studyOnOffline) {
                1 -> {
                    text = "온라인"
                    setTextColor(Color.parseColor("#0FA981"))
                }
                2 -> {
                    text = "오프라인"
                    setTextColor(Color.parseColor("#EB9C58"))
                }
                3 -> {
                    text = "온오프혼합"
                    setTextColor(Color.parseColor("#0096FF"))
                }
            }
        }
    }

    // 스터디 기간 설정 (스터디/프로젝트/공모전)
    private fun setStudyPeriod(studyPeriod: Int) {
        with(binding.textViewStudyMyType) {
            // 스터디 기간에 따라 텍스트를 설정
            when (studyPeriod) {
                1 -> {
                    text = "스터디"
                }
                2 -> {
                    text = "프로젝트"
                }
                else -> {
                    text = "공모전"
                }
            }
        }
    }

    // 스터디 인원 설정
    private fun setStudyMembers(studyData: Pair<StudyData, Int>) {
        // 스터디 최대 인원과 현재 인원을 설정
        binding.textViewStudyMyMaxMember.text = studyData.first.studyMaxMember.toString()
        binding.textViewStudyMyCurrentMember.text = studyData.second.toString()
    }

    // 스터디 신청 방식 설정
    private fun setStudyApplyMethod(studyData: Pair<StudyData, Int>) {
        with(binding){
            textViewStudyMyApplyMethod.text = when(studyData.first.studyApplyMethod){
                1 -> "선착순"
                2 -> "신청제"
                else -> ""
            }
        }
    }

    // 찜 버튼 설정
    private fun setupFavoriteButton() {
        with(binding.imageViewStudyMyFavorite) {
            setOnClickListener {

            }
        }
    }
}
