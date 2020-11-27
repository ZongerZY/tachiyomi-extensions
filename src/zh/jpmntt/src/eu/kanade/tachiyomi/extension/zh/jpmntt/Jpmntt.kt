package eu.kanade.tachiyomi.extension.zh.jpmntt

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import java.net.URLEncoder
import java.util.regex.Pattern
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class Jpmntt : HttpSource() {
    // 两个分类JS
    // http://js.yhmeinv.com/js/dh.js
    // 分类js组装JSON在老版本的eclipse -> String操作 -> Test666.java
    // G:\16044329\String操作\src\com\String\Test666.java

    // 网站
    // http://js.ycmnt.com/dz.html

    override val name = "美女套图"
    override val baseUrl = ""
    override val lang = "zh"
    override val supportsLatest = true

    private val baseUrlJson = """{"baseUrlJson":[{"url":"www.ywmmt.com"},{"url":"www.mnwht.com"},{"url":"www.ycmeinv.com"},{"url":"www.jcmeinv.com"},{"url":"www.ztxzt.com"},{"url":"www.mfsft.com"},{"url":"www.jptaotu.com"},{"url":"www.ycmzt.com"},{"url":"www.flwht.com"},{"url":"www.threnti.com"},{"url":"www.wjtaotu.com"},{"url":"www.ztmeinv.com"},{"url":"www.mstaotu.com"},{"url":"www.tstaotu.com"},{"url":"www.zftaotu.com"},{"url":"www.mgtaotu.com"},{"url":"www.prmzt.com"},{"url":"www.xrtaotu.com"},{"url":"www.jjtaotu.com"},{"url":"www.prmeinv.com"},{"url":"www.axtaotu.com"},{"url":"www.mgmeinv.com"},{"url":"www.xsmeinv.com"},{"url":"www.ugtaotu.com"},{"url":"www.msmeinv.com"},{"url":"www.flxzw.com"},{"url":"www.axmeinv.com"},{"url":"www.swtaotu.com"},{"url":"www.hjtaotu.com"},{"url":"www.nsxzw.com"},{"url":"www.ugmeinv.com"},{"url":"www.hytaotu.com"},{"url":"www.xrmeinv.com"},{"url":"www.zfmeinv.com"},{"url":"www.jjmeinv.com"},{"url":"www.zttaotu.com"},{"url":"www.jcxzt.com"},{"url":"www.ykmeinv.com"},{"url":"www.qjtaotu.com"},{"url":"www.pmtaotu.com"},{"url":"www.ddtaotu.com"},{"url":"www.plxzw.com"},{"url":"www.mfxzt.com"},{"url":"www.tstuku.com"},{"url":"www.fltuku.com"},{"url":"www.yhtuku.com"},{"url":"www.ycmeitu.com"},{"url":"www.mttuku.com"},{"url":"www.xhtuku.com"},{"url":"www.qjtuku.com"},{"url":"www.xzmnt.com"},{"url":"www.sftuku.com"},{"url":"www.yctuk.com"},{"url":"www.yktuk.com"},{"url":"www.ywtuk.com"},{"url":"www.jctuk.com"},{"url":"www.xstuk.com"},{"url":"www.xgtuk.com"},{"url":"www.mztuk.com"},{"url":"www.xztuk.com"},{"url":"www.sytuk.com"},{"url":"www.gcxzt.com"},{"url":"www.tsxzt.com"},{"url":"www.gqxzt.com"},{"url":"www.xgxzt.com"},{"url":"www.spxzt.com"},{"url":"www.yhxzt.com"},{"url":"www.mtxzt.com"},{"url":"www.nsxzt.com"},{"url":"www.jdxzt.com"},{"url":"www.ykxzt.com"},{"url":"www.sfxzt.com"},{"url":"www.yhmeitu.com"},{"url":"www.mzmeitu.com"},{"url":"www.qpmzt.com"},{"url":"www.yzmzt.com"},{"url":"www.ywsft.com"},{"url":"www.wkmzt.com"},{"url":"www.brmzt.com"},{"url":"www.wbmzt.com"},{"url":"www.brtaotu.com"},{"url":"www.brmeinv.com"},{"url":"www.qtmzt.com"},{"url":"www.sfmnt.com"},{"url":"www.jrmzt.com"},{"url":"www.yztaotu.com"},{"url":"www.jrmeinv.com"},{"url":"www.xsmzt.com"},{"url":"www.zbtaotu.com"},{"url":"www.tsmnt.com"},{"url":"www.zbmzt.com"},{"url":"www.xjjtaotu.com"},{"url":"www.symzt.com"},{"url":"www.ywmeitu.com"},{"url":"www.whmeinv.com"},{"url":"www.ftmeinv.com"},{"url":"www.xjjmzt.com"},{"url":"www.smtaotu.com"},{"url":"www.whtaotu.com"},{"url":"www.xstaotu.com"},{"url":"www.jdtaotu.com"},{"url":"www.xgyouwu.com"},{"url":"www.zbmeinv.com"},{"url":"www.gqyouwu.com"},{"url":"www.mtmnt.com"},{"url":"www.nmtaotu.com"},{"url":"www.jpyouwu.com"},{"url":"www.flmeitu.com"},{"url":"www.gqtaot.com"},{"url":"www.plmeitu.com"},{"url":"www.xgwht.com"},{"url":"www.xztaotu.com"},{"url":"www.fcrenti.com"},{"url":"www.sfwht.com"},{"url":"www.gqsft.com"},{"url":"www.yhmeinv.com"},{"url":"www.jdmnt.com"},{"url":"www.yctaotu.com"},{"url":"www.wkrenti.com"},{"url":"www.yzrenti.com"},{"url":"www.ymtaotu.com"},{"url":"www.sptaotu.com"},{"url":"www.mttaotu.com"},{"url":"www.wsgtu.com"},{"url":"www.ywtaotu.com"},{"url":"www.flmzt.com"},{"url":"www.sftaotu.com"},{"url":"www.gcmeinv.com"},{"url":"www.nstaotu.com"},{"url":"www.xhtaotu.com"},{"url":"www.jdwht.com"},{"url":"www.mtmeinv.com"},{"url":"www.gqwht.com"},{"url":"www.xzmeitu.com"},{"url":"www.jcwht.com"},{"url":"www.tptaotu.com"},{"url":"www.spyouwu.com"},{"url":"www.xgmeitu.com"},{"url":"www.plxzt.com"},{"url":"www.jstaotu.com"},{"url":"www.yhtaotu.com"},{"url":"www.sytaotu.com"},{"url":"www.taotudq.com"},{"url":"www.taotuqj.com"},{"url":"www.jpmzt.com"},{"url":"www.xsmnt.com"},{"url":"www.nsxiez.com"},{"url":"www.nsmeitu.com"},{"url":"www.plmzt.com"},{"url":"www.yhmzt.com"},{"url":"www.mtmzt.com"},{"url":"www.xzmzt.com"},{"url":"www.spmzt.com"},{"url":"www.jpxzt.com"},{"url":"www.wsgmzt.com"},{"url":"www.ywmzt.com"},{"url":"www.mfmzt.com"},{"url":"www.snmeitu.com"},{"url":"www.jcmzt.com"},{"url":"www.zpmzt.com"},{"url":"www.swmzt.com"},{"url":"www.rsmzt.com"},{"url":"www.snmzt.com"},{"url":"www.thmzt.com"},{"url":"www.gcmeitu.com"},{"url":"www.aimzt.com"},{"url":"www.plwht.com"},{"url":"www.spmeitu.com"},{"url":"www.yhmnt.com"},{"url":"www.lmtaotu.com"},{"url":"www.mtwht.com"},{"url":"www.tswht.com"}]}"""

    private val classUrlJson = """{"classUrlJson":[{"url":"www.ywmmt.com/tags"},{"url":"www.mnwht.com/art/zla"},{"url":"www.ycmeinv.com/photos"},{"url":"www.jcmeinv.com/jingcai/image"},{"url":"www.ztxzt.com/juru/rutaotds"},{"url":"www.mfsft.com/gaoqing/mingmo"},{"url":"www.jptaotu.com/html/vip"},{"url":"www.ycmzt.com/piaoliang/zhuanti"},{"url":"www.flwht.com/guomo"},{"url":"www.threnti.com/metcn/renti"},{"url":"www.wjtaotu.com/wu/jitus"},{"url":"www.ztmeinv.com/zhuanti/meinvtus"},{"url":"www.mstaotu.com/mei/setus"},{"url":"www.tstaotu.com/te/sepians"},{"url":"www.zftaotu.com/zhi/futus"},{"url":"www.mgtaotu.com/mei/guantaos"},{"url":"www.prmzt.com/pir/meizhis"},{"url":"www.xrtaotu.com/xiu/rentaos"},{"url":"www.jjtaotu.com/jiao/jiaotus"},{"url":"www.prmeinv.com/pirmei/nvtus"},{"url":"www.axtaotu.com/aix/tuxius"},{"url":"www.mgmeinv.com/mu/guas"},{"url":"www.xsmeinv.com/xingshang/meinvpics"},{"url":"www.ugtaotu.com/uguo/ugirlss"},{"url":"www.msmeinv.com/mei/setaos"},{"url":"www.flxzw.com/meir/utaos"},{"url":"www.axmeinv.com/ai/xiumeinvs"},{"url":"www.swtaotu.com/siw/atups"},{"url":"www.hjtaotu.com/he/jitus"},{"url":"www.nsxzw.com/huan/yangtus"},{"url":"www.ugmeinv.com/uoug/angmeis"},{"url":"www.hytaotu.com/hua/hytaotus"},{"url":"www.xrmeinv.com/xiu/renmeis"},{"url":"www.zfmeinv.com/zhi/fumeinvs"},{"url":"www.jjmeinv.com/jiao/jiaomeinvs"},{"url":"www.zttaotu.com/zhuanti/taotus"},{"url":"www.jcxzt.com/yao/kantus"},{"url":"www.ykmeinv.com/yan/kongnvs"},{"url":"www.qjtaotu.com/quanji/tutups"},{"url":"www.pmtaotu.com/pai/meituks"},{"url":"www.ddtaotu.com/da/dankus"},{"url":"www.plxzw.com/wangh/ongtuks"},{"url":"www.mfxzt.com/zhuant/itukus"},{"url":"www.tstuku.com/te/setukus"},{"url":"www.fltuku.com/fuli/itukus"},{"url":"www.yhtuku.com/youh/uotukus"},{"url":"www.ycmeitu.com/yuan/meinvpimg"},{"url":"www.mttuku.com/mote/motkuts"},{"url":"www.xhtuku.com/xih/uantuks"},{"url":"www.qjtuku.com/quanj/jikutus"},{"url":"www.xzmnt.com/gaoqi/qingkutuos"},{"url":"www.sftuku.com/sif/fangtukus"},{"url":"www.yctuk.com/yuanc/chuangtkus"},{"url":"www.yktuk.com/yaok/kankutus"},{"url":"www.ywtuk.com/youw/wukutus"},{"url":"www.jctuk.com/jingca/caitukuuas"},{"url":"www.xstuk.com/xinsh/angukuts"},{"url":"www.xgtuk.com/xin/gguttks"},{"url":"www.mztuk.com/mizhi/zhitukts"},{"url":"www.xztuk.com/xiezh/enuikrus"},{"url":"www.sytuk.com/shey/yingtiiurs"},{"url":"www.gcxzt.com/guoct/chantekses"},{"url":"www.tsxzt.com/tese/sezhentus"},{"url":"www.gqxzt.com/gaoqingr/qingzxies"},{"url":"www.xgxzt.com/xingganhg/ganturs"},{"url":"www.spxzt.com/shipad/paixztus"},{"url":"www.yhxzt.com/ykou/yhuodzxs"},{"url":"www.mtxzt.com/moterder/mtxzts"},{"url":"www.nsxzt.com/nvs/shentuxies"},{"url":"www.jdxzt.com/jingda/diantuxs"},{"url":"www.ykxzt.com/ykao/kantuxzs"},{"url":"www.sfxzt.com/shifnag/fnhtuxszs"},{"url":"www.yhmeitu.com/you/tttvips"},{"url":"www.mzmeitu.com/mei/zissgis"},{"url":"www.qpmzt.com/qing/viphoto"},{"url":"www.yzmzt.com/ya/zhoutuji"},{"url":"www.ywsft.com/nv/shengyouwu"},{"url":"www.wkmzt.com/wo/nvmingi"},{"url":"www.brmzt.com/bao/rutum"},{"url":"www.wbmzt.com/wei/bomeizip"},{"url":"www.brtaotu.com/bao/rutuegs"},{"url":"www.brmeinv.com/bao/rumeiags"},{"url":"www.qtmzt.com/qiao/tuntus"},{"url":"www.sfmnt.com/piaol/liangtkus"},{"url":"www.jrmzt.com/ju/rummtups"},{"url":"www.yztaotu.com/yazhou/meimei"},{"url":"www.jrmeinv.com/juru/jrmeinvtus"},{"url":"www.xsmzt.com/xue/shengmeis"},{"url":"www.zbtaotu.com/zhu/botus"},{"url":"www.tsmnt.com/yuan/tuntus"},{"url":"www.zbmzt.com/zhubo/tumeimeis"},{"url":"www.xjjtaotu.com/xiao/jiejies"},{"url":"www.symzt.com/se/yintus"},{"url":"www.ywmeitu.com/you/nvtuk"},{"url":"www.whmeinv.com/wag/hongnvphoto"},{"url":"www.ftmeinv.com/fei/tungirlsp"},{"url":"www.xjjmzt.com/xiaojiejie/tuapicgs"},{"url":"www.smtaotu.com/simi/taomis"},{"url":"www.whtaotu.com/wang/hongtus"},{"url":"www.xstaotu.com/xiu/meises"},{"url":"www.jdtaotu.com/jing/diantusde"},{"url":"www.xgyouwu.com/xing/ganyous"},{"url":"www.zbmeinv.com/zhubo/nvmeizimeis"},{"url":"www.gqyouwu.com/gqoqingyou/wus"},{"url":"www.mtmnt.com/meinv/tags"},{"url":"www.nmtaotu.com/nam/meituzis"},{"url":"www.jpyouwu.com/jiping/yutuwus"},{"url":"www.flmeitu.com/fuli/meimei"},{"url":"www.gqtaot.com/gaoqing/meinv"},{"url":"www.plmeitu.com/piao/tutus"},{"url":"www.xgwht.com/rentihtml/renwu"},{"url":"www.xztaotu.com/kanmm/guomo"},{"url":"www.fcrenti.com/rentiyishu/taotu"},{"url":"www.sfwht.com/art/meinv"},{"url":"www.gqsft.com/shijue/remen"},{"url":"www.yhmeinv.com/metart/wanghong"},{"url":"www.jdmnt.com/meinv/zhuanti"},{"url":"www.yctaotu.com/yaokan/gaoqing"},{"url":"www.wkrenti.com/kanmeinv/sheyin"},{"url":"www.yzrenti.com/renti/guomo"},{"url":"www.ymtaotu.com/guochang/moter"},{"url":"www.sptaotu.com/sipai/html"},{"url":"www.mttaotu.com/meinv/hot"},{"url":"www.wsgtu.com/wushengguang/daquan"},{"url":"www.ywtaotu.com/youwutu/meitaotus"},{"url":"www.flmzt.com/ful/itumeis"},{"url":"www.sftaotu.com/sifang/meinv"},{"url":"www.gcmeinv.com/guochan/quanji"},{"url":"www.nstaotu.com/nvsheng/image"},{"url":"www.xhtaotu.com/lumeimei/picture"},{"url":"www.jdwht.com/zg/meinv"},{"url":"www.mtmeinv.com/mote/html"},{"url":"www.gqwht.com/gaoqing/meinvji"},{"url":"www.xzmeitu.com/xiezhen/taotuji"},{"url":"www.jcwht.com/zipai/meinv"},{"url":"www.tptaotu.com/toupai/taotu"},{"url":"www.spyouwu.com/sipai/tupaiws"},{"url":"www.xgmeitu.com/xinggan/tui"},{"url":"www.plxzt.com/mianfei/taotu"},{"url":"www.jstaotu.com/juese/tupian"},{"url":"www.yhtaotu.com/youhuo/meinv"},{"url":"www.sytaotu.com/seying/motetags"},{"url":"www.taotudq.com/taotu/youwu"},{"url":"www.taotuqj.com/taotuqj/meinvlist"},{"url":"www.jpmzt.com/jingpin/meizi"},{"url":"www.xsmnt.com/pic/photos"},{"url":"www.nsxiez.com/nvshen/tutupic"},{"url":"www.nsmeitu.com/nvshen/mtpic"},{"url":"www.plmzt.com/piaoliang/mztags"},{"url":"www.yhmzt.com/youhuo/nvshen"},{"url":"www.mtmzt.com/mote/girls"},{"url":"www.xzmzt.com/xiezhen/nvzi"},{"url":"www.spmzt.com/sipai/meinvim"},{"url":"www.jpxzt.com/jingd/zimeis"},{"url":"www.wsgmzt.com/wushengguang/pictags"},{"url":"www.ywmzt.com/youwu/meitui"},{"url":"www.mfmzt.com/mianfei/nvzhuanti"},{"url":"www.snmeitu.com/shao/ssvipb"},{"url":"www.jcmzt.com/jing/qiantu"},{"url":"www.zpmzt.com/zi/wnvw"},{"url":"www.swmzt.com/si/fenlei"},{"url":"www.rsmzt.com/rou/aitu"},{"url":"www.snmzt.com/shaonv/picshaofs"},{"url":"www.thmzt.com/tao/meitao"},{"url":"www.gcmeitu.com/guoc/vipgirls"},{"url":"www.aimzt.com/ai/ssgirls"},{"url":"www.plwht.com/jin/caituvp"},{"url":"www.spmeitu.com/si/paituvip"},{"url":"www.yhmnt.com/ai/mmpicture"},{"url":"www.lmtaotu.com/lamei/aitus"},{"url":"www.mtwht.com/qiaot/aituns"},{"url":"www.tswht.com/jues/eyous"},{"url":"www.spmnt.com/sipaitu/spmtm"},{"url":"www.jpmnt.com/jipinmnv/tupics"},{"url":"www.xzmeitu.com/xiezhen/taotuj"}]}"""

    private val baseClassUrlJson = """{"baseClassUrlJson":[{"url":"www.ywmmt.com/html/mm"},{"url":"www.mnwht.com/art/meinvliebiao"},{"url":"www.ycmeinv.com/meinv/list"},{"url":"www.jcmeinv.com/jingcai/picture"},{"url":"www.ztxzt.com/juru/rutaotdm"},{"url":"www.mfsft.com/gaoqing/tulie"},{"url":"www.jptaotu.com/html/sylist"},{"url":"www.ycmzt.com/piaoliang/lista"},{"url":"www.flwht.com/beauty/list"},{"url":"www.threnti.com/metcn/mma"},{"url":"www.wjtaotu.com/wu/jitum"},{"url":"www.ztmeinv.com/zhuanti/meinvtum"},{"url":"www.mstaotu.com/mei/setum"},{"url":"www.tstaotu.com/te/sepianm"},{"url":"www.zftaotu.com/zhi/futum"},{"url":"www.mgtaotu.com/mei/guantaom"},{"url":"www.prmzt.com/pir/meizhim"},{"url":"www.xrtaotu.com/xiu/rentaom"},{"url":"www.jjtaotu.com/jiao/jiaotum"},{"url":"www.prmeinv.com/pirmei/nvtum"},{"url":"www.axtaotu.com/aix/tuxium"},{"url":"www.mgmeinv.com/mu/guam"},{"url":"www.xsmeinv.com/xingshang/meinvpicm"},{"url":"www.ugtaotu.com/uguo/ugirlsm"},{"url":"www.msmeinv.com/mei/setaom"},{"url":"www.flxzw.com/meir/utaom"},{"url":"www.axmeinv.com/ai/xiumeinvm"},{"url":"www.swtaotu.com/siw/atupm"},{"url":"www.hjtaotu.com/he/jitum"},{"url":"www.nsxzw.com/huan/yangtum"},{"url":"www.ugmeinv.com/uoug/angmeim"},{"url":"www.hytaotu.com/hua/hytaotum"},{"url":"www.xrmeinv.com/xiu/renmeim"},{"url":"www.zfmeinv.com/zhi/fumeinvm"},{"url":"www.jjmeinv.com/jiao/jiaomeinvm"},{"url":"www.zttaotu.com/zhuanti/taotum"},{"url":"www.jcxzt.com/yao/kantum"},{"url":"www.ykmeinv.com/yan/kongnvm"},{"url":"www.qjtaotu.com/quanji/tutupm"},{"url":"www.pmtaotu.com/pai/meitukm"},{"url":"www.ddtaotu.com/da/dankum"},{"url":"www.plxzw.com/wangh/ongtukm"},{"url":"www.mfxzt.com/zhuant/itukum"},{"url":"www.tstuku.com/te/setukum"},{"url":"www.fltuku.com/fuli/itukum"},{"url":"www.yhtuku.com/youh/uotukum"},{"url":"www.ycmeitu.com/yuan/meitupic"},{"url":"www.mttuku.com/mote/motkutm"},{"url":"www.xhtuku.com/xih/uantukm"},{"url":"www.qjtuku.com/quanj/jikutum"},{"url":"www.xzmnt.com/gaoqi/qingkutuom"},{"url":"www.sftuku.com/sif/fangtukum"},{"url":"www.yctuk.com/yuanc/chuangtkum"},{"url":"www.yktuk.com/yaok/kankutum"},{"url":"www.ywtuk.com/youw/wukutum"},{"url":"www.jctuk.com/jingca/caitukuuam"},{"url":"www.xstuk.com/xinsh/angukutm"},{"url":"www.xgtuk.com/xin/gguttkm"},{"url":"www.mztuk.com/mizhi/zhituktm"},{"url":"www.xztuk.com/xiezh/enuikrum"},{"url":"www.sytuk.com/shey/yingtiiurm"},{"url":"www.gcxzt.com/guoct/chanteksem"},{"url":"www.tsxzt.com/tese/sezhentum"},{"url":"www.gqxzt.com/gaoqingr/qingzxiem"},{"url":"www.xgxzt.com/xingganhg/ganturm"},{"url":"www.spxzt.com/shipad/paixztum"},{"url":"www.yhxzt.com/ykou/yhuodzxm"},{"url":"www.mtxzt.com/moterder/mtxztm"},{"url":"www.nsxzt.com/nvs/shentuxiem"},{"url":"www.jdxzt.com/jingda/diantuxm"},{"url":"www.ykxzt.com/ykao/kantuxzm"},{"url":"www.sfxzt.com/shifnag/fnhtuxszm"},{"url":"www.yhmeitu.com/you/huomm"},{"url":"www.mzmeitu.com/mei/zipicmms"},{"url":"www.qpmzt.com/qing/paibiao"},{"url":"www.yzmzt.com/ya/zhoutuam"},{"url":"www.ywsft.com/nv/shengyoupm"},{"url":"www.wkmzt.com/wo/kanmeinvp"},{"url":"www.brmzt.com/bao/rutup"},{"url":"www.wbmzt.com/wei/bomeizis"},{"url":"www.brtaotu.com/bao/rutuem"},{"url":"www.brmeinv.com/bao/rumeimm"},{"url":"www.qtmzt.com/qiao/tuntum"},{"url":"www.sfmnt.com/piaol/liangtkum"},{"url":"www.jrmzt.com/ju/rummtum"},{"url":"www.yztaotu.com/yazhou/html"},{"url":"www.jrmeinv.com/juru/jrmeinvtum"},{"url":"www.xsmzt.com/xue/shengmeim"},{"url":"www.zbtaotu.com/zhu/botum"},{"url":"www.tsmnt.com/yuan/tuntum"},{"url":"www.zbmzt.com/zhubo/tumeimeim"},{"url":"www.xjjtaotu.com/xiao/jiejiem"},{"url":"www.symzt.com/se/yintum"},{"url":"www.ywmeitu.com/you/wutmm"},{"url":"www.whmeinv.com/wag/hongnvs"},{"url":"www.ftmeinv.com/fei/tungirlm"},{"url":"www.xjjmzt.com/xiaojiejie/tuapicgm"},{"url":"www.smtaotu.com/simi/taomim"},{"url":"www.whtaotu.com/wang/hongtum"},{"url":"www.xstaotu.com/xiu/meisem"},{"url":"www.jdtaotu.com/jing/diantum"},{"url":"www.xgyouwu.com/xing/ganyoum"},{"url":"www.zbmeinv.com/zhubo/nvmeizimeim"},{"url":"www.gqyouwu.com/gqoqingyou/wum"},{"url":"www.mtmnt.com/meinv/mulu"},{"url":"www.nmtaotu.com/nam/meituzim"},{"url":"www.jpyouwu.com/jiping/yutuwum"},{"url":"www.flmeitu.com/fuli/meitulist"},{"url":"www.gqtaot.com/gaoqing/list"},{"url":"www.plmeitu.com/piao/lianglie"},{"url":"www.xgwht.com/rentihtml/a"},{"url":"www.xztaotu.com/kanmm/motere"},{"url":"www.fcrenti.com/rentiyishu/imgp"},{"url":"www.sfwht.com/art/sypic"},{"url":"www.gqsft.com/shijue/sheyinga"},{"url":"www.yhmeinv.com/metart/b"},{"url":"www.jdmnt.com/meinv/niebiao"},{"url":"www.yctaotu.com/yaokan/xinggan"},{"url":"www.wkrenti.com/kanmeinv/tupiana"},{"url":"www.yzrenti.com/renti/tulie"},{"url":"www.ymtaotu.com/guochan/list"},{"url":"www.sptaotu.com/sipai/liebiao"},{"url":"www.mttaotu.com/model/list"},{"url":"www.wsgtu.com/wushengguang/mulu"},{"url":"www.ywtaotu.com/youwutu/meitaotum"},{"url":"www.flmzt.com/ful/itumeim"},{"url":"www.sftaotu.com/sifang/menu"},{"url":"www.gcmeinv.com/guochan/tubiao"},{"url":"www.nstaotu.com/nvsheng/meinv"},{"url":"www.xhtaotu.com/lumeimei/seyin"},{"url":"www.jdwht.com/zg/list"},{"url":"www.mtmeinv.com/mote/lanmu"},{"url":"www.gqwht.com/gaoqing/meinvtu"},{"url":"www.xzmeitu.com/xiezhen/daquan"},{"url":"www.jcwht.com/zipai/nanmu"},{"url":"www.tptaotu.com/toupai/daohang"},{"url":"www.spyouwu.com/sipai/tupaiwm"},{"url":"www.xgmeitu.com/xinggan/meinvlist"},{"url":"www.plxzt.com/mianfei/image"},{"url":"www.jstaotu.com/juese/pic"},{"url":"www.yhtaotu.com/youhuo/img"},{"url":"www.sytaotu.com/seying/meinv"},{"url":"www.taotudq.com/taotu/listhtnl"},{"url":"www.taotuqj.com/taotuqj/piclist"},{"url":"www.jpmzt.com/jingpin/meizilist"},{"url":"www.xsmnt.com/pic/menu"},{"url":"www.nsxiez.com/nvshen/meimeitu"},{"url":"www.nsmeitu.com/nvshen/mtlist"},{"url":"www.plmzt.com/piaoliang/mztlist"},{"url":"www.yhmzt.com/youhuo/mmpict"},{"url":"www.mtmzt.com/mote/tutu"},{"url":"www.xzmzt.com/xiezhen/girllist"},{"url":"www.spmzt.com/sipai/meimzt"},{"url":"www.jpxzt.com/jingd/zimeim"},{"url":"www.wsgmzt.com/wushengguang/daohang"},{"url":"www.ywmzt.com/youwu/ptui"},{"url":"www.mfmzt.com/mianfei/nvtg"},{"url":"www.snmeitu.com/shao/nvbiao"},{"url":"www.jcmzt.com/jing/baitu"},{"url":"www.zpmzt.com/zi/wanpit"},{"url":"www.swmzt.com/si/tuwa"},{"url":"www.rsmzt.com/rou/mof"},{"url":"www.snmzt.com/shaonv/shaolisw"},{"url":"www.thmzt.com/tao/meilbi"},{"url":"www.gcmeitu.com/guoc/pipitu"},{"url":"www.aimzt.com/ai/meinvtuhsa"},{"url":"www.plwht.com/jin/caitup"},{"url":"www.spmeitu.com/si/paitum"},{"url":"www.yhmnt.com/ai/mm"},{"url":"www.lmtaotu.com/lamei/aitum"},{"url":"www.mtwht.com/qiaot/aitunm"},{"url":"www.tswht.com/jues/eyoum"},{"url":"www.spmnt.com/sipaitu/spmts"},{"url":"www.jpmnt.com/jipinmnv/tupicm"}]}"""

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
        return if (page == 1)
            myGet("https://www.95mm.net/home-ajax/index.html?tabcid=%E6%9C%80%E6%96%B0&append=list-home&paged=$page&query=&pos=home&page=$page&contentsPages=2")
        else
            myGet("https://www.95mm.net/home-ajax/index.html?tabcid=%E6%9C%80%E6%96%B0&append=list-home&paged=$page&query=&pos=home&page=$page&contentsPages=${page - 1}")
    }

    override fun latestUpdatesParse(response: Response): MangasPage = popularMangaParse(response)

    override fun mangaDetailsParse(response: Response): SManga = SManga.create().apply {
        var requestUtl = response.request().url().toString()
        if (requestUtl.indexOf("vip") >= 0) {
            author = "MM范 VIP"
            genre = "VIP"
            status = 3
            description = "VIP相册"
        } else {
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
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        var requestUtl = response.request().url().toString()
        var chapterList = ArrayList<SChapter>()
        if (requestUtl.indexOf("vip") >= 0) {
            chapterList.add(SChapter.create().apply {
                name = "Ch.1 点击此处观看VIP相册"
                url = requestUtl.replace("https://www.95mm.net", "")
            })
        } else {
            val body = response.body()!!.string()
            var document = Jsoup.parseBodyFragment(body)
            chapterList.add(SChapter.create().apply {
                name = document.select("div.d-none.d-md-block.breadcrumbs.mb-3.mb-md-4 span.current").text()
                url = document.select("div.post-content div.post div.nc-light-gallery p a.nc-light-gallery-item").attr("href").split("#")[0].replace("https://www.95mm.net", "")
            })
        }
        return chapterList
    }

    override fun pageListParse(response: Response): List<Page> {
        var requestUtl = response.request().url().toString()

        var htmlText = response.body()!!.string().trim()
        var body = Pattern.compile("\\s*|\t|\r|\n").matcher(htmlText).replaceAll("")
        val chapterImagesRegex = Regex("""dynamicEl\:(.*?)\,\]\}\)\}\)\;""")

        var pageJsonStr = chapterImagesRegex.find(body)?.groups?.get(1)?.value
            ?: throw Exception("pageCodeStr not found")
        pageJsonStr = pageJsonStr.replace("\'", "\"") + "]"

        var pageJson = JSONArray(pageJsonStr)
        var arrList = ArrayList<Page>(pageJson.length())
        for (i in 0 until pageJson.length()) {
            if (pageJson.getJSONObject(i).getString("downloadUrl").substring(pageJson.getJSONObject(i).getString("downloadUrl").length - 5, pageJson.getJSONObject(i).getString("downloadUrl").length).indexOf(".") >= 0) {
                arrList.add(Page(i, "", pageJson.getJSONObject(i).getString("downloadUrl")))
            } else {
                continue
            }
        }
        return arrList
    }

    override fun imageUrlParse(response: Response): String {
        throw UnsupportedOperationException("This method should not be called!")
    }

    private fun isInteger(str: String): Boolean {
        var pattern = Pattern.compile("^[-\\+]?[\\d]*$")
        return pattern.matcher(str).matches()
    }

    private var queryBaseUrl_ID = 0
    private var queryBaseUrl_ALLPage = 0
    private var queryBaseUrl_Page = 1

    private var filteUrl_ID = 0
    private var filteUrl_ALLPage = 0
    private var filteUrl_Page = 1

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query != "") {
            throw UnsupportedOperationException("http://${JSONObject(baseUrlJson).getJSONArray("baseUrlJson").getJSONObject(queryBaseUrl_ID).getString("url")}/s.asp?page=$queryBaseUrl_Page&act=topic&classid=&keyword=" + URLEncoder.encode(query, "GB2312"))
            return myGet("http://${JSONObject(baseUrlJson).getJSONArray("baseUrlJson").getJSONObject(queryBaseUrl_ID).getString("url")}/s.asp?page=$queryBaseUrl_Page&act=topic&classid=&keyword=" + URLEncoder.encode(query, "GB2312"))
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            throw UnsupportedOperationException("http://${JSONObject(classUrlJson).getJSONArray("classUrlJson").getJSONObject(filteUrl_ID).getString("url")}$params$filteUrl_Page.html")
            return myGet("http://${JSONObject(classUrlJson).getJSONArray("classUrlJson").getJSONObject(filteUrl_ID).getString("url")}$params$filteUrl_Page.html")
        }
    }

    override fun searchMangaParse(response: Response): MangasPage {
        var requestUrl = response.request().url().toString()
        if (requestUrl.contains("s.asp")) {
            val body = response.body()!!.string()
            var document = Jsoup.parseBodyFragment(body)
            if(queryBaseUrl_Page == 1){
                if(document.select("""div.pagelist form p a[title="尾页"]""").size == 0)
                    queryBaseUrl_ALLPage = 1
                else
                    queryBaseUrl_ALLPage = document.select("""div.pagelist form p a[title="尾页"]""").attr("href").split("page=")[1].split("&")[0].toInt()

            }
            if (queryBaseUrl_Page == queryBaseUrl_ALLPage) {
                queryBaseUrl_ID++
                queryBaseUrl_Page = 1
            } else
                queryBaseUrl_Page++
            var mangasElements = document.select("div.list ul li")
            var mangas = ArrayList<SManga>()
            if (mangasElements.size == 0) {
                queryBaseUrl_ID++
                queryBaseUrl_Page = 1
                return MangasPage(mangas, true)
            }
            for (mangaElement in mangasElements) {
                mangas.add(SManga.create().apply {
                    title = mangaElement.select("a").attr("title")
                    thumbnail_url = mangaElement.select("a img").attr("src")
                    url = requestUrl + mangaElement.select("a").attr("title")
                })
            }
            return MangasPage(mangas, true)
        } else {
            val body = response.body()!!.string()
            var document = Jsoup.parseBodyFragment(body)
            if(queryBaseUrl_Page == 1){
                if(document.select("""div.pagelist form p a[title="尾页"]""").size == 0)
                    queryBaseUrl_ALLPage = 1
                else
                    filteUrl_ALLPage = document.select("""div.pagelist form p a[title="尾页"]""").attr("href").split(".html")[0].split("_")[1].toInt()
            }
            if (filteUrl_Page == filteUrl_ALLPage) {
                filteUrl_ID++
                filteUrl_Page = 1
            } else
                filteUrl_Page++
            var mangasElements = document.select("div.list ul li")
            var mangas = ArrayList<SManga>(mangasElements.size)
            if (mangasElements.size == 0) {
                filteUrl_ID++
                filteUrl_Page = 1
                return MangasPage(mangas, false)
            }
            for (mangaElement in mangasElements) {
                mangas.add(SManga.create().apply {
                    title = mangaElement.select("a").attr("title")
                    thumbnail_url = mangaElement.select("a img").attr("src")
                    url = requestUrl + mangaElement.select("a").attr("title")
                })
            }
            return MangasPage(mangas, false)
        }
    }

    override fun getFilterList() = FilterList(
        ReaderFilter()
    )

    private open class ReaderFilter : UriPartFilter("查询分类", arrayOf(
        Pair("无圣光", "/wusenggang/"), Pair("pr社", "/pr/"), Pair("私拍", "/sipai/"), Pair("renti艺术", "/rentiyish/"), Pair("微博红人", "/weibo/"), Pair("萝莉", "/loli/"), Pair("秀人网", "/xiuren/"), Pair("蜜桃社", "/mitao/"), Pair("尤果网", "/ugirls/"), Pair("轰趴猫", "/Partycat/"), Pair("爱丝", "/aiss/"), Pair("画语界", "/XIAOYU/"), Pair("模范学院", "/mfstar/"), Pair("潘多拉", "/Pandora/"), Pair("蜜丝", "/missleg/"), Pair("御女郎", "/dkgirl/"), Pair("推女郎", "/tuigirl/"), Pair("爱蜜社", "/imiss/"), Pair("魅妍社", "/mistar/"), Pair("尤蜜荟", "/youmi/"), Pair("美媛馆", "/meiyuanguan/"), Pair("尤蜜Youmi", "/Youmi/"), Pair("果团网", "/girlt/"), Pair("星颜社", "/xingyan/"), Pair("禁忌摄影", "/jinjiseying/"), Pair("美腿宝贝", "/legbaby/"), Pair("头条女神", "/toutiaogirls/"), Pair("尤物馆", "/youwu/"), Pair("嗲囡囡", "/FEILIN/"), Pair("dk御女郎", "/dkgirl/"), Pair("雅拉伊", "/YALAY/"), Pair("克拉女神", "/kela/"), Pair("波萝社", "/bololi/"), Pair("ROSI写真", "/rosi/"), Pair("异思趣向", "/Iess/"), Pair("丝慕写真", "/simu/"), Pair("盘丝洞PANS", "/PANS/"), Pair("伊甸园", "/yidianyuan/"), Pair("青豆客", "/qingdouke/"), Pair("花漾show", "/huayang/"), Pair("花の颜", "/dkgirl/"), Pair("糖果画报", "/candy/"), Pair("兔几盟", "/Tukmo/"), Pair("猫萌榜", "/Micat/"), Pair("瑞丝馆", "/ruiss/"), Pair("优星馆", "/uxing/"), Pair("顽味生活", "/taste/"), Pair("激萌文化", "/kimoe/"), Pair("星乐园", "/leyuan/"), Pair("推女神", "/tgod/"), Pair("尤美", "/YouMei/"), Pair("猎女神", "/SLADY/"), Pair("无忌影社", "/wujiyingshe/"), Pair("风俗娘", "/fusuliang/"), Pair("阳光宝贝", "/sungirl/"), Pair("TBA美女", "/TBA/"), Pair("Graphis", "/Graphis/"), Pair("Sabra", "/wloobSabra/"), Pair("Girlz", "/wloob-Girlz-High/"), Pair("Digital", "/DigitalBooks/"), Pair("Minisuka", "/Minisukatv/"), Pair("TopQueen", "/TopQueen/"), Pair("自拍福利姬", "/zipaifuliji/"), Pair("映画系列", "/yinghuaxilie/"), Pair("领域系列", "/lingyuxilie/"), Pair("兔玩映画", "/tuwanyh/"), Pair("喵糖映画", "/miaotangyinhua/"), Pair("少女酱系列", "/wanghongjiang/"), Pair("亚女lu图", "/yazhanlutu/"), Pair("Factory", "/FantasyFactory/"), Pair("美尤网", "/meiyouwang/"), Pair("网红福利", "/zipaifuliji/"), Pair("V女郎", "/dongguanvnvlang/"), Pair("JVID女郎", "/JVID/"), Pair("杨晨晨", "/yangcc/"), Pair("小热巴", "/xiaoreba/"), Pair("艾莉", "/aili/"), Pair("王语纯", "/wangyuchun/"), Pair("徐微微mia", "/weimia/"), Pair("玛鲁娜", "/maluna/"), Pair("芝芝boody", "/boody/"), Pair("筱慧", "/xiaohui/"), Pair("尤妮丝", "/egg/"), Pair("Miko酱", "/Mikoj/"), Pair("尹菲", "/yifei/"), Pair("黄楽然", "/hangran/"), Pair("艾小青", "/aixiaoqing/"), Pair("卓娅祺", "/Cris/"), Pair("若兮", "/ruoxi/"), Pair("闫盼盼", "/yanpanpan/"), Pair("刘钰儿", "/liuyuer/"), Pair("柚木", "/youmu/"), Pair("阿朱", "/azhu/"), Pair("土肥圆", "/tufeiyuan/"), Pair("k8傲娇萌萌", "/k8/"), Pair("心妍小公主", "/xinyanxiaogongzhu/"), Pair("萌宝儿", "/mengboa/"), Pair("周妍希", "/tufeiyuan/"), Pair("糯美子", "/mini/"), Pair("李可可", "/likeke/"), Pair("林美惠子", "/Mieko/"), Pair("小狐狸", "/Sica/"), Pair("乔依琳", "/qiaoyilin/"), Pair("宋KiKi", "/kiki/"), Pair("小尤奈", "/xiaoyoul/"), Pair("绯月樱", "/Cherry/"), Pair("萌汉药", "/mhanyaobaby/"), Pair("易阳", "/yiyang/"), Pair("沈梦瑶", "/shengmy/"), Pair("赵小米", "/kittyz/"), Pair("冯木木", "/mumulris/"), Pair("刘飞儿", "/faye/"), Pair("夏美酱", "/xiameijiang/"), Pair("许诺", "/xuruo/"), Pair("妲己", "/toxic/"), Pair("张雨萌", "/zhangyumeng/"), Pair("柳侑绮", "/liuyouqi/"), Pair("小九月", "/xiaojiuyue/"), Pair("悦爷妖精", "/yueyeyaojing/"), Pair("徐cake", "/xucake/"), Pair("赵梦洁", "/zhaomengjie/"), Pair("王馨瑶", "/wangxingyao/"), Pair("米妮", "/mini/"), Pair("小甜心", "/yangcc/"), Pair("娜露", "/nalu/"), Pair("萌琪琪", "/mengqiqi/"), Pair("陈思琪", "/chensiqi/"), Pair("孙梦瑶", "/shunmengyao/"), Pair("温心怡", "/wenxinyi/"), Pair("sukki可儿", "/sukki/"), Pair("爱丽莎", "/ailisa/"), Pair("李丽莎", "/lilisha/"), Pair("梦心玥", "/mengxingyue/"), Pair("艾霓莎", "/ailisa/"), Pair("E杯奶茶", "/naicha/"), Pair("楚楚", "/chuchu/"), Pair("苍井优香", "/cjyouxiang/"), Pair("唐琪儿", "/tangqier/"), Pair("李梓熙", "/lizixi/"), Pair("李雅", "/Abby/"), Pair("熊吖", "/xiangya/"), Pair("刘娅希", "/liuyaxi/"), Pair("兜豆靓", "/doudouliang/"), Pair("宅兔兔", "/zhaitutu/"), Pair("雪千寻", "/xueqx/"), Pair("赵惟依", "/zhaohuaiyi/"), Pair("沈佳熹", "/shenjiajia/"), Pair("黄可", "/huangke/"), Pair("多香子", "/duoxiangzi/"), Pair("纯小希", "/chunxiaoxi/"), Pair("娜依灵儿", "/nayile/"), Pair("小探戈", "/xiaotange/"), Pair("朱可儿", "/zhukeer/"), Pair("梁莹", "/liangyin/"), Pair("王乔恩", "/wangqiaoen/"), Pair("周心怡", "/zhouxinyi/"), Pair("婕西儿", "/jiexier/"), Pair("李妍曦", "/liyanxi/"), Pair("白甜", "/baitian/"), Pair("佘贝拉", "/sbla/"), Pair("乔柯涵", "/qiaokehan/"), Pair("Angela喜欢猫", "/angelaxhm/"), Pair("李筱乔", "/lixiaoqiao/"), Pair("夏小秋", "/xiaxiaoqiu/"), Pair("刘雪妮", "/liuxueli/"), Pair("七宝", "/liuyouqi/"), Pair("程小烦", "/cxiaofan/"), Pair("于大乔", "/yudaqiao/"), Pair("王婉悠", "/wangwanyou/"), Pair("于姬", "/yuji/"), Pair("夏瑶", "/xiayao/"), Pair("于大小姐", "/yudaxiaojie/"), Pair("顾欣怡", "/guxingyi/"), Pair("青树", "/chervl/"), Pair("麦苹果", "/maipinguo/"), Pair("沐子熙", "/muzixi/"), Pair("妮儿", "/nier/"), Pair("本能", "/benneng/"), Pair("陆瓷", "/luci/"), Pair("谭晓彤", "/tanxiaotong/"), Pair("木木", "/mumu/"), Pair("叶佳颐", "/yejiayi/"), Pair("信悦儿", "/xinyueer/"), Pair("西希白兔", "/xixibaitu/"), Pair("韩子萱", "/hanzixuan/"), Pair("伊莉娜", "/yilina/"), Pair("李宓儿", "/limier/"), Pair("孟狐狸", "/menghuli/"), Pair("绮里嘉", "/yilijia/"), Pair("穆菲菲", "/mufeifei/"), Pair("李雪婷", "/lixueting/"), Pair("王瑞儿", "/wangruier/"), Pair("梓萱", "/zixuan/"), Pair("李七喜", "/liqixi/"), Pair("潘娇娇", "/panjiaojiao/"), Pair("嘉宝贝儿", "/jiabaobei/"), Pair("松果儿", "/songguoer/"), Pair("月音瞳", "/yueyingtong/"), Pair("栗子Riz", "/liziriz/"), Pair("苏可可", "/sukeke/"), Pair("谢芷馨", "/Sindy/"), Pair("笑笑", "/xiaoxiao/"), Pair("夏茉", "/xiamo/"), Pair("战姝羽", "/zhansy/"), Pair("邹晶晶", "/zhoujingjing/"), Pair("可儿", "/keer/"), Pair("盼盼已鸠", "/panpanyi/"), Pair("凯竹", "/kaizhu/"), Pair("李凌子", "/lilingzi/"), Pair("李思宁", "/SiByl/"), Pair("韩恩熙", "/hanenxi/"), Pair("陈秋雨", "/chenqiuyu/"), Pair("欣杨", "/xinyan/"), Pair("索菲", "/suofei/"), Pair("慕羽茜", "/muyuxi/"), Pair("陈雅漫", "/chenyaman/"), Pair("丽莉", "/Lily/"), Pair("张优", "/ayoyo/"), Pair("刘雪妮", "/verna/"), Pair("陈宇曦", "/chenyuxi/"), Pair("羽住", "/yuzu/"), Pair("杨伊", "/yangyi/"), Pair("黄歆苑", "/huangyunyi/"), Pair("何晨曦", "/hechenxi/"), Pair("久久", "/Aimee/"), Pair("猫宝", "/maobao/"), Pair("唐思琪", "/tangsiqi/"), Pair("考拉", "/koala/"), Pair("晓梦", "/xiaommay/"), Pair("白一晗", "/baiyihan/"), Pair("恩一", "/enyi/"), Pair("刘奕宁", "/Lynnng/"), Pair("奶昔", "/Kyra/"), Pair("丁筱南", "/dingxiaonan/"), Pair("杨漫妮", "/yangmanni/"), Pair("卤蛋luna", "/lunad/"), Pair("米娅Miya", "/Miya/"), Pair("Miki兔", "/Mikitu/"), Pair("顾灿", "/guchan/"), Pair("白沫", "/baimo/"), Pair("小沫琳", "/xiaomolin/"), Pair("雪儿Cier", "/xuercier/"), Pair("妤薇Vivian", "/Vivian/"), Pair("大城小爱Alice", "/Alice/"), Pair("葉晶金", "/yejinjin/"), Pair("唐婉儿Lucky", "/Lucky/"), Pair("楚恬Olivia", "/Olivia/"), Pair("桃子marry", "/marry/"), Pair("Annie安妮", "/Annie/"), Pair("童安琪", "/tonganqi/"), Pair("铃木美咲", "/MisakiSuzuki/"), Pair("木奈奈", "/munainai/"), Pair("顾奈奈", "/Emily/"), Pair("果儿Victoria", "/Victoria/"), Pair("林文文yooki", "/yooki/"), Pair("陈宇曦", "/chenyuxi/"), Pair("周于希", "/zhouyixi/"), Pair("三上悠亚", "/shansanyouya/"), Pair("pr社", "/pr/"), Pair("麻酥酥哟", "/masusu/"), Pair("福利姬", "/fuliji/"), Pair("少女映画", "/shaonvyinhua/"), Pair("私人玩物", "/sirenwanwu/"), Pair("原来是茜公举殿下", "/xigongjudianxia/"), Pair("云宝宝er", "/yunbaobao/"), Pair("隔壁小姐姐", "/gebixiaojiejie/"), Pair("小鸟酱", "/xiaoniaojiang/"), Pair("九尾狐狸m", "/jiuweihu/"), Pair("团子VIP", "/tuanzi/"), Pair("完具少女", "/wanjushaonv/"), Pair("你们的小秋秋", "/nmdexiaoqq/"), Pair("软萌萝莉小仙儿", "/ruanmengloli/"), Pair("发条少女", "/fatiaoshaonv/"), Pair("悠宝", "/youbao/"), Pair("甜味弥漫", "/tianweimf/"), Pair("奈樱少女", "/lanyshaonv/"), Pair("夏茉果果", "/xiaomogg/"), Pair("极品萝莉杏仁", "/xingren/"), Pair("萌白酱", "/mengbaojiang/"), Pair("魔法少女", "/mofashaonv/"), Pair("工口小妖精", "/gongkouxyaoj/"), Pair("Kanami酱", "/Kanami/"), Pair("宛如福利", "/wanruofuli/"), Pair("芒果酱", "/mangguojiang/"), Pair("瑶瑶", "/yaoyao/"), Pair("姗姗就打奥特曼", "/ssjdaaoteman/"), Pair("宇航员", "/yuhangyuan/"), Pair("棉尾兔几", "/mianweituji/"), Pair("萝莉液液酱", "/luoliyeyejiang/"), Pair("jk邪魔暖暖", "/jkxierluan/"), Pair("吃一口兔子", "/cyikutu/"), Pair("兜兜飞", "/doudoufei/"), Pair("花狸追", "/hualizui/"), Pair("小兔牙", "/xiaotuya/"), Pair("花野美", "/huayemei/"), Pair("甜心奶猫酱", "/tianxlmjia/"), Pair("穹妹", "/bcydqimei/"), Pair("趴在床單上", "/rrpzaichuans/"), Pair("我是你可爱的小猫", "/wsnkadxma/"), Pair("镜颜欢", "/jingyanhuan/"), Pair("千岁娇", "/qiansuijiao/"), Pair("西尔酱", "/loli/"), Pair("Mika啾", "/Mikajiu/"), Pair("福利", "/fuli/"), Pair("花狸追", "/hualizui/"), Pair("芝麻酱", "/zhimajiang/"), Pair("软妹摇摇乐", "/ruanmeiyaoyaole/"), Pair("疯猫ss", "/fenfmaoss/"), Pair("过期米线喵喵", "/guoqimixianmm/"), Pair("魔物喵", "/mowumiao/"), Pair("Nagesa魔物女", "/Nagesamowunv/"), Pair("少女", "/shaonv/"), Pair("萝莉", "/loli/"), Pair("学生妹", "/xuemei/"), Pair("护士", "/hushi/"), Pair("丁字裤", "/dingzhiku/"), Pair("女仆", "/nvpu/"), Pair("制服", "/zhifu/"), Pair("空姐", "/kongjie/"), Pair("巨乳", "/juru/"), Pair("捆绑", "/kunbang/"), Pair("翘臀", "/qiaotun/"), Pair("OL女郎", "/ol/"), Pair("兔女郎", "/tunvlang/"), Pair("一字马", "/yizima/"), Pair("SM", "/sm/"), Pair("美乳", "/juru/"), Pair("少妇", "/shaofu/"), Pair("女优", "/nvyou/"), Pair("辣妹", "/lamei/"), Pair("素人", "/shuren/"), Pair("熟女", "/shunv/"), Pair("爆乳", "/baoru/"), Pair("美腿", "/meitiu/"), Pair("丝袜", "/siwa/"), Pair("高跟鞋", "/gaogen/"), Pair("内衣", "/neiyi/"), Pair("泳衣", "/yongyi/"), Pair("旗袍", "/qipao/"), Pair("湿身", "/sishen/"), Pair("浴室", "/yushi/"), Pair("网袜", "/wangwa/"), Pair("美臀", "/meitun/"), Pair("写真", "/xiezheng/"), Pair("cosplay", "/cosplay/"), Pair("女星", "/nvxing/"), Pair("美女", "/meinv/"), Pair("风俗娘", "/fusuliang/"), Pair("日本美女", "/jp/"), Pair("韩国", "/hanguo/"), Pair("校服", "/xiaofu/"), Pair("透视", "/toushinv/"), Pair("半裸", "/banluonv/"), Pair("白嫩", "/bainennv/"), Pair("肉感", "/rougan/"), Pair("丰满", "/fengmannv/"), Pair("丰乳肥臀", "/fengrunv/"), Pair("户外", "/huwai/"), Pair("沙滩", "/shatan/"), Pair("私房照", "/sifangnv/"), Pair("风骚", "/fengshaonv/"), Pair("治愈系", "/zhiyuxi/"), Pair("嫩模", "/nenemonv/"), Pair("极品", "/jipinnv/"), Pair("美腿宝贝", "/legbaby/"), Pair("ROSI写真", "/rosi/"), Pair("异思趣向", "/Iess/"), Pair("丝慕写真", "/simu/"), Pair("纳丝摄影", "/nssheyin/"), Pair("爱丝", "/aiss/"), Pair("妖精视觉", "/yaojinshijue/"), Pair("SSA丝社", "/ssasishe/"), Pair("大生模拍", "/dsmopai/"), Pair("零度摄影", "/ldsheyin/"), Pair("奈丝写真", "/naisisheyin/"), Pair("思话", "/SiHua/"), Pair("盘丝洞PANS", "/PANS/"), Pair("SJA佳爷", "/sja/"), Pair("袜小喵", "/kittyWawa/"), Pair("物恋传媒", "/wlcm/"), Pair("SIEE丝意", "/SIEE/"), Pair("斯文传媒", "/siwencuanmei/"), Pair("战前女神", "/zhanqiannvshen/"), Pair("袜涩", "/wasexz/"), Pair("禁忌摄影", "/jinjiseying/"), Pair("tpimage", "/tpimage/"), Pair("梦丝女神", "/MSLASS/"), Pair("DISI", "/disi/"), Pair("拍美VIP", "/paimei/"), Pair("丝间舞", "/sjw/"), Pair("丝尚", "/Sityle/"), Pair("Beautyleg", "/Beautyleg/"), Pair("中国高跟", "/gaogengcn/"), Pair("黑丝爱", "/HeiSiAi/"), Pair("王朝贵足", "/wangchao/"), Pair("丽柜", "/ligui/"), Pair("中高艺", "/zhonggaoyi/"), Pair("爱秀", "/ishow/"), Pair("RQ-STAR写真", "/RQSTAr/"), Pair("4K-Star", "/4KStar/"), Pair("丽图摄影", "/litu/"), Pair("ru1mm写真", "/ru1mm/"), Pair("3Agirl", "/3Agirl/"), Pair("收费图包全集", "/siwamtvip/"), Pair("绝对领域", "/jueduilinyu/"), Pair("兔玩映画", "/tuwanyh/"), Pair("风之领域", "/fengzhilinyu/"), Pair("喵糖映画", "/miaotangyinhua/"), Pair("森萝财团", "/shenluocaituan/"), Pair("少女映画", "/shaonvyinhua/"), Pair("素人渔夫", "/shurenufu/"), Pair("木花琳琳是勇者", "/mhllsyz/"), Pair("桜桃喵", "/taomiao/"), Pair("过期米线喵喵", "/guoqimixianmm/"), Pair("星之迟迟", "/xinzcchi/"), Pair("轻兰映画", "/qinglanyinhua/"), Pair("喵写真", "/miaoxiezhen/"), Pair("少女秩序", "/shaonvzhixv/"), Pair("洛丽塔", "/luolita/"), Pair("魔物喵", "/mowumiao/"), Pair("花狸追", "/hualizui/"), Pair("软妹摇摇乐", "/ruanmeiyaoyaole/"), Pair("镜颜欢", "/jingyanhuan/"), Pair("面饼仙儿", "/mianbinxer/"), Pair("橙香静静", "/chenxjjin/"), Pair("柚木", "/youmu/"), Pair("艾莉", "/aili/"), Pair("宅兔兔", "/zhaitutu/"), Pair("女仆", "/nvpu/"), Pair("cos写真", "/cosplay/"), Pair("眼酱大魔王w", "/yanjingdmw/"), Pair("coser萌妹萝莉", "/cosermengmei/"), Pair("顽味生活", "/taste/"), Pair("激萌文化", "/kimoe/"), Pair("兔几盟", "/Tukmo/"), Pair("少女", "/shaonv/"), Pair("萝莉", "/loli/"), Pair("学生妹", "/xuemei/"), Pair("星乐园", "/leyuan/"), Pair("白丝", "/baisi/"), Pair("过膝袜", "/guoxiwa/"), Pair("JK制服", "/jkzhifu/"), Pair("映画系列", "/yinghuaxilie/"), Pair("领域系列", "/lingyuxilie/"), Pair("少女酱系列", "/wanghongjiang/"), Pair("一小央泽", "/yixiaoyanzhe/"), Pair("水淼", "/aquashuimiao/"), Pair("蜜汁猫裘", "/mizhimaoqiu/"), Pair("古川kagura", "/kagura/"), Pair("蠢沫沫", "/momomom/"), Pair("鬼畜瑶", "/guichuyao/"), Pair("二佐", "/erzhuoz/"), Pair("过期米线线喵", "/guoqimixianxianmiao/"), Pair("爱老师_PhD", "/ailaoshiphd/"), Pair("rioko凉凉子", "/rioko/"), Pair("", "/fengmaoss/"), Pair("爱宕", "/aidangcos/"), Pair("蕾姆", "/leimunvcos/"), Pair("狂三", "/kuangsancos/"), Pair("玛修", "/maxiucos/"), Pair("花嫁", "/huajianv/"), Pair("碧蓝航线", "/bihaihangxian/"), Pair("玉藻前", "/yuzhaoqian/"), Pair("贞德", "/zhendenvcos/"), Pair("穹妹", "/qionhmei/"), Pair("修女", "/xiunvcos/"), Pair("加藤惠", "/jiatenghui/"), Pair("南小鸟", "/nanxiaoliao/"), Pair("大凤", "/dafengss/"), Pair("玉玲珑", "/yulinglong/"), Pair("黑贞", "/heizhen/"), Pair("索尼子", "/suonizi/"), Pair("阿狸", "/alialiali/"), Pair("尼禄", "/nilunilu/"), Pair("猫女", "/maonvcos/"), Pair("明日方舟", "/mingrifangzhou/"), Pair("jk制服", "/jkzhifu/"), Pair("少女前线", "/shaonvqianxian/"), Pair("不知火舞", "/buzhihuwu/"), Pair("情趣", "/qingqunv/"), Pair("大尺度", "/dachidu/"), Pair("透视", "/toushinv/"), Pair("众筹", "/zhouchounv/"), Pair("私拍", "/sipainv/"), Pair("绝色", "/juesenv/"), Pair("嫩模", "/nenemonv/"), Pair("学妹", "/xuemeinv/"), Pair("黑丝", "/heisinv/"), Pair("私房", "/sifangnv/"), Pair("甜美", "/tianmei/"), Pair("女神", "/nvshennv/"), Pair("巨乳", "/jurunv/"), Pair("风骚", "/fengshaonv/"), Pair("饱满", "/baomannv/"), Pair("翘臀", "/qiaotunnv/"), Pair("奶牛", "/nainiunv/"), Pair("甜心", "/tianxinnv/"), Pair("粉嫩", "/fennennv/"), Pair("丰满", "/fengmannv/"), Pair("诱人", "/yourennv/"), Pair("香艳", "/xiangyannv/"), Pair("乳神", "/rushennv/"), Pair("校花", "/xiaohuanv/"), Pair("全裸", "/quanluonv/"), Pair("高中", "/gaozhongnv/"), Pair("可爱", "/keainv/"), Pair("小美女", "/xiaomeinv/"), Pair("秘书", "/mishunv/"), Pair("比基尼", "/bijininv/"), Pair("丰胸", "/fengxiongnv/"), Pair("唯美", "/weimeinv/"), Pair("家居", "/jiajunv/"), Pair("童颜", "/tongyannv/"), Pair("双峰", "/shuangfengnv/"), Pair("性感", "/xinggannv/"), Pair("真空", "/zhengkongnv/"), Pair("肚兜", "/dudounv/"), Pair("纯情", "/chunqingnv/"), Pair("火辣", "/huolanv/"), Pair("连体衣", "/liantiyi/"), Pair("吊带", "/diaodainv/"), Pair("写真", "/xiezhengnv/"), Pair("魅惑", "/meihuonv/"), Pair("蕾丝", "/leisinv/"), Pair("酒店", "/jiudiannv/"), Pair("勾魂", "/gouhunnv/"), Pair("浑圆", "/hunyuannv/"), Pair("极品", "/jipinnv/"), Pair("白皙", "/baixinv/"), Pair("镂空", "/loukongnv/"), Pair("娇躯", "/jiaoqunv/"), Pair("惹火", "/rehuonv/"), Pair("丰乳", "/fengrunv/"), Pair("精品", "/jinpinnv/"), Pair("玲珑", "/linglongnv/"), Pair("情欲", "/qinyunv/"), Pair("风情", "/fenqingnv/"), Pair("诱惑", "/youhuonv/"), Pair("姐妹", "/jiemeinv/"), Pair("傲人", "/aorenvn/"), Pair("牛仔裤", "/niuzainv/"), Pair("玉体", "/yutinv/"), Pair("半裸", "/banluonv/"), Pair("无内", "/wuneinv/"), Pair("内衣", "/neiyinv/"), Pair("女孩", "/nvhainv/"), Pair("身材", "/shengcainv/"), Pair("模特", "/motenv/"), Pair("妩媚", "/wumeinv/"), Pair("领域", "/linyunv/"), Pair("御姐", "/yujienv/"), Pair("衬衫", "/cunshannv/"), Pair("美乳", "/meirunv/"), Pair("大胆", "/dadannv/"), Pair("成熟", "/chengshunv/"), Pair("睡衣", "/suiyinv/"), Pair("曼妙", "/manmiaonv/"), Pair("妖娆", "/yaoraonv/"), Pair("亮丝", "/liangsinv/"), Pair("私人", "/sirenvn/"), Pair("萌萌", "/mengmengnv/"), Pair("女王", "/nvwangnv/"), Pair("36d大奶", "/36d/"), Pair("特刊", "/tekannv/"), Pair("高清", "/gaoqingnv/"), Pair("尤物", "/youwunv/"), Pair("美人", "/meirennv/"), Pair("薄纱", "/boshanv/"), Pair("精选", "/jingxuannv/"), Pair("女生", "/ncsennv/"), Pair("短裙", "/duanqunnv/"), Pair("娇艳", "/jiaoyannv/"), Pair("紧身", "/jinshengyinv/"), Pair("肉色", "/rousenv/"), Pair("激情", "/jiqingnv/"), Pair("闺房", "/guifangnv/"), Pair("大奶", "/danainv/"), Pair("颜值", "/yaznhinv/"), Pair("小背心", "/xiaobeixinnv/"), Pair("绝美", "/juemeinv/"), Pair("办公室", "/bangongshinv/"), Pair("混血", "/hunxuenv/"), Pair("特辑", "/tejinv/"), Pair("出品", "/chupinnv/"), Pair("作品", "/zuoinnv/"), Pair("微博", "/weibonv/"), Pair("美腿", "/meituinv/"), Pair("抹胸", "/moxiongnv/"), Pair("包臀", "/baotunnv/"), Pair("高叉", "/gaochanv/"), Pair("白金", "/baijinnv/"), Pair("短裤", "/duankunv/"), Pair("流出", "/liuchunv/"), Pair("酥胸", "/shuxiongnv/"), Pair("乳贴", "/rutienv/"), Pair("姐姐", "/jiejienv/"), Pair("死库", "/sikunv/"), Pair("玉足", "/yuzunv/"), Pair("女友", "/nvyounv/"), Pair("马尾", "/maweinv/"), Pair("职场", "/zhichangnv/"), Pair("精致", "/jinzhinv/"), Pair("佳人", "/jiarennv/"), Pair("完美", "/wanmeinv/"), Pair("情人", "/qingrennv/"), Pair("美胸", "/meixiongnv/"), Pair("清新", "/qingxinnv/"), Pair("撩人", "/liaorenvn/"), Pair("极致", "/jizhinv/"), Pair("翘臀", "/qiaotunnv/"), Pair("肌肤", "/jifunv/"), Pair("曼妙", "/manmiaonv/"), Pair("粉色", "/fensenv/"), Pair("温柔", "/wenrounv/"), Pair("胸器", "/xiongqinv/"), Pair("婚纱", "/hunshanv/"), Pair("修长", "/xiuchangnv/"), Pair("曲线", "/quxiannv/"), Pair("销魂", "/xiaohunnv/"), Pair("白嫩", "/bainennv/"), Pair("大胸", "/daxiongnv/"), Pair("透明", "/toumingnv/"), Pair("豪乳", "/haorunv/"), Pair("嫩妹", "/nenmeinv/"), Pair("床上", "/chuangshangnv/"), Pair("露出", "/luchunv/"), Pair("妖精", "/yaojingnv/"), Pair("嫩妹", "/nenmei/"), Pair("萌妹", "/mengmei/"), Pair("chokmoson作品", "/chokmoson/"), Pair("唐兴作品", "/tangxin/"), Pair("无忌影社", "/wujiyingshe/"), Pair("希威社", "/xiweishe/"), Pair("V女郎", "/vnvlang/"), Pair("花狐狸出品", "/HuaFox/"), Pair("EROONICHAN出品", "/EROONICHAN/"), Pair("北娃大王", "/dawang/"), Pair("PERRY.X作品", "/PERRY/"), Pair("sweetlyman作品", "/sweetlyman/"), Pair("WANIMAL出品", "/WANIMAL/"), Pair("段王爷", "/duanwangye/"), Pair("森萝财团", "/shenluocaituan/"), Pair("风之领域", "/fengzhilinyu/"), Pair("素人渔夫", "/shurenufu/"), Pair("旅拍全集", "/nvpai/"), Pair("普吉岛旅拍", "/pujidao/"), Pair("塞班岛旅拍", "/saibandao/"), Pair("苏梅岛旅拍", "/sumeidao/"), Pair("巴厘岛旅拍", "/balidao/"), Pair("马尔代夫旅拍", "/maerdaifu/"), Pair("沙巴旅拍", "/shaba/"), Pair("越南旅拍", "/yuenan/"), Pair("大理旅拍", "/dali/"), Pair("重庆旅拍", "/chongqing/"), Pair("三亚旅拍", "/shanya/"), Pair("云曼旅拍", "/yunman/"), Pair("御水温泉旅拍", "/yunshui/")
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
