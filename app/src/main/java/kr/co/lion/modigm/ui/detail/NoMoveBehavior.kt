package kr.co.lion.modigm.ui.detail

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NoMoveBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<FloatingActionButton>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        return false
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        // Do nothing to prevent FAB from moving
        return false
    }
}