package eu.kanade.tachiyomi.extension.zh.xianmanwang

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
import org.json.JSONArray
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class Xianmanwang : HttpSource() {

    override val name = "仙漫网"
    override val baseUrl = ""
    override val lang = "zh"
    override val supportsLatest = true

    private val htmlUrl = "https://www.xianman123.com"

    private fun myGet(url: String) = GET(url, headers)

    override fun popularMangaRequest(page: Int): Request {
        return myGet("https://www.xianman123.com/f-1-0-0-0-0-1-$page.html")
    }

    override fun popularMangaParse(response: Response): MangasPage = searchMangaParse(response)

    override fun latestUpdatesRequest(page: Int): Request {
        return myGet("https://www.xianman123.com/f-1-0-0-0-0-0-$page.html")
    }

    override fun latestUpdatesParse(response: Response): MangasPage = searchMangaParse(response)

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)

        title = document.select("section.banner_detail div.info h1").text()
        thumbnail_url = document.select("section.banner_detail div.banner_border_bg img.banner_detail_bg").attr("src").split("/suolue")[0]
        author = document.select("section.banner_detail div.info p.subtitle a").text()
        artist = "Tachiyomi:ZongerZY"
        genre = getGenre(document.select("section.banner_detail div.info p.tip span.block.ticai a"))
        status = if (document.select("section.banner_detail div.info p.tip span.block span").text().contains("完结")) 2 else if (document.select("section.banner_detail div.info p.tip span.block span").text().contains("连载")) 1 else 0
        description = document.select("section.banner_detail div.info p.content").text()
    }

    private fun getGenre(elements: Elements): String {
        var genre = ""
        for (i in 0 until elements.size) {
            if (i == elements.size - 1)
                genre = genre + elements.get(i).select("a").text()
            else
                genre = genre + elements.get(i).select("a").text() + ", "
        }
        return genre
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val body = response.body()!!.string()
        var chapterList = ArrayList<SChapter>()

        var elements = Jsoup.parseBodyFragment(body).select("#detail-list-select-1").select("li")

        for (element in elements) {
            chapterList.add(SChapter.create().apply {
                name = element.select("a").text()
                url = "${htmlUrl}${element.select("a").attr("href")}"
            })
        }

        return chapterList
    }

    override fun pageListParse(response: Response): List<Page> {
        val body = response.body()!!.string()
        var imageBaseUrl = try {
            Regex("""\'(.*?)\'""").find(Regex("""var\s+imgDomain\s+\=\s+\'((https|http|ftp|rtsp|mms)?:\/\/)([^\s]+)(\/?)\'""").find(body)!!.value)!!.value.replace("\'", "")
        } catch (e: Exception) {
            "https://res.xiaoqinre.com/"
        }

        if (imageBaseUrl == "") {
            imageBaseUrl = "https://res.xiaoqinre.com/"
        }
        var imageUrlStr = Regex("""\[(.*?)\]""").find(Regex("""var\s+picdata\s+\=\s+\[(.*?)\]""").find(body)!!.value)!!.value

        var imageUrlList = JSONArray(imageUrlStr)

        var arrList = ArrayList<Page>()
        for (i in 0 until imageUrlList.length()) {
            arrList.add(Page(i, "", imageBaseUrl + imageUrlList.getString(i)))
        }
        return arrList
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException("This method should not be called!")
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        val document = Jsoup.parseBodyFragment(body)
        var mangasElements = document.select("div.box-body ul.mh-list.col7 li")

        var mangas = ArrayList<SManga>()
        for (mangaElement in mangasElements) {
            mangas.add(SManga.create().apply {
                title = mangaElement.select("div.mh-item-detali h2.title a").text()
                thumbnail_url = mangaElement.select("a p.mh-cover").attr("style").split("url(")[1].split(")")[0]
                url = "${htmlUrl}${mangaElement.select("div.mh-item-detali h2.title a").attr("href")}"
            })
        }
        return MangasPage(mangas, mangasElements.size == 35)
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return myGet("$htmlUrl/index/index/search.html?keyboard=$query&page=$page")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("-")
            return myGet("$htmlUrl/f-1-$params-$page.html")
        }
    }

    override fun getFilterList() = FilterList(
        ThemeFilter(),
        FinishFilter(),
        AudienceFilter(),
        CopyrightFilter(),
        SortFilter()
    )

    private class ThemeFilter : UriPartFilter("题材", arrayOf(
        Pair("全部", "0"),
        Pair("热血", "1"),
        Pair("恋爱", "2"),
        Pair("校园", "3"),
        Pair("百合", "4"),
        Pair("耽美", "5"),
        Pair("冒险", "6"),
        Pair("后宫", "7"),
        Pair("仙侠", "8"),
        Pair("武侠", "9"),
        Pair("悬疑", "10"),
        Pair("推理", "11"),
        Pair("搞笑", "12"),
        Pair("奇幻", "13"),
        Pair("恐怖", "14"),
        Pair("玄幻", "15"),
        Pair("古风", "16"),
        Pair("萌系", "17"),
        Pair("日常", "18"),
        Pair("治愈", "19"),
        Pair("烧脑", "20"),
        Pair("邪恶", "21"),
        Pair("都市", "22"),
        Pair("竞技", "23"),
        Pair("欢乐向", "24"),
        Pair("其它", "25")
    ))

    private class AudienceFilter : UriPartFilter("地区", arrayOf(
        Pair("全部", "0"),
        Pair("国产", "1"),
        Pair("日本", "2"),
        Pair("欧美", "3"),
        Pair("港台", "4"),
        Pair("韩国", "5")

    ))

    private class FinishFilter : UriPartFilter("进度", arrayOf(
        Pair("全部", "0"),
        Pair("连载", "1"),
        Pair("完结", "2")
    ))

    private class CopyrightFilter : UriPartFilter("字母", arrayOf(
        Pair("全部", "0"),
        Pair("A", "A"),
        Pair("B", "B"),
        Pair("C", "C"),
        Pair("D", "D"),
        Pair("E", "E"),
        Pair("F", "F"),
        Pair("G", "G"),
        Pair("H", "H"),
        Pair("I", "I"),
        Pair("J", "J"),
        Pair("K", "K"),
        Pair("L", "L"),
        Pair("M", "M"),
        Pair("N", "N"),
        Pair("O", "O"),
        Pair("P", "P"),
        Pair("Q", "Q"),
        Pair("R", "R"),
        Pair("S", "S"),
        Pair("T", "T"),
        Pair("U", "U"),
        Pair("V", "V"),
        Pair("W", "W"),
        Pair("X", "X"),
        Pair("Y", "Y"),
        Pair("Z", "Z")
    ))

    private class SortFilter : UriPartFilter("排序", arrayOf(
        Pair("更新", "0"),
        Pair("热门", "1"),
        Pair("新品", "2")
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
