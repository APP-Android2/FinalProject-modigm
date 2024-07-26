package kr.co.lion.modigm.ui.study

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FABBehavior(context: Context, attrs: AttributeSet) : FloatingActionButton.Behavior(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        return dependency is RecyclerView
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        if (dependency is RecyclerView) {
            val recyclerView = dependency as RecyclerView
            val dy = recyclerView.computeVerticalScrollOffset()
            if (dy > 0 && child.visibility == View.VISIBLE) {
                hideWithoutAnimation(child)
            } else if (dy <= 0 && child.visibility != View.VISIBLE) {
                showWithAnimation(child)
            }
        }
        return super.onDependentViewChanged(parent, child, dependency)
    }

    private fun showWithAnimation(fab: FloatingActionButton) {
        fab.clearAnimation()
        fab.visibility = View.VISIBLE
        fab.alpha = 1f
        fab.clearAnimation()
    }

    private fun hideWithoutAnimation(fab: FloatingActionButton) {
        fab.clearAnimation()
        fab.visibility = View.INVISIBLE
    }
}