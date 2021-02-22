package eu.kanade.tachiyomi.extension.zh.jmzj

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Base64
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import java.io.ByteArrayOutputStream
import java.io.InputStream
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.jsoup.Jsoup

class Jmzj : HttpSource() {

    override val baseUrl = ""
    override val name = "Jmzj(禁漫之家)"
    override val lang = "zh"
    override val supportsLatest = true

    private fun myGet(url: String, myHeaders: Headers) = GET(url, myHeaders)
    private fun myPost(url: String, myHeaders: Headers, body: RequestBody) = POST(url, myHeaders, body)

    // 220980
    // 算法 html页面 1800 行左右
    // 图片开始分割的ID编号
    val scramble_id = 220980

    // 处理URL请求
    override val client: OkHttpClient = network.cloudflareClient.newBuilder().addInterceptor(
        fun(chain): Response {
            val url = chain.request().url().toString()
            val response = chain.proceed(chain.request())
            if (!url.contains("media/photos", ignoreCase = true)) return response // 对非漫画图片连接直接放行
            if (url.substring(url.indexOf("photos/") + 7, url.lastIndexOf("/")).toInt() < scramble_id) return response // 对在漫画章节ID为220980之前的图片未进行图片分割,直接放行
            // 章节ID:220980(包含)之后的漫画(2020.10.27之后)图片进行了分割倒序处理
            val res = response.body()!!.byteStream().use {
                decodeImage(it)
            }
            val mediaType = MediaType.parse("image/avif,image/webp,image/apng,image/*,*/*")
            val outputBytes = ResponseBody.create(mediaType, res)
            return response.newBuilder().body(outputBytes).build()
        }
    ).build()

    // 对被分割的图片进行分割,排序处理
    private fun decodeImage(img: InputStream): ByteArray {
        // 使用bitmap进行图片处理
        val input = BitmapFactory.decodeStream(img)
        // 漫画高度 and width
        val height = input.height
        val width = input.width
        // 水平分割10个小图
        val rows = 10
        // 未除尽像素
        var remainder = (height % rows)
        // 创建新的图片对象
        val resultBitmap = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        // 分割图片
        for (x in 0 until rows) {
            // 分割算法(详情见html源码页的方法"function scramble_image(img)")
            var copyH = Math.floor(height / rows.toDouble()).toInt()
            var py = copyH * (x)
            var y = height - (copyH * (x + 1)) - remainder
            if (x == 0) {
                copyH = copyH + remainder
            } else {
                py = py + remainder
            }
            // 要裁剪的区域
            val crop = Rect(0, y, width, (height - (copyH * x) - remainder))
            // 裁剪后应放置到新图片对象的区域
            val splic = Rect(0, py, width, (py + copyH))

            canvas.drawBitmap(input, crop, splic, null)
        }
        // 创建输出流
        val output = ByteArrayOutputStream()
        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        return output.toByteArray()
    }

    private val client2: OkHttpClient = network.client.newBuilder()
        .build()

    private var loginHeaders = Headers.of(mapOf(
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Encoding" to "",
        "Accept-Language" to "zh-CN,zh;q=0.9",
        "User-Agent" to "Mozilla/5.0 (Linux; U; Android 9; zh-cn; V1809A Build/PKQ1.181030.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/10.1 Mobile Safari/537.36",
        "Connection" to "close"
    ))

    private var loginCookie = getLoginCookie()

    private fun getLoginCookie(): String {
        var formBody = FormBody.Builder()
            .add("user_name", "z94269664")
            .add("user_pwd", "94269664.0")
            .build()
        var headers = client2.newCall(myPost("http://jmzj.xyz/user/login.html", loginHeaders, formBody)).execute().headers().values("Set-Cookie")
        var cookies = ""
        for (str in headers) {
            cookies += str.split(";")[0] + ";"
        }
        return cookies.trim(';')
    }

    override fun popularMangaRequest(page: Int): Request {
        return myGet("http://jmzj.xyz/label/hits/page/$page.html", headers)
    }

    override fun popularMangaParse(response: Response): MangasPage = searchMangaParse(response)

    override fun latestUpdatesRequest(page: Int): Request {
        return myGet("http://jmzj.xyz/label/update/page/$page.html", headers)
    }

    override fun latestUpdatesParse(response: Response): MangasPage = searchMangaParse(response)

