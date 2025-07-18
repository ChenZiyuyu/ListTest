package com.example.listtest

import com.google.gson.annotations.SerializedName

// 接口返回的原始数据模型保持不变
data class TopLevelResponse(
    @SerializedName("result") val result: ApiResult
)

data class ApiResult(
    @SerializedName("data") val data: NewsDataContainer
)

data class NewsDataContainer(
    @SerializedName("data") val newsList: List<NewsItem>
)

data class NewsItem(
    @SerializedName("content") val title: String?,
    @SerializedName("view_num") val source: String?,
    @SerializedName("ctime") val time: String? // 这是Unix时间戳（秒）
)

/**
 * 新增部分：用于RecyclerView的统一列表项模型
 * Sealed Interface确保我们的列表只能包含预定义的这几种类型
 */
sealed interface TimelineItem {
    // 用于表示日期分组的标题，例如“2025年7月15日”
    data class DateHeader(val date: String) : TimelineItem

    // 用于表示单条新闻
    data class NewsArticle(val newsItem: NewsItem) : TimelineItem
}