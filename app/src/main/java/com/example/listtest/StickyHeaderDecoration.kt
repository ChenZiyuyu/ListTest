package com.example.listtest

import android.graphics.Canvas
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StickyHeaderDecoration(private val adapter: NewsAdapter) : RecyclerView.ItemDecoration() {

    private var headerView: View? = null
    private var headerTextView: TextView? = null

    // onDrawOver 会在所有 item 绘制完毕后调用，我们在这里绘制悬浮的头部
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val topChild = parent.getChildAt(0) ?: return
        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) return

        // 找到当前位置对应的Header
        val currentHeader = getHeaderForPosition(topChildPosition) ?: return
        val headerText = (currentHeader as TimelineItem.DateHeader).date

        // 复用或创建Header View
        val view = getHeaderView(parent)
        headerTextView?.text = headerText

        // 将Header绘制到Canvas上
        drawHeader(c, view, parent, findHeaderToPush(parent, topChildPosition))
    }

    // 寻找下一个header，用于实现“推挤”效果
    private fun findHeaderToPush(parent: RecyclerView, position: Int): View? {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val adapterPos = parent.getChildAdapterPosition(child)

            if (adapterPos != RecyclerView.NO_POSITION && adapter.getItemViewType(adapterPos) == 1) continue

            // 如果下一个 item 是一个 Header
            if (adapter.getItemViewType(adapterPos) == 0 && adapterPos > position) {
                // 并且它的顶部位置小于我们正在绘制的Header的高度
                if (child.top < (headerView?.height ?: 0)) {
                    return child // 返回这个即将把悬浮header推上去的header
                }
            }
        }
        return null
    }

    private fun drawHeader(c: Canvas, view: View, parent: ViewGroup, pushView: View?) {
        c.save()
        var y = 0f
        // 如果有需要“推挤”的Header，调整绘制的Y坐标
        if (pushView != null) {
            y = (pushView.top - view.height).toFloat()
        }
        c.translate(0f, y)
        view.draw(c)
        c.restore()
    }

    // 根据当前位置，向上查找最近的一个Header
    private fun getHeaderForPosition(position: Int): TimelineItem? {
        for (i in position downTo 0) {
            val item = (adapter as NewsAdapter).timelineItems[i]
            if (item is TimelineItem.DateHeader) {
                return item
            }
        }
        return null
    }

    // 获取并测量Header View
    private fun getHeaderView(parent: RecyclerView): View {
        if (headerView == null) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_date_header, parent, false)
            headerTextView = view.findViewById(R.id.dateHeaderTextView)

            // 测量 View
            val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)
            view.measure(widthSpec, heightSpec)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            headerView = view
        }
        return headerView!!
    }
}