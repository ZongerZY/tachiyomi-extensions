package eu.kanade.tachiyomi.extension.zh.manhuatai

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

class Manhuatai : HttpSource() {
    // 和 爱漫画 漫画台 用得同一个连接请求
    override val name = "漫画台"
    override val baseUrl = ""
    override val lang = "zh"
    override val supportsLatest = true

    private var requestJsonHeaders = Headers.of(mapOf(
        "Cache-Control" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
        "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.76 Safari/537.36",
        "Host" to "getconfig-globalapi.yyhao.com",
        "Connection" to "close"
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
        val obj = JSONObject(json)
        val arr = obj.getJSONArray("data")
        val ret = java.util.ArrayList<SManga>(arr.length())
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
                url = "http://comic.321mh.com/app_api/v5/getcomicinfo_body/?comic_id=$comic_id&from_page=search&platformname=android&productname=mht"
            })
        }
        return MangasPage(ret, arr.length() != 0)
    }

    // 点击量
    override fun popularMangaRequest(page: Int): Request {
        return jsonGet("https://getconfig-globalapi.yyhao.com/app_api/v5/getsortlist/?page=$page&orderby=click&search_key=&platformname=android&productname=mht")
    }

    // 处理点击量请求
    override fun popularMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        return mangaFromJSON(body)
    }

    // 按更新
    override fun latestUpdatesRequest(page: Int): Request {
        return jsonGet("https://getconfig-globalapi.yyhao.com/app_api/v5/getsortlist/?page=$page&orderby=date&search_key=&platformname=android&productname=mht")
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
                url = "http://comic.321mh.com/app_api/v5/getcomicinfo_body/?comic_id=$comic_id&young_mode=0&from_page=search&platformname=android&productname=mht&pages=$i"
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
            return jsonGet("https://getconfig-globalapi.yyhao.com/app_api/v5/getsortlist/?page=$page&size=7&orderby=click&search_key=$query&young_mode=0&platformname=android&productname=mht")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            return jsonGet("https://getconfig-globalapi.yyhao.com/app_api/v5/getsortlist/?search_type=&search_key=&platformname=android&productname=mht&comic_sort=$params&page=$page")
        }
    }

    // 封装分类
    override fun getFilterList() = FilterList(
        ClassifyFilter(),
        UpdateFilter()
    )

    // 漫画分类
    private class ClassifyFilter : UriPartFilter("查询分类", arrayOf(
        Pair("全部", ""),
        Pair("完结", "wanjie"),
        Pair("连载", "lianzai"),
        Pair("精品", "jingpin"),
        Pair("热血", "rexue"),
        Pair("机战", "jizhan"),
        Pair("运动", "yundong"),
        Pair("推理", "tuili"),
        Pair("冒险", "maoxian"),
        Pair("搞笑", "gaoxiao"),
        Pair("战争", "zhanzhen"),
        Pair("神魔", "shenmo"),
        Pair("竞技", "jingji"),
        Pair("悬疑", "xuanyi"),
        Pair("社会", "shehui"),
        Pair("恋爱", "lianai"),
        Pair("宠物", "chongwu"),
        Pair("吸血", "xixue"),
        Pair("霸总", "bazong"),
        Pair("玄幻", "xuanhuan"),
        Pair("古风", "gufeng"),
        Pair("历史", "lishi"),
        Pair("漫改", "mangai"),
        Pair("游戏", "youxi"),
        Pair("穿越", "chuanyue"),
        Pair("恐怖", "kongbu"),
        Pair("真人", "zhenren"),
        Pair("科幻", "kehuan"),
        Pair("防疫", "fangyi"),
        Pair("都市", "dushi"),
        Pair("武侠", "wuxia"),
        Pair("修真", "xiuzhen"),
        Pair("生活", "shenghuo"),
        Pair("动作", "dongzuo")
    ))

    // 状态分类
    private class UpdateFilter : UriPartFilter("状态分类", arrayOf(
        Pair("点击量", "&orderby=click"),
        Pair("连载", "&orderby=wanjie"),
        Pair("完结", "&orderby=date")

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
