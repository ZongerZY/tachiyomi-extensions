package eu.kanade.tachiyomi.extension.zh.mianman

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.w3c.dom.Document

class Mianman : HttpSource() {
    // 和 爱漫画 漫画台 用得同一个连接请求
    override val name = "免漫"
    override val baseUrl = ""
    override val lang = "zh"
    override val supportsLatest = true

    private var requestJsonHeaders = Headers.of(mapOf(
        "Cache-Control" to "application/json",
        "User-Agent" to "okhttp/3.12.1",
        "Connection" to "close"
    ))
    private var requestPageHeaders = Headers.of(mapOf(
        "flag" to "3b68bb53f7e8e9644481570e9ef47d39",
        "signature" to "BAAD093A82829FB432A7B28CB4CCF0E9F37DAE58",
        "uuid" to "00000",
        "version" to "2.6.7",
        "time" to "1612182376431",
        "Host" to "comicfree.cn",
        "token" to "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyMTAyMDEyMDEzNTkwODMyMTQiLCJhdWQiOiJjb21pYyB1c2VyIiwiaXAiOiIxODIuMTI0LjMuMTIyIiwiaXNzIjoic2lsZW50IiwiZW5kVGltZVRpbWVzdGFtcCI6MTYxMjc4NjQzOTY0MSwibW9kZWwiOiJNdU11IiwiZXhwIjoxNjEzMzkyODcwLCJ0eXBlIjoidXNlciIsImlhdCI6MTYxMjE4MzI3MCwidXVpZCI6Ijk4MDAwMDAwMDEzMzc2Myw5ODAwMDAwMDAxMzM3NjMiLCJ2ZXJzaW9uIjoiMi42LjciLCJlbWFpbCI6Inp6eWFuYmx6bCJ9.T82QMsueeroR9LnH1H4Ebm758Mw0ercY76L37GWq8Do"
    ))

    private fun jsonGet(url: String) = GET(url, requestJsonHeaders)
    private fun pageGet(url: String) = GET(url, requestPageHeaders)

    // 重写图片的访问方式,以添加请求头
    override fun imageUrlRequest(page: Page): Request {
        throw UnsupportedOperationException("This method should not be called!")
    }

    // 点击量
    override fun popularMangaRequest(page: Int): Request {
        return jsonGet("https://getconfig-globalapi.yyhao.com/app_api/v5/getsortlist/?page=$page&size=7&orderby=click&search_key=&young_mode=0&platformname=android&productname=kmh")
    }

