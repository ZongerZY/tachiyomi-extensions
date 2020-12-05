package eu.kanade.tachiyomi.extension.zh.manhuadui

import android.util.Base64
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import java.net.HttpURLConnection
import java.net.URL
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Manhuadui : ParsedHttpSource() {

    override val name = "漫画堆"
    override val baseUrl = "https://www.manhuadai.com"
    override val lang = "zh"
    override val supportsLatest = true
    private val imageServer = arrayOf("https://manga.mipcdn.com/i/s/img01.eshanyao.com", "https://manga9.mlxsc.com", "https://res02.333dm.com")

    companion object {
        private const val DECRYPTION_KEY = "KA58ZAQ321oobbG8"
        private const val DECRYPTION_IV = "A1B2C3DEF1G321o8"
    }

    // 处理URL请求
    override val client: OkHttpClient = network.cloudflareClient.newBuilder().addInterceptor(
        fun(chain): Response {
            val url = chain.request().url().toString()
            val response = chain.proceed(chain.request())
            if (!url.contains("manhuadai.com", ignoreCase = true)) return response // 对非漫画图片连接直接放行
            val res = response.body()!!.byteStream().use {

                (URL(url).openConnection() as HttpURLConnection).inputStream.readBytes()
            }
            val mediaType = MediaType.parse("image/avif,image/webp,image/apng,image/*,*/*")
            val outputBytes = ResponseBody.create(mediaType, res)
            return response.newBuilder().body(outputBytes).build()
        }
    ).build()

    override fun popularMangaSelector() = "li.list-comic"
    override fun searchMangaSelector() = popularMangaSelector()
    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun chapterListSelector() = "ul[id^=chapter-list] > li a"

    override fun searchMangaNextPageSelector() = "li.next"
    override fun popularMangaNextPageSelector() = searchMangaNextPageSelector()
    override fun latestUpdatesNextPageSelector() = searchMangaNextPageSelector()

    override fun headersBuilder(): Headers.Builder = super.headersBuilder()
        .add("Referer", baseUrl)

    override fun popularMangaRequest(page: Int) = GET("$baseUrl/list_$page/", headers)
    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/update/$page/", headers)
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return if (query != "") {
            val url = HttpUrl.parse("$baseUrl/search/?keywords=$query")?.newBuilder()
            GET(url.toString(), headers)
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("-")
            val url = HttpUrl.parse("$baseUrl/list/$params/$page/")?.newBuilder()
            GET(url.toString(), headers)
        }
    }

    override fun popularMangaFromElement(element: Element) = mangaFromElement(element)
    override fun latestUpdatesFromElement(element: Element) = mangaFromElement(element)
    private fun mangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        element.select("a.comic_img").first().let {
            manga.setUrlWithoutDomain(it.attr("href"))
            manga.title = it.select("img").attr("alt").trim()
            manga.thumbnail_url = if (it.select("img").attr("src").trim().indexOf("http") == -1)
                "https:${it.select("img").attr("src").trim()}"
            else it.select("img").attr("src").trim()
        }
        manga.author = element.select("span.comic_list_det > p").first()?.text()?.substring(3)
        return manga
    }

    override fun searchMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        val els = element.select("a.image-link")
        if (els.size == 0) {
            element.select("li.list-comic").first().let {
                manga.setUrlWithoutDomain(it.select("a").attr("href"))
                manga.title = it.select("span").attr("title").trim()
                manga.thumbnail_url = it.select("a > img").attr("src").trim()
                manga.author = it.select("span > p").first().text().split("：")[1].trim()
            }
        } else {
            element.select("a.image-link").first().let {
                manga.setUrlWithoutDomain(it.attr("href"))
                manga.title = it.attr("title").trim()
                manga.thumbnail_url = it.select("img").attr("src").trim()
            }
            manga.author = element.select("p.auth").text().trim()
        }
        return manga
    }

    override fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()
        chapter.setUrlWithoutDomain(element.attr("href"))
        chapter.name = element.select("span:first-child").text().trim()
        return chapter
    }

    override fun mangaDetailsParse(document: Document): SManga {
        val manga = SManga.create()
        manga.description = document.select("p.comic_deCon_d").text().trim()
        manga.thumbnail_url = document.select("div.comic_i_img > img").attr("src")
        return manga
    }

    override fun chapterListRequest(manga: SManga) = GET(baseUrl.replace("www", "m") + manga.url)
    override fun chapterListParse(response: Response): List<SChapter> {
        return super.chapterListParse(response).asReversed()
    }

    // ref: https://jueyue.iteye.com/blog/1830792
    private fun decryptAES(value: String): String? {
        return try {
            val secretKey = SecretKeySpec(DECRYPTION_KEY.toByteArray(), "AES")
            val ivParams = IvParameterSpec(DECRYPTION_IV.toByteArray())
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams)

            val code = Base64.decode(value, Base64.NO_WRAP)

            String(cipher.doFinal(code))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private val chapterImagesRegex = Regex("""var chapterImages =\s*"(.*?)";""")
    private val imgPathRegex = Regex("""var chapterPath =\s*"(.*?)";""")
    private val imgCodeCleanupRegex = Regex("""[\[\]"\\]""")

    override fun pageListParse(document: Document): List<Page> {
        val html = document.html()
        val imgCodeStr = chapterImagesRegex.find(html)?.groups?.get(1)?.value ?: throw Exception("imgCodeStr not found")
        val imgCode = decryptAES(imgCodeStr)
            ?.replace(imgCodeCleanupRegex, "")
            ?.replace("%", "%25")
            ?: throw Exception("Decryption failed")
        val imgPath = imgPathRegex.find(document.html())?.groups?.get(1)?.value ?: throw Exception("imgPath not found")
        var list = imgCode.split(",").mapIndexed { i, imgStr ->
            Page(i, "", getImageUrl(imgStr, imgPath))
        }
        return list
    }

    private fun getImageUrl(imgStr: String, imgPath: String): String {
        if (imgStr.contains("images.dmzj.com")) {
            return "https://img01.eshanyao.com/showImage.php?url=$imgStr"
        } else if (imgStr.contains("himgsmall.dmzj.com")) {
            return "https://img01.eshanyao.com/showImage.php?url=$imgStr"
        } else if (imgStr.contains("manhua.qpic.cn")) {
            return "https://manga.mipcdn.com/i/s/$imgStr"
        } else if (imgStr.contains("mhimg.eshanyao.com")) {
            return "https://manga.mipcdn.com/i/s/$imgStr"
        } else if (imgStr.contains("dd.wstts.com")) {
            return "https://manga.mipcdn.com/i/s/$imgStr"
        } else if (imgStr.contains("http://") || imgStr.contains("https://") || imgStr.contains("ftp://")) {
            return imgStr
        } else {
            return "${imageServer[0]}/$imgPath$imgStr"
        }
    }

    override fun imageUrlParse(document: Document) = throw UnsupportedOperationException("Not used")

    private val phone = mapOf<Int, String>(
        1 to "Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",
        2 to "Mozilla/5.0 (Linux; Android 9; V1838A Build/PKQ1.190302.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.0.14 (Baidu; P1 9)",
        3 to "Mozilla/5.0 (Linux; Android 7.1.1; OPPO R11 Plus Build/NMF26X; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/63.0.3239.83 Mobile Safari/537.36 T7/11.16 SP-engine/2.12.0 baiduboxapp/11.16.2.10 (Baidu; P1 7.1.1)",
        4 to "Mozilla/5.0 (Linux; U; Android 5.1; zh-cn; OPPO R9tm Build/LMY47I) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/10.1 Mobile Safari/537.36",
        5 to "Mozilla/5.0 (Linux; Android 9; BND-AL00 Build/HONORBND-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/63.0.3239.83 Mobile Safari/537.36 T7/11.6 baiduboxapp/11.6.1.10 (Baidu; P1 9)",
        6 to "Mozilla/5.0 (Linux; Android 9; MI 6X Build/PKQ1.180904.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/6.2 TBS/045111 Mobile Safari/537.36 MMWEBID/5003 MicroMessenger/7.0.11.1600(0x27000B33) Process/tools NetType/WIFI Language/zh_CN ABI/arm64",
        7 to "Mozilla/5.0 (Linux; Android 9; CLT-TL00 Build/HUAWEICLT-TL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.0.14 (Baidu; P1 9)",
        8 to "Mozilla/5.0 (Linux; Android 9; FIG-TL10 Build/HUAWEIFIG-TL10; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.18 SP-engine/2.14.0 baiduboxapp/11.18.0.12 (Baidu; P1 9)",
        9 to "Mozilla/5.0 (Linux; U; Android 8.1.0; zh-cn; vivo X20 Build/OPM1.171019.011) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/9.9 Mobile Safari/537.36",
        10 to "Mozilla/5.0 (iPhone; CPU iPhone OS 12_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16B92 MicroMessenger/7.0.9(0x17000929) NetType/WIFI Language/zh_HK",
        11 to "Mozilla/5.0 (Linux; Android 9; MI 8 Lite Build/PKQ1.181007.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/67.0.3396.87 XWEB/1169 MMWEBSDK/191201 Mobile Safari/537.36 MMWEBID/673 MicroMessenger/7.0.11.1600(0x27000B33) Process/tools NetType/WIFI Language/zh_CN ABI/arm64",
        12 to "Mozilla/5.0 (Linux; Android 9; PCAM00 Build/PKQ1.190519.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.0.14 (Baidu; P1 9)",
        13 to "Mozilla/5.0 (iPhone; CPU iPhone OS 12_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/7.0.11(0x17000b21) NetType/WIFI Language/zh_CN",
        14 to "Mozilla/5.0 (Linux; U; Android 7.1.1; zh-cn; ONEPLUS A5000 Build/NMF26X) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/10.1 Mobile Safari/537.36",
        15 to "Mozilla/5.0 (Linux; Android 7.1.2; vivo X9i Build/N2G47H; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.0.14 (Baidu; P1 7.1.2)",
        16 to "Mozilla/5.0 (Linux; Android 9; SEA-AL10 Build/HUAWEISEA-AL1001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.0.14 (Baidu; P1 9)",
        17 to "Mozilla/5.0 (Linux; Android 9; MIX 2 Build/PKQ1.190118.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.0.14 (Baidu; P1 9)",
        18 to "Mozilla/5.0 (Linux; Android 10; V1914A Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.0.14 (Baidu; P1 10)",
        19 to "Mozilla/5.0 (Linux; U; Android 10; zh-cn; GM1910 Build/QKQ1.190716.003) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/10.1 Mobile Safari/537.36",
        20 to "Mozilla/5.0 (Linux; Android 10; GM1910 Build/QKQ1.190716.003; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/6.2 TBS/045120 Mobile Safari/537.36 V1_AND_SQ_8.2.7_1334_YYB_D QQ/8.2.7.4410 NetType/WIFI WebP/0.3.0 Pixel/1440 StatusBarHeight/128 SimpleUISwitch/0",
        21 to "Mozilla/5.0 (Linux; Android 9; V1838T Build/PKQ1.190302.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.0.14 (Baidu; P1 9)",
        22 to "Mozilla/5.0 (Linux; Android 9; PACT00 Build/PPR1.180610.011; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.0.14 (Baidu; P1 9)",
        23 to "Mozilla/5.0 (Linux; Android 8.1.0; Mi Note 3 Build/OPM1.171019.019; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.0.14 (Baidu; P1 8.1.0)",
        24 to "Mozilla/5.0 (Linux; Android 10; LYA-TL00 Build/HUAWEILYA-TL00L; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.2.2 (Baidu; P1 10)",
        25 to "Mozilla/5.0 (Linux; Android 9; Mi9 Pro 5G Build/PKQ1.190714.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.0.14 (Baidu; P1 9)",
        26 to "(Linux; U; Android 8.0.0; zh-CN; VTR-AL00 Build/HUAWEIVTR-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/11.8.0.960 Mobile Safari/537.36",
        27 to "Mozilla/5.0 (Linux; U; Android 8.0.0; zh-CN; VTR-AL00 Build/HUAWEIVTR-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/11.8.0.960 Mobile Safari/537.36",
        28 to "Mozilla/5.0 (Linux; Android 8.0.0; VTR-AL00 Build/HUAWEIVTR-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/6.2 TBS/045120 Mobile Safari/537.36 V1_AND_SQ_8.2.0_1296_YYB_D QQ/8.2.0.4310 NetType/WIFI WebP/0.3.0 Pixel/1080 StatusBarHeight/72 SimpleUISwitch/0",
        29 to "Mozilla/5.0 (Linux; Android 9; ART-AL00x Build/HUAWEIART-AL00x; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.0.14 (Baidu; P1 9)",
        30 to "Mozilla/5.0 (Linux; U; Android 9; zh-CN; MHA-TL00 Build/HUAWEIMHA-TL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/12.8.9.1069 Mobile Safari/537.36",
        31 to "Mozilla/5.0 (Linux; U; Android 9; zh-cn; MI 9 SE Build/PKQ1.181121.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/71.0.3578.141 Mobile Safari/537.36 XiaoMi/MiuiBrowser/11.8.12",
        32 to "Mozilla/5.0 (Linux; U; Android 10; zh-CN; SEA-AL10 Build/HUAWEISEA-AL10) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/12.5.6.1036 Mobile Safari/537.36",
        33 to "Mozilla/5.0 (Linux; U; Android 10; zh-cn; Redmi K20 Pro Build/QKQ1.190825.002) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/71.0.3578.141 Mobile Safari/537.36 XiaoMi/MiuiBrowser/11.8.12",
        34 to "Mozilla/5.0 (Linux; U; Android 9; zh-cn; Redmi Note 7 Build/PKQ1.180904.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/71.0.3578.141 Mobile Safari/537.36 XiaoMi/MiuiBrowser/11.5.12",
        35 to "Mozilla/5.0 (Linux; U; Android 9; zh-CN; COR-AL10 Build/HUAWEICOR-AL10) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/12.8.9.1069 Mobile Safari/537.36",
        36 to "Mozilla/5.0 (Linux; U; Android 9; zh-CN; PAR-AL00 Build/HUAWEIPAR-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/12.8.9.1069 Mobile Safari/537.36",
        37 to "Mozilla/5.0 (Linux; Android 9; vivo X21A Build/PKQ1.180819.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.20 SP-engine/2.16.0 baiduboxapp/11.20.0.14 (Baidu; P1 9)",
        38 to "Mozilla/5.0 (Linux; U; Android 10; zh-cn; MI 9 Build/QKQ1.190825.002) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/10.1 Mobile Safari/537.36",
        39 to "Mozilla/5.0 (Linux; U; Android 1; zh-CN; MI 9S Build/PKQ1.180904.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/12.8.6.1066 Mobile Safari/537.36",
        40 to "Mozilla/5.0 (Linux; U; Android 9; zh-cn; V1809A Build/PKQ1.181030.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/10.1 Mobile Safari/537.36"
    )

    override fun imageRequest(page: Page): Request {
        var myHeaders = Headers.of(mapOf(
            "User-Agent" to phone.get((1..40).random()),
            "Accept" to "image/avif,image/webp,image/apng,image/*,*/*;q=0.8",
            "Accept-Language" to "zh-CN,zh;q=0.9",
            "Accept-Encoding" to "gzip, deflate, br",
            "Cookie" to "__cfduid=d800f389be52eae48fcf6778f2eefa6851606671215",
            "Referer" to "https://www.manhuadai.com/",
            "Sec-Fetch-Dest" to "image",
            "Sec-Fetch-Mode" to "no-cors",
            "Sec-Fetch-Site" to "cross-site",
            "Connection" to "keep-alive",
            "Host" to "${page.imageUrl!!.substring(page.imageUrl!!.indexOf("://") + 3, page.imageUrl!!.indexOf(".com"))}.com"
        ))
        return GET(page.imageUrl!!, myHeaders)
    }

    override fun getFilterList() = FilterList(
        CategoryGroup(),
        RegionGroup(),
        GenreGroup(),
        ProgressGroup()
    )

    private class CategoryGroup : UriPartFilter("按类型", arrayOf(
        Pair("全部", ""),
        Pair("儿童漫画", "ertong"),
        Pair("少年漫画", "shaonian"),
        Pair("少女漫画", "shaonv"),
        Pair("青年漫画", "qingnian")
    ))

    private class ProgressGroup : UriPartFilter("按进度", arrayOf(
        Pair("全部", ""),
        Pair("已完结", "wanjie"),
        Pair("连载中", "lianzai")
    ))

    private class RegionGroup : UriPartFilter("按地区", arrayOf(
        Pair("全部", ""),
        Pair("日本", "riben"),
        Pair("大陆", "dalu"),
        Pair("香港", "hongkong"),
        Pair("台湾", "taiwan"),
        Pair("欧美", "oumei"),
        Pair("韩国", "hanguo"),
        Pair("其他", "qita")
    ))

    private class GenreGroup : UriPartFilter("按剧情", arrayOf(
        Pair("全部", ""),
        Pair("热血", "rexue"),
        Pair("冒险", "maoxian"),
        Pair("玄幻", "xuanhuan"),
        Pair("搞笑", "gaoxiao"),
        Pair("恋爱", "lianai"),
        Pair("宠物", "chongwu"),
        Pair("新作", "xinzuo")
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