    override fun mangaDetailsRequest(manga: SManga): Request {
        return myGet("http://jmzj.xyz${manga.url}", headers)
    }

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)

        title = document.select("div.mainForm div.comicInfo div.ib.info h1.name_mh").text()
        thumbnail_url = "http://jmzj.xyz" + document.select("div.mainForm div.comicInfo div.ib.cover img").attr("src")
        author = document.select("div.mainForm div.comicInfo div.ib.info p span.ib.l")[0].text()
        artist = "Tachiyomi:ZongerZY"
        status = if (document.select("div.mainForm div.comicInfo div.ib.info p.gray span.ib.s").text().contains("連載")) 1 else 2
        description = document.select("div.mainForm div.comicInfo div.ib.info p.content").text()
    }

    override fun chapterListRequest(manga: SManga): Request {
        return myGet("http://jmzj.xyz${manga.url}", headers)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        var chapterList = ArrayList<SChapter>()
        val body = response.body()!!.string()
        var elements = Jsoup.parseBodyFragment(body).select("div.mainForm div.chapterList div div.list a.ib")
        for (element in elements) {
            chapterList.add(SChapter.create().apply {
                name = element.text().trim()
                url = element.attr("href")
            })
        }
        return chapterList.reversed()
    }

    override fun pageListRequest(chapter: SChapter): Request {
        var loginHeaders = Headers.of(mapOf(
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-Encoding" to "",
            "Accept-Language" to "zh-CN,zh;q=0.9",
            "User-Agent" to "Mozilla/5.0 (Linux; U; Android 9; zh-cn; V1809A Build/PKQ1.181030.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/10.1 Mobile Safari/537.36",
            "Cookie" to loginCookie,
            "Connection" to "close"
        ))
        return myGet("http://jmzj.xyz${chapter.url}", loginHeaders)
    }

    override fun pageListParse(response: Response): List<Page> {
        var body = response.body()!!.string()
        if (body.contains("请升级会员")) {
            loginCookie = getLoginCookie()
            var loginHeaders = Headers.of(mapOf(
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                "Accept-Encoding" to "",
                "Accept-Language" to "zh-CN,zh;q=0.9",
                "User-Agent" to "Mozilla/5.0 (Linux; U; Android 9; zh-cn; V1809A Build/PKQ1.181030.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/10.1 Mobile Safari/537.36",
                "Cookie" to loginCookie,
                "Connection" to "close"
            ))
            body = client2.newCall(myGet("${response.request().url()}", loginHeaders)).execute().body()!!.string()
        }
        val imageRule = Regex("""image\_urls\=\"(.*?)\"\,note\=""").find(body)!!.value

        val imagebase64 = Regex("""\"(.*?)\"""").find(imageRule)!!.value.replace("\"", "")
        // decode C_DATA by Base64
        val decodedData = String(Base64.decode(imagebase64, Base64.NO_WRAP))

        var imageList = decodedData.split(",")

        var arrList = ArrayList<Page>(imageList.size)
        for (i in imageList.indices) {
            arrList.add(Page(i, "", if (imageList[i].contains("http")) imageList[i] else "http://jmzj.xyz${imageList[i]}"))
        }
        return arrList
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException("This method should not be called!")
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        val requestUrl = response.request().url().toString()
        val document = Jsoup.parseBodyFragment(body)
        var mangasElements = document.select("div.mainForm div.updateList div.bookList_3 div.item.ib")
        var mangas = ArrayList<SManga>()
        for (mangaElement in mangasElements) {
            mangas.add(SManga.create().apply {
                title = mangaElement.select("p.title a").text()
                thumbnail_url = "http://jmzj.xyz" + mangaElement.select("div.book a img.cover").attr("src") // zhuyi
                url = mangaElement.select("p.title a").attr("href")
            })
        }
        if (requestUrl.contains("/search/")) {
            return MangasPage(mangas, mangasElements.size == 21)
        }
        return MangasPage(mangas, mangasElements.size == 20)
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return myGet("http://jmzj.xyz/search/$query------$page-.html", headers)
        } else {
            val theme = filters.map {
                if (it is ThemeFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            val area = filters.map {
                if (it is AreaFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            val finish = filters.map {
                if (it is FinishFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            val charts = filters.map {
                if (it is ChartsFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")

            return if (charts == "") {
                myGet("http://jmzj.xyz/booktype/5--${theme}${area}${finish}$page.html", headers)
            } else {
                if (theme == "-" && area == "-" && finish == "-") {
                    myGet("http://jmzj.xyz${charts}$page.html", headers)
                } else {
                    throw UnsupportedOperationException("若使用<排行榜(非默认)>选项进行检索,需要将<题材><地区><进度>选项调制为(默认)项!!!")
                }
            }
        }
    }

    override fun getFilterList() = FilterList(
        ThemeFilter(),
        AreaFilter(),
        FinishFilter(),
        ChartsFilter()
    )

    private class ThemeFilter : UriPartFilter("题材", arrayOf(
        Pair("全部(默认)", "-"),
        Pair("韩漫", "韓漫-"),
        Pair("同人", "同人-"),
        Pair("单本", "單本-"),
        Pair("短篇", "短篇-"),
        Pair("Cosplay", "cosplay-"),
        Pair("CG", "CG-"),
        Pair("YAOI", "YAOI-"),
        Pair("其他", "其它-"),
        Pair("精品", "精品-")
    ))

    private class AreaFilter : UriPartFilter("地區", arrayOf(
        Pair("全部(默认)", "-"),
        Pair("韩国", "韓國-"),
        Pair("日本", "日本-"),
        Pair("大陆", "大陸-"),
        Pair("其它", "其它-")

    ))

    private class FinishFilter : UriPartFilter("进度", arrayOf(
        Pair("全部(默认)", "-"),
        Pair("连载", "2-"),
        Pair("完结", "1-")

    ))

    private class ChartsFilter : UriPartFilter("排行榜", arrayOf(
        Pair("人气榜(默认)", ""),
        Pair("周读榜", "/label/hits_week/page/"),
        Pair("月读榜", "/label/hits_month/page/"),
        Pair("火爆榜", "/label/up/page/"),
        Pair("更新榜", "/label/update/page/"),
        Pair("新漫榜", "/label/time_add/page/")
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
