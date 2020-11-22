package eu.kanade.tachiyomi.extension.zh.aiyouman

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

class Aiyouman : HttpSource() {

    override val name = "爱优漫"
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

    private fun jsonGet(url: String) = GET(url, requestJsonHeaders)
    private fun imageGet(url: String) = GET(url, requestImageHeaders)

    // 重写图片的访问方式,以添加请求头
    override fun imageUrlRequest(page: Page): Request {
        throw UnsupportedOperationException("This method should not be called!")
    }

    // 处理漫画列表信息
    private fun mangaFromJSON(json: String): MangasPage {
        var arr = JSONObject(json).getJSONArray("data")
        val ret = java.util.ArrayList<SManga>(arr.length())
        if (arr.length() == 0)
            return MangasPage(ret, false)
        for (i in 0 until arr.length()) {
            val objArr = arr.getJSONObject(i)
            val comic_id = objArr.getString("cartoon_id")
            val comic_name = objArr.getString("cartoon_name")
            ret.add(SManga.create().apply {
                title = comic_name
                thumbnail_url = "http://image.yqmh.com/mh/$comic_id.jpg-600x800.jpg.webp"
                url = "http://comic.321mh.com/app_api/v5/getcomicinfo_body/?comic_id=$comic_id&from_page=other&platformname=android&productname=aym"
            })
        }
        return MangasPage(ret, arr.length() != 0)
    }

    // 点击量
    override fun popularMangaRequest(page: Int): Request {
        return jsonGet("https://xcxweb.kanman.com/wechat/api/query.do?type=device&cartoon_status_id=&pay_type=&tag=&sortby=total_view_num&pagesize=30&pageno=$page&platformname=android&productname=aym")
    }

    // 处理点击量请求
    override fun popularMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        return mangaFromJSON(body)
    }

    // 按更新
    override fun latestUpdatesRequest(page: Int): Request {
        return jsonGet("https://xcxweb.kanman.com/wechat/api/query.do?type=device&cartoon_status_id=&pay_type=&tag=&sortby=update_time&pagesize=30&pageno=$page&platformname=android&productname=aym")
    }

    // 处理更新请求
    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    override fun mangaDetailsRequest(manga: SManga): Request {
        return jsonGet(manga.url)
    }

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

    override fun chapterListRequest(manga: SManga): Request {
        return jsonGet(manga.url)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        var requestUrl = response.request().url().toString()
        val json = response.body()!!.string()
        var arr = JSONObject(json).getJSONArray("comic_chapter")
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
                    thumbnail_url = "http://image.yqmh.com/mh/$comic_id.jpg-600x800.jpg.webp"
                    url = "http://comic.321mh.com/app_api/v5/getcomicinfo_body/?comic_id=$comic_id&from_page=other&platformname=android&productname=aym"
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
            return jsonGet("https://getconfig-globalapi.yyhao.com/app_api/v5/getsortlist/?page=$page&size=7&orderby=click&search_key=$query&young_mode=0&platformname=android&productname=aym")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            return jsonGet("https://xcxweb.kanman.com/wechat/api/query.do?type=device&pagesize=30&pageno=$page&platformname=android&productname=aym$params")
        }
    }

    // 封装分类
    override fun getFilterList() = FilterList(
        ThemeFilter(),
        AudienceFilter(),
        FinishFilter(),
        MoneyFilter()
    )

    // 漫画分类
    private class ThemeFilter : UriPartFilter("题材", arrayOf(
        Pair("全部", "&tag="),
        Pair("运动", "&tag=yundong"),
        Pair("推理", "&tag=tuili"),
        Pair("搞笑", "&tag=gaoxiao"),
        Pair("悬疑", "&tag=xuanyi"),
        Pair("社会", "&tag=shehui"),
        Pair("恋爱", "&tag=lianai"),
        Pair("宠物", "&tag=chongwu"),
        Pair("霸总", "&tag=bazong"),
        Pair("玄幻", "&tag=xuanhuan"),
        Pair("古风", "&tag=gufeng"),
        Pair("历史", "&tag=lishi"),
        Pair("漫改", "&tag=mangai"),
        Pair("穿越", "&tag=chuanyue"),
        Pair("真人", "&tag=zhenren"),
        Pair("科幻", "&tag=kehuan"),
        Pair("防疫", "&tag=fangyi"),
        Pair("修真", "&tag=xiuzhen"),
        Pair("生活", "&tag=shenghuo")

    ))

    private class AudienceFilter : UriPartFilter("受众", arrayOf(
        Pair("最热", "&sortby=total_view_num"),
        Pair("最新", "&sortby=update_time")
    ))

    private class FinishFilter : UriPartFilter("进度", arrayOf(
        Pair("全部", "&cartoon_status_id="),
        Pair("连载", "&cartoon_status_id=1"),
        Pair("完结", "&cartoon_status_id=2")

    ))

    // 状态分类
    private class MoneyFilter : UriPartFilter("资费", arrayOf(
        Pair("全部", "&pay_type="),
        Pair("免费", "&pay_type=2"),
        Pair("付费", "&pay_type=1")
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
