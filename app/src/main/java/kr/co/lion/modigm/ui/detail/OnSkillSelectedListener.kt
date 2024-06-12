package kr.co.lion.modigm.ui.detail

import kr.co.lion.modigm.util.Skill

interface OnSkillSelectedListener {
    fun onSkillSelected(selectedSkills: List<Skill>)
}