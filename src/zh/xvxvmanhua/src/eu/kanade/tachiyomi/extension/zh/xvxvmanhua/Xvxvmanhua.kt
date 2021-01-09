package eu.kanade.tachiyomi.extension.zh.xvxvmanhua

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.jsoup.Jsoup

class Xvxvmanhua : HttpSource() {
    // 和 爱漫画 漫画台 用得同一个连接请求
    override val name = "羞羞漫画APP"
    override val baseUrl = ""
    override val lang = "zh"
    override val supportsLatest = true

    private val jsonUrl = "http://haosou.xxmh0.com"
    private val imageUrl = "http://9img.santei.net"

    private var requestJsonHeaders = Headers.of(mapOf(
        "Cache-Control" to "application/json",
        "User-Agent" to "okhttp/3.12.1",
        "Connection" to "close"
    ))

    private fun jsonGet(url: String) = GET(url, requestJsonHeaders)

    // 重写图片的访问方式,以添加请求头
    override fun imageUrlRequest(page: Page): Request {
        throw UnsupportedOperationException("This method should not be called!")
    }

    // 点击量
    override fun popularMangaRequest(page: Int): Request {
        return jsonGet("$jsonUrl/home/api/cate/tp/1-0-2-2-$page")
    }

    // 处理点击量请求
    override fun popularMangaParse(response: Response): MangasPage = searchMangaParse(response)

    // 按更新
    override fun latestUpdatesRequest(page: Int): Request {
        return jsonGet("$jsonUrl/home/api/cate/tp/1-0-2-1-$page")
    }

    // 处理更新请求
    override fun latestUpdatesParse(response: Response): MangasPage = searchMangaParse(response)

    override fun mangaDetailsRequest(manga: SManga): Request {
        return GET(manga.url.split("(!)")[0])
    }

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val body = response.body()!!.string()
        val doc = Jsoup.parseBodyFragment(body)
        title = doc.select("div.navbar-inner div.title").text()
        artist = "Tachiyomi:ZongerZY"
    }

    override fun chapterListRequest(manga: SManga): Request {
        return GET(manga.url.split("(!)")[1])
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val json = response.body()!!.string()
        var requestUrl = response.request().url().toString()
        val arr = JSONObject(json).getJSONObject("result").getJSONArray("list")
        val ret = java.util.ArrayList<SChapter>()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        for (i in 0 until arr.length()) {
            val chapter = arr.getJSONObject(i)
            ret.add(SChapter.create().apply {
                name = chapter.getString("title")
                date_upload = sdf.parse(chapter.getString("update_time").trim())?.time ?: 0
                url = requestUrl.split("-")[0] + "-1-${i + 1}-1"
            })
        }
        return ret.reversed()
    }

    override fun pageListParse(response: Response): List<Page> {
        var json = response.body()!!.string().trim()
        var obj = JSONObject(json).getJSONObject("result").getJSONArray("list").getJSONObject(0)
        var imageStr = obj.getString("imagelist").replace("./", "")
        var imageList = imageStr.split(",")
        var arrList = ArrayList<Page>()
        for (i in 0 until imageList.size) {
            arrList.add(Page(i, "", "$imageUrl/${imageList[i]}"))
        }
        return arrList
    }

    override fun imageRequest(page: Page): Request {
        var requestImageHeaders = Headers.of(mapOf(
            "Accept" to "image/webp,image/*,*/*;q=0.8",
            "Accept-Encoding" to "gzip, deflate",
            "User-Agent" to "Mozilla/5.0 (Linux; Android 6.0.1; MuMu Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36",
            "X-Requested-With" to "com.haosou.xxmh",
            "Referer" to "http://haosou.xxmh0.com/home/book/capter/id/"
        ))
        return GET(page.imageUrl!!, requestImageHeaders)
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException("This method should not be called!")
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val json = response.body()!!.string()
        val arr = JSONObject(json).getJSONObject("result").getJSONArray("list")
        val ret = java.util.ArrayList<SManga>()
        for (i in 0 until arr.length()) {
            val objArr = arr.getJSONObject(i)
            val comic_id = objArr.getString("id")
            ret.add(SManga.create().apply {
                title = objArr.getString("title")
                thumbnail_url = "${imageUrl}${objArr.getString("image")}"
                author = objArr.getString("auther")
                genre = objArr.getString("keyword").replace(",", ", ")
                description = objArr.getString("desc")
                status = if (objArr.getString("mhstatus").toInt() == 1) 1 else if (objArr.getString("mhstatus").toInt() == 0) 2 else 3
                url = "$jsonUrl/home/book/index/id/$comic_id(!)$jsonUrl/home/api/chapter_list/tp/$comic_id-1-1-100000"
            })
        }
        return MangasPage(ret, arr.length() != 0)
    }

    // 查询及分类查询
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return GET("$jsonUrl/home/api/searchk?keyword=$query&type=0&pageNo=$page")
        } else {
            var params1 = filters.map {
                if (it is ClassifyFilter)
                    it.toUriPart()
                else ""
            }.filter { it != "" }.joinToString("-")
            var params2 = filters.map {
                if (it is UpdateFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            if (params1.contains("vip"))
                return GET("${jsonUrl}$params1-$page")
            else
                return GET("${jsonUrl}${params1}$params2-$page")
        }
    }

    // 封装分类
    override fun getFilterList() = FilterList(
        ClassifyFilter(),
        UpdateFilter()
    )

    // 漫画分类
    private class ClassifyFilter : UriPartFilter("查询分类", arrayOf(
        Pair("全部", "/home/api/cate/tp/1-0-2"),
        Pair("连载中", "/home/api/cate/tp/1-0-0"),
        Pair("已完结", "/home/api/cate/tp/1-0-1"),
        Pair("付费专区", "/home/api/cate/tp/2-0-1"),
        Pair("VIP专区", "/home/api/getpage/tp/1-vip")
    ))

    // 状态分类
    private class UpdateFilter : UriPartFilter("状态分类", arrayOf(
        Pair("点击量", "-2"),
        Pair("更新", "-1")
    ))

    private open class UriPartFilter(
        displayName: String,
        val vals: Array<Pair<String, String>>,
        defaultValue: Int = 0
    ) :
        Filter.Select<String>(displayName, vals.map { it.first }.toTypedArray(), defaultValue) {
        open fun toUriPart() = vals[state].second
    }
}
