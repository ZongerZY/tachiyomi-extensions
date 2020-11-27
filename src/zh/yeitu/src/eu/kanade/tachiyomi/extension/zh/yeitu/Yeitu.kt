package eu.kanade.tachiyomi.extension.zh.yeitu

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
import org.jsoup.nodes.Document

class Yeitu : HttpSource() {

    override val name = "亿图全景图库"
    override val baseUrl = ""
    override val lang = "zh"
    override val supportsLatest = true

    private fun myGet(url: String) = GET(url, headers)

    override fun popularMangaRequest(page: Int): Request {
        if (page == 1) return myGet("https://www.yeitu.net/meinv/xinggan/index.html") else return myGet("https://www.yeitu.net/meinv/xinggan/$page.html")
    }

    override fun popularMangaParse(response: Response): MangasPage = searchMangaParse(response)

    override fun latestUpdatesRequest(page: Int): Request {
        if (page == 1) return myGet("https://www.yeitu.net/meinv/xinggan/index.html") else return myGet("https://www.yeitu.net/meinv/xinggan/$page.html")
    }

    override fun latestUpdatesParse(response: Response): MangasPage = searchMangaParse(response)

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)

        title = document.select("h1").text()
        thumbnail_url = document.select("div.picture a img").attr("src")
        author = "Tachiyomi : ZongerZY"
        genre = getGenre(document)
        status = 3
        description = "${document.select("h1").text()}"
    }

    private fun getGenre(document: Document): String {
        var gener = ""
        if (document.select("div.related_tag").size == 0) {
            var tagsForDoc = document.select("div.tags").get(0).select("a")
            for (i in 0 until tagsForDoc.size)
                if (i == tagsForDoc.size - 1) gener = gener + tagsForDoc.get(i).text() else gener = gener + tagsForDoc.get(i).text() + ", "
        } else {
            var tagsForDoc = document.select("div.related_tag p a")
            for (i in 0 until tagsForDoc.size)
                if (i == tagsForDoc.size - 1) gener = gener + tagsForDoc.get(i).text() else gener = gener + tagsForDoc.get(i).text() + ", "
        }
        return gener
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        var chapterList = ArrayList<SChapter>()
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)
        chapterList.add(SChapter.create().apply {
            name = document.select("h1").text()
            url = response.request().url().toString()
        })
        return chapterList
    }

    override fun pageListParse(response: Response): List<Page> {
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)
        var pageNum = document.select("#pages").select("a").get(document.select("#pages a").size - 2).text().toInt()
        var arrList = ArrayList<Page>()
        arrList.add(Page(0, response.request().url().toString()))
        for (i in 2 until pageNum + 1) {
            arrList.add(Page(i - 1, response.request().url().toString().split(".html")[0] + "_$i" + ".html"))
        }
        return arrList
    }

    override fun imageUrlRequest(page: Page): Request {
        return myGet(page.url)
    }

    override fun imageUrlParse(response: Response): String {
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)
        if (document.select("dic.picture div.img_box").size != 0)
            return document.select("div.picture div.img_box a img").attr("src")
        else
            return document.select("div.picture a img").attr("src")
    }

    override fun imageRequest(page: Page): Request {
        return myGet(page.imageUrl!!)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        var requestUrl = response.request().url()!!.toString()
        if (requestUrl.contains("m=search")) {
            val body = response.body()!!.string()
            val document = Jsoup.parseBodyFragment(body)
            var mangasElements = document.select("ul.list_box").select("li")
            var mangas = ArrayList<SManga>(mangasElements.size)
            for (mangaElement in mangasElements) {
                if (mangaElement.select("h5 a").attr("href").contains("meinvbaike")) {
                    continue
                }
                mangas.add(SManga.create().apply {
                    title = mangaElement.select("h5 a").text()
                    thumbnail_url = mangaElement.select("div.list_box_img a img").attr("src")
                    url = mangaElement.select("h5 a").attr("href")
                })
            }
            return MangasPage(mangas, true)
        } else if (requestUrl.contains("/tag/")) {
            val body = response.body()!!.string()
            val document = Jsoup.parseBodyFragment(body)
            var mangasElements = document.select("#tag_box div.tag_list")
            var mangas = ArrayList<SManga>(mangasElements.size)
            for (mangaElement in mangasElements) {
                mangas.add(SManga.create().apply {
                    title = mangaElement.select("div.title a").text()
                    thumbnail_url = mangaElement.select("a img").attr("src")
                    url = mangaElement.select("div.title a").attr("href")
                })
            }
            return MangasPage(mangas, mangasElements.size == 24)
        } else if (requestUrl.contains("meinvbaike")) {
            val body = response.body()!!.string()
            val document = Jsoup.parseBodyFragment(body)
            var mangasElements = document.select("div.baike_interfix div.wf ul li")
            var mangas = ArrayList<SManga>(mangasElements.size)
            for (mangaElement in mangasElements) {
                mangas.add(SManga.create().apply {
                    title = mangaElement.select("div.title a").text()
                    thumbnail_url = mangaElement.select("a img").attr("data-echo")
                    url = mangaElement.select("div.title a").attr("href")
                })
            }
            return MangasPage(mangas, false)
        } else {
            val body = response.body()!!.string()
            val document = Jsoup.parseBodyFragment(body)
            var mangasElements = document.select("div.w.yt-list ul li")
            var mangas = ArrayList<SManga>(mangasElements.size)
            for (mangaElement in mangasElements) {
                mangas.add(SManga.create().apply {
                    title = mangaElement.select("a div.list-title").text()
                    thumbnail_url = mangaElement.select("a img").attr("data-echo")
                    url = mangaElement.select("a").attr("href")
                })
            }
            return MangasPage(mangas, mangasElements.size == 20)
        }
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            return myGet("https://www.yeitu.net/index.php?m=search&c=index&a=init&typeid=&siteid=1&q=$query&page=$page")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            return if (params.contains("tag"))
                myGet("$params?&page=$page")
            else if (params.contains("meinvbaike"))
                myGet("$params")
            else {
                if (page == 1)
                    myGet("${params}index.html")
                else
                    myGet("${params}$page.html")
            }
        }
    }

    override fun getFilterList() = FilterList(
        ThemeFilter()
    )

    private class ThemeFilter : UriPartFilter("题材", arrayOf(
        Pair("全部", ""),
        Pair("美女明星", "https://www.yeitu.net/mingxing/nv/"),
        Pair("男星写真", "https://www.yeitu.net/mingxing/nan/"),
        Pair("COSPLAY", "https://www.yeitu.net/dongman/cosplay/"),
        Pair("动漫图片", "https://www.yeitu.net/dongman/dongmantupian/"),
        Pair("风光摄影", "https://www.yeitu.net/sheying/fengguang/"),
        Pair("婚纱摄影", "https://www.yeitu.net/sheying/hunsha/"),
        Pair("性感美女", "https://www.yeitu.net/meinv/xinggan/"),
        Pair("丝袜美腿", "https://www.yeitu.net/meinv/siwameitui/"),
        Pair("唯美写真", "https://www.yeitu.net/meinv/weimei/"),
        Pair("性感车模", "https://www.yeitu.net/meinv/chemo/"),
        Pair("网络美女", "https://www.yeitu.net/meinv/wangluomeinv/"),
        Pair("体育美女", "https://www.yeitu.net/meinv/tiyumeinv/"),
        Pair("人体艺术", "https://www.yeitu.net/meinv/rentiyishu/"),
        Pair("情侣纹身", "https://www.yeitu.net/wenshen/qinglv/"),
        Pair("纹身图案", "https://www.yeitu.net/wenshen/tuanws/"),
        Pair("美女纹身", "https://www.yeitu.net/wenshen/meinvws/"),
        Pair("明星纹身", "https://www.yeitu.net/wenshen/mingxingws/"),
        Pair("游戏壁纸", "https://www.yeitu.net/bizhi/youxi/"),
        Pair("军事壁纸", "https://www.yeitu.net/bizhi/junshi/"),
        Pair("美食壁纸", "https://www.yeitu.net/bizhi/meishi/"),
        Pair("美女壁纸", "https://www.yeitu.net/bizhi/meinvbz/"),
        Pair("明星壁纸", "https://www.yeitu.net/bizhi/mingxingbz/"),

        Pair("尤果网", "https://www.yeitu.net/tag/youguowang/"),
        Pair("推女郎", "https://www.yeitu.net/tag/tuinvlang/"),
        Pair("YouMi尤蜜", "https://www.yeitu.net/tag/YouMiyoumi/"),
        Pair("秀人网", "https://www.yeitu.net/tag/xiurenwang/"),
        Pair("推女神", "https://www.yeitu.net/tag/tuinvshen/"),
        Pair("波萝社", "https://www.yeitu.net/tag/boluoshe/"),
        Pair("星乐园", "https://www.yeitu.net/tag/xingleyuan/"),
        Pair("御女郎", "https://www.yeitu.net/tag/yunvlang/"),
        Pair("头条女神", "https://www.yeitu.net/tag/toutiaonvshen/"),
        Pair("糖果画报", "https://www.yeitu.net/tag/tangguohuabao/"),
        Pair("尤蜜荟", "https://www.yeitu.net/tag/youmihui/"),
        Pair("喵糖映画", "https://www.yeitu.net/tag/miaotangyinghua/"),
        Pair("爱蜜社", "https://www.yeitu.net/tag/aimishe/"),
        Pair("花漾写真", "https://www.yeitu.net/tag/huayang/"),
        Pair("模范学院", "https://www.yeitu.net/tag/mofanxueyuan/"),

        Pair("素颜", "https://www.yeitu.net/tag/suyan/"),
        Pair("白嫩", "https://www.yeitu.net/tag/bainen/"),
        Pair("酥胸", "https://www.yeitu.net/tag/suxiong/"),
        Pair("湿身", "https://www.yeitu.net/tag/shishen/"),
        Pair("爆乳", "https://www.yeitu.net/tag/baoru/"),
        Pair("粉嫩", "https://www.yeitu.net/tag/fennen/"),
        Pair("冷艳", "https://www.yeitu.net/tag/lengyan/"),
        Pair("风骚", "https://www.yeitu.net/tag/fengsao/"),
        Pair("丰满美女", "https://www.yeitu.net/tag/fengman/"),
        Pair("浴室", "https://www.yeitu.net/tag/yushi/"),
        Pair("美臀", "https://www.yeitu.net/tag/meitun/"),
        Pair("美腿", "https://www.yeitu.net/tag/meitui/"),
        Pair("美胸", "https://www.yeitu.net/tag/meixiong/"),
        Pair("制服诱惑", "https://www.yeitu.net/tag/zhifuyouhuo/"),
        Pair("自拍", "https://www.yeitu.net/tag/zipai/"),
        Pair("短发", "https://www.yeitu.net/tag/duanfa/"),
        Pair("长发", "https://www.yeitu.net/tag/changfa/"),
        Pair("玉足", "https://www.yeitu.net/tag/yuzu/"),
        Pair("网袜", "https://www.yeitu.net/tag/wangwa/"),
        Pair("丝袜", "https://www.yeitu.net/tag/siwameitui/"),
        Pair("美乳", "https://www.yeitu.net/tag/meiru/"),
        Pair("乳沟", "https://www.yeitu.net/tag/rugou/"),
        Pair("黑丝", "https://www.yeitu.net/tag/heisi/"),
        Pair("旗袍", "https://www.yeitu.net/tag/qipao/"),
        Pair("蕾丝", "https://www.yeitu.net/tag/leisi/"),
        Pair("泳衣", "https://www.yeitu.net/tag/yongyi/"),
        Pair("肉丝", "https://www.yeitu.net/tag/rousi/"),
        Pair("长裙", "https://www.yeitu.net/tag/changqun/"),
        Pair("睡衣", "https://www.yeitu.net/tag/shuiyi/"),
        Pair("少妇", "https://www.yeitu.net/tag/shaofu/"),
        Pair("嫩模", "https://www.yeitu.net/tag/nenmo/"),
        Pair("护士", "https://www.yeitu.net/tag/hushi/"),
        Pair("女仆", "https://www.yeitu.net/tag/nvpu/"),
        Pair("空姐", "https://www.yeitu.net/tag/kongjie/"),
        Pair("熟女", "https://www.yeitu.net/tag/shunv/"),
        Pair("女神", "https://www.yeitu.net/tag/nvshen/"),
        Pair("大尺度", "https://www.yeitu.net/tag/dachidu/"),
        Pair("私房照", "https://www.yeitu.net/tag/sifangzhao/"),
        Pair("丁字裤", "https://www.yeitu.net/tag/dingziku/"),
        Pair("超短裙", "https://www.yeitu.net/tag/chaoduanqun/"),
        Pair("高跟鞋", "https://www.yeitu.net/tag/gaogenxie/"),
        Pair("透视装", "https://www.yeitu.net/tag/toushizhuang/"),
        Pair("学生妹", "https://www.yeitu.net/tag/xueshengmei/"),
        Pair("小清新", "https://www.yeitu.net/tag/xiaoqingxin/"),
        Pair("萌妹子", "https://www.yeitu.net/tag/mengmeizi/"),
        Pair("姐妹花", "https://www.yeitu.net/tag/jiemeihua/"),
        Pair("可爱美女", "https://www.yeitu.net/tag/keaimeinv/"),
        Pair("美女照片", "https://www.yeitu.net/tag/meinvzhaopian/"),
        Pair("妖娆美女", "https://www.yeitu.net/tag/yaoraomeinv/"),
        Pair("养眼美女", "https://www.yeitu.net/tag/yangyanmeinv/"),
        Pair("混血美女", "https://www.yeitu.net/tag/hunxuemeinv/"),
        Pair("气质美女", "https://www.yeitu.net/tag/qizhimeinv/"),
        Pair("美女主播", "https://www.yeitu.net/tag/meinvzhubo/"),
        Pair("美女校花", "https://www.yeitu.net/tag/meinvxiaohua/"),
        Pair("足球宝贝", "https://www.yeitu.net/tag/zuqiubaobei/"),
        Pair("日本美女", "https://www.yeitu.net/tag/ribenmeinv/"),
        Pair("萝莉Loli", "https://www.yeitu.net/tag/luoli/"),
        Pair("比基尼美女", "https://www.yeitu.net/tag/bijini/"),
        Pair("长腿美女", "https://www.yeitu.net/tag/changtui/"),
        Pair("巨乳美女", "https://www.yeitu.net/tag/juru/"),
        Pair("翘臀美女", "https://www.yeitu.net/tag/qiaotun/"),
        Pair("内衣写真", "https://www.yeitu.net/tag/neiyixiezhen/"),
        Pair("内衣美女", "https://www.yeitu.net/tag/neiyi/"),
        Pair("绝色美女", "https://www.yeitu.net/tag/juesemeinv/"),
        Pair("黑丝美腿", "https://www.yeitu.net/tag/heisimeitui/"),
        Pair("腿模", "https://www.yeitu.net/tag/tuimo/"),
        Pair("宅男女神", "https://www.yeitu.net/tag/zhainannvshen/"),
        Pair("丝袜美腿", "https://www.yeitu.net/tag/siwameitui/"),
        Pair("童颜巨乳", "https://www.yeitu.net/tag/tongyanjuru/"),
        Pair("情趣内衣", "https://www.yeitu.net/tag/qingquneiyi/"),
        Pair("兔女郎", "https://www.yeitu.net/tag/tunvlang/"),

        Pair("周妍希", "https://www.yeitu.net/meinvbaike/20170228_21.html"),
        Pair("杨晨晨", "https://www.yeitu.net/meinvbaike/20171011_514.html"),
        Pair("李梓熙", "https://www.yeitu.net/meinvbaike/20170301_22.html"),
        Pair("刘钰儿", "https://www.yeitu.net/meinvbaike/20170414_108.html"),
        Pair("小尤奈", "https://www.yeitu.net/meinvbaike/20181208_1121.html"),
        Pair("Yumi尤美", "https://www.yeitu.net/meinvbaike/20170513_186.html"),
        Pair("SOLO尹菲", "https://www.yeitu.net/meinvbaike/20180101_757.html"),
        Pair("小热巴", "https://www.yeitu.net/meinvbaike/20180706_994.html"),
        Pair("唐琪儿", "https://www.yeitu.net/meinvbaike/20170405_57.html"),
        Pair("就是阿朱啊", "https://www.yeitu.net/meinvbaike/20180609_967.html"),
        Pair("黄乐然", "https://www.yeitu.net/meinvbaike/20180703_993.html"),
        Pair("孙允珠", "https://www.yeitu.net/meinvbaike/20180208_822.html"),
        Pair("于姬Una", "https://www.yeitu.net/meinvbaike/20170227_15.html"),
        Pair("雪千寻", "https://www.yeitu.net/meinvbaike/20170818_374.html"),
        Pair("冰露", "https://www.yeitu.net/meinvbaike/20170509_159.html"),
        Pair("罗云琦", "https://www.yeitu.net/meinvbaike/20170910_471.html"),
        Pair("缇娜美", "https://www.yeitu.net/meinvbaike/20180715_997.html"),
        Pair("猫九酱", "https://www.yeitu.net/meinvbaike/20170428_120.html"),
        Pair("沈梦瑶", "https://www.yeitu.net/meinvbaike/20170407_78.html"),
        Pair("闫盼盼", "https://www.yeitu.net/meinvbaike/20170521_200.html"),
        Pair("白一晗", "https://www.yeitu.net/meinvbaike/20170923_501.html"),
        Pair("张雪馨", "https://www.yeitu.net/meinvbaike/20170513_188.html"),
        Pair("穆菲菲", "https://www.yeitu.net/meinvbaike/20170428_121.html"),
        Pair("筱慧Icon", "https://www.yeitu.net/meinvbaike/20170921_497.html"),
        Pair("乔柯涵", "https://www.yeitu.net/meinvbaike/20170508_150.html"),
        Pair("恩一", "https://www.yeitu.net/meinvbaike/20180128_801.html"),
        Pair("李可可", "https://www.yeitu.net/meinvbaike/20180404_861.html"),
        Pair("伊小七", "https://www.yeitu.net/meinvbaike/20170303_25.html"),
        Pair("闵妮", "https://www.yeitu.net/meinvbaike/20170410_100.html"),
        Pair("王诗琪", "https://www.yeitu.net/meinvbaike/20170925_502.html"),
        Pair("易阳ELLY", "https://www.yeitu.net/meinvbaike/20170512_162.html"),
        Pair("李妍曦", "https://www.yeitu.net/meinvbaike/20170715_246.html"),
        Pair("艾小青", "https://www.yeitu.net/meinvbaike/20170712_242.html"),
        Pair("徐微微", "https://www.yeitu.net/meinvbaike/20180726_1026.html"),
        Pair("叶梦轩", "https://www.yeitu.net/meinvbaike/20170508_155.html"),
        Pair("唐思琪", "https://www.yeitu.net/meinvbaike/20180728_1028.html"),
        Pair("程彤颜", "https://www.yeitu.net/meinvbaike/20170227_17.html"),
        Pair("魏扭扭", "https://www.yeitu.net/meinvbaike/20170918_495.html"),
        Pair("孙梦瑶", "https://www.yeitu.net/meinvbaike/20170906_468.html"),
        Pair("夏美酱", "https://www.yeitu.net/meinvbaike/20170227_13.html"),
        Pair("连欣", "https://www.yeitu.net/meinvbaike/20170809_305.html"),
        Pair("谢芷馨", "https://www.yeitu.net/meinvbaike/20170407_82.html"),
        Pair("傲娇萌萌", "https://www.yeitu.net/meinvbaike/20170709_239.html"),
        Pair("林美惠子", "https://www.yeitu.net/meinvbaike/20170703_232.html"),
        Pair("艾栗栗", "https://www.yeitu.net/meinvbaike/20170808_296.html"),
        Pair("Miko酱", "https://www.yeitu.net/meinvbaike/20180908_1071.html"),
        Pair("王婉悠", "https://www.yeitu.net/meinvbaike/20170625_221.html"),
        Pair("梦心玥", "https://www.yeitu.net/meinvbaike/20170809_310.html"),
        Pair("心妍公主", "https://www.yeitu.net/meinvbaike/20180513_929.html"),
        Pair("何晨曦", "https://www.yeitu.net/meinvbaike/20171217_733.html"),
        Pair("乔依琳", "https://www.yeitu.net/meinvbaike/20180414_879.html"),
        Pair("凯竹", "https://www.yeitu.net/meinvbaike/20170302_24.html"),
        Pair("赵伊彤", "https://www.yeitu.net/meinvbaike/20170719_249.html"),
        Pair("苏小曼", "https://www.yeitu.net/meinvbaike/20170807_275.html"),
        Pair("周熙妍", "https://www.yeitu.net/meinvbaike/20170805_272.html"),
        Pair("卓娅祺", "https://www.yeitu.net/meinvbaike/20180414_878.html"),
        Pair("小魔女奈奈", "https://www.yeitu.net/meinvbaike/20170501_123.html"),
        Pair("绯月樱Cherry", "https://www.yeitu.net/meinvbaike/20180620_980.html"),
        Pair("不柠bling", "https://www.yeitu.net/meinvbaike/20170726_266.html"),
        Pair("雪千紫", "https://www.yeitu.net/meinvbaike/20170923_500.html"),
        Pair("久久Aimee", "https://www.yeitu.net/meinvbaike/20180604_963.html"),
        Pair("青树", "https://www.yeitu.net/meinvbaike/20170304_34.html"),
        Pair("赵小米", "https://www.yeitu.net/meinvbaike/20170226_2.html"),
        Pair("郑瑞熙", "https://www.yeitu.net/meinvbaike/20170820_400.html"),
        Pair("娜露Selena", "https://www.yeitu.net/meinvbaike/20170524_206.html"),
        Pair("李易童", "https://www.yeitu.net/meinvbaike/20180208_819.html"),
        Pair("安娜金", "https://www.yeitu.net/meinvbaike/20170606_213.html"),
        Pair("王乔恩", "https://www.yeitu.net/meinvbaike/20170405_61.html"),
        Pair("金禹熙", "https://www.yeitu.net/meinvbaike/20170512_167.html"),
        Pair("萌琪琪", "https://www.yeitu.net/meinvbaike/20170227_16.html"),
        Pair("沈佳熹", "https://www.yeitu.net/meinvbaike/20170309_40.html"),
        Pair("陆瓷", "https://www.yeitu.net/meinvbaike/20170708_236.html"),
        Pair("于思琪", "https://www.yeitu.net/meinvbaike/20170407_75.html"),
        Pair("陈良玲Carry", "https://www.yeitu.net/meinvbaike/20181213_1123.html"),
        Pair("悦爷妖精", "https://www.yeitu.net/meinvbaike/20171021_548.html"),
        Pair("王梓童", "https://www.yeitu.net/meinvbaike/20170817_371.html"),
        Pair("黛诺欣", "https://www.yeitu.net/meinvbaike/20171215_725.html"),
        Pair("甜心CC", "https://www.yeitu.net/meinvbaike/20170311_43.html"),
        Pair("little贝壳", "https://www.yeitu.net/meinvbaike/20170801_271.html"),
        Pair("米妮", "https://www.yeitu.net/meinvbaike/20170227_10.html"),
        Pair("徐cake", "https://www.yeitu.net/meinvbaike/20170227_7.html"),
        Pair("赵娜娜", "https://www.yeitu.net/meinvbaike/20171219_741.html"),
        Pair("杨漫妮", "https://www.yeitu.net/meinvbaike/20170227_14.html"),
        Pair("菳baby", "https://www.yeitu.net/meinvbaike/20170425_118.html"),
        Pair("张灵儿", "https://www.yeitu.net/meinvbaike/20180109_764.html"),
        Pair("佳佳JiaJia", "https://www.yeitu.net/meinvbaike/20170420_109.html"),
        Pair("何何夕", "https://www.yeitu.net/meinvbaike/20171025_592.html"),
        Pair("猫宝", "https://www.yeitu.net/meinvbaike/20180525_944.html"),
        Pair("周于希", "https://www.yeitu.net/meinvbaike/20170625_222.html"),
        Pair("芝芝", "https://www.yeitu.net/meinvbaike/20170412_105.html"),
        Pair("杨依Rokie", "https://www.yeitu.net/meinvbaike/20170812_338.html"),
        Pair("陈秋雨", "https://www.yeitu.net/meinvbaike/20170812_314.html"),
        Pair("李雅", "https://www.yeitu.net/meinvbaike/20170423_115.html"),
        Pair("Miss爱菲儿", "https://www.yeitu.net/meinvbaike/20170722_263.html"),
        Pair("木婉晴", "https://www.yeitu.net/meinvbaike/20170812_328.html"),
        Pair("姗姗就打奥特曼", "https://www.yeitu.net/meinvbaike/20170721_255.html"),
        Pair("沈蜜桃", "https://www.yeitu.net/meinvbaike/20170405_62.html"),
        Pair("赵智妍", "https://www.yeitu.net/meinvbaike/20170901_450.html"),
        Pair("蔡文钰", "https://www.yeitu.net/meinvbaike/20170629_226.html"),
        Pair("刘奕宁", "https://www.yeitu.net/meinvbaike/20170407_81.html"),
        Pair("小狐狸Sica", "https://www.yeitu.net/meinvbaike/20171214_724.html"),
        Pair("岑雨桥", "https://www.yeitu.net/meinvbaike/20170508_148.html"),
        Pair("雨瞳", "https://www.yeitu.net/meinvbaike/20170812_342.html"),
        Pair("张多多", "https://www.yeitu.net/meinvbaike/20180329_859.html"),
        Pair("杨果果", "https://www.yeitu.net/meinvbaike/20170701_227.html"),
        Pair("梁莹", "https://www.yeitu.net/meinvbaike/20170308_39.html"),
        Pair("周小然", "https://www.yeitu.net/meinvbaike/20171019_524.html"),
        Pair("凌希儿", "https://www.yeitu.net/meinvbaike/20171207_712.html"),
        Pair("胡允儿", "https://www.yeitu.net/meinvbaike/20170818_382.html"),
        Pair("舒林培", "https://www.yeitu.net/meinvbaike/20170422_111.html"),
        Pair("然素儿", "https://www.yeitu.net/meinvbaike/20180307_832.html"),
        Pair("宅兔兔", "https://www.yeitu.net/meinvbaike/20180318_846.html"),
        Pair("伊莉娜", "https://www.yeitu.net/meinvbaike/20170905_466.html"),
        Pair("梁允儿YUNER", "https://www.yeitu.net/meinvbaike/20170518_193.html"),
        Pair("夏雪爱", "https://www.yeitu.net/meinvbaike/20180327_855.html"),
        Pair("欣杨Kitty", "https://www.yeitu.net/meinvbaike/20170823_419.html"),
        Pair("冯木木LRIS", "https://www.yeitu.net/meinvbaike/20180803_1042.html"),
        Pair("李凌子", "https://www.yeitu.net/meinvbaike/20170504_140.html"),
        Pair("童安琪", "https://www.yeitu.net/meinvbaike/20170504_141.html"),
        Pair("雪瑞", "https://www.yeitu.net/meinvbaike/20170410_94.html"),
        Pair("小探戈", "https://www.yeitu.net/meinvbaike/20180325_851.html"),
        Pair("伊素妍", "https://www.yeitu.net/meinvbaike/20180425_889.html"),
        Pair("田慕儿", "https://www.yeitu.net/meinvbaike/20171021_549.html"),
        Pair("陈思雨", "https://www.yeitu.net/meinvbaike/20170830_434.html"),
        Pair("周心情", "https://www.yeitu.net/meinvbaike/20180430_902.html"),
        Pair("何周周", "https://www.yeitu.net/meinvbaike/20180506_916.html"),
        Pair("安柔Anrou", "https://www.yeitu.net/meinvbaike/20171129_700.html"),
        Pair("韩雨婵", "https://www.yeitu.net/meinvbaike/20180127_798.html"),
        Pair("柳侑绮", "https://www.yeitu.net/meinvbaike/20170508_158.html"),
        Pair("晗大大", "https://www.yeitu.net/meinvbaike/20170708_237.html"),
        Pair("刘飞儿", "https://www.yeitu.net/meinvbaike/20170225_1.html"),
        Pair("张秒秒", "https://www.yeitu.net/meinvbaike/20180428_896.html"),
        Pair("张小西", "https://www.yeitu.net/meinvbaike/20170407_69.html"),
        Pair("李颖诗", "https://www.yeitu.net/meinvbaike/20180103_759.html"),
        Pair("琪歌", "https://www.yeitu.net/meinvbaike/20170513_172.html"),
        Pair("若涵", "https://www.yeitu.net/meinvbaike/20170512_168.html"),
        Pair("刘天天", "https://www.yeitu.net/meinvbaike/20171210_716.html"),
        Pair("姚沐迪", "https://www.yeitu.net/meinvbaike/20180204_811.html"),
        Pair("李宝儿", "https://www.yeitu.net/meinvbaike/20180409_872.html"),
        Pair("韩恩熙", "https://www.yeitu.net/meinvbaike/20170405_59.html"),
        Pair("M梦baby", "https://www.yeitu.net/meinvbaike/20170925_503.html"),
        Pair("陈亦菲", "https://www.yeitu.net/meinvbaike/20180409_873.html"),
        Pair("谭冰", "https://www.yeitu.net/meinvbaike/20170817_368.html"),
        Pair("陈七七", "https://www.yeitu.net/meinvbaike/20171105_611.html"),
        Pair("夏瑶", "https://www.yeitu.net/meinvbaike/20170406_65.html"),
        Pair("琳琳ailin", "https://www.yeitu.net/meinvbaike/20170303_26.html"),
        Pair("妮小妖", "https://www.yeitu.net/meinvbaike/20170328_45.html"),
        Pair("陈梓涵Sunny", "https://www.yeitu.net/meinvbaike/20170407_79.html"),
        Pair("施诗", "https://www.yeitu.net/meinvbaike/20170329_47.html"),
        Pair("ElyEE子", "https://www.yeitu.net/meinvbaike/20180127_797.html"),
        Pair("陈大榕", "https://www.yeitu.net/meinvbaike/20170914_476.html"),
        Pair("杜花花", "https://www.yeitu.net/meinvbaike/20170921_498.html"),
        Pair("苏朵朵", "https://www.yeitu.net/meinvbaike/20180317_844.html"),
        Pair("悠悠酱", "https://www.yeitu.net/meinvbaike/20170513_189.html"),
        Pair("何嘉颖", "https://www.yeitu.net/meinvbaike/20171111_623.html"),
        Pair("孟思雨", "https://www.yeitu.net/meinvbaike/20170817_355.html"),
        Pair("李七喜", "https://www.yeitu.net/meinvbaike/20170303_28.html"),
        Pair("陈美熙", "https://www.yeitu.net/meinvbaike/20171210_715.html"),
        Pair("刘果", "https://www.yeitu.net/meinvbaike/20171215_726.html"),
        Pair("王俪丁", "https://www.yeitu.net/meinvbaike/20170819_384.html"),
        Pair("宋梓诺", "https://www.yeitu.net/meinvbaike/20170507_144.html"),
        Pair("夏乔", "https://www.yeitu.net/meinvbaike/20170414_107.html"),
        Pair("萌汉药baby", "https://www.yeitu.net/meinvbaike/20180525_945.html"),
        Pair("尤柔美", "https://www.yeitu.net/meinvbaike/20180122_789.html"),
        Pair("金梓馨", "https://www.yeitu.net/meinvbaike/20180725_1021.html"),
        Pair("韩贝贝", "https://www.yeitu.net/meinvbaike/20171119_635.html"),
        Pair("兔子NINA", "https://www.yeitu.net/meinvbaike/20180505_908.html"),
        Pair("程梓", "https://www.yeitu.net/meinvbaike/20180212_823.html"),
        Pair("蜜蕊Mary", "https://www.yeitu.net/meinvbaike/20170517_192.html"),
        Pair("林薇多", "https://www.yeitu.net/meinvbaike/20180225_828.html"),
        Pair("金梓琳", "https://www.yeitu.net/meinvbaike/20180307_833.html"),
        Pair("严佳丽", "https://www.yeitu.net/meinvbaike/20171104_610.html"),
        Pair("倪诗茵", "https://www.yeitu.net/meinvbaike/20171017_520.html"),
        Pair("张之龄", "https://www.yeitu.net/meinvbaike/20180114_773.html"),
        Pair("张馨梓", "https://www.yeitu.net/meinvbaike/20171227_751.html"),
        Pair("黄可", "https://www.yeitu.net/meinvbaike/20170227_11.html"),
        Pair("月音瞳", "https://www.yeitu.net/meinvbaike/20170304_30.html"),
        Pair("青青子w", "https://www.yeitu.net/meinvbaike/20180128_802.html"),
        Pair("王依萌", "https://www.yeitu.net/meinvbaike/20170812_321.html"),
        Pair("赵馨玥", "https://www.yeitu.net/meinvbaike/20171217_731.html"),
        Pair("Vichy", "https://www.yeitu.net/meinvbaike/20180720_1009.html"),
        Pair("于大小姐", "https://www.yeitu.net/meinvbaike/20170227_9.html"),
        Pair("李颜儿", "https://www.yeitu.net/meinvbaike/20170625_225.html"),
        Pair("SukkiQ可儿", "https://www.yeitu.net/meinvbaike/20170501_124.html"),
        Pair("文雪", "https://www.yeitu.net/meinvbaike/20170523_202.html"),
        Pair("酸酱兔", "https://www.yeitu.net/meinvbaike/20170825_420.html"),
        Pair("李猩一", "https://www.yeitu.net/meinvbaike/20170403_48.html"),
        Pair("齐贝贝", "https://www.yeitu.net/meinvbaike/20170915_485.html"),
        Pair("杜小雨", "https://www.yeitu.net/meinvbaike/20180101_758.html"),
        Pair("爱丽莎", "https://www.yeitu.net/meinvbaike/20170405_63.html"),
        Pair("方子萱", "https://www.yeitu.net/meinvbaike/20171023_560.html"),
        Pair("顾欣怡", "https://www.yeitu.net/meinvbaike/20170305_35.html"),
        Pair("夏小秋", "https://www.yeitu.net/meinvbaike/20170226_6.html"),
        Pair("福妮娅", "https://www.yeitu.net/meinvbaike/20171219_738.html"),
        Pair("孟狐狸", "https://www.yeitu.net/meinvbaike/20170405_60.html"),
        Pair("韩雨菲", "https://www.yeitu.net/meinvbaike/20170914_478.html"),
        Pair("颜亦汐", "https://www.yeitu.net/meinvbaike/20180404_864.html"),
        Pair("施忆佳", "https://www.yeitu.net/meinvbaike/20170526_208.html"),
        Pair("王竹", "https://www.yeitu.net/meinvbaike/20171117_631.html"),
        Pair("叶汐", "https://www.yeitu.net/meinvbaike/20180221_827.html")
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
