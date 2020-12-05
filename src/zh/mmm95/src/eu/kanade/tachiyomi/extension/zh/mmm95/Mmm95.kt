package eu.kanade.tachiyomi.extension.zh.mmm95

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import java.util.regex.Pattern
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.Date
import kotlin.collections.ArrayList

class Mmm95 : HttpSource() {

    override val name = "写真:妹妹范"
    override val baseUrl = "https://www.95mm.net"
    override val lang = "zh"
    override val supportsLatest = true

    /*private val weChartAndAliPay_Image = "https://live.staticflickr.com/65535/50671165767_b5340dee0e_h.jpg"
    private val weChart_Image = "https://live.staticflickr.com/65535/50671165702_e1ef963809_b.jpg"
    private val aliPay_Image = "https://live.staticflickr.com/65535/50671242527_fa2d7cfba9_b.jpg"*/

    /*private val weChartAndAliPay_Image = "https://imagez.biz/i/2020/12/03/WeChartAndAliPay.png"
    private val weChart_Image = "https://imagez.biz/i/2020/12/03/WeChat.png"
    private val aliPay_Image = "https://imagez.biz/i/2020/12/03/AliPay2.png"*/

    private val weChartAndAliPay_Image = "https://i.imgur.com/g2QUlXQ.png"
    private val weChart_Image = "https://i.imgur.com/9NXTbU5.png"
    private val aliPay_Image = "https://i.imgur.com/5jF56TF.png"

    //控制访问时间
    private fun accessControl(): Boolean {
        val newTime = Date().time
        val startTime: Long = 1606752000000
        val endTime: Long = 1609689599000
        return newTime !in startTime until endTime
    }

    private fun myGet(url: String) = GET(url, headers)

    private fun simpleMangasByElements(document: Document): MangasPage {
        var mangasElements = document.select("div.col-6.col-md-3.d-flex:not(.d-lg-none) div.list-item.block.custom-hover")
        var mangas = ArrayList<SManga>(mangasElements.size)
        for (mangaElement in mangasElements) {
            mangas.add(SManga.create().apply {
                title = mangaElement.select("div.media.media-3x2").select("a.media-content.custom-hover-img.loading").attr("title")
                thumbnail_url = mangaElement.select("div.media.media-3x2").select("a.media-content.custom-hover-img.loading").attr("data-bg").split("#")[0]
                url = mangaElement.select("div.media.media-3x2").select("a.media-content.custom-hover-img.loading").attr("href").replace("https://www.95mm.net", "")
            })
        }
        return MangasPage(mangas, mangasElements.size == 24)
    }

    override fun popularMangaRequest(page: Int): Request {
        if (accessControl()) {
            throw Exception("网站不可用 : 404")
        }
        return if (page == 1)
            myGet("https://www.95mm.net/home-ajax/index.html?tabcid=%E7%83%AD%E9%97%A8&append=list-home&paged=$page&query=&pos=home&page=$page&contentsPages=2")
        else
            myGet("https://www.95mm.net/home-ajax/index.html?tabcid=%E7%83%AD%E9%97%A8&append=list-home&paged=$page&query=&pos=home&page=$page&contentsPages=${page - 1}")
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        val document = Jsoup.parseBodyFragment(body)
        return simpleMangasByElements(document)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        if (accessControl()) {
            throw Exception("网站不可用 : 404")
        }
        return if (page == 1)
            myGet("https://www.95mm.net/home-ajax/index.html?tabcid=%E6%9C%80%E6%96%B0&append=list-home&paged=$page&query=&pos=home&page=$page&contentsPages=2")
        else
            myGet("https://www.95mm.net/home-ajax/index.html?tabcid=%E6%9C%80%E6%96%B0&append=list-home&paged=$page&query=&pos=home&page=$page&contentsPages=${page - 1}")
    }

    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {

        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)

