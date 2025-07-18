package com.example.listtest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

// 唯一的改动在这里：将 "private var" 改为了 "var"
class NewsAdapter(var timelineItems: List<TimelineItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 定义两种视图类型常量
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_NEWS = 1
    }

    // 新闻条目的 ViewHolder
    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val sourceTextView: TextView = itemView.findViewById(R.id.sourceTextView)
    }

    // 日期标题的 ViewHolder
    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateHeaderTextView: TextView = itemView.findViewById(R.id.dateHeaderTextView)
    }

    // 根据位置判断项目类型
    override fun getItemViewType(position: Int): Int {
        return when (timelineItems[position]) {
            is TimelineItem.DateHeader -> VIEW_TYPE_HEADER
            is TimelineItem.NewsArticle -> VIEW_TYPE_NEWS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            VIEW_TYPE_NEWS -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_news, parent, false)
                NewsViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = timelineItems[position]) {
            is TimelineItem.DateHeader -> {
                (holder as DateHeaderViewHolder).dateHeaderTextView.text = item.date
            }
            is TimelineItem.NewsArticle -> {
                val newsHolder = holder as NewsViewHolder
                val currentNews = item.newsItem

                newsHolder.titleTextView.text = currentNews.title
                newsHolder.sourceTextView.text = currentNews.source

                // 格式化时间戳
                currentNews.time?.let {
                    try {
                        // 时间戳是秒，需要乘以1000转为毫秒
                        val timestampMs = it.toLong() * 1000L
                        // 定义时间的格式，例如 "16:55"
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        newsHolder.timeTextView.text = timeFormat.format(Date(timestampMs))
                    } catch (e: NumberFormatException) {
                        newsHolder.timeTextView.text = "" // 格式错误则不显示
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return timelineItems.size
    }

    fun updateItems(newItems: List<TimelineItem>) {
        this.timelineItems = newItems
        notifyDataSetChanged() // 通知RecyclerView刷新
    }
}