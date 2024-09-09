package kr.co.lion.modigm.ui.write

import kr.co.lion.modigm.model.TechStackData

interface OnTechStackSelectedListener {
    fun onTechStackSelected(selectedTechStacks: List<TechStackData>)
}