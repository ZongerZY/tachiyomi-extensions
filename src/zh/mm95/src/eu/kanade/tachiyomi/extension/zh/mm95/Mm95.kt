package eu.kanade.tachiyomi.extension.zh.mm95

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import java.util.regex.Pattern
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class Mm95 : HttpSource() {

    override val name = "写真:MM范"
    override val baseUrl = "https://www.95mm.net"
    override val lang = "zh"
    override val supportsLatest = true

    private val vipManga = """[{"title":"CHAEYEONG2016.05.07[44P_200MB]","url":"/9199.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/6/f/a/6fa51333-f084-45d7-878b-73d9b9e6ebae.jpg"},
{"title":"katoon-01[97P_136M]","url":"/9200.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/1/e/d/1ed21fa2-7df6-403b-b613-a9196feeb8f1.jpg"},
{"title":"CAPRICE_xclusive_JamSession_88P_117M","url":"/9201.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/d/c/b/dcbd662f-ac44-4b75-bbe5-9a11a53d25a8.jpg"},
{"title":"COSIMAT_Ulyana_FollowMe_99P_264M","url":"/9202.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/4/b/d/4bd4960a-745b-4599-9499-82b347786601.jpg"},
{"title":"album_title","url":"/9203.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/3/8/e/38e73b3f-38a3-4778-b4aa-15532e29c32b.jpg"},
{"title":"DARAW_TomLeonard_Classy_110P_381M","url":"/9204.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/e/0/3/e030cdc1-f70d-435c-83db-63e226db2deb.jpg"},
{"title":"IZABELLA_Platonoff_TakeMe_113P_285M","url":"/9205.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/a/8/3/a83f0afb-4631-48f8-8a63-3d7f0a899121.jpg"},
{"title":"CARINE_SvenWildhan_AllYouWant_116P_430M","url":"/9206.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/9/f/a/9fa7e8d3-e22f-4c80-8451-b855594ad963.jpg"},
{"title":"JOANNA_Platonoff_Glamorous_105P_231M","url":"/9207.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/e/1/3/e1346397-bc31-4b8e-accd-988972aca598.jpg"},
{"title":"IZABELLA_SvenWildhan_Desire_110P_454M","url":"/9208.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/2/d/c/2dc4ba9f-a982-4cc4-9d79-3abfe220a606.jpg"},
{"title":"CARINE_SvenWildhan_IAmYours_108P_477M","url":"/9209.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/a/8/6/a8699395-8946-45bb-a9e5-dab119045666.jpg"},
{"title":"JOANNA_Platonoff_Love_109P_153M","url":"/9210.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/c/4/8/c48cbf16-99c1-42c6-9b90-50286a88d448.jpg"},
{"title":"JOANNA_Platonoff_SatisfyMe_109P_182M","url":"/9211.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/0/c/2/0c2631d1-fd36-4b40-9e10-8bd860bf7154.jpg"},
{"title":"jinny-jang-03_128P","url":"/9212.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/6/5/d/65de85f6-7730-49f2-b90c-297babececcd.jpg"},
{"title":"CHAEYEONG2016.03.29[169P_404M]","url":"/9213.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/3/6/9/369f9357-33f6-4066-b3c1-9b0c8259fe0a.jpg"},
{"title":"IEL2016.05.06[214P_534M]","url":"/9214.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/e/1/c/e1c3f8f3-9b08-4517-869e-f814a42ae115.jpg"},
{"title":"KAROE_SvenWildhan_NoNeedToExplain_103P_455M","url":"/9215.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/9/1/7/917682f6-1b02-4dbe-a32d-ec3d856872e5.jpg"},
{"title":"BREEH_AlexandrPetek_AllDay_85P_420M","url":"/9216.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/4/9/4/494d748a-b8fa-4700-9110-79cb60d697c7.jpg"},
{"title":"CLOVER_xclusive_DreamsLikeThis_123P_233M","url":"/9217.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/9/1/3/91366b6a-6f32-4480-9225-dbcdd40daf95.jpg"},
{"title":"karina_160P_605M","url":"/9218.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/e/c/1/ec149b43-27f4-42c0-babf-51b6ad73ea1b.jpg"},
{"title":"title","url":"/9228.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/e/5/9/e597c90d-5d3f-4ff8-a204-cf71f7d0efd2.jpg"},
{"title":"title","url":"/9230.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/e/0/1/e0199314-3533-4261-aeef-e6750555b7a1.jpg"},
{"title":"title","url":"/9231.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/9/8/b/98b9f78f-d81d-4586-9b66-9cc7d249e092.jpg"},
{"title":"title","url":"/9232.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/f/4/c/f4cf3270-c5a3-403e-8ef5-a4ad01b7b389.jpg"},
{"title":"title","url":"/9233.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/a/7/a/a7a21f88-5a04-41b0-a693-7bde44b217d5.jpg"},
{"title":"title","url":"/9234.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/0/3/6/03699f4c-326f-4478-9fa7-5509d9a076c6.jpg"},
{"title":"title","url":"/9235.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/7/4/8/74828cd3-87cf-434f-aefd-fd14b029aa6c.jpg"},
{"title":"title","url":"/9236.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/9/8/c/98c0692c-31d2-454d-8e4f-d23fbb5fd0b2.jpg"},
{"title":"白色女神hannah-02","url":"/9237.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/7/d/6/7d6b6e27-379a-4cc1-a503-5e1bd0725e60.jpg"},
{"title":"身着白色睡衣，与你分享早上第一缕阳光","url":"/9238.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/3/6/2/3628b968-45f9-4f9f-8f48-1809586564f9.jpg"},
{"title":"琴早妃Saki Koto海边嬉戏，景色很美，人如画中","url":"/9239.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/4/1/2/412003d5-a23b-44d9-91a0-c92e07988291.jpg"},
{"title":"areeya-oki 第一季","url":"/9240.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/5/b/e/5be093d9-55e8-4b1a-907d-55424356689f.jpg"},
{"title":"Ryo Shinohara 性感来袭","url":"/9241.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/1/5/e/15e7ae51-cb67-44f8-9b3f-c35fccb925ea.jpg"},
{"title":"性感美腿HD高清美的窒息身临其境","url":"/9242.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/0/4/8/0488cc1d-b3e7-44e6-9205-dc338fcc468a.jpg"},
{"title":"相崎琴音Kotone Kisaki","url":"/9243.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/1/9/a/19a783f1-1bb6-4998-95a2-85f3457e550f.jpg"},
{"title":"少女的年纪 少女的情愫 少女的睡衣 少女的心思","url":"/9244.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/d/2/9/d2989ec3-0707-4973-ba06-8364c54ac6cb.jpg"},
{"title":"初为少妇气质短发五官精致何须勾引","url":"/9245.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/f/3/8/f38a583e-5d08-40d3-bd44-42145c0daaa8.jpg"},
{"title":"极品身材校花辜怡媃","url":"/9246.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/2/4/2/24285a6f-cefb-4854-8fcb-b63af41dd9a6.jpg"},
{"title":"美乳女神小鸟酱之小草莓系列","url":"/9247.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/3/c/d/3cd84eb4-1f24-4d1a-8546-36e4c6cb7bff.jpg"},
{"title":"katoon-01[97P_136M] (1)","url":"/9248.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/f/b/7/fb72bdb8-6691-4efb-9881-820f3aee29c1.jpg"},
{"title":"CAPRICE_xclusive_JamSession_88P_117M (1)","url":"/9257.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/5/2/2/52246aac-34da-4c93-b7ef-2d0cce5c8cff.jpg"},
{"title":"COSIMAT_Ulyana_FollowMe_99P_264M (1)","url":"/9258.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/d/b/3/db33d26c-ed18-4a2e-853f-020d9d5afbe0.jpg"},
{"title":"DARAW_TomLeonard_Classy_110P_381M (1)","url":"/9259.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/d/4/c/d4cf170b-100c-4ede-a424-45386394123a.jpg"},
{"title":"IZABELLA_Platonoff_TakeMe_113P_285M (1)","url":"/9260.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/8/b/a/8babb019-706f-4380-baff-40b40a0c5b64.jpg"},
{"title":"CARINE_SvenWildhan_AllYouWant_116P_430M (1)","url":"/9261.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/f/6/a/f6a3ce4f-3c95-4331-916f-7c30a8aab237.jpg"},
{"title":"JOANNA_Platonoff_Glamorous_105P_231M (1)","url":"/9262.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/9/f/2/9f273e05-b08b-47be-893b-82e5e8bc23a7.jpg"},
{"title":"IZABELLA_SvenWildhan_Desire_110P_454M (1)","url":"/9263.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/9/5/7/9571dcd1-6057-4ac7-87e7-0fdefd1ee21b.jpg"},
{"title":"CARINE_SvenWildhan_IAmYours_108P_477M (1)","url":"/9264.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/1/0/9/109c9be7-0e7b-4e6f-b9fa-6563de68079c.jpg"},
{"title":"JOANNA_Platonoff_Love_109P_153M (1)","url":"/9265.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/8/0/1/80101f09-c31c-4c4a-ab1b-036e86699a7e.jpg"},
{"title":"JOANNA_Platonoff_SatisfyMe_109P_182M (1)","url":"/9266.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/5/5/0/55069ef7-b295-402a-9e4d-673855a3b1cb.jpg"},
{"title":"jinny-jang-03_128P (1)","url":"/9267.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/8/f/3/8f3be701-45d1-43a7-8f48-0c27e5d86594.jpg"},
{"title":"jinny-jang-03_128P (1) (2)","url":"/9268.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/8/9/8/8982e859-1b73-417c-992c-7d1e701e04df.jpg"},
{"title":"IEL2016.05.06[214P_534M] (1)","url":"/9269.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/c/a/0/ca0927d6-b6f3-4df3-b90e-00008816ad12.jpg"},
{"title":"IEL2016.05.06[214P_534M] (2)","url":"/9270.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/5/4/d/54ddb7f0-c4b1-487e-92aa-83e6df13faca.jpg"},
{"title":"IEL2016.05.06[214P_534M] (1) (2) (3)","url":"/9271.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/e/e/c/eec1ca3a-04ff-4e7a-9df6-abe68c9dd249.jpg"},
{"title":"KAROE_SvenWildhan_NoNeedToExplain_103P_455M (1)","url":"/9272.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/6/1/8/618b9249-ca32-4156-950d-39d6e2dd1a5b.jpg"},
{"title":"BREEH_AlexandrPetek_AllDay_85P_420M (1)","url":"/9273.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/9/1/5/915da4d7-d9c4-4b29-a141-b1efac7c2d6c.jpg"},
{"title":"CLOVER_xclusive_DreamsLikeThis_123P_233M (1)","url":"/9274.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/f/2/9/f29809a8-7c12-47a1-ab7e-66402274b6cf.jpg"},
{"title":"CLOVER_xclusive_DreamsLikeThis_123P_233M (2)","url":"/9275.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/8/3/3/83343c3e-08f0-4eb8-9077-f8c8f887e92c.jpg"},
{"title":"karina_160P_605M (1)","url":"/9276.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/f/7/8/f78a5b04-8103-4098-baff-f8740ab58f9d.jpg"},
{"title":"karina_160P_605M (1) (2)","url":"/9277.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/e/7/4/e74a7d70-3767-4c51-82b9-b448cea433f8.jpg"},
{"title":"title (1)","url":"/9278.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/9/0/c/90cf44b6-1206-4bce-ad45-8758b749cb5f.jpg"},
{"title":"title (1)","url":"/9279.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/4/1/3/413b1a92-8539-4a88-a6e7-5f634ad4790b.jpg"},
{"title":"title (1) (2)","url":"/9280.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/f/5/0/f506ac84-5946-40e2-9801-576083df59df.jpg"},
{"title":"title (1)","url":"/9281.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/9/0/f/90f74746-5218-4468-89e5-353ff62bc42b.jpg"},
{"title":"title (1)","url":"/9282.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/7/a/9/7a98807a-06ea-46f7-b276-92cf31c8884b.jpg"},
{"title":"title (1)","url":"/9283.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/a/1/b/a1b0a861-d0fc-42a1-bb2b-adfafb6af354.jpg"},
{"title":"title (1) (2)","url":"/9284.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/4/1/a/41a3c281-b91c-4635-a170-18aefc8dbb1b.jpg"},
{"title":"title (1) (2) (3)","url":"/9285.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/4/0/6/406804ff-913d-4ce9-9211-704c5eee2982.jpg"},
{"title":"title (1) (2) (3) (4)","url":"/9286.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/0/e/4/0e4ced15-f117-47f1-aeda-f5673e690a86.jpg"},
{"title":"title (1) (2) (3) (4) (5)","url":"/9287.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/e/e/2/ee28954d-d365-4e6b-9ce8-d0a88d4d7422.jpg"},
{"title":"title (1)","url":"/9288.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/c/9/9/c9989b1a-4f52-4df6-88d2-8425111cb98b.jpg"},
{"title":"title (1) (2)","url":"/9289.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/8/8/3/8830f608-7a75-4241-bf82-200974d6a1d3.jpg"},
{"title":"title (1) (2) (3)","url":"/9290.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/c/9/5/c95c380e-98b5-49e9-85ba-588badb4e8d4.jpg"},
{"title":"title (1) (2) (3) (4)","url":"/9291.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/5/6/d/56d4c9d3-4832-4d6e-896c-c092327dbb5e.jpg"},
{"title":"白色女神hannah-02 (1)","url":"/9292.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/1/4/c/14c4ecbf-1aa8-486c-995e-582fba9ebaf3.jpg"},
{"title":"白色女神hannah-02 (2)","url":"/9293.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/d/e/1/de1c9227-2feb-46ae-842f-1cc244bc3dc5.jpg"},
{"title":"琴早妃Saki Koto海边嬉戏，景色很美，人如画中 (1)","url":"/9294.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/e/4/3/e433d33e-20bf-41c4-a5cc-15efd4fd527a.jpg"},
{"title":"areeya-oki 第二季","url":"/9295.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/a/7/e/a7e6b933-c6d0-46e9-80c4-d674d7f445b4.jpg"},
{"title":"Ryo Shinohara 性感来袭 (1)","url":"/9296.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/f/4/3/f432b088-4bd4-47cc-9c6e-7c233df71ba4.jpg"},
{"title":"相崎琴音Kotone Kisaki (1)","url":"/9297.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/7/d/9/7d9bda60-9af0-491a-a297-8c693236152a.jpg"},
{"title":"初为少妇气质短发五官精致何须勾引 (1)","url":"/9298.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/6/e/0/6e00a2f4-5b69-488e-ad53-8311ef4925e0.jpg"},
{"title":"极品身材校花辜怡媃 (1)","url":"/9299.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/8/9/8/89821e7c-e9cc-448b-8f6c-8094d0eff4cc.jpg"},
{"title":"极品身材校花辜怡媃 (2)","url":"/9300.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/0/1/3/013a766b-63f8-4f7a-bbbe-c3e4fff39e88.jpg"},
{"title":"性感30P","url":"/9301.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/6/b/7/6b773c2c-efa1-4dcb-bad5-392c8cd9445c.jpg"},
{"title":"日本G奶女优黑川绮罗 Kirara Kurokawa","url":"/9302.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/7/7/e/77e73e95-e8d1-4e06-921d-50ebbe4cea65.jpg"},
{"title":"粉嫩小女友自拍私照","url":"/9303.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/a/5/6/a562b39c-e722-435d-a14e-06e4d8ae6354.jpg"},
{"title":"CHAEYEONG2016","url":"/9304.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/f/e/b/febf210f-0571-44c0-9142-a828e1415507.jpg"},
{"title":"魔鬼身材巨乳小姐姐大尺度性感自拍","url":"/9307.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/9/a/e/9aef5aab-5aad-4285-a119-dde96573a756.jpg"},
{"title":"音羽里子Reon Otowa","url":"/9308.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/0/c/9/0c9c74b2-a096-49e8-b8f2-226e1a0e6669.jpg"},
{"title":"身材五官都很不错的模特小尺度私拍","url":"/9309.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/f/1/c/f1c5199d-288f-4960-885f-6e7124ee2821.jpg"},
{"title":"粉嫩肉丝","url":"/9310.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/8/1/6/8169b85e-858e-4034-8f81-3de97d8771b3.jpg"},
{"title":"PartyCat极品女神特刊 1","url":"/9311.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/a/a/5/aa54bfa5-525f-49ad-a0b0-91a8dabb5bbb.jpg"},
{"title":"PartyCat极品女神特刊 2","url":"/9312.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/9/0/d/90dab127-3f09-47b2-ba8e-172dad410f10.jpg"},
{"title":"PartyCat极品女神特刊 3","url":"/9313.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/f/9/b/f9b2dd6e-3b76-4e29-9f57-084b1b132be4.jpg"},
{"title":"PartyCat极品女神特刊 4","url":"/9398.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/6/a/1/6a15bfb2-e27e-403c-ba97-bd376ada86b0.jpg"},
{"title":"Risa Kasumi","url":"/9399.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/5/8/7/5872f15a-c189-4ff9-8dc7-d54d1d58f91b.jpg"},
{"title":"靴下绅士缌先生豪华黑丝篇-情趣黑丝女神骚浪挑逗 大屌无套插入快速猛操 淫荡浪叫 高清私拍62P 高清1080P版","url":"/9400.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/b/1/9/b1994bf8-43c0-4900-afc5-2ca23ae56f93.jpg"},
{"title":"Ellie","url":"/9401.html","thumbnail_url":"https://cdn.zzdaye.com/images/vip/5/c/c/5ccc0781-622c-489e-8715-5353b232419e.jpg"}]"""

    private fun myGet(url: String) = GET(url)
        .newBuilder()
        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
        .header("Accept-Encoding", "")
        .header("Accept-Language", "zh-CN,zh;q=0.9")
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
        .header("X-Requested-With", "XMLHttpRequest")
        .header("Sec-Fetch-Dest", "document")
        .header("Sec-Fetch-Mode", "navigate")
        .header("Sec-Fetch-Site", "none")
        .header("Sec-Fetch-User", "?1")
        .header("Upgrade-Insecure-Requests", "1")
        .header("Connection", "keep-alive")
        .header("Host", "www.95mm.net")
        .build()!!

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
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)

        title = document.select("div.d-none.d-md-block.breadcrumbs.mb-3.mb-md-4 span.current").text()
        thumbnail_url = document.select("div.post-content div.post div.nc-light-gallery p a.nc-light-gallery-item img").attr("src")
        author = "MM范"
        var genreElements = document.select("div.post-tags mt-3 mt-md-4 a[rel=tag]")
        var genreList = ArrayList<String>(genreElements.size)
        for (genreElement in genreElements) {
            genreList.add(genreElement.text())
        }
        genre = genreList.joinToString(", ")
        status = 3
        description = document.select("div.d-none.d-md-block.breadcrumbs.mb-3.mb-md-4 span.current").text()
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val body = response.body()!!.string()
        var document = Jsoup.parseBodyFragment(body)
        var chapterList = ArrayList<SChapter>()
        chapterList.add(SChapter.create().apply {
            name = document.select("div.d-none.d-md-block.breadcrumbs.mb-3.mb-md-4 span.current").text()
            url = document.select("div.post-content div.post div.nc-light-gallery p a.nc-light-gallery-item").attr("href").split("#")[0].replace("https://www.95mm.net", "")
        })
        return chapterList
    }

    override fun pageListParse(response: Response): List<Page> {
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

    override fun searchMangaParse(response: Response): MangasPage {
        var requestUtl = response.request().url().toString()
        val chapterImagesRegex = Regex("""95mm.net/(.*?).html""")
        if (requestUtl.indexOf("vip") >= 0) {
            var mangasJson = JSONArray(vipManga)
            var mangas = ArrayList<SManga>(mangasJson.length())
            for (i in 0 until mangasJson.length()) {
                mangas.add(SManga.create().apply {
                    title = mangasJson.getJSONObject(i).getString("title")
                    thumbnail_url = mangasJson.getJSONObject(i).getString("thumbnail_url")
                    url = mangasJson.getJSONObject(i).getString("url")
                })
            }
            return MangasPage(mangas, false)
        } else if (isInteger(chapterImagesRegex.find(requestUtl)?.groups?.get(1)?.value.toString())) {
            val body = response.body()!!.string()
            var document = Jsoup.parseBodyFragment(body)
            var mangas = ArrayList<SManga>(1)
            mangas.add(SManga.create().apply {
                title = document.select("div.d-none.d-md-block.breadcrumbs.mb-3.mb-md-4 span.current").text()
                thumbnail_url = document.select("div.post-content div.post div.nc-light-gallery p a.nc-light-gallery-item img").attr("src")
                url = response.request().url().toString().split("#")[0].replace("https://www.95mm.net", "")
            })
            return MangasPage(mangas, false)
        } else {
            val body = response.body()!!.string()
            val document = Jsoup.parseBodyFragment(body)
            return simpleMangasByElements(document)
        }
    }

    private fun isInteger(str: String): Boolean {
        var pattern = Pattern.compile("^[-\\+]?[\\d]*$")
        return pattern.matcher(str).matches()
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query.equals("vip", true)) {
            return myGet("$baseUrl/search/?keywords=$query&append=list-home&pos=search&page=$page&paged=$page")
        } else if (query != "") {
            if (query.trim().indexOf("@") >= 0)
                if (query.split("@")[1].trim() != "")
                    if (isInteger(query.split("@")[1].trim())) {
                        return myGet("$baseUrl/${query.split("@")[1].trim()}.html")
                    }
            return myGet("$baseUrl/search/?keywords=$query&append=list-home&pos=search&page=$page&paged=$page")
        } else {
            val params = filters.map {
                if (it is UriPartFilter) {
                    it.toUriPart()
                } else ""
            }.filter { it != "" }.joinToString("")
            return myGet("${baseUrl}$params&page=$page&paged=$page")
        }
    }

    override fun getFilterList() = FilterList(
        ReaderFilter()
    )

    private var requestImageHeaders = Headers.of(mapOf(
        "Accept" to "image/webp,image/apng,*/*;q=0.8",
        "Accept-Encoding" to "",
        "Accept-Language" to "zh-CN,zh;q=0.9",
        "User-Agent" to "(Linux; U; Android 8.0.0; zh-CN; VTR-AL00 Build/HUAWEIVTR-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.108 UCBrowser/11.8.0.960 Mobile Safari/537.36",
        "Sec-Fetch-Dest" to "image",
        "Sec-Fetch-Mode" to "no-cors",
        "Sec-Fetch-Site" to "same-site",
        "Connection" to "keep-alive",
        "Host" to "cdn.zzdaye.com"
    ))
    // 重写图片的访问方式,以添加请求头
    override fun imageUrlRequest(page: Page): Request {
        return GET(page.url, requestImageHeaders)
    }

    // &page=2&paged=2
    // Pair("异域美景", "/category-6/list-1/index.html?append=list-home&pos=cate"),
    // Pair("怀旧古风", "/category-3/list-1/index.html?append=list-home&pos=cate"),
    private class ReaderFilter : UriPartFilter("查询分类", arrayOf(
        Pair("热门", "/home-ajax/index.html?tabcid=热门&append=list-home&query=&pos=home"),
        Pair("最新", "/home-ajax/index.html?tabcid=最新&append=list-home&query=&pos=home"),
        Pair("性感妖姬", "/category-7/list-1/index.html?append=list-home&pos=cate"),
        Pair("三次元", "/category-5/list-1/index.html?append=list-home&pos=cate"),
        Pair("明星写真", "/category-4/list-1/index.html?append=list-home&pos=cate"),
        Pair("摄影私房", "/category-2/list-1/index.html?append=list-home&pos=cate"),
        Pair("清纯唯美", "/category-1/list-1/index.html?append=list-home&pos=cate"),
        Pair("VIP相册", "/tag-vip/page-1/index.html?append=list-home&pos=tag"),
        Pair("小九月", "/tag-小九月/page-1/index.html?append=list-home&pos=tag"),
        Pair("薄纱", "/tag-薄纱/page-1/index.html?append=list-home&pos=tag"),
        Pair("透明", "/tag-透明/page-1/index.html?append=list-home&pos=tag"),
        Pair("何嘉颖", "/tag-何嘉颖/page-1/index.html?append=list-home&pos=tag"),
        Pair("巨乳", "/tag-巨乳/page-1/index.html?append=list-home&pos=tag"),
        Pair("女神99", "/tag-女神99/page-1/index.html?append=list-home&pos=tag"),
        Pair("泳装", "/tag-泳装/page-1/index.html?append=list-home&pos=tag"),
        Pair("绯月樱", "/tag-绯月樱/page-1/index.html?append=list-home&pos=tag"),
        Pair("寂寞", "/tag-寂寞/page-1/index.html?append=list-home&pos=tag"),
        Pair("潘琳琳", "/tag-潘琳琳/page-1/index.html?append=list-home&pos=tag"),
        Pair("周于希", "/tag-周于希/page-1/index.html?append=list-home&pos=tag"),
        Pair("情趣", "/tag-情趣/page-1/index.html?append=list-home&pos=tag"),
        Pair("熟女", "/tag-熟女/page-1/index.html?append=list-home&pos=tag"),
        Pair("黑丝", "/tag-黑丝/page-1/index.html?append=list-home&pos=tag"),
        Pair("芝芝", "/tag-芝芝/page-1/index.html?append=list-home&pos=tag"),
        Pair("制服", "/tag-制服/page-1/index.html?append=list-home&pos=tag"),
        Pair("艺轩", "/tag-艺轩/page-1/index.html?append=list-home&pos=tag"),
        Pair("学生", "/tag-学生/page-1/index.html?append=list-home&pos=tag"),
        Pair("朱可儿", "/tag-朱可儿/page-1/index.html?append=list-home&pos=tag"),
        Pair("童颜", "/tag-童颜/page-1/index.html?append=list-home&pos=tag"),
        Pair("心妍", "/tag-心妍/page-1/index.html?append=list-home&pos=tag"),
        Pair("夏日", "/tag-夏日/page-1/index.html?append=list-home&pos=tag"),
        Pair("少女", "/tag-少女/page-1/index.html?append=list-home&pos=tag"),
        Pair("日系", "/tag-日系/page-1/index.html?append=list-home&pos=tag"),
        Pair("清纯", "/tag-清纯/page-1/index.html?append=list-home&pos=tag"),
        Pair("校园", "/tag-校园/page-1/index.html?append=list-home&pos=tag"),
        Pair("校花", "/tag-校花/page-1/index.html?append=list-home&pos=tag"),
        Pair("汤音璇", "/tag-汤音璇/page-1/index.html?append=list-home&pos=tag"),
        Pair("长发", "/tag-长发/page-1/index.html?append=list-home&pos=tag"),
        Pair("少妇", "/tag-少妇/page-1/index.html?append=list-home&pos=tag"),
        Pair("尹菲", "/tag-尹菲/page-1/index.html?append=list-home&pos=tag"),
        Pair("周妍希", "/tag-周妍希/page-1/index.html?append=list-home&pos=tag"),
        Pair("秘书", "/tag-秘书/page-1/index.html?append=list-home&pos=tag"),
        Pair("梦心月", "/tag-梦心月/page-1/index.html?append=list-home&pos=tag"),
        Pair("杨晨晨", "/tag-杨晨晨/page-1/index.html?append=list-home&pos=tag"),
        Pair("高跟", "/tag-高跟/page-1/index.html?append=list-home&pos=tag"),
        Pair("妲己", "/tag-妲己/page-1/index.html?append=list-home&pos=tag"),
        Pair("王雨纯", "/tag-王雨纯/page-1/index.html?append=list-home&pos=tag"),
        Pair("御姐", "/tag-御姐/page-1/index.html?append=list-home&pos=tag"),
        Pair("女仆", "/tag-女仆/page-1/index.html?append=list-home&pos=tag"),
        Pair("木木夕", "/tag-木木夕/page-1/index.html?append=list-home&pos=tag"),
        Pair("月音瞳", "/tag-月音瞳/page-1/index.html?append=list-home&pos=tag"),
        Pair("糯美子", "/tag-糯美子/page-1/index.html?append=list-home&pos=tag"),
        Pair("老师", "/tag-老师/page-1/index.html?append=list-home&pos=tag"),
        Pair("韩雨馨", "/tag-韩雨馨/page-1/index.html?append=list-home&pos=tag"),
        Pair("气质", "/tag-气质/page-1/index.html?append=list-home&pos=tag"),
        Pair("长腿", "/tag-长腿/page-1/index.html?append=list-home&pos=tag"),
        Pair("小尤奈", "/tag-小尤奈/page-1/index.html?append=list-home&pos=tag"),
        Pair("玛鲁娜", "/tag-玛鲁娜/page-1/index.html?append=list-home&pos=tag"),
        Pair("徐微微", "/tag-徐微微/page-1/index.html?append=list-home&pos=tag"),
        Pair("比基尼", "/tag-比基尼/page-1/index.html?append=list-home&pos=tag"),
        Pair("沙滩", "/tag-沙滩/page-1/index.html?append=list-home&pos=tag"),
        Pair("筱慧", "/tag-筱慧/page-1/index.html?append=list-home&pos=tag"),
        Pair("丝袜", "/tag-丝袜/page-1/index.html?append=list-home&pos=tag"),
        Pair("萌汉药", "/tag-萌汉药/page-1/index.html?append=list-home&pos=tag"),
        Pair("森系", "/tag-森系/page-1/index.html?append=list-home&pos=tag"),
        Pair("吃的好琛", "/tag-吃的好琛/page-1/index.html?append=list-home&pos=tag"),
        Pair("暂无标签", "/tag-暂无标签/page-1/index.html?append=list-home&pos=tag"),
        Pair("野外", "/tag-野外/page-1/index.html?append=list-home&pos=tag"),
        Pair("海边", "/tag-海边/page-1/index.html?append=list-home&pos=tag"),
        Pair("冯木木", "/tag-冯木木/page-1/index.html?append=list-home&pos=tag"),
        Pair("曼苏拉娜", "/tag-曼苏拉娜/page-1/index.html?append=list-home&pos=tag"),
        Pair("内衣", "/tag-内衣/page-1/index.html?append=list-home&pos=tag"),
        Pair("美臀", "/tag-美臀/page-1/index.html?append=list-home&pos=tag"),
        Pair("黄乐然", "/tag-黄乐然/page-1/index.html?append=list-home&pos=tag"),
        Pair("牛仔", "/tag-牛仔/page-1/index.html?append=list-home&pos=tag"),
        Pair("肉丝", "/tag-肉丝/page-1/index.html?append=list-home&pos=tag"),
        Pair("卓娅祺", "/tag-卓娅祺/page-1/index.html?append=list-home&pos=tag"),
        Pair("人体艺术摄影", "/tag-人体艺术摄影/page-1/index.html?append=list-home&pos=tag"),
        Pair("泳池", "/tag-泳池/page-1/index.html?append=list-home&pos=tag"),
        Pair("孙梦瑶", "/tag-孙梦瑶/page-1/index.html?append=list-home&pos=tag"),
        Pair("艾小青", "/tag-艾小青/page-1/index.html?append=list-home&pos=tag"),
        Pair("嫩模", "/tag-嫩模/page-1/index.html?append=list-home&pos=tag"),
        Pair("模特", "/tag-模特/page-1/index.html?append=list-home&pos=tag"),
        Pair("真空", "/tag-真空/page-1/index.html?append=list-home&pos=tag"),
        Pair("菲菲", "/tag-菲菲/page-1/index.html?append=list-home&pos=tag"),
        Pair("小热巴", "/tag-小热巴/page-1/index.html?append=list-home&pos=tag"),
        Pair("萝莉", "/tag-萝莉/page-1/index.html?append=list-home&pos=tag"),
        Pair("果儿", "/tag-果儿/page-1/index.html?append=list-home&pos=tag"),
        Pair("全裸", "/tag-全裸/page-1/index.html?append=list-home&pos=tag"),
        Pair("睡衣", "/tag-睡衣/page-1/index.html?append=list-home&pos=tag"),
        Pair("清凉", "/tag-清凉/page-1/index.html?append=list-home&pos=tag"),
        Pair("湿身", "/tag-湿身/page-1/index.html?append=list-home&pos=tag"),
        Pair("空姐", "/tag-空姐/page-1/index.html?append=list-home&pos=tag"),
        Pair("易阳", "/tag-易阳/page-1/index.html?append=list-home&pos=tag"),
        Pair("紧身裤", "/tag-紧身裤/page-1/index.html?append=list-home&pos=tag"),
        Pair("萌妹", "/tag-萌妹/page-1/index.html?append=list-home&pos=tag"),
        Pair("大胆", "/tag-大胆/page-1/index.html?append=list-home&pos=tag"),
        Pair("护士", "/tag-护士/page-1/index.html?append=list-home&pos=tag"),
        Pair("张雨萌", "/tag-张雨萌/page-1/index.html?append=list-home&pos=tag"),
        Pair("尤妮丝", "/tag-尤妮丝/page-1/index.html?append=list-home&pos=tag"),
        Pair("李雅", "/tag-李雅/page-1/index.html?append=list-home&pos=tag"),
        Pair("兔女郎", "/tag-兔女郎/page-1/index.html?append=list-home&pos=tag"),
        Pair("透视装", "/tag-透视装/page-1/index.html?append=list-home&pos=tag"),
        Pair("王婉悠", "/tag-王婉悠/page-1/index.html?append=list-home&pos=tag"),
        Pair("仓井优香", "/tag-仓井优香/page-1/index.html?append=list-home&pos=tag"),
        Pair("温心怡", "/tag-温心怡/page-1/index.html?append=list-home&pos=tag"),
        Pair("刘钰儿", "/tag-刘钰儿/page-1/index.html?append=list-home&pos=tag"),
        Pair("浴室", "/tag-浴室/page-1/index.html?append=list-home&pos=tag"),
        Pair("夏美酱", "/tag-夏美酱/page-1/index.html?append=list-home&pos=tag"),
        Pair("车模", "/tag-车模/page-1/index.html?append=list-home&pos=tag"),
        Pair("韩国", "/tag-韩国/page-1/index.html?append=list-home&pos=tag"),
        Pair("黄美姬", "/tag-黄美姬/page-1/index.html?append=list-home&pos=tag"),
        Pair("日本", "/tag-日本/page-1/index.html?append=list-home&pos=tag"),
        Pair("秋山莉奈", "/tag-秋山莉奈/page-1/index.html?append=list-home&pos=tag"),
        Pair("崔星儿", "/tag-崔星儿/page-1/index.html?append=list-home&pos=tag"),
        Pair("豹纹", "/tag-豹纹/page-1/index.html?append=list-home&pos=tag"),
        Pair("私密", "/tag-私密/page-1/index.html?append=list-home&pos=tag"),
        Pair("台湾", "/tag-台湾/page-1/index.html?append=list-home&pos=tag"),
        Pair("艾尚真", "/tag-艾尚真/page-1/index.html?append=list-home&pos=tag"),
        Pair("潘春春", "/tag-潘春春/page-1/index.html?append=list-home&pos=tag"),
        Pair("范冰冰", "/tag-范冰冰/page-1/index.html?append=list-home&pos=tag"),
        Pair("婚纱", "/tag-婚纱/page-1/index.html?append=list-home&pos=tag"),
        Pair("闺房", "/tag-闺房/page-1/index.html?append=list-home&pos=tag"),
        Pair("希志", "/tag-希志/page-1/index.html?append=list-home&pos=tag"),
        Pair("柏木由纪", "/tag-柏木由纪/page-1/index.html?append=list-home&pos=tag"),
        Pair("陈颖嫦", "/tag-陈颖嫦/page-1/index.html?append=list-home&pos=tag"),
        Pair("平山蓝里", "/tag-平山蓝里/page-1/index.html?append=list-home&pos=tag"),
        Pair("推女郎", "/tag-推女郎/page-1/index.html?append=list-home&pos=tag"),
        Pair("女优", "/tag-女优/page-1/index.html?append=list-home&pos=tag"),
        Pair("纹身", "/tag-纹身/page-1/index.html?append=list-home&pos=tag"),
        Pair("美背", "/tag-美背/page-1/index.html?append=list-home&pos=tag"),
        Pair("赵小米", "/tag-赵小米/page-1/index.html?append=list-home&pos=tag"),
        Pair("李雪婷", "/tag-李雪婷/page-1/index.html?append=list-home&pos=tag"),
        Pair("丁字裤", "/tag-丁字裤/page-1/index.html?append=list-home&pos=tag"),
        Pair("沈佳熹", "/tag-沈佳熹/page-1/index.html?append=list-home&pos=tag"),
        Pair("黄诗思", "/tag-黄诗思/page-1/index.html?append=list-home&pos=tag"),
        Pair("钟曼菲", "/tag-钟曼菲/page-1/index.html?append=list-home&pos=tag"),
        Pair("原干惠", "/tag-原干惠/page-1/index.html?append=list-home&pos=tag"),
        Pair("半裸", "/tag-半裸/page-1/index.html?append=list-home&pos=tag"),
        Pair("西田麻衣", "/tag-西田麻衣/page-1/index.html?append=list-home&pos=tag"),
        Pair("杉原杏璃", "/tag-杉原杏璃/page-1/index.html?append=list-home&pos=tag"),
        Pair("大尺度", "/tag-大尺度/page-1/index.html?append=list-home&pos=tag"),
        Pair("中村静香", "/tag-中村静香/page-1/index.html?append=list-home&pos=tag"),
        Pair("森下悠里", "/tag-森下悠里/page-1/index.html?append=list-home&pos=tag"),
        Pair("吉沢明步", "/tag-吉沢明步/page-1/index.html?append=list-home&pos=tag"),
        Pair("滝川绫", "/tag-滝川绫/page-1/index.html?append=list-home&pos=tag"),
        Pair("胸模", "/tag-胸模/page-1/index.html?append=list-home&pos=tag"),
        Pair("泡泡浴", "/tag-泡泡浴/page-1/index.html?append=list-home&pos=tag"),
        Pair("尤果女郎", "/tag-尤果女郎/page-1/index.html?append=list-home&pos=tag"),
        Pair("晓茜", "/tag-晓茜/page-1/index.html?append=list-home&pos=tag"),
        Pair("夏小秋", "/tag-夏小秋/page-1/index.html?append=list-home&pos=tag"),
        Pair("凯竹", "/tag-凯竹/page-1/index.html?append=list-home&pos=tag"),
        Pair("爱丽莎", "/tag-爱丽莎/page-1/index.html?append=list-home&pos=tag"),
        Pair("林美惠子", "/tag-林美惠子/page-1/index.html?append=list-home&pos=tag")

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
