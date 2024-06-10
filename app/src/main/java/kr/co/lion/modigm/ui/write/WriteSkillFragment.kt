package kr.co.lion.modigm.ui.write

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteSkillBinding
import kr.co.lion.modigm.ui.detail.OnSkillSelectedListener
import kr.co.lion.modigm.ui.detail.SkillBottomSheetFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel
import kr.co.lion.modigm.util.Skill

class WriteSkillFragment : Fragment(), OnSkillSelectedListener {

    private lateinit var binding: FragmentWriteSkillBinding
    private val viewModel: WriteViewModel by activityViewModels()
    private val tabName = "skill"
    private var selectedSkillList: MutableList<Int> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_write_skill, container, false)
        binding.writeViewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingView()
        settingEvent()
        // 입력 확인 처리
        getAnswer()
    }


    fun settingEvent(){
        // 바텀시트 출력
        settingBottomSheeet()

    }
    fun settingBottomSheeet(){
        // textLaout 클릭 시 리스너
        binding.textInputLayoutWriteSkill.editText?.setOnClickListener {
            // bottom sheet
            val bottomSheet = SkillBottomSheetFragment().apply {
                setOnSkillSelectedListener(this@WriteSkillFragment)
            }
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }
    }

    fun settingView(){
        // cardView 클릭 시 Stroke 색상 변경
        binding.apply {
            val context = requireContext()
            val clickedStrokeColor = ContextCompat.getColor(context, R.color.pointColor)
            val unclickedStrokeColor = ContextCompat.getColor(context, R.color.textGray)

            // 신청제 Card
            cardviewWriteSkillApplicationSystem.setOnClickListener {
                cardviewWriteSkillApplicationSystem.apply {
                    if (cardElevation == 20F && strokeColor == clickedStrokeColor){
                        cardElevation = 0F
                        strokeColor = unclickedStrokeColor

                        viewModel.gettingApplyMethod(0)
                    } else {

                        // Stroke 색상 변경
                        strokeColor = clickedStrokeColor
                        cardviewWriteSkillFirstCome.strokeColor = unclickedStrokeColor

                        // Elevation 추가
                        cardElevation = 20F
                        cardviewWriteSkillFirstCome.cardElevation = 0F

                        viewModel.gettingApplyMethod(1)
                    }
                }

            }

            // 선착순 Card
            cardviewWriteSkillFirstCome.setOnClickListener {
                cardviewWriteSkillFirstCome.apply {
                    if (cardElevation == 20F && strokeColor == clickedStrokeColor){
                        cardElevation = 0F
                        strokeColor = unclickedStrokeColor

                        viewModel.gettingApplyMethod(0)

                    } else {

                        // Stroke 색상 변경
                        strokeColor = clickedStrokeColor
                        cardviewWriteSkillApplicationSystem.strokeColor = unclickedStrokeColor

                        // Elevation 추가
                        cardElevation = 20F
                        cardviewWriteSkillApplicationSystem.cardElevation = 0F

                        viewModel.gettingApplyMethod(2)
                    }
                }
            }
        }
    }

    override fun onSkillSelected(selectedSkills: List<Skill>) {
        // ChipGroup에 칩 추가
        addChipsToGroup(binding.chipGroupWriteSkill, selectedSkills)
        getAnswer()
    }

    fun addChipsToGroup(chipGroup: ChipGroup, skills: List<Skill>) {
        // 기존의 칩들을 삭제
        chipGroup.removeAllViews()
        selectedSkillList.clear()


        // 전달받은 스킬 리스트를 이용하여 칩을 생성 및 추가
        for (skill in skills) {
            selectedSkillList.add(skill.num) // 초기 스킬 목록을 selectedSkillList에 추가
            val chip = Chip(context).apply {
                text = skill.displayName
                isClickable = true
                isCheckable = true
                isCloseIconVisible=true
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dividerView))
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTextAppearance(R.style.ChipTextStyle)
                id = View.generateViewId()

                // 'X' 아이콘 클릭시 해당 칩을 ChipGroup에서 제거
                setOnCloseIconClickListener {
                    chipGroup.removeView(this)  // 'this'는 현재 클릭된 Chip 인스턴스를 참조
                    // skill List에서 목록 제거
                    selectedSkillList.remove(skill.num)
                }
            }
            chipGroup.addView(chip)
        }
        viewModel.gettingSkillList(selectedSkillList)
    }

    // 사용자가 입력을 했는지 확인
    fun getAnswer(){
        var result1 = false
        var result2 = false
        viewModel.studyApplyMethod.observe(viewLifecycleOwner){
            // 신청방식 - 입력 OK
            if (!it.equals(0)){
                result1 = true
            } else {
                result1 = false
            }
        }
        viewModel.studySkillList.observe(viewLifecycleOwner){
            // 필요한 기술 스택 - 입력 OK
            if (it != null) {
                if (it.size != 0){
                    // 입력 OK
                    result2 = true
                } else{
                    result2 = false
                }
            }
        }

        if (result1 && result2){
            viewModel.userDidAnswer(tabName)
        } else{
            viewModel.userDidNotAnswer(tabName)
        }
    }
}