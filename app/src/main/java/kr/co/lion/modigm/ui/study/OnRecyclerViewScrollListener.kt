package kr.co.lion.modigm.ui.study

interface OnRecyclerViewScrollListener {
    fun onRecyclerViewScrolled(dy: Int)
    fun onRecyclerViewScrollStateChanged(newState: Int)
}