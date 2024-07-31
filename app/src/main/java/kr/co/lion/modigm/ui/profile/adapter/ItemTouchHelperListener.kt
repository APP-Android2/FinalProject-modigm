package kr.co.lion.modigm.ui.profile.adapter

// RecyclerView의 Adapter와 ItemTouchHelper.Callback을 연결시켜 주는 리스너
interface ItemTouchHelperListener {
    fun onItemMove(from: Int, to: Int): Boolean
    fun onItemSwipe(position: Int)
}