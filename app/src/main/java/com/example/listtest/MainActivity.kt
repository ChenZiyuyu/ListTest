package com.example.listtest

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var emptyView: TextView

    private val client = OkHttpClient()
    private val gson = Gson()

    private val TAG = "MainActivityNews"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        newsAdapter = NewsAdapter(emptyList())
        recyclerView.adapter = newsAdapter
        recyclerView.addItemDecoration(StickyHeaderDecoration(newsAdapter))

        // 恢复调用真实的网络请求
        fetchNews()
    }

    private fun fetchNews() {
        Log.d(TAG, "开始从网络获取新闻...")
        emptyView.text = "正在加载新闻..."
        emptyView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE


        val url = "https://news.cj.sina.cn/app/v1/news724/list?from=hmwidget&version=8.16.0&deviceId=0062738dda096582bead16efd7d0dd72&is_important=0&tag=0&nonce=72000382&ts=1752547529&sign=c5cc82cb675340e53ccb43130a8389e1&apptype=10&id=&dire=f&up=0&num=20&deviceid=0062738dda096582bead16efd7d0dd72"
        val userAgent = "sinafinancehmscar__1.1.11__android__f41eb02ce388f1c086eb8d24a821c18c__12__OCE-AN50"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "网络请求失败", e)
                runOnUiThread {
                    emptyView.text = "加载失败，请检查网络"
                    Toast.makeText(applicationContext, "网络请求失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e(TAG, "请求失败，状态码: ${response.code}")
                    runOnUiThread {
                        emptyView.text = "加载失败 (Code: ${response.code})"
                    }
                    return
                }

                val responseBody = response.body?.string()
                Log.d(TAG, "收到的原始JSON: $responseBody")

                if (responseBody.isNullOrEmpty()) {
                    Log.w(TAG, "服务器返回内容为空")
                    runOnUiThread {
                        emptyView.text = "没有获取到任何内容"
                    }
                    return
                }

                try {
                    val topLevelResponse = gson.fromJson(responseBody, TopLevelResponse::class.java)
                    val newsList = topLevelResponse.result.data.newsList
                    Log.d(TAG, "Gson解析成功，原始新闻数量: ${newsList.size}")

                    val timelineItems = processNewsIntoTimeline(newsList)
                    Log.d(TAG, "数据处理完成，最终列表项数量: ${timelineItems.size}")

                    runOnUiThread {
                        if (timelineItems.isEmpty()) {
                            emptyView.text = "暂无新闻"
                            emptyView.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            newsAdapter.updateItems(timelineItems)
                            emptyView.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "JSON解析或数据处理失败", e)
                    runOnUiThread {
                        emptyView.text = "数据解析错误"
                    }
                }
            }
        })
    }

    private fun processNewsIntoTimeline(newsList: List<NewsItem>): List<TimelineItem> {
        val timelineItems = mutableListOf<TimelineItem>()
        val dateFormat = SimpleDateFormat("yyyy年M月d日", Locale.getDefault())

        val sortedNews = newsList.filter { it.time?.toLongOrNull() != null }
            .sortedByDescending { it.time!!.toLong() }

        val groupedNews = sortedNews.groupBy { newsItem ->
            val timestampMs = newsItem.time!!.toLong() * 1000L
            dateFormat.format(Date(timestampMs))
        }

        groupedNews.forEach { (date, newsOnDate) ->
            timelineItems.add(TimelineItem.DateHeader(date))
            newsOnDate.forEach { news ->
                timelineItems.add(TimelineItem.NewsArticle(news))
            }
        }
        return timelineItems
    }
}