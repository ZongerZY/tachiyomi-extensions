package eu.kanade.tachiyomi.extension.zh.mkanmanhua886

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup

class Kanmanhua886 : HttpSource() {

    override val name = "看漫画K886"
    override val baseUrl = "https://m.k886.net"
    override val lang = "zh"
    override val supportsLatest = true

    private val htmlUrl = "https://www.k886.net"

    override fun popularMangaRequest(page: Int): Request {
        return GET("$htmlUrl/index-html-status-0-typeid-0-sort-hot?&page=$page")
    }

    override fun popularMangaParse(response: Response): MangasPage = searchMangaParse(response)

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$htmlUrl/index-html-status-0-typeid-0-sort-uptime?&page=$page")
    }

    override fun latestUpdatesParse(response: Response): MangasPage = searchMangaParse(response)

    override fun mangaDetailsRequest(manga: SManga): Request {
        return GET(manga.url)
    }

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)

        title = document.select("div.fl.show-center div.twobleft.center_nr div.box-bd.comic-earn div.box-hd h1").text()
        thumbnail_url = document.select("div.fl.show-center div.twobleft.center_nr div.box-bd.comic-earn dl.mh-detail dt a img").attr("src")
        author = document.select("div.fl.show-center div.twobleft.center_nr div.box-bd.comic-earn dl.mh-detail dd p").get(0).select("a[itemprop=author]").text()
        artist = "Tachiyomi:ZongerZY"
        status = if (document.select("div.fl.show-center div.twobleft.center_nr div.box-bd.comic-earn dl.mh-detail dd p").get(1).select("a").text().contains("完結")) 2 else 1
        genre = document.select("#classdh").text()
        description = document.select("div.fl.show-center div.twobleft.center_nr div.box-bd.comic-earn div.mh-introduce p").text()
    }

    override fun chapterListRequest(manga: SManga): Request {
        return GET(manga.url)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        var chapterList = ArrayList<SChapter>()
        val body = response.body()!!.string()
        var elements = Jsoup.parseBodyFragment(body).select("#oneCon2 ul.b1").select("li")
        for (element in elements) {
            chapterList.add(SChapter.create().apply {
                name = element.select("a").text().trim()
                url = element.select("a").attr("href")
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
        return Jsoup.parseBodyFragment(body).select("#ComicPic").attr("src")
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        val document = Jsoup.parseBodyFragment(body)
        var mangasElements = document.select("div.box.across.list div.box-bd ul.liemh.htmls.indliemh li")
        var mangas = ArrayList<SManga>(mangasElements.size)
        for (mangaElement in mangasElements) {
            mangas.add(SManga.create().apply {
                title = mangaElement.select("a div.tit").text()
                thumbnail_url = mangaElement.select("a img").attr("src")
                url = mangaElement.select("a").attr("href")
            })
        }
        return MangasPage(mangas, mangasElements.size == 30)
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return GET("$htmlUrl/search-index?searchType=1&q=$query&page=$page")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("-")
            return GET("$htmlUrl/index-html-$params?&page=$page")
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
        Pair("观看次数", "sort-hot"),
        Pair("更新时间", "sort-uptime"),
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