    // 处理点击量请求
    override fun popularMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        return mangaFromJSON(body)
    }

    // 按更新
    override fun latestUpdatesRequest(page: Int): Request {
        return jsonGet("https://getconfig-globalapi.yyhao.com/app_api/v5/getsortlist/?page=$page&size=7&orderby=date&search_key=&young_mode=0&platformname=android&productname=kmh")
    }

    // 处理更新请求
    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val body = response.body()!!.string()
        val obj = JSONObject(body)
        val comic_id = obj.getString("comic_id")
        title = obj.getString("comic_name")
        thumbnail_url = "http://image.yqmh.com/mh/$comic_id.jpg-600x800.jpg.webp"
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

    override fun chapterListParse(response: Response): List<SChapter> {
        val json = response.body()!!.string()
        val obj = JSONObject(json)
        var arr = obj.getJSONArray("comic_chapter")
        var comic_id = obj.getString("comic_id")
        val ret = java.util.ArrayList<SChapter>()
        for (i in 0 until arr.length()) {
            val chapter = arr.getJSONObject(i)
            ret.add(SChapter.create().apply {
                name = chapter.getString("chapter_name")
                date_upload = chapter.getString("create_date").toLong() * 1000 // milliseconds
                url = "http://comic.321mh.com/app_api/v5/getcomicinfo_body/?comic_id=$comic_id&young_mode=0&from_page=search&platformname=android&productname=kmh&pages=$i"
            })
        }
        return ret
    }

    override fun pageListParse(response: Response): List<Page> {
        val url = response.request().url().toString()
        val page = url.split("&pages=")[1].toInt()
        var json = response.body()!!.string().trim()

        var pageJson = JSONObject(json).getJSONArray("comic_chapter").getJSONObject(page)
        var start_num = pageJson.getString("start_num").toInt()
        var end_num = pageJson.getString("end_num").toInt()
        var arrList = ArrayList<Page>(pageJson.length())
        var chapter_domain = pageJson.getString("chapter_domain")
        var pageBaseurl = pageJson.getJSONObject("chapter_image").getString("high")
        var pageBaseurl1 = pageBaseurl.split("$$")[0]
        var pageBaseurl2 = pageBaseurl.split("$$")[1]

        for (i in start_num until end_num + 1) {
            arrList.add(Page(i, "", "http://mhpic.$chapter_domain$pageBaseurl1$i$pageBaseurl2"))
        }
        return arrList
    }

    override fun imageRequest(page: Page): Request {
        //return imageGet(page.imageUrl!!)
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException("This method should not be called!")
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val requestUrl = response.request().url().toString();
        val json = response.body()!!.string()
        val obj = JSONObject(json)
        val ret = java.util.ArrayList<SManga>()
        if (requestUrl.contains("/Search/result") && obj.getString("msg") == "success") {
            val arr = obj.getJSONArray("data")
            if (arr.length() == 0)
                return MangasPage(ret, false)
            for (i in 0 until arr.length()) {
                val objArr = arr.getJSONObject(i)
                val comic_id = objArr.getString("comic_id")
                val comic_name = objArr.getString("title")
                val comic_author = objArr.getString("artist_name")
                ret.add(SManga.create().apply {
                    title = comic_name
                    thumbnail_url = objArr.getString("cover_v_url")
                    author = comic_author
                    url = "https://wx.ac.qq.com/1.0.0/Detail/comic?id=${comic_id}"
                })
            }
            return MangasPage(ret, true)
        } else if (requestUrl.contains("/Search/result") && obj.getString("msg") == "no more"){
            return MangasPage(ret, false)
        } else {

        }

        val arr = obj.getJSONArray("data")
        if (arr.length() == 0)
            return MangasPage(ret, false)
        for (i in 0 until arr.length()) {
            val objArr = arr.getJSONObject(i)
            val comic_id = objArr.getString("comic_id")
            val comic_name = objArr.getString("comic_name")
            val comic_author = objArr.getString("comic_author")
            ret.add(SManga.create().apply {
                title = comic_name
                thumbnail_url = "http://image.yqmh.com/mh/$comic_id.jpg-600x800.jpg.webp"
                author = comic_author
                url = "http://comic.321mh.com/app_api/v5/getcomicinfo_body/?comic_id=$comic_id&young_mode=0&from_page=search&platformname=android&productname=kmh"
            })
        }
        return MangasPage(ret, arr.length() != 0)
    }

    // 查询及分类查询
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return jsonGet("https://wx.ac.qq.com/1.0.0/Search/result?word=${query}&page=${page}")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            return jsonGet("https://comicfree.cn/module/comic${params}${page}/25")
        }
    }

    private fun ascii2native(asciicode: String): String? {
        val asciis = asciicode.split("\\\\u".toRegex()).toTypedArray()
        var nativeValue = asciis[0]
        try {
            for (i in 1 until asciis.size) {
                val code = asciis[i]
                nativeValue += code.substring(0, 4).toInt(16).toChar()
                if (code.length > 4) {
                    nativeValue += code.substring(4, code.length)
                }
            }
        } catch (e: NumberFormatException) {
            return asciicode
        }
        return nativeValue
    }

    // 封装分类
    override fun getFilterList() = FilterList(
        ClassifyFilter()
    )

    // 漫画分类
    private class ClassifyFilter : UriPartFilter("查询分类", arrayOf(
        Pair("推荐", "/index/2/14"),
        Pair("热血", "/theme/%E7%83%AD%E8%A1%80/"),
        Pair("冒险", "/theme/%E5%86%92%E9%99%A9/"),
        Pair("玄幻", "/theme/%E7%8E%84%E5%B9%BB/"),
        Pair("都市", "/theme/%E9%83%BD%E5%B8%82/"),
        Pair("恋爱", "/theme/%E6%81%8B%E7%88%B1/"),
        Pair("恐怖", "/theme/%E6%81%90%E6%80%96/"),
        Pair("蔷薇", "/theme/%E8%94%B7%E8%96%87/"),
        Pair("古风", "/theme/%E5%8F%A4%E9%A3%8E/"),
        Pair("校园", "/theme/%E6%A0%A1%E5%9B%AD/"),
        Pair("爆笑", "/theme/%E7%88%86%E7%AC%91/"),
        Pair("虐心", "/theme/%E8%99%90%E5%BF%83/"),
        Pair("推理", "/theme/%E6%8E%A8%E7%90%86/"),
        Pair("权谋", "/theme/%E6%9D%83%E8%B0%8B/"),
        Pair("宫斗", "/theme/%E5%AE%AB%E6%96%97/"),
        Pair("唯美", "/theme/%E5%94%AF%E7%BE%8E/"),
        Pair("纯爱", "/theme/%E7%BA%AF%E7%88%B1/"),
        Pair("穿越", "/theme/%E7%A9%BF%E8%B6%8A/"),
        Pair("科幻", "/theme/%E7%A7%91%E5%B9%BB/"),
        Pair("竞技", "/theme/%E7%AB%9E%E6%8A%80/"),
        Pair("其他", "/theme/%E5%85%B6%E5%AE%83/"),
        Pair("悬疑", "/theme/%E6%82%AC%E7%96%91/"),
        Pair("明星", "/theme/%E6%98%8E%E6%98%9F/"),
        Pair("格斗", "/theme/%E6%A0%BC%E6%96%97/"),
        Pair("脑洞", "/theme/%E8%84%91%E6%B4%9E/"),
        Pair("治愈", "/theme/%E6%B2%BB%E6%84%88/"),
        Pair("后宫", "/theme/%E5%90%8E%E5%AE%AB/"),
        Pair("萌系", "/theme/%E8%90%8C%E7%B3%BB/"),
        Pair("励志", "/theme/%E5%8A%B1%E5%BF%97/"),
        Pair("动作", "/theme/%E5%8A%A8%E4%BD%9C/")

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
