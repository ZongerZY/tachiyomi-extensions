package eu.kanade.tachiyomi.extension.zh.qinqinmanhua

import android.util.Base64
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import java.util.Collections
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup

class Qinqinmanhua : HttpSource() {

    override val name = "亲亲漫画"
    override val baseUrl = ""
    override val lang = "zh"
    override val supportsLatest = true

    companion object {
        private const val DECRYPTION_KEY = "cxNB23W8xzKJV26O"
        private const val DECRYPTION_IV = "opb4x7z21vg1f3gI"
    }

    private var requestHeaders = Headers.of(mapOf(
        "User-Agent" to "Mozilla/5.0 (X11; Linux x86_64; MuMu 6.0.1 Build/V417IR) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.34 Safari/534.24",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Accept-Language" to "zh-CN,en-US;q=0.8",
        "X-Requested-With" to "com.android.browser",
        "Upgrade-Insecure-Requests" to "1",
        "Connection" to "Keep-Alive",
        "Host" to "m.acgcd.com"
    ))

    private var searchRequestHeaders = Headers.of(mapOf(
        "Connection" to "close"
    ))

    private fun myGet(url: String) = GET(url, requestHeaders)

    override fun popularMangaRequest(page: Int): Request {
        return myGet("https://m.acgcd.com/list/click/?page=$page")
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val body = response.body()!!.string()
        val document = Jsoup.parseBodyFragment(body)
        var mangasElements = document.select("#comic-items").select("li.list-comic")
        var mangas = ArrayList<SManga>(mangasElements.size)
        for (mangaElement in mangasElements) {
            mangas.add(SManga.create().apply {
                title = mangaElement.select("a.txtA").text().trim()
                thumbnail_url = "http://mikimiki.dailytu.com:45678/" + mangaElement.select("a.ImgA img").attr("src").split(".com/")[1]
                url = mangaElement.select("a.ImgA").attr("href")
            })
        }
        return MangasPage(mangas, true)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        return myGet("https://m.acgcd.com/list/update/?page=$page")
    }

    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)

        title = document.select("div.BarTit").text()
        thumbnail_url = "http://mikimiki.dailytu.com:45678/" + document.select("div.pic img").attr("src").split(".com/")[1]
        author = document.select("span.icon01").get(0).parent().text()
        status = if (document.select("span.icon02~a:last-child").text().contains("已完结")) 2 else 1
        genre = getGenre(document.select("span.icon02~a").toString())
        description = document.select("#full-des").text()
    }

    private fun getGenre(str: String): String {
        var genre = ""
        var document = Jsoup.parseBodyFragment(str)
        var elements = document.select("a")
        for (i in 0 until elements.size) {
            if (i == elements.size - 1)
                genre = genre + elements.get(i).text()
            else
                genre = genre + elements.get(i).text() + ", "
        }
        return genre
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        var chapterList = ArrayList<SChapter>()
        val body = response.body()!!.string()
        var elements = Jsoup.parseBodyFragment(body).select("div.chapter-body ul.Drama").select("li")
        for (element in elements) {
            chapterList.add(SChapter.create().apply {
                name = element.select("a span").text().trim()
                url = "https://m.acgcd.com" + element.select("a").attr("href")
            })
        }
        Collections.reverse(chapterList)
        return chapterList
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

    override fun pageListParse(response: Response): List<Page> {
        val html = response.body()!!.string()
        val imgCodeStr = chapterImagesRegex.find(html)?.groups?.get(1)?.value ?: throw Exception("imgCodeStr not found")
        val imgCode = decryptAES(imgCodeStr)
            ?.replace(imgCodeCleanupRegex, "")
            ?.replace("%", "%25")
            ?: throw Exception("Decryption failed")
        val imgPath = imgPathRegex.find(html)?.groups?.get(1)?.value ?: throw Exception("imgPath not found")
        return imgCode.split(",").mapIndexed { i, imgStr ->
            Page(i, "", "http://mikimiki.dailytu.com:45678/$imgPath$imgStr")
        }
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException("This method should not be called!")
    }

    override fun searchMangaParse(response: Response): MangasPage {
        var urls = response.request().url().toString()
        if (urls.contains("keywords")) {
            val body = response.body()!!.string()
            val document = Jsoup.parseBodyFragment(body)
            var mangasElements = document.select("ul.update_con").select("li.list-comic")
            var mangas = ArrayList<SManga>(mangasElements.size)
            for (mangaElement in mangasElements) {
                mangas.add(SManga.create().apply {
                    title = mangaElement.select("p a").text()
                    thumbnail_url = "http://mikimiki.dailytu.com:45678/" + mangaElement.select("a.image-link img").attr("src").split(".com/")[1]
                    url = mangaElement.select("a.image-link").attr("href").replace("www.", "m.")
                })
            }
            return MangasPage(mangas, mangasElements.size == 36)
        } else {
            return popularMangaParse(response)
        }
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return GET("http://www.acgcd.com/search/?keywords=$query&sort=click&page=$page", searchRequestHeaders)
        } else {
            var params1 = filters.map {
                if (it is ThemeFilter)
                    it.toUriPart()
                else if (it is FinishFilter)
                    it.toUriPart()
                else if (it is AudienceFilter)
                    it.toUriPart()
                else if (it is CopyrightFilter)
                    it.toUriPart()
                else ""
            }.filter { it != "" }.joinToString("-")
            var params2 = filters.map {
                if (it is MoneyFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")

            val params = "$params1$params2"
            var url = "https://m.acgcd.com/list/$params?page=$page"
            if (url.contains("list//"))
                return myGet(remove(url, "", url.indexOf("list//") + 5))
            else
                return myGet(url)
        }
    }

    // 移除指定位置字符
    private fun remove(str: String, str2: String, strPlace: Int): String {
        var s = str
        var string = str2
        var i = strPlace
        if (i == 1) {
            var j: Int = s.indexOf(string)
            s = s.substring(0, j) + s.substring(j + 1)
            return s
        } else {
            var j: Int = s.indexOf(string)
            i--
            return s.substring(0, j + 1) + remove(s.substring(j + 1), string, i)
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
        Pair("冒险", "maoxian"),
        Pair("奇幻", "qihuan"),
        Pair("爱情", "aiqing"),
        Pair("音乐舞蹈", "yinyuewudao"),
        Pair("校园", "xiaoyuan"),
        Pair("竞技", "jingji"),
        Pair("百合", "baihe"),
        Pair("治愈", "zhiyu"),
        Pair("搞笑", "gaoxiao"),
        Pair("格斗", "gedou"),
        Pair("颜艺", "yanyi"),
        Pair("欢乐向", "huanlexiang"),
        Pair("轻小说", "qingxiaoshuo"),
        Pair("耽美", "danmei"),
        Pair("热血", "rexue"),
        Pair("生活", "shenghuo"),
        Pair("历史", "lishi"),
        Pair("科幻", "kehuan"),
        Pair("悬疑", "xuanyi"),
        Pair("美食", "meishi"),
        Pair("仙侠", "xianxia"),
        Pair("战争", "zhanzheng"),
        Pair("邪恶", "xiee"),
        Pair("本子", "benzi"),
        Pair("后宫", "hougong"),
        Pair("神鬼", "shengui"),
        Pair("杂志", "zazhi"),
        Pair("伪娘", "weiniang"),
        Pair("恐怖", "kongbu"),
        Pair("魔幻", "mohuan"),
        Pair("侦探", "zhentan"),
        Pair("推理", "tuili"),
        Pair("魔法", "mofa"),
        Pair("武侠", "wuxia"),
        Pair("舞蹈", "wudao"),
        Pair("腐女", "funu"),
        Pair("宅男", "zhainan"),
        Pair("励志", "lizhi"),
        Pair("萌系", "mengji"),
        Pair("体育", "tiyu"),
        Pair("音乐", "yinyue"),
        Pair("职场", "zhichang"),
        Pair("四格", "sige"),
        Pair("社会", "shehui"),
        Pair("黑道", "heidao"),
        Pair("宅系", "zhaiji"),
        Pair("萌", "meng"),
        Pair("高清单行", "gaoqingdanxing"),
        Pair("性转换", "xingzhuanhuan"),
        Pair("节操", "jiecao"),
        Pair("耽美BL", "danmeiBL"),
        Pair("机战", "jizhan"),
        Pair("东方", "dongfang"),
        Pair("恋爱", "lianai"),
        Pair("全彩", "quancai"),
        Pair("美女漫画", "meinumanhua"),
        Pair("复仇", "fuchou"),
        Pair("青年", "qingnian2"),
        Pair("足控", "jukong"),
        Pair("穿越", "chuanyue"),
        Pair("虐心", "nuexin"),
        Pair("其他", "qita2"),
        Pair("惊险", "jingxian"),
        Pair("本子", "benzi2"),
        Pair("综合其它", "zongheqita"),
        Pair("都市", "dushi"),
        Pair("爆笑", "baoxiao"),
        Pair("玄幻", "xuanhuan"),
        Pair("彩虹", "caihong"),
        Pair("纯爱", "chunai"),
        Pair("唯美", "weimei"),
        Pair("浪漫", "langman"),
        Pair("动作", "dongzuo"),
        Pair("青春", "qingchun"),
        Pair("古风", "gufeng"),
        Pair("恶搞", "egao"),
        Pair("运动", "yundong"),
        Pair("脑洞", "naodong"),
        Pair("僵尸", "jiangshi"),
        Pair("血腥", "xuexing"),
        Pair("震撼", "zhenhan"),
        Pair("明星", "mingxing"),
        Pair("其它", "qita"),
        Pair("暗黑", "anhei"),
        Pair("机甲", "jijia"),
        Pair("权谋", "quanmou"),
        Pair("同人", "tongren"),
        Pair("灵异", "lingyi"),
        Pair("宫斗", "gongdou"),
        Pair("栏目", "lanmu"),
        Pair("日常", "richang"),
        Pair("感动", "gandong"),
        Pair("惊奇", "jingqi"),
        Pair("重口味", "zhongkouwei"),
        Pair("逗比", "doubi"),
        Pair("异能", "yineng"),
        Pair("丧尸", "sangshi"),
        Pair("宠物", "chongwu"),
        Pair("宫廷", "gongting"),
        Pair("轻松", "qingsong"),
        Pair("架空", "jiakong"),
        Pair("战斗", "zhandou"),
        Pair("未来", "weilai"),
        Pair("科技", "keji"),
        Pair("温馨", "wenxin"),
        Pair("逆袭", "nixi"),
        Pair("游戏", "youxi"),
        Pair("内涵", "neihan"),
        Pair("末世", "moshi"),
        Pair("神话", "shenhua"),
        Pair("装逼", "zhuangbi"),
        Pair("豪门", "haomen"),
        Pair("异世界", "yishijie"),
        Pair("性转", "xingzhuan"),
        Pair("乡村", "xiangcun"),
        Pair("正剧", "zhengju"),
        Pair("伦理", "lunli"),
        Pair("家庭", "jiating"),
        Pair("段子", "duanzi"),
        Pair("婚姻", "hunyin"),
        Pair("主仆", "zhupu"),
        Pair("烧脑", "shaonao"),
        Pair("史诗", "shishi"),
        Pair("致郁", "zhiyu2"),
        Pair("召唤兽", "zhaohuanshou"),
        Pair("娱乐圈", "yulequan"),
        Pair("爽流", "shuangliu"),
        Pair("纠结", "jiujie"),
        Pair("屌丝", "diaosi"),
        Pair("商战", "shangzhan"),
        Pair("蔷薇", "qiangwei"),
        Pair("真人", "zhenren"),
        Pair("惊悚", "jingsong"),
        Pair("高智商", "gaozhishang"),
        Pair("悬疑推理", "xuanyituili"),
        Pair("机智", "jizhi"),
        Pair("同人漫画", "tongrenmanhua"),
        Pair("历史漫画", "lishimanhua"),
        Pair("短篇漫画", "duanpianmanhua"),
        Pair("游戏改编", "youxigaibian"),
        Pair("美少女", "meishaonv"),
        Pair("NTR", "NTR3"),
        Pair("秀吉", "xiuji"),
        Pair("AA", "AA"),
        Pair("GL", "GL"),
        Pair("剧情", "juqing"),
        Pair("总裁", "zongcai"),
        Pair("高甜", "gaotian"),
        Pair("神豪", "shenhao"),
        Pair("系统", "xitong"),
        Pair("大女主", "danvzhu"),
        Pair("怪物", "guaiwu"),
        Pair("重生", "zhongsheng"),
        Pair("修仙", "xiuxian"),
        Pair("宅斗", "zhaidou"),
        Pair("神仙", "shenxian"),
        Pair("妖怪", "yaoguai"),
        Pair("末日", "mori"),
        Pair("BL", "BL"),
        Pair("豪快", "haokuai"),
        Pair("电竞", "dianjing"),
        Pair("猎奇", "lieqi")
    ))

    private class FinishFilter : UriPartFilter("进度", arrayOf(
        Pair("全部", ""),
        Pair("连载", "lianzai"),
        Pair("完结", "wanjie")

    ))

    private class AudienceFilter : UriPartFilter("地区", arrayOf(
        Pair("全部", ""),
        Pair("日本", "riben"),
        Pair("国产", "dalu"),
        Pair("香港", "hongkong"),
        Pair("台湾", "taiwan"),
        Pair("欧美", "oumei"),
        Pair("韩国", "hanguo"),
        Pair("其他", "qita")

    ))

    private class CopyrightFilter : UriPartFilter("受众", arrayOf(
        Pair("全部", ""),
        Pair("儿童", "ertong"),
        Pair("少年", "shaonian"),
        Pair("少女", "shaonv"),
        Pair("青年", "qingnian")

    ))

    private class MoneyFilter : UriPartFilter("排序", arrayOf(
        Pair("点击量", "/click/"),
        Pair("更新", "/update/"),
        Pair("发布", "/post/")
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
