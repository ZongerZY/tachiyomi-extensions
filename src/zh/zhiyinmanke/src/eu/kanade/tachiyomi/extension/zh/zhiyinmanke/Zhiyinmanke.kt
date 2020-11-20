package eu.kanade.tachiyomi.extension.zh.zhiyinmanke

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlin.collections.ArrayList
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

class Zhiyinmanke : HttpSource() {

    override val name = "知音漫客"
    override val baseUrl = ""
    override val lang = "zh"
    override val supportsLatest = true

    private var requestJsonHeaders = Headers.of(mapOf(
        "Cache-Control" to "application/json",
        "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.76 Safari/537.36",
        "Connection" to "close"
    ))
    private var requestImageHeaders = Headers.of(mapOf(
        "Accept-Encoding" to "gzip",
        "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.76 Safari/537.36",
        "Referer" to "http://www.zymk.cn/"
    ))

    private fun jsonGet(url: String) = GET(url, requestJsonHeaders)
    private fun imageGet(url: String) = GET(url, requestImageHeaders)

    // 重写图片的访问方式,以添加请求头
    override fun imageUrlRequest(page: Page): Request {
        throw UnsupportedOperationException("This method should not be called!")
    }

    // 处理漫画列表信息
    private fun mangaFromJSON(json: String): MangasPage {
        val obj = JSONObject(json)
        val arr = obj.getJSONObject("data").getJSONObject("page").getJSONArray("comic_list")
        val ret = java.util.ArrayList<SManga>(arr.length())
        if (arr.length() == 0)
            return MangasPage(ret, false)
        for (i in 0 until arr.length()) {
            val objArr = arr.getJSONObject(i)
            val comic_id = objArr.getString("comic_id")
            val comic_name = objArr.getString("comic_name")
            ret.add(SManga.create().apply {
                title = comic_name
                thumbnail_url = getThumbnail_url(comic_id)
                url = "https://getcomicinfo-globalapi.zymk.cn/app_api/v5/getcomicinfo/?comic_id=$comic_id"
            })
        }
        return MangasPage(ret, arr.length() != 0)
    }

    private fun getThumbnail_url(comic_id: String): String {
        var b = ""
        for (i in 0 until 9) {
            if (i % 3 == 0 && i != 0) b = b + "/"
            if (i < 9 - comic_id.length)
                b = b + "0"
            else
                b = b + comic_id.substring(i - (9 - comic_id.length), i - (9 - comic_id.length) + 1)
        }
        return "https://image.zymkcdn.com/file/cover/" + b + "_3_4.jpg-600x800.jpg.webp"
    }

    // 点击量
    override fun popularMangaRequest(page: Int): Request {
        return jsonGet("https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?sort=click&page=$page")
    }

    // 处理点击量请求
    override fun popularMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        return mangaFromJSON(body)
    }

    // 按更新
    override fun latestUpdatesRequest(page: Int): Request {
        return jsonGet("https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?sort=click&page=$page")
    }

    // 处理更新请求
    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val body = response.body()!!.string()
        val obj = JSONObject(body).getJSONObject("data")
        val comic_id = obj.getString("comic_id")
        title = obj.getString("comic_name")
        thumbnail_url = getThumbnail_url(comic_id)
        author = obj.getString("author_name")
        genre = getMangaGenre(obj.getString("comic_type"))
        description = obj.getString("desc")
        status = 3
    }

    private fun getMangaGenre(json: String): String {
        val arr = JSONArray(json)
        var genre = ""
        for (i in 0 until arr.length()) {
            val objArr = arr.getJSONObject(i)
            if (i == arr.length() - 1) {
                genre += objArr.getString("name")
            } else {
                genre = genre + objArr.getString("name") + ", "
            }
        }
        return genre
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val json = response.body()!!.string()
        val obj = JSONObject(json).getJSONObject("data")
        var arr = obj.getJSONArray("chapter_list")
        var comic_id = obj.getString("comic_id")
        val ret = java.util.ArrayList<SChapter>()
        for (i in 0 until arr.length()) {
            val chapter = arr.getJSONObject(i)
            ret.add(SChapter.create().apply {
                name = chapter.getString("chapter_name") + " " + chapter.getString("chapter_title")
                date_upload = chapter.getString("create_time").toLong()
                url = "https://getcomicinfo-globalapi.zymk.cn/app_api/v5/getcomicinfo/?comic_id=$comic_id&pages=$i"
            })
        }
        return ret
    }

    override fun pageListParse(response: Response): List<Page> {
        val url = response.request().url().toString()
        val page = url.split("&pages=")[1].toInt()
        var json = response.body()!!.string().trim()
        var pageJson = JSONObject(json).getJSONObject("data").getJSONArray("chapter_list").getJSONObject(page)
        var start_num = pageJson.getString("start_var").toInt()
        var end_num = pageJson.getString("end_var").toInt()
        var arrList = ArrayList<Page>(pageJson.length())
        var pageBaseurl = pageJson.getJSONObject("chapter_image").getString("high")
        var pageBaseurl1 = pageBaseurl.split("$$")[0]
        var pageBaseurl2 = pageBaseurl.split("$$")[1]

        for (i in start_num until end_num + 1) {
            arrList.add(Page(i, "", "http://mhpic.xiaomingtaiji.net/comic/$pageBaseurl1$i$pageBaseurl2"))
        }
        return arrList
    }

    override fun imageRequest(page: Page): Request {
        return imageGet(page.imageUrl!!)
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException("This method should not be called!")
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        return mangaFromJSON(body)
    }

    // 查询及分类查询
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return jsonGet("https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?key=$query&sort=click&page=$page")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            return jsonGet("$params&page=$page")
        }
    }

    // 封装分类
    override fun getFilterList() = FilterList(
        ClassifyFilter(),
        UpdateFilter()
    )

    // 漫画分类
    private class ClassifyFilter : UriPartFilter("查询分类", arrayOf(
        Pair("连载", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=23"),
        Pair("完结", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=24"),
        Pair("短片", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=57"),
        Pair("热血", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=5"),
        Pair("修真", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=53"),
        Pair("霸总", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=62"),
        Pair("古风", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=63"),
        Pair("游戏", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=64"),
        // //Pair("耽美", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=54"),
        // //Pair("百合", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=55"),
        Pair("搞笑", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=6"),
        Pair("玄幻", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=7"),
        Pair("生活", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=8"),
        Pair("恋爱", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=9"),
        Pair("动作", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=10"),
        Pair("科幻", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=11"),
        Pair("战争", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=12"),
        Pair("悬疑", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=13"),
        Pair("恐怖", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=14"),
        Pair("校园", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=15"),
        Pair("历史", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=16"),
        Pair("穿越", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=17"),
        Pair("后宫", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=18"),
        // Pair("体育", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=19"),
        Pair("都市", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=20"),
        // Pair("萝莉", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=21"),
        Pair("漫改", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=22"),
        Pair("少男", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=25"),
        Pair("少女", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=26"),
        Pair("青年", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=27"),
        Pair("其他", "https://getconfig-globalapi.zymk.cn/app_api/v5/getsortlist_new/?type=58")

    ))

    // 状态分类
    private class UpdateFilter : UriPartFilter("状态分类", arrayOf(
        Pair("人气", "&sort=click"),
        Pair("更新", "&sort=date"),
        Pair("评分", "&sort=score"),
        Pair("打赏", "&sort=gold"),
        Pair("月票", "&sort=monthly")

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
