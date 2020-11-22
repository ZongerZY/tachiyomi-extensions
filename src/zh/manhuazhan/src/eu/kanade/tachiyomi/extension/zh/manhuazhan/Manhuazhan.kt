package eu.kanade.tachiyomi.extension.zh.manhuazhan

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

class Manhuazhan : HttpSource() {
    // 漫画栈 用HTML网页爬取

    override val name = "漫画栈"
    override val baseUrl = "https://www.mkzhan.com"
    override val lang = "zh"
    override val supportsLatest = true

    private fun myGet(url: String) = GET(url, headers)

    override fun popularMangaRequest(page: Int): Request {
        return myGet("https://www.mkzhan.com/top/popularity/")
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        val document = Jsoup.parseBodyFragment(body)
        var mangasElements = document.select("div.top-list__box-item")
        var mangas = ArrayList<SManga>(mangasElements.size)
        for (mangaElement in mangasElements) {
            mangas.add(SManga.create().apply {
                title = mangaElement.select("p.comic__title a").text()
                thumbnail_url = mangaElement.select("a.cover img.lazy").attr("data-src")
                url = mangaElement.select("a.cover").attr("href")
            })
        }
        return MangasPage(mangas, false)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        return myGet("https://www.mkzhan.com/update/")
    }

    override fun latestUpdatesParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        val document = Jsoup.parseBodyFragment(body)
        var mangasElements = document.select("div.common-comic-item")
        var mangas = ArrayList<SManga>(mangasElements.size)
        for (mangaElement in mangasElements) {
            mangas.add(SManga.create().apply {
                title = mangaElement.select("p.comic__title a").text()
                thumbnail_url = mangaElement.select("a.cover img.lazy").attr("data-src")
                url = mangaElement.select("a.cover").attr("href")
            })
        }
        return MangasPage(mangas, false)
    }

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)

        title = document.select("p.comic-title.j-comic-title").text()
        thumbnail_url = document.select("div.de-info__cover img.lazy").attr("data-src")
        author = document.select("div.comic-author span.name a").text()
        status = if (document.select("div.de-chapter__title span").get(0).text().equals("完结")) 2 else 1
        description = document.select("p.intro-total").text()
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        var chapterList = ArrayList<SChapter>()
        val body = response.body()!!.string()
        var elements = Jsoup.parseBodyFragment(body).select("div.chapter__list ul.chapter__list-box").select("li.j-chapter-item")
        for (element in elements) {
            chapterList.add(SChapter.create().apply {
                name = element.select("a.j-chapter-link").text().trim()
                url = element.select("a.j-chapter-link").attr("data-hreflink")
            })
        }
        return chapterList
    }

    override fun pageListParse(response: Response): List<Page> {
        val body = response.body()!!.string()
        var elements = Jsoup.parseBodyFragment(body).select("div.rd-article-wr").select("div.rd-article__pic img.lazy-read")
        var arrList = ArrayList<Page>(elements.size)
        for (i in 0 until elements.size) {
            arrList.add(Page(i, "", elements.get(i).attr("data-src")))
        }
        return arrList
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException("This method should not be called!")
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        val document = Jsoup.parseBodyFragment(body)
        var mangasElements = document.select("div.common-comic-item")
        var mangas = ArrayList<SManga>(mangasElements.size)
        for (mangaElement in mangasElements) {
            mangas.add(SManga.create().apply {
                title = mangaElement.select("p.comic__title a").text()
                thumbnail_url = mangaElement.select("a.cover img.lazy").attr("data-src")
                url = mangaElement.select("a.cover").attr("href")
            })
        }
        return MangasPage(mangas, mangasElements.size == 30)
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return myGet("$baseUrl/search/?keyword=$query&page=$page")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            return myGet("$baseUrl/category/?$params&page=$page")
        }
    }

    override fun getFilterList() = FilterList(
        ThemeFilter(),
        FinishFilter(),
        AudienceFilter(),
        CopyrightFilter(),
        MoneyFilter()
    )

    private class ThemeFilter : UriPartFilter("题材", arrayOf(
        Pair("全部", ""),
        Pair("霸总", "&theme_id=1"),
        Pair("修真", "&theme_id=2"),
        Pair("恋爱", "&theme_id=3"),
        Pair("校园", "&theme_id=4"),
        Pair("冒险", "&theme_id=5"),
        Pair("搞笑", "&theme_id=6"),
        Pair("生活", "&theme_id=7"),
        Pair("热血", "&theme_id=8"),
        Pair("架空", "&theme_id=9"),
        Pair("后宫", "&theme_id=10"),
        Pair("玄幻", "&theme_id=12"),
        Pair("悬疑", "&theme_id=13"),
        Pair("恐怖", "&theme_id=14"),
        Pair("灵异", "&theme_id=15"),
        Pair("动作", "&theme_id=16"),
        Pair("科幻", "&theme_id=17"),
        Pair("战争", "&theme_id=18"),
        Pair("古风", "&theme_id=19"),
        Pair("穿越", "&theme_id=20"),
        Pair("竞技", "&theme_id=21"),
        Pair("励志", "&theme_id=23"),
        Pair("同人", "&theme_id=24"),
        Pair("真人", "&theme_id=26")
    ))

    private class FinishFilter : UriPartFilter("进度", arrayOf(
        Pair("全部", ""),
        Pair("连载", "&finish=1"),
        Pair("完结", "&finish=2")

    ))

    private class AudienceFilter : UriPartFilter("受众", arrayOf(
        Pair("全部", ""),
        Pair("少年", "&audience=1"),
        Pair("少女", "&audience=2"),
        Pair("青年", "&audience=3"),
        Pair("少儿", "&audience=4")

    ))

    private class CopyrightFilter : UriPartFilter("版权", arrayOf(
        Pair("全部", ""),
        Pair("独家", "&copyright=1"),
        Pair("合作", "&copyright=2")

    ))

    private class MoneyFilter : UriPartFilter("资费", arrayOf(
        Pair("全部", ""),
        Pair("免费", "&is_free=1"),
        Pair("付费", "&is_fee=1"),
        Pair("VIP", "&is_vip=1")
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