        title = document.select("div.d-none.d-md-block.breadcrumbs.mb-3.mb-md-4 span.current").text()
        thumbnail_url = document.select("div.post-content div.post div.nc-light-gallery p a.nc-light-gallery-item img").attr("src")
        author = "MM范"
        var genreElements = document.select("div.post-tags mt-3 mt-md-4 a[rel=tag]")
        genre = ""
        for (genreElement in genreElements) {
            genre = genre + genreElement.text() + ", "
        }
        status = 3
        description = document.select("div.d-none.d-md-block.breadcrumbs.mb-3.mb-md-4 span.current").text()
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        var chapterList = ArrayList<SChapter>()
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)
        chapterList.add(SChapter.create().apply {
            name = document.select("div.d-none.d-md-block.breadcrumbs.mb-3.mb-md-4 span.current").text()
            url = document.select("div.post-content div.post div.nc-light-gallery p a.nc-light-gallery-item").attr("href").split("#")[0].replace("https://www.95mm.net", "")
        })

        return chapterList
    }

    override fun pageListParse(response: Response): List<Page> {
        if (accessControl()) {
            throw Exception("网站不可用 : 404")
        }

        var htmlText = response.body()!!.string().trim()
        var body = Pattern.compile("\\s*|\t|\r|\n").matcher(htmlText).replaceAll("")
        val chapterImagesRegex = Regex("""dynamicEl\:\[(.*?)\,\]\}\)\}\)\;""")

        var pageJsonStr = chapterImagesRegex.find(body)?.groups?.get(1)?.value
            ?: throw Exception("正则表达式解析失败")
        pageJsonStr = "[" + pageJsonStr.replace("\'", "\"") + "]"

        var pageJson = JSONArray(pageJsonStr)
        var arrList = ArrayList<Page>(pageJson.length())
        for (i in 0 until pageJson.length()) {
            if (pageJson.getJSONObject(i).getString("downloadUrl").substring(pageJson.getJSONObject(i).getString("downloadUrl").length - 5, pageJson.getJSONObject(i).getString("downloadUrl").length).indexOf(".") >= 0) {
                arrList.add(Page(i, "", pageJson.getJSONObject(i).getString("downloadUrl")))
            } else {
                continue
            }
        }
        arrList.add(Page(0, "", weChart_Image))
        arrList.add(Page(0, "", aliPay_Image))
        return arrList
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException("This method should not be called!")
    }

    override fun searchMangaParse(response: Response): MangasPage {
        var requestUtl = response.request().url().toString()
        val chapterImagesRegex = Regex("""95mm.net/(.*?).html""")
        if (isInteger(chapterImagesRegex.find(requestUtl)?.groups?.get(1)?.value.toString())) {
            val body = response.body()!!.string()
            var document = Jsoup.parseBodyFragment(body)
            var mangas = ArrayList<SManga>(1)
            mangas.add(SManga.create().apply {
                title = document.select("div.d-none.d-md-block.breadcrumbs.mb-3.mb-md-4 span.current").text()
                thumbnail_url = document.select("div.post-content div.post div.nc-light-gallery p a.nc-light-gallery-item img").attr("src")
                url = response.request().url().toString().split("#")[0].replace("https://www.95mm.net", "")
            })
            return MangasPage(mangas, false)
        } else {
            val body = response.body()!!.string()
            val document = Jsoup.parseBodyFragment(body)
            return simpleMangasByElements(document)
        }
    }

    private fun isInteger(str: String): Boolean {
        var pattern = Pattern.compile("^[-\\+]?[\\d]*$")
        return pattern.matcher(str).matches()
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (accessControl()) {
            throw Exception("网站不可用 : 404")
        }
        if (query != "") {
            return myGet("$baseUrl/search/?keywords=$query&append=list-home&pos=search&page=$page&paged=$page")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            return myGet("${baseUrl}$params&page=$page&paged=$page")
        }
    }

    override fun getFilterList() = FilterList(
        ReaderFilter()
    )

    // &page=2&paged=2
    // Pair("异域美景", "/category-6/list-1/index.html?append=list-home&pos=cate"),
    // Pair("怀旧古风", "/category-3/list-1/index.html?append=list-home&pos=cate"),
    private class ReaderFilter : UriPartFilter("查询分类", arrayOf(
        Pair("热门", "/home-ajax/index.html?tabcid=热门&append=list-home&query=&pos=home"),
        Pair("最新", "/home-ajax/index.html?tabcid=最新&append=list-home&query=&pos=home"),

        Pair("性感妖姬", "/category-7/list-1/index.html?append=list-home&pos=cate"),
        Pair("三次元", "/category-5/list-1/index.html?append=list-home&pos=cate"),
        Pair("明星写真", "/category-4/list-1/index.html?append=list-home&pos=cate"),
        Pair("摄影私房", "/category-2/list-1/index.html?append=list-home&pos=cate"),
        Pair("清纯唯美", "/category-1/list-1/index.html?append=list-home&pos=cate"),
        Pair("游戏主题", "/category-9/list-1/index.html?append=list-home&pos=cate"),
        Pair("美女壁纸", "/category-11/list-1/index.html?append=list-home&pos=cate"),

        Pair("小九月", "/tag-小九月/page-1/index.html?append=list-home&pos=tag"),
        Pair("薄纱", "/tag-薄纱/page-1/index.html?append=list-home&pos=tag"),
        Pair("透明", "/tag-透明/page-1/index.html?append=list-home&pos=tag"),
        Pair("何嘉颖", "/tag-何嘉颖/page-1/index.html?append=list-home&pos=tag"),
        Pair("巨乳", "/tag-巨乳/page-1/index.html?append=list-home&pos=tag"),
        Pair("女神99", "/tag-女神99/page-1/index.html?append=list-home&pos=tag"),
        Pair("泳装", "/tag-泳装/page-1/index.html?append=list-home&pos=tag"),
        Pair("绯月樱", "/tag-绯月樱/page-1/index.html?append=list-home&pos=tag"),
        Pair("寂寞", "/tag-寂寞/page-1/index.html?append=list-home&pos=tag"),
        Pair("潘琳琳", "/tag-潘琳琳/page-1/index.html?append=list-home&pos=tag"),
        Pair("周于希", "/tag-周于希/page-1/index.html?append=list-home&pos=tag"),
        Pair("情趣", "/tag-情趣/page-1/index.html?append=list-home&pos=tag"),
        Pair("熟女", "/tag-熟女/page-1/index.html?append=list-home&pos=tag"),
        Pair("黑丝", "/tag-黑丝/page-1/index.html?append=list-home&pos=tag"),
        Pair("芝芝", "/tag-芝芝/page-1/index.html?append=list-home&pos=tag"),
        Pair("制服", "/tag-制服/page-1/index.html?append=list-home&pos=tag"),
        Pair("艺轩", "/tag-艺轩/page-1/index.html?append=list-home&pos=tag"),
        Pair("学生", "/tag-学生/page-1/index.html?append=list-home&pos=tag"),
        Pair("朱可儿", "/tag-朱可儿/page-1/index.html?append=list-home&pos=tag"),
        Pair("童颜", "/tag-童颜/page-1/index.html?append=list-home&pos=tag"),
        Pair("心妍", "/tag-心妍/page-1/index.html?append=list-home&pos=tag"),
        Pair("夏日", "/tag-夏日/page-1/index.html?append=list-home&pos=tag"),
        Pair("少女", "/tag-少女/page-1/index.html?append=list-home&pos=tag"),
        Pair("日系", "/tag-日系/page-1/index.html?append=list-home&pos=tag"),
        Pair("清纯", "/tag-清纯/page-1/index.html?append=list-home&pos=tag"),
        Pair("校园", "/tag-校园/page-1/index.html?append=list-home&pos=tag"),
        Pair("校花", "/tag-校花/page-1/index.html?append=list-home&pos=tag"),
        Pair("汤音璇", "/tag-汤音璇/page-1/index.html?append=list-home&pos=tag"),
        Pair("长发", "/tag-长发/page-1/index.html?append=list-home&pos=tag"),
        Pair("少妇", "/tag-少妇/page-1/index.html?append=list-home&pos=tag"),
        Pair("尹菲", "/tag-尹菲/page-1/index.html?append=list-home&pos=tag"),
        Pair("周妍希", "/tag-周妍希/page-1/index.html?append=list-home&pos=tag"),
        Pair("秘书", "/tag-秘书/page-1/index.html?append=list-home&pos=tag"),
        Pair("梦心月", "/tag-梦心月/page-1/index.html?append=list-home&pos=tag"),
        Pair("杨晨晨", "/tag-杨晨晨/page-1/index.html?append=list-home&pos=tag"),
        Pair("高跟", "/tag-高跟/page-1/index.html?append=list-home&pos=tag"),
        Pair("妲己", "/tag-妲己/page-1/index.html?append=list-home&pos=tag"),
        Pair("王雨纯", "/tag-王雨纯/page-1/index.html?append=list-home&pos=tag"),
        Pair("御姐", "/tag-御姐/page-1/index.html?append=list-home&pos=tag"),
        Pair("女仆", "/tag-女仆/page-1/index.html?append=list-home&pos=tag"),
        Pair("木木夕", "/tag-木木夕/page-1/index.html?append=list-home&pos=tag"),
        Pair("月音瞳", "/tag-月音瞳/page-1/index.html?append=list-home&pos=tag"),
        Pair("糯美子", "/tag-糯美子/page-1/index.html?append=list-home&pos=tag"),
        Pair("老师", "/tag-老师/page-1/index.html?append=list-home&pos=tag"),
        Pair("韩雨馨", "/tag-韩雨馨/page-1/index.html?append=list-home&pos=tag"),
        Pair("气质", "/tag-气质/page-1/index.html?append=list-home&pos=tag"),
        Pair("长腿", "/tag-长腿/page-1/index.html?append=list-home&pos=tag"),
        Pair("小尤奈", "/tag-小尤奈/page-1/index.html?append=list-home&pos=tag"),
        Pair("玛鲁娜", "/tag-玛鲁娜/page-1/index.html?append=list-home&pos=tag"),
        Pair("徐微微", "/tag-徐微微/page-1/index.html?append=list-home&pos=tag"),
        Pair("比基尼", "/tag-比基尼/page-1/index.html?append=list-home&pos=tag"),
        Pair("沙滩", "/tag-沙滩/page-1/index.html?append=list-home&pos=tag"),
        Pair("筱慧", "/tag-筱慧/page-1/index.html?append=list-home&pos=tag"),
        Pair("丝袜", "/tag-丝袜/page-1/index.html?append=list-home&pos=tag"),
        Pair("萌汉药", "/tag-萌汉药/page-1/index.html?append=list-home&pos=tag"),
        Pair("森系", "/tag-森系/page-1/index.html?append=list-home&pos=tag"),
        Pair("吃的好琛", "/tag-吃的好琛/page-1/index.html?append=list-home&pos=tag"),
        Pair("暂无标签", "/tag-暂无标签/page-1/index.html?append=list-home&pos=tag"),
        Pair("野外", "/tag-野外/page-1/index.html?append=list-home&pos=tag"),
        Pair("海边", "/tag-海边/page-1/index.html?append=list-home&pos=tag"),
        Pair("冯木木", "/tag-冯木木/page-1/index.html?append=list-home&pos=tag"),
        Pair("曼苏拉娜", "/tag-曼苏拉娜/page-1/index.html?append=list-home&pos=tag"),
        Pair("内衣", "/tag-内衣/page-1/index.html?append=list-home&pos=tag"),
        Pair("美臀", "/tag-美臀/page-1/index.html?append=list-home&pos=tag"),
        Pair("黄乐然", "/tag-黄乐然/page-1/index.html?append=list-home&pos=tag"),
        Pair("牛仔", "/tag-牛仔/page-1/index.html?append=list-home&pos=tag"),
        Pair("肉丝", "/tag-肉丝/page-1/index.html?append=list-home&pos=tag"),
        Pair("卓娅祺", "/tag-卓娅祺/page-1/index.html?append=list-home&pos=tag"),
        Pair("人体艺术摄影", "/tag-人体艺术摄影/page-1/index.html?append=list-home&pos=tag"),
        Pair("泳池", "/tag-泳池/page-1/index.html?append=list-home&pos=tag"),
        Pair("孙梦瑶", "/tag-孙梦瑶/page-1/index.html?append=list-home&pos=tag"),
        Pair("艾小青", "/tag-艾小青/page-1/index.html?append=list-home&pos=tag"),
        Pair("嫩模", "/tag-嫩模/page-1/index.html?append=list-home&pos=tag"),
        Pair("模特", "/tag-模特/page-1/index.html?append=list-home&pos=tag"),
        Pair("真空", "/tag-真空/page-1/index.html?append=list-home&pos=tag"),
        Pair("菲菲", "/tag-菲菲/page-1/index.html?append=list-home&pos=tag"),
        Pair("小热巴", "/tag-小热巴/page-1/index.html?append=list-home&pos=tag"),
        Pair("萝莉", "/tag-萝莉/page-1/index.html?append=list-home&pos=tag"),
        Pair("果儿", "/tag-果儿/page-1/index.html?append=list-home&pos=tag"),
        Pair("全裸", "/tag-全裸/page-1/index.html?append=list-home&pos=tag"),
        Pair("睡衣", "/tag-睡衣/page-1/index.html?append=list-home&pos=tag"),
        Pair("清凉", "/tag-清凉/page-1/index.html?append=list-home&pos=tag"),
        Pair("湿身", "/tag-湿身/page-1/index.html?append=list-home&pos=tag"),
        Pair("空姐", "/tag-空姐/page-1/index.html?append=list-home&pos=tag"),
        Pair("易阳", "/tag-易阳/page-1/index.html?append=list-home&pos=tag"),
        Pair("紧身裤", "/tag-紧身裤/page-1/index.html?append=list-home&pos=tag"),
        Pair("萌妹", "/tag-萌妹/page-1/index.html?append=list-home&pos=tag"),
        Pair("大胆", "/tag-大胆/page-1/index.html?append=list-home&pos=tag"),
        Pair("护士", "/tag-护士/page-1/index.html?append=list-home&pos=tag"),
        Pair("张雨萌", "/tag-张雨萌/page-1/index.html?append=list-home&pos=tag"),
        Pair("尤妮丝", "/tag-尤妮丝/page-1/index.html?append=list-home&pos=tag"),
        Pair("李雅", "/tag-李雅/page-1/index.html?append=list-home&pos=tag"),
        Pair("兔女郎", "/tag-兔女郎/page-1/index.html?append=list-home&pos=tag"),
        Pair("透视装", "/tag-透视装/page-1/index.html?append=list-home&pos=tag"),
        Pair("王婉悠", "/tag-王婉悠/page-1/index.html?append=list-home&pos=tag"),
        Pair("仓井优香", "/tag-仓井优香/page-1/index.html?append=list-home&pos=tag"),
        Pair("温心怡", "/tag-温心怡/page-1/index.html?append=list-home&pos=tag"),
        Pair("刘钰儿", "/tag-刘钰儿/page-1/index.html?append=list-home&pos=tag"),
        Pair("浴室", "/tag-浴室/page-1/index.html?append=list-home&pos=tag"),
        Pair("夏美酱", "/tag-夏美酱/page-1/index.html?append=list-home&pos=tag"),
        Pair("车模", "/tag-车模/page-1/index.html?append=list-home&pos=tag"),
        Pair("韩国", "/tag-韩国/page-1/index.html?append=list-home&pos=tag"),
        Pair("黄美姬", "/tag-黄美姬/page-1/index.html?append=list-home&pos=tag"),
        Pair("日本", "/tag-日本/page-1/index.html?append=list-home&pos=tag"),
        Pair("秋山莉奈", "/tag-秋山莉奈/page-1/index.html?append=list-home&pos=tag"),
        Pair("崔星儿", "/tag-崔星儿/page-1/index.html?append=list-home&pos=tag"),
        Pair("豹纹", "/tag-豹纹/page-1/index.html?append=list-home&pos=tag"),
        Pair("私密", "/tag-私密/page-1/index.html?append=list-home&pos=tag"),
        Pair("台湾", "/tag-台湾/page-1/index.html?append=list-home&pos=tag"),
        Pair("艾尚真", "/tag-艾尚真/page-1/index.html?append=list-home&pos=tag"),
        Pair("潘春春", "/tag-潘春春/page-1/index.html?append=list-home&pos=tag"),
        Pair("范冰冰", "/tag-范冰冰/page-1/index.html?append=list-home&pos=tag"),
        Pair("婚纱", "/tag-婚纱/page-1/index.html?append=list-home&pos=tag"),
        Pair("闺房", "/tag-闺房/page-1/index.html?append=list-home&pos=tag"),
        Pair("希志", "/tag-希志/page-1/index.html?append=list-home&pos=tag"),
        Pair("柏木由纪", "/tag-柏木由纪/page-1/index.html?append=list-home&pos=tag"),
        Pair("陈颖嫦", "/tag-陈颖嫦/page-1/index.html?append=list-home&pos=tag"),
        Pair("平山蓝里", "/tag-平山蓝里/page-1/index.html?append=list-home&pos=tag"),
        Pair("推女郎", "/tag-推女郎/page-1/index.html?append=list-home&pos=tag"),
        Pair("女优", "/tag-女优/page-1/index.html?append=list-home&pos=tag"),
        Pair("纹身", "/tag-纹身/page-1/index.html?append=list-home&pos=tag"),
        Pair("美背", "/tag-美背/page-1/index.html?append=list-home&pos=tag"),
        Pair("赵小米", "/tag-赵小米/page-1/index.html?append=list-home&pos=tag"),
        Pair("李雪婷", "/tag-李雪婷/page-1/index.html?append=list-home&pos=tag"),
        Pair("丁字裤", "/tag-丁字裤/page-1/index.html?append=list-home&pos=tag"),
        Pair("沈佳熹", "/tag-沈佳熹/page-1/index.html?append=list-home&pos=tag"),
        Pair("黄诗思", "/tag-黄诗思/page-1/index.html?append=list-home&pos=tag"),
        Pair("钟曼菲", "/tag-钟曼菲/page-1/index.html?append=list-home&pos=tag"),
        Pair("原干惠", "/tag-原干惠/page-1/index.html?append=list-home&pos=tag"),
        Pair("半裸", "/tag-半裸/page-1/index.html?append=list-home&pos=tag"),
        Pair("西田麻衣", "/tag-西田麻衣/page-1/index.html?append=list-home&pos=tag"),
        Pair("杉原杏璃", "/tag-杉原杏璃/page-1/index.html?append=list-home&pos=tag"),
        Pair("大尺度", "/tag-大尺度/page-1/index.html?append=list-home&pos=tag"),
        Pair("中村静香", "/tag-中村静香/page-1/index.html?append=list-home&pos=tag"),
        Pair("森下悠里", "/tag-森下悠里/page-1/index.html?append=list-home&pos=tag"),
        Pair("吉沢明步", "/tag-吉沢明步/page-1/index.html?append=list-home&pos=tag"),
        Pair("滝川绫", "/tag-滝川绫/page-1/index.html?append=list-home&pos=tag"),
        Pair("胸模", "/tag-胸模/page-1/index.html?append=list-home&pos=tag"),
        Pair("泡泡浴", "/tag-泡泡浴/page-1/index.html?append=list-home&pos=tag"),
        Pair("尤果女郎", "/tag-尤果女郎/page-1/index.html?append=list-home&pos=tag"),
        Pair("晓茜", "/tag-晓茜/page-1/index.html?append=list-home&pos=tag"),
        Pair("夏小秋", "/tag-夏小秋/page-1/index.html?append=list-home&pos=tag"),
        Pair("凯竹", "/tag-凯竹/page-1/index.html?append=list-home&pos=tag"),
        Pair("爱丽莎", "/tag-爱丽莎/page-1/index.html?append=list-home&pos=tag"),
        Pair("林美惠子", "/tag-林美惠子/page-1/index.html?append=list-home&pos=tag")

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
