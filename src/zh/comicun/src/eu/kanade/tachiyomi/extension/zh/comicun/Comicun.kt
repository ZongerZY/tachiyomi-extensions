package eu.kanade.tachiyomi.extension.zh.comicun

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup

class Comicun : HttpSource() {

    override val name = "漫畫聯合國:海外"
    override val baseUrl = "https://www.comicun.com"
    override val lang = "zh"
    override val supportsLatest = true

    private val baseIP = arrayOf("https://64.185.231.138", "https://64.185.231.139", "https://64.185.231.140")

    private val trustManager = object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return emptyArray()
        }

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }
    }

    private val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, arrayOf(trustManager), SecureRandom())
    }

    override val client: OkHttpClient = network.client.newBuilder()
        .addInterceptor { dataHtmlInterceptor(it) }
        .build()

    private val client2: OkHttpClient = network.client.newBuilder()
        .sslSocketFactory(sslContext.socketFactory, trustManager)
        .hostnameVerifier { _, _ -> true }
        .build()

    private fun dataHtmlInterceptor(chain: Interceptor.Chain): Response {
        val url = chain.request().url().toString()
        return if (!url.startsWith("https://img.k886.net")) {
            client2.newCall(chain.request()).execute()
        } else {
            chain.proceed(chain.request())
        }
    }

    override fun popularMangaRequest(page: Int): Request {
        return GET("${baseIP[(0..2).random()]}/index-html-status-0-typeid-0-sort-hot?&page=$page")
    }

    override fun popularMangaParse(response: Response): MangasPage = searchMangaParse(response)

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("${baseIP[(0..2).random()]}/index-html-status-0-typeid-0-sort-pubtime?&page=$page")
    }

    override fun latestUpdatesParse(response: Response): MangasPage = searchMangaParse(response)

    override fun mangaDetailsRequest(manga: SManga): Request {
        return GET(manga.url)
    }

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)

        title = document.select("div.w980.mt10.clearfix div.intro_l div.title h1").text()
        thumbnail_url = document.select("div.w980.mt10.clearfix div.intro_l div.info_cover p.cover img.pic").attr("src").replace("img.comicun.com", "img.k886.net")
        author = document.select("div.w980.mt10.clearfix div.intro_l div.info p.w260").get(1).select("a[itemprop=author]").text()
        artist = "Tachiyomi:ZongerZY"
        status = if (document.select("div.w980.mt10.clearfix div.intro_l div.info p.w260").get(2).select("a").text().contains("完結")) 2 else 1
        genre = document.select("#classdh").text()
        description = document.select("div.introduction").text()
    }

    override fun chapterListRequest(manga: SManga): Request {
        return GET(manga.url)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        var chapterList = ArrayList<SChapter>()
        val body = response.body()!!.string()
        var elements = Jsoup.parseBodyFragment(body).select("#play_1 ul").select("li")
        for (element in elements) {
            chapterList.add(SChapter.create().apply {
                name = element.select("a").text().trim()
                url = element.select("a").attr("href").replace(baseUrl, baseIP[(0..2).random()])
            })
        }
        return chapterList
    }

    override fun pageListRequest(chapter: SChapter): Request {
        return GET(chapter.url)
    }

    override fun pageListParse(response: Response): List<Page> {
        val body = response.body()!!.string()
        val requestUrl = response.request().url().toString()
        var elements = Jsoup.parseBodyFragment(body).select("select[name=select1]").select("option")
        var arrList = ArrayList<Page>(elements.size)
        for (i in 0 until elements.size) {
            arrList.add(Page(i, "$requestUrl-p-${elements[i].attr("value")}"))
        }
        return arrList
    }

    override fun imageUrlRequest(page: Page): Request {
        return GET(page.url)
    }

    override fun imageUrlParse(response: Response): String {
        val body = response.body()!!.string()
        return Jsoup.parseBodyFragment(body).select("#ComicPic").attr("src").replace("img.comicun.com", "img.k886.net")
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        val document = Jsoup.parseBodyFragment(body)
        var mangasElements = document.select("div.dmList.clearfix ul li")
        var mangas = ArrayList<SManga>(mangasElements.size)
        for (mangaElement in mangasElements) {
            mangas.add(SManga.create().apply {
                title = mangaElement.select("dl dt a").text()
                thumbnail_url = mangaElement.select("p.fl.cover a.pic img").attr("src").replace("img.comicun.com", "img.k886.net")
                url = mangaElement.select("p.fl.cover a.pic").attr("href").replace(baseUrl, baseIP[(0..2).random()])
            })
        }
        return MangasPage(mangas, mangasElements.size == 30)
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return GET("${baseIP[(0..2).random()]}/search-index?entry=1&ie=gbk&q=$query&page=$page")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("-")
            return GET("${baseIP[(0..2).random()]}/index-html-status-$params?&page=$page")
        }
    }

    override fun getFilterList() = FilterList(
        ThemeFilter(),
        SortFilter(),
        FinishFilter()
    )

    private class FinishFilter : UriPartFilter("进度", arrayOf(
        Pair("全部", "status-0"),
        Pair("连载", "status-1"),
        Pair("完结", "status-2")

    ))

    private class ThemeFilter : UriPartFilter("题材", arrayOf(
        Pair("全部", "typeid-0"),
        Pair("萌系", "typeid-1"),
        Pair("搞笑", "typeid-2"),
        Pair("格鬥", "typeid-3"),
        Pair("科幻", "typeid-4"),
        Pair("劇情", "typeid-5"),
        Pair("偵探", "typeid-6"),
        Pair("競技", "typeid-7"),
        Pair("魔法", "typeid-8"),
        Pair("神鬼", "typeid-9"),
        Pair("校園", "typeid-10"),
        Pair("驚栗", "typeid-11"),
        Pair("廚藝", "typeid-12"),
        Pair("偽娘", "typeid-13"),
        Pair("圖片", "typeid-14"),
        Pair("冒險", "typeid-15"),
        Pair("小說", "typeid-16"),
        Pair("港漫", "typeid-17"),
        Pair("耽美", "typeid-18"),
        Pair("經典", "typeid-19"),
        Pair("歐美", "typeid-20"),
        Pair("日本", "typeid-21"),
        Pair("親情", "typeid-22"),
        Pair("修真", "typeid-25"),
        Pair("韓漫", "typeid-27"),
        Pair("真人", "typeid-28"),
        Pair("English", "typeid-30"),
        Pair("3D", "typeid-31")
    ))

    private class SortFilter : UriPartFilter("排序", arrayOf(
        Pair("点击量", "sort-hot"),
        Pair("增加时间", "sort-pubtime")
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
