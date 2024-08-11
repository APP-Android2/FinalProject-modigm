package kr.co.lion.modigm.ui.profile.adapter

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

// ItemTouchHelper.Callback 클래스를 상속
class ItemTouchHelperCallback(val listener: ItemTouchHelperListener) : ItemTouchHelper.Callback() {
    // 활성화된 이동 방향을 정의하는 플래그를 반환하는 메소드
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        // 드래그 방향
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        // 스와이프 방향
        val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        // 이동을 만드는 메소드
        // 드래그 & 드랍의 기능만 수행할 것이면 makeMovementFlags(dragFlags, 0)를 반환
        // 스와이프 기능만 수행할 것이면 swipe(0, swipeFlags)를 반환
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // 아이템이 꾹 눌려 드래그 상태가 되었을 때 실행될 코드
            val context = viewHolder?.itemView?.context
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context?.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(50)
                }

            viewHolder.itemView.setBackgroundColor(Color.LTGRAY) // 예를 들어 배경색을 변경
        }
    }

    // 드래그된 item을 이전 위치에서 새로운 위치로 옮길 때 호출
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        // 리스너의 onMove 메소드 호출
//        val moved = listener.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
//
//        if (moved) {
//            val context = viewHolder.itemView.context
//            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
//                // VibratorManager를 통해 Vibrator를 가져옴
//                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
//                vibratorManager.defaultVibrator
//            } else {
//                // 기존의 방법으로 Vibrator를 가져옴
//                @Suppress("DEPRECATION")
//                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//            }
//
//            // 진동 실행
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
//            } else {
//                vibrator.vibrate(50)
//            }
//        }
//
//        return moved

        // 리스너의 onMove 메소드 호출
        return listener.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
    }

    // 사용자에 의해 swipe될 때 호출
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // 리스너의 onItemSwipe 메소드 호출
        listener.onItemSwipe(viewHolder.adapterPosition)
    }
}