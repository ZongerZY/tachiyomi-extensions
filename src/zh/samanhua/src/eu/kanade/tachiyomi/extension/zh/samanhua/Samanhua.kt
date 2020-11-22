package eu.kanade.tachiyomi.extension.zh.samanhua

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlin.collections.ArrayList
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

class Samanhua : HttpSource() {
    // 爱飒漫画网页版不爬取原因
    // https://www.isamanhua.com/api/getComicList/?product_id=1&productname=asmh&platformname=pc
    // 一次性加载出全部漫画 两千多个

    override val name = "飒漫画"
    override val baseUrl = ""
    override val lang = "zh"
    override val supportsLatest = true

    private var requestJsonHeaders = Headers.of(mapOf(
        "Cache-Control" to "application/json",
        "channel" to "oppo"
    ))
    private var requestImageHeaders = Headers.of(mapOf(
        "Accept-Encoding" to "gzip",
        "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.76 Safari/537.36",
        "Referer" to "http://www.manhuatai.com/"
    ))

    private fun jsonPost(url: String, body: RequestBody) = POST(url, requestJsonHeaders, body)
    private fun jsonGet(url: String) = GET(url, requestJsonHeaders)
    private fun imageGet(url: String) = GET(url, requestImageHeaders)

    // 处理漫画列表信息
    private fun mangaFromJSON(json: String): MangasPage {
        var arr = JSONObject(json).getJSONArray("data").getJSONObject(0).getJSONArray("comic_info")
        val ret = java.util.ArrayList<SManga>(arr.length())
        if (arr.length() == 0)
            return MangasPage(ret, false)
        for (i in 0 until arr.length()) {
            val objArr = arr.getJSONObject(i)
            val comic_id = objArr.getString("comic_id")
            val comic_name = objArr.getString("comic_name")
            ret.add(SManga.create().apply {
                title = comic_name
                thumbnail_url = "http://image.samh.xndm.tech/cartoon/standard_cover/$comic_id.jpg-600x800.jpg.webp"
                url = "https://m.samh.xndm.tech/api/v1/comics/getcomicinfo_body?comic_id=$comic_id&productname=asmh&platform=android&platformname=android"
            })
        }
        return MangasPage(ret, arr.length() != 0)
    }

    // 点击量
    override fun popularMangaRequest(page: Int): Request {
        // "device_id":"861916036008014","uid":"1605708843327454570"
        var json = """{"platform":"android","page_size":18,"gender":"0","page_num":$page,"comic_id":0,"classify_type":"all","sort_type":"hot"}"""
        var body = RequestBody.create(MediaType.parse("application/json"), json)
        return jsonPost("https://recommend.samh.xndm.tech:6443/recommend/classify/v3", body)
    }

