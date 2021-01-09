package eu.kanade.tachiyomi.extension.zh.zongerzysealegend

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
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

class Zongerzysealegend : HttpSource() {

    override val name = "海洋漫画"
    override val baseUrl = ""
    override val lang = "zh"
    override val supportsLatest = true

    /*private val weChartAndAliPay_Image = "https://live.staticflickr.com/65535/50671165767_b5340dee0e_h.jpg"
    private val weChart_Image = "https://live.staticflickr.com/65535/50671165702_e1ef963809_b.jpg"
    private val aliPay_Image = "https://live.staticflickr.com/65535/50671242527_fa2d7cfba9_b.jpg"*/

    /*private val weChartAndAliPay_Image = "https://imagez.biz/i/2020/12/03/WeChartAndAliPay.png"
    private val weChart_Image = "https://imagez.biz/i/2020/12/03/WeChat.png"
    private val aliPay_Image = "https://imagez.biz/i/2020/12/03/AliPay2.png"*/

    // private val weChartAndAliPay_Image = "https://i.imgur.com/g2QUlXQ.png"
    // private val weChart_Image = "https://i.imgur.com/9NXTbU5.png"
    // private val aliPay_Image = "https://i.imgur.com/5jF56TF.png"

    private var requestJsonHeaders = Headers.of(mapOf(
        "Cache-Control" to "application/json",
        "channel" to "oppo"
    ))
    private var requestImageHeaders = Headers.of(mapOf(
        "Accept-Encoding" to "gzip",
        "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.76 Safari/537.36",
        "Referer" to "http://www.manhuatai.com/",
        "Host" to "mhpic.cnmanhua.com"
    ))

    private fun jsonPost(url: String, body: RequestBody) = POST(url, requestJsonHeaders, body)
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
            val comic_id = objArr.getString("comic_id")
            val comic_name = objArr.getString("comic_name")
            ret.add(SManga.create().apply {
                title = comic_name
                thumbnail_url = "http://image.yqmh.com/mh/$comic_id.jpg-600x800.jpg.webp"
                url = "http://comic.321mh.com/app_api/v5/getcomicinfo_body/?comic_id=$comic_id&from_page=search&platformname=android&productname=smh"
            })
        }
        return MangasPage(ret, arr.length() != 0)
    }

    // 点击量
    override fun popularMangaRequest(page: Int): Request {
        return jsonGet("https://getconfig-globalapi.yyhao.com/app_api/v5/getsortlist/?page=$page&search_type=&comic_sort=&orderby=click&search_key=&platformname=android&productname=smh")
    }

    // 处理点击量请求
    override fun popularMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        return mangaFromJSON(body)
    }

    // 按更新
    override fun latestUpdatesRequest(page: Int): Request {
        return jsonGet("https://getconfig-globalapi.yyhao.com/app_api/v5/getsortlist/?page=$page&search_type=&comic_sort=&orderby=date&search_key=&platformname=android&productname=smh")
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
                if (i % 5 == 0) {
                    url = "$requestUrl&pages=$i&isAddPayImage=1"
                } else if (i == arr.length() - 1) {
                    url = "$requestUrl&pages=$i&isAddPayImage=1"
                } else {
                    url = "$requestUrl&pages=$i&isAddPayImage=0"
                }
            })
        }
        return ret
    }

    override fun pageListRequest(chapter: SChapter): Request {
        return jsonGet(chapter.url)
    }

    override fun pageListParse(response: Response): List<Page> {
        val url = response.request().url().toString()
        var requestUrl = response.request().url().toString().split("isAddPayImage=")[1]
        val page = url.split("&pages=")[1].split("&isAddPayImage=")[0].toInt()
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
        val json = response.body()!!.string()
        return mangaFromJSON(json)
    }

    // 查询及分类查询
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return jsonGet("https://getconfig-globalapi.yyhao.com/app_api/v5/getsortlist/?page=1&orderby=click&search_type=&comic_sort=&search_key=$query&size=21&young_mode=0&platformname=android&productname=smh")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            return jsonGet("https://getconfig-globalapi.yyhao.com/app_api/v5/getsortlist/?page=$page&search_type=&comic_sort=$params&search_key=&platformname=android&productname=smh")
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
        Pair("热血", "rexue"),
        Pair("武侠", "wuxia"),
        Pair("玄幻", "xuanhuan"),
        Pair("穿越", "chuanyue"),
        Pair("修真", "xiuzhen"),
        Pair("神魔", "shenmo"),
        Pair("冒险", "maoxian"),
        Pair("游戏", "youxi"),
        Pair("社会", "shehui"),
        Pair("机战", "jizhan"),
        Pair("运动", "yundong"),
        Pair("推理", "tuili"),
        Pair("搞笑", "gaoxiao"),
        Pair("战争", "zhanzhen"),
        Pair("忍者", "renzhe"),
        Pair("竞技", "jingji"),
        Pair("悬疑", "xuanyi"),
        Pair("恋爱", "lianai"),
        Pair("小说改编", "xiaoshuo"),
        Pair("完结", "wanjie"),
        Pair("全彩", "quancai"),
        Pair("黑白", "heibai"),
        Pair("精品", "jingpin"),
        Pair("杂志", "zazhi"),
        Pair("港台", "gangtai"),
        Pair("欧美", "oumei"),
        Pair("生活", "shenghuo"),
        Pair("动作", "dongzuo"),
        Pair("大陆", "dalu"),
        Pair("日本", "riben"),
        Pair("真人", "zhenren"),
        Pair("科幻", "kehuan"),
        Pair("防疫", "fangyi"),
        Pair("都市", "dushi"),
        Pair("霸总", "bazong"),
        Pair("古风", "gufeng"),
        Pair("历史", "lishi"),
        Pair("恐怖", "kongbu"),
        Pair("宠物", "chongwu"),
        Pair("吸血", "xixue"),
        Pair("萝莉", "luoli"),
        Pair("御姐", "yujie"),
        Pair("韩国", "os"),
        Pair("连载", "lianzai"),
        Pair("漫改", "mangai")

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
