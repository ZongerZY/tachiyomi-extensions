package eu.kanade.tachiyomi.extension.zh.acgemanga

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import java.util.regex.Pattern
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup

class Acgemanga : HttpSource() {

    override val name = "ACG漫画网"
    override val baseUrl = "https://www.acgemanga.com"
    override val lang = "zh"
    override val supportsLatest = true

    private val imageBaseUrl = "https://img.88comic.com"

    override val client: OkHttpClient = network.client.newBuilder()
        .build()

    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/index-$page.html")
    }

    override fun popularMangaParse(response: Response): MangasPage = searchMangaParse(response)

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl/hot/index-$page.html")
    }

    override fun latestUpdatesParse(response: Response): MangasPage = searchMangaParse(response)

    override fun mangaDetailsRequest(manga: SManga): Request {
        return GET(baseUrl + manga.url)
    }

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val body = response.body()!!.string()
        val requestUrl = response.request().url().toString()
        var document = Jsoup.parseBodyFragment(body)
        if (requestUrl.contains("$baseUrl/webtoon/")) {
            title = document.select("div.main div.webtoon.list dl.webtoon-info dd.webtoon-desc h1").text()
            thumbnail_url = document.select("div.main div.webtoon.list dl.webtoon-info dt.webtoon-thumb img").attr("src")
            author = "Tachiyomi"
            artist = "ZongerZY"
            genre = "网络漫画"
            status = 3
            description = document.select("div.main div.webtoon.list dl.webtoon-info dd.webtoon-desc div.webtoon-content").text()
        } else {
            title = document.select("h2.title").text()
            thumbnail_url = document.select("p.manga-picture img").attr("src")
            author = "Tachiyomi"
            artist = "ZongerZY"
            genre = document.select("div.acg-manga span a").text().replace(" ", ", ")
            status = 3
            description = document.select("h2.title").text()
        }
    }

    override fun chapterListRequest(manga: SManga): Request {
        return GET(baseUrl + manga.url)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        var chapterList = ArrayList<SChapter>()
        val requestUrl = response.request().url().toString()
        val body = response.body()!!.string()
        if (requestUrl.contains("$baseUrl/webtoon/")) {
            var elements = Jsoup.parseBodyFragment(body).select("dl.categorys dd.chapters")
            for (element in elements) {
                chapterList.add(SChapter.create().apply {
                    name = element.select("a.chapter-name h3").text().trim()
                    url = element.select("a.chapter-name").attr("href")
                })
            }
        } else {
            chapterList.add(SChapter.create().apply {
                name = "点击阅读 -> "
                url = requestUrl
            })
        }
        return chapterList.reversed()
    }

    override fun pageListRequest(chapter: SChapter): Request {
        return GET(if (chapter.url.contains("https://www.acgemanga.com")) chapter.url else "${baseUrl}${chapter.url}")
    }

    override fun pageListParse(response: Response): List<Page> {
        val body = response.body()!!.string()
        val requestUrl = response.request().url().toString()
        var elements = Jsoup.parseBodyFragment(body).select("#pages").select("a")
        var maxPage = 1
        for (element in elements) {
            if (isInteger(element.text()))
                if (maxPage < element.text().toInt())
                    maxPage = element.text().toInt()
        }
        var arrList = ArrayList<Page>()
        for (i in 1 until maxPage + 1) {
            arrList.add(Page(i, "${requestUrl.replace(".html","")}-$i.html"))
        }
        return arrList
    }

    private fun isInteger(str: String): Boolean {
        var pattern = Pattern.compile("^[-\\+]?[\\d]*$")
        return pattern.matcher(str).matches()
    }

    override fun imageUrlRequest(page: Page): Request {
        return GET(page.url)
    }

    override fun imageUrlParse(response: Response): String {
        val body = response.body()!!.string()
        return Jsoup.parseBodyFragment(body).select("p.manga-picture img").attr("src")
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return GET("$baseUrl/q/$query-$page.html")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            var topUrl = if (params.contains("/special/")) "" else "-"
            var endUrl = if (params.contains("/special/")) "" else ".html"

            return GET("${baseUrl}${params}${topUrl}${page}$endUrl")
        }
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        val requestUrl = response.request().url().toString()
        val document = Jsoup.parseBodyFragment(body)
        var mangas = ArrayList<SManga>()
        var elementsNum: Int
        if (requestUrl.contains("$baseUrl/webtoon/index")) {
            var mangasElements = document.select("#doujin_album li.webtoon")
            elementsNum = mangasElements.size
            for (mangaElement in mangasElements) {
                mangas.add(SManga.create().apply {
                    title = mangaElement.select("div.album_info h3 a").text()
                    thumbnail_url = mangaElement.select("a.thumb img").attr("src")
                    url = mangaElement.select("div.album_info h3 a").attr("href")
                })
            }
        } else {
            var mangasElements = document.select("#list li")
            elementsNum = mangasElements.size
            for (mangaElement in mangasElements) {
                if (mangaElement.select("span a").attr("href").equals("")) {
                    continue
                }
                mangas.add(SManga.create().apply {
                    title = mangaElement.select("span a").text()
                    thumbnail_url = mangaElement.select("a img").attr("src")
                    url = mangaElement.select("span a").attr("href")
                })
            }
        }
        var mangaNum = 24
        if (requestUrl.contains("$baseUrl/index"))
            mangaNum = 40
        else if (requestUrl.contains("$baseUrl/hot/index"))
            mangaNum = 36
        else if (requestUrl.contains("$baseUrl/h/index"))
            mangaNum = 28
        else if (requestUrl.contains("$baseUrl/hentai/index"))
            mangaNum = 28
        else if (requestUrl.contains("$baseUrl/hentai/index"))
            mangaNum = 28
        else if (requestUrl.contains("$baseUrl/webtoon/index"))
            mangaNum = 12
        else if (requestUrl.contains("$baseUrl/special/"))
            mangaNum = 16
        return MangasPage(mangas, elementsNum == mangaNum)
    }

    override fun getFilterList() = FilterList(
        ThemeFilter()
    )

    private class ThemeFilter : UriPartFilter("题材", arrayOf(
        Pair("最热", "/index"), // 8 * 5
        Pair("最新", "/hot/index"), // 9 * 4
        Pair("中文汉化", "/chinese"),
        Pair("日语漫画", "/japanese"),
        Pair("英文同人", "/english"),
        Pair("同人漫画", "/h/index"), // 7 * 4
        Pair("同人图", "/hentai/index"), // 7 * 4
        Pair("全彩漫画", "/tags/full-color"), // tags  6 * 4
        Pair("网漫", "/webtoon/index"), // 12 * 1

        Pair("合辑:海贼王", "/anime/one-piece"), // 6 * 4
        Pair("合辑:火影忍者", "/anime/naruto"),
        Pair("合辑:一脸嫌弃表情的妹子给你看胖次", "/anime/iya-na-kao-sare-nagara-opantsu-misete-moraitai"),
        Pair("合辑:狐妖小红娘", "/anime/huyaoxiaohongniang"),
        Pair("合辑:传说对决/王者荣耀", "/anime/arena-of-valor"),
        Pair("合辑:斗罗大陆", "/anime/douluodalu"),
        Pair("合辑:名侦探柯南", "/anime/detective-conan"),
        Pair("合辑:碧蓝航线", "/anime/azur-lane"),
        Pair("合辑:原创日本工番口番A漫(Original Hentai Manga)", "/anime/original"),
        Pair("合辑:勇者斗恶龙", "/anime/dragon-quest"),
        Pair("合辑:龙珠Z", "/anime/dragon-ball-z"),
        Pair("合辑:女皇之刃", "/anime/queens-blade"),

        Pair("合辑:魔法禁书目录", "/anime/toaru-majutsu-no-index"),
        Pair("合辑:科学超电磁炮", "/anime/toaru-kagaku-no-railgun"),
        Pair("合辑:我的女神", "/anime/ah-my-goddess"),
        Pair("合辑:魔法少女小圆", "/anime/puella-magi-madoka-magica"),
        Pair("合辑:生化危机", "/anime/resident-evil"),
        Pair("合辑:在地下城寻求邂逅是否搞错了什么", "/anime/dungeon-ni-deai-o-motomeru-no-wa-machigatteiru-darou-ka"),
        Pair("合辑:反叛的鲁路修", "/anime/code-geass"),
        Pair("合辑:食戟之灵", "/anime/shokugeki-no-soma"),
        Pair("合辑:监狱学园", "/anime/prison-school-kangoku-gakuen"),
        Pair("合辑:请问您今天要来点兔子吗", "/anime/gochuumon-wa-usagi-desu-ka-is-the-order-a-rabbit"),
        Pair("合辑:中二病也要谈恋爱", "/anime/chuunibyou-demo-koi-ga-shitai"),
        Pair("合辑:奥特曼", "/anime/ultraman"),

        Pair("合辑:女神异闻录PERSONA·Be Your True Mind", "/anime/persona"),
        Pair("合辑:约会大作战", "/anime/date-a-live"),
        Pair("合辑:魔法少女奈叶", "/anime/mahou-shoujo-lyrical-nanoha"),
        Pair("合辑:碧蓝幻想", "/anime/granblue-fantasy"),
        Pair("合辑:少女与战车", "/anime/girls-und-panzer"),
        Pair("合辑:少女前线", "/anime/girls-frontline"),
        Pair("合辑:机动战士高达", "/anime/gundam"),
        Pair("合辑:守望先锋", "/anime/overwatch"),
        Pair("合辑:魔兽世界(WOW)", "/anime/world-of-warcraft"),
        Pair("合辑:LOL/英雄联盟", "/anime/league-of-legends"),
        Pair("合辑:我的英雄学院", "/anime/my-hero-academia-boku-no-hero-academia"),
        Pair("合辑:一拳超人", "/anime/one-punch-man"),

        Pair("合辑:美少女战士", "/anime/sailor-moon"),
        Pair("合辑:死神", "/anime/bleach"),
        Pair("合辑:妖精的尾巴", "/anime/fairy-tail"),
        Pair("合辑:街头霸王", "/anime/street-fighter"),
        Pair("合辑:我的英雄学院", "/anime/my-hero-academia"),
        Pair("合辑:拳皇", "/anime/king-of-fighters"),
        Pair("合辑:生死格斗/死或生", "/anime/dead-or-alive"),
        Pair("合辑:进击的巨人", "/anime/shingeki-no-kyojin"),
        Pair("合辑:Love Live! School idol project", "/anime/love-live"),
        Pair("合辑:刀剑神域", "/anime/sword-art-online"),
        Pair("合辑:偶像大师", "/anime/the-idolmaster"),
        Pair("合辑:口袋妖怪/宠物小精灵/精灵宝可梦", "/anime/pokemon"),

        Pair("合辑:新世纪福音战士", "/anime/neon-genesis-evangelion"),
        Pair("合辑:舰队收藏(舰队Collection舰娘系列)", "/anime/kantai-collection"),
        Pair("合辑:东方project", "/anime/touhou-project"),
        Pair("合辑:命运之夜（Fate/stay night）", "/anime/fate-stay-night"),
        Pair("合辑:Fate/Grand Order 命运冠位指定", "/anime/fate-grand-order"),
        Pair("合辑:瑞克与莫蒂", "/anime/rick-and-morty"),
        // 不加 .html
        Pair("专题:海贼王h同人漫 one piece hentai", "/special/4/"), // 4 * 4
        Pair("专题:火影忍者h同人本子NARUTO HENTAI", "/special/6/"),
        Pair("专题:守望先锋h同人本子dva黑寡妇福利图", "/special/7/"),
        Pair("专题:yin三国梦想全集", "/special/17/"),
        Pair("专题:英雄联盟h女英雄同人lol本子league of hentai", "/special/8/"),
        Pair("专题:初音未来h同人本子福利图", "/special/9/"),
        Pair("专题:命运之夜h同人saber本子远坂凛h福利图fatestay night hentai", "/special/10/"),
        Pair("专题:街机女角色不知火舞和春丽被虐", "/special/12/"),
        Pair("专题:舰娘本子合集舰队collection", "/special/11/"),
        Pair("专题:大尺度无下限无节操cosplay美女图片", "/special/13/"),
        Pair("专题:邪恶触手怪漫画", "/special/19/"),
        Pair("专题:魔兽世界h同人WOW Hentai", "/special/18/"),
        Pair("专题:偶像大师", "/special/20/"),

        Pair("标签:不知火舞", "/q/%E4%B8%8D%E7%9F%A5%E7%81%AB%E8%88%9E"), // 6 * 4
        Pair("标签:足交", "/q/%E8%B6%B3%E4%BA%A4"),
        Pair("标签:巨乳", "/tags/big-breasts"),
        Pair("标签:YAOI/耽美/男同", "/tags/yaoi"),
        Pair("标签:fate", "/q/fate"),
        Pair("标签:柯南", "/q/%E6%9F%AF%E5%8D%97"),
        Pair("标签:奴役囚禁捆绑", "/tags/bondage"),
        Pair("标签:Doujin(同人)", "/tags/doujin"),
        Pair("标签:寝取偷情(NTR)", "/tags/netorare"),
        Pair("标签:斗罗大陆", "/q/%E6%96%97%E7%BD%97%E5%A4%A7%E9%99%86"),
        Pair("标签:鬼灭之刃", "/q/%E9%AC%BC%E7%81%AD%E4%B9%8B%E5%88%83"),
        Pair("标签:全彩", "/q/%E5%85%A8%E5%BD%A9"),
        Pair("标签:邪恶触手", "/tags/tentacles"),
        Pair("标签:神奇宝贝", "/q/%E7%A5%9E%E5%A5%87%E5%AE%9D%E8%B4%9D"),
        Pair("标签:扶她", "/tags/futanari"),
        Pair("标签:3d", "/q/3d"),
        Pair("标签:我的英雄学院", "/q/%E6%88%91%E7%9A%84%E8%8B%B1%E9%9B%84%E5%AD%A6%E9%99%A2"),
        Pair("标签:无码无圣光", "/tags/uncensored"),
        Pair("标签:中小学生", "/tags/schoolboy"),
        Pair("标签:丝袜", "/tags/stockings"),
        Pair("标签:刀剑神域", "/q/%E5%88%80%E5%89%91%E7%A5%9E%E5%9F%9F"),
        Pair("标签:萝莉", "/tags/lolicon"),
        Pair("标签:乱伦", "/tags/incest"),
        Pair("标签:毛利兰", "/q/%E6%AF%9B%E5%88%A9%E5%85%B0"),
        Pair("标签:三国", "/q/%E4%B8%89%E5%9B%BD"),
        Pair("标签:人妻熟女", "/tags/milf"),
        Pair("标签:王者荣耀", "/q/%E7%8E%8B%E8%80%85%E8%8D%A3%E8%80%80"),
        Pair("标签:人兽奸", "/tags/bestiality"),
        Pair("标签:海贼王", "/q/%E6%B5%B7%E8%B4%BC%E7%8E%8B"),
        Pair("标签:约会大作战", "/q/%E7%BA%A6%E4%BC%9A%E5%A4%A7%E4%BD%9C%E6%88%98"),
        Pair("标签:无码", "/q/%E6%97%A0%E7%A0%81"),
        Pair("标签:母子", "/q/%E6%AF%8D%E5%AD%90"),
        Pair("标签:哆啦A梦", "/q/%E5%93%86%E5%95%A6A%E6%A2%A6"),
        Pair("标签:碧蓝航线", "/q/%E7%A2%A7%E8%93%9D%E8%88%AA%E7%BA%BF"),
        Pair("标签:一拳超人", "/q/%E4%B8%80%E6%8B%B3%E8%B6%85%E4%BA%BA"),
        Pair("标签:火影忍者", "/q/%E7%81%AB%E5%BD%B1%E5%BF%8D%E8%80%85"),
        Pair("标签:学生", "/tags/schoolboy"),
        Pair("标签:中文", "/q/%E4%B8%AD%E6%96%87"),
        Pair("标签:扶他/扶她", "/tags/futanari"),
        Pair("标签:龙珠", "/q/%E9%BE%99%E7%8F%A0"),
        Pair("标签:触手", "/q/%E8%A7%A6%E6%89%8B"),
        Pair("标签:英雄联盟", "/q/%E8%8B%B1%E9%9B%84%E8%81%94%E7%9B%9F"),
        Pair("标签:强奸", "/tags/rape"),
        Pair("标签:妖精的尾巴", "/q/%E5%A6%96%E7%B2%BE%E7%9A%84%E5%B0%BE%E5%B7%B4"),
        Pair("标签:精神控制/洗脑催眠", "/tags/mind-control"),
        Pair("标签:囚禁捆绑", "/tags/bondage"),
        Pair("标签:正太控", "/tags/shotacon"),
        Pair("标签:守望先锋", "/q/%E5%AE%88%E6%9C%9B%E5%85%88%E9%94%8B")
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