    // 处理点击量请求
    override fun popularMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        return mangaFromJSON(body)
    }

    // 按更新
    override fun latestUpdatesRequest(page: Int): Request {
        // "device_id":"861916036008014","uid":"1605708843327454570"
        var json = """{"platform":"android","page_size":18,"gender":"0","page_num":$page,"comic_id":0,"classify_type":"all","sort_type":"new"}"""
        var body = RequestBody.create(MediaType.parse("application/json"), json)
        return jsonPost("https://recommend.samh.xndm.tech:6443/recommend/classify/v3", body)
    }

    // 处理更新请求
    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    override fun mangaDetailsRequest(manga: SManga): Request {
        return jsonGet(manga.url)
    }

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val body = response.body()!!.string()
        val obj = JSONObject(body)
        // val comic_id = obj.getString("comic_id")
        title = obj.getString("comic_name")
        // thumbnail_url = "http://image.yqmh.com/mh/$comic_id.jpg-600x800.jpg.webp"
        author = obj.getString("comic_author")
        artist = obj.getString("comic_media")
        genre = getMangaGenre(obj.getString("comic_type_new"))
        status = obj.getString("comic_status").toInt()
        description = obj.getString("comic_desc")
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

    override fun chapterListRequest(manga: SManga): Request {
        return jsonGet(manga.url)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        var requestUrl = response.request().url().toString()
        val json = response.body()!!.string()
        val obj = JSONObject(json)
        var arr = obj.getJSONArray("comic_chapter")
        val ret = java.util.ArrayList<SChapter>()
        for (i in 0 until arr.length()) {
            val chapter = arr.getJSONObject(i)
            ret.add(SChapter.create().apply {
                name = chapter.getString("chapter_name")
                date_upload = chapter.getString("create_date").toLong() * 1000 // milliseconds
                url = requestUrl + "&pages=" + i
            })
        }
        return ret
    }

    override fun pageListRequest(chapter: SChapter): Request {
        return jsonGet(chapter.url)
    }

    override fun pageListParse(response: Response): List<Page> {
        val url = response.request().url().toString()
        val page = url.split("&pages=")[1].toInt()
        var json = response.body()!!.string().trim()
        var pageJson = JSONObject(json).getJSONArray("comic_chapter").getJSONObject(page)
        var start_num = pageJson.getString("start_num").toInt()
        var end_num = pageJson.getString("end_num").toInt()
        var arrList = ArrayList<Page>(pageJson.length())
        var pageBaseurl = pageJson.getJSONObject("chapter_image").getString("high")
        var pageBaseurl1 = pageBaseurl.split("$$")[0]
        var pageBaseurl2 = pageBaseurl.split("$$")[1]

        for (i in start_num until end_num + 1) {
            arrList.add(Page(i, "", "http://mhpic.cnmanhua.com$pageBaseurl1$i$pageBaseurl2"))
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
        var requestUrl = response.request().url().toString()
        val json = response.body()!!.string()
        if (requestUrl.contains("search_key")) {
            var arr = JSONObject(json).getJSONArray("data")
            val ret = java.util.ArrayList<SManga>(arr.length())
            if (arr.length() == 0)
                return MangasPage(ret, false)
            for (i in 0 until arr.length()) {
                val objArr = arr.getJSONObject(i)
                val comic_id = objArr.getString("comic_id")
                val comic_name = objArr.getString("comic_name")
                ret.add(SManga.create().apply {
                    title = comic_name
                    thumbnail_url = "http://image.samh.xndm.tech/cartoon/standard_cover/$comic_id.jpg-600x800.jpg.webp"
                    url = "https://m.samh.xndm.tech/api/v1/comics/getcomicinfo_body?comic_id=$comic_id&productname=asmh&platform=android&platformname=android"
                })
            }
            return MangasPage(ret, arr.length() != 0)
        } else {
            return mangaFromJSON(json)
        }
    }

    // 查询及分类查询
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return jsonGet("https://m.samh.xndm.tech/api/v1/comics/getsortlist?page=$page&size=7&orderby=click&search_key=$query&productname=asmh&platform=android&platformname=android")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            // "device_id":"861916036008014","uid":"1605708843327454570"
            var json = """{"platform":"android","page_size":18,"gender":"0","page_num":$page,"comic_id":0$params}"""
            var body = RequestBody.create(MediaType.parse("application/json"), json)
            return jsonPost("https://recommend.samh.xndm.tech:6443/recommend/classify/v3", body)
        }
    }

    // 封装分类
    override fun getFilterList() = FilterList(
        ClassifyFilter(),
        UpdateFilter()
    )

    // 漫画分类
    private class ClassifyFilter : UriPartFilter("查询分类", arrayOf(
        Pair("全部", ""","classify_type":"all""""),
        Pair("玄幻", ""","classify_type":"fantasy""""),
        Pair("穿越", ""","classify_type":"time_travel""""),
        Pair("异能", ""","classify_type":"ability""""),
        Pair("重生", ""","classify_type":"reborn""""),
        Pair("末世", ""","classify_type":"doom""""),
        Pair("都市", ""","classify_type":"city""""),
        Pair("热血", ""","classify_type":"blood""""),
        Pair("古风", ""","classify_type":"ancient""""),
        Pair("冒险", ""","classify_type":"risk""""),
        Pair("男主", ""","classify_type":"harem""""),
        Pair("漫改", ""","classify_type":"comic""""),
        Pair("恋爱", ""","classify_type":"love","""),
        Pair("霸总", ""","classify_type":"president""""),
        Pair("悬疑", ""","classify_type":"suspense""""),
        Pair("校园", ""","classify_type":"school""""),
        Pair("爆笑", ""","classify_type":"amuse""""),
        Pair("完结", ""","classify_type":"finish""""),
        Pair("会员", ""","classify_type":"vip""""),
        Pair("免费", ""","classify_type":"free""""),
        Pair("独家", ""","classify_type":"exclusive"""")

    ))

    // 状态分类
    private class UpdateFilter : UriPartFilter("状态分类", arrayOf(
        Pair("推荐", ""","sort_type":"recommend""""),
        Pair("最热", ""","sort_type":"hot""""),
        Pair("最新", ""","sort_type":"new"""")
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
