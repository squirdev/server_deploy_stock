package com.nq.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.dao.SiteNewsMapper;
import com.nq.pojo.SiteNews;
import com.nq.service.IFileUploadService;
import com.nq.service.ISiteNewsService;
import com.nq.utils.DateTimeUtil;
import com.nq.utils.HttpRequest;
import com.nq.utils.PropertiesUtil;
import com.nq.utils.StringUtils;
import com.nq.utils.translate.GoogleTranslateUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 新闻资讯
 * @author lr
 * @date 2020/07/24
 */
@Service("ISiteNewsService")
public class SiteNewsServiceImpl implements ISiteNewsService {

    private static final Logger log = LoggerFactory.getLogger(SiteNewsServiceImpl.class);

    @Autowired
    IFileUploadService iFileUploadService;

    @Resource
    private SiteNewsMapper siteNewsMapper;


    @Override
    public int insert(SiteNews model) {
        int ret = 0;
        if (model == null) {
            return 0;
        }
        ret = siteNewsMapper.insert(model);
        return ret;
    }

    @Override
    public int update(SiteNews model) {
        int ret = siteNewsMapper.update(model);
        return ret>0 ? ret: 0;
    }

    /**
     * 新闻资讯-保存
     */
    @Override
    public ServerResponse save(SiteNews model) {
        int ret = 0;
        if(model!=null && model.getId()>0){
            ret = siteNewsMapper.update(model);
        } else{
            ret = siteNewsMapper.insert(model);
        }
        if(ret>0){
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失败");
    }

    /*新闻资讯-查询列表*/
    @Override
    public ServerResponse<PageInfo> getList(int pageNum, int pageSize, Integer type, String sort, String keyword, HttpServletRequest request) {
        PageHelper.startPage(pageNum, pageSize);
        List<SiteNews> listData = this.siteNewsMapper.pageList(pageNum, pageSize, type, sort, keyword);
        GoogleTranslateUtil googleTranslateUtil = new GoogleTranslateUtil();
        //取出每一条数据的内容，进行翻译

        if (request.getHeader("lang") != null && !request.getHeader("lang").equals("zh-CN")&& !request.getHeader("lang").equals("tww")&& !request.getHeader("lang").equals("")) {
            for (SiteNews siteNews : listData) {
                //将title description content放一个数组里面


//                String zh = siteNews.getContent();

                try {
//                    siteNews.setContent(googleTranslateUtil.translate(siteNews.getContent(), request.getHeader("lang")));
                    siteNews.setTitle(googleTranslateUtil.translate(siteNews.getTitle(), request.getHeader("lang")));
//                    siteNews.setDescription(googleTranslateUtil.translate(siteNews.getDescription(), request.getHeader("lang")));
                    siteNews.setSourceName(googleTranslateUtil.translate(siteNews.getSourceName(), request.getHeader("lang")));
                } catch (Exception e) {
                   log.error("翻译失败");
                }
            }
    }


        PageInfo pageInfo = new PageInfo(listData);
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /*新闻资讯-查询详情*/
    @Override
    public ServerResponse getDetail(int id, HttpServletRequest request) {
        SiteNews model = siteNewsMapper.load(id);
        if(model!=null){
            GoogleTranslateUtil googleTranslateUtil = new GoogleTranslateUtil();
            //取出每一条数据的内容，进行翻译

            if (request.getHeader("lang") != null && !request.getHeader("lang").equals("zh-CN")&& !request.getHeader("lang").equals("tww")&& !request.getHeader("lang").equals("")) {
                try {
                    model.setContent(googleTranslateUtil.translate(model.getContent(), request.getHeader("lang")));
                    model.setTitle(googleTranslateUtil.translate(model.getTitle(), request.getHeader("lang")));
                    model.setDescription(googleTranslateUtil.translate(model.getDescription(), request.getHeader("lang")));
                } catch (Exception e) {
                    log.error("翻译失败");
                }
            }
            return ServerResponse.createBySuccess(model);
        }
        return ServerResponse.createBySuccess(model);
    }

    /*新闻资讯-修改新闻浏览量*/
    @Override
    public ServerResponse updateViews(Integer id) {
        return ServerResponse.createBySuccess(this.siteNewsMapper.updateViews(id));
    }

    /*新闻资讯-top最新新闻资讯*/
    @Override
    public ServerResponse getTopNewsList(int pageSize){
        List<SiteNews> listData = this.siteNewsMapper.getTopNewsList(pageSize);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /*新闻资讯-抓取*/
    @Override
    public int grabNews() {
        int ret = 0;
        
        //新闻类型：1、财经要闻，2、经济数据，3、全球股市，4、7*24全球，5、商品资讯，6、上市公司，7、全球央行
        ret = addNews(1, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetImportantNewsList");
        log.info("财经要闻-抓取条数：" + ret);

        ret = addNews(2, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetInfoList?code=125&pageNumber=1&pagesize=20&condition=&r=");
        log.info("经济数据-抓取条数：" + ret);

        ret = addNews(3, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetInfoList?code=105&pageNumber=1&pagesize=20&condition=&r=");
        log.info("全球股市-抓取条数：" + ret);

        ret = addNews(4, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetInfoList?code=100&pageNumber=1&pagesize=20&condition=&r=");
        log.info("7*24全球-抓取条数：" + ret);

        ret = addNews(5, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetInfoList?code=106&pageNumber=1&pagesize=20&condition=&r=");
        log.info("商品资讯-抓取条数：" + ret);

        ret = addNews(6, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetInfoList?code=103&pageNumber=1&pagesize=20&condition=&r=");
        log.info("上市公司-抓取条数：" + ret);

        ret = addNews(7, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetInfoList?code=118&pageNumber=1&pagesize=20&condition=&r=");
        log.info("全球央行-抓取条数：" + ret);

        return ret;
    }

    /*
    *抓取新闻专用
    * type：新闻类型：1、财经要闻，2、经济数据，3、全球股市，4、7*24全球，5、商品资讯，6、上市公司，7、全球央行
    * */
    private int addNews(Integer type, String url){
        int k = 0;
        try {
            String newlist = HttpRequest.doGrabGet(url);

            JSONObject json = JSONObject.fromObject(newlist);
            if(json != null && json.getJSONArray("items") != null && json.getJSONArray("items").size() > 0){
                for (int i = 0; i < json.getJSONArray("items").size(); i++){
                    JSONObject model = JSONObject.fromObject(json.getJSONArray("items").getString(i));
                    String newsId = model.getString("code");
                    String imgUrl = null;
                    if(model.has("imgUrl")){
                      //  imgUrl = model.getString("imgUrl");
                        imgUrl = iFileUploadService.downloadImages(model.getString("imgUrl"));
                    }
                    //新闻不存在则添加
                    if(siteNewsMapper.getNewsBySourceIdCount(newsId) == 0){
                        //获取新闻详情
                        String newdata = HttpRequest.doGrabGet(PropertiesUtil.getProperty("news.main.url") + "/PC_News/Detail/GetDetailContent?id="+ newsId +"&type=1");
                        newdata = newdata.substring(1,newdata.length()-1).replace("\\\\\\\"","\"");
                        //替换所有引号

                        newdata = newdata.replace("\\\"","\"");
                        newdata = StringUtils.UnicodeToCN(newdata);
                        newdata = StringUtils.delHTMLTag(newdata);

                        JSONObject jsonnew = JSONObject.fromObject(newdata);
                        if(jsonnew != null && jsonnew.get("data") != null){
                            JSONObject news = JSONObject.fromObject(jsonnew.get("data"));
                            SiteNews siteNews = new SiteNews();
                            siteNews.setSourceId(newsId);
                            siteNews.setSourceName(news.getString("source"));
                            siteNews.setTitle(news.getString("title"));
                            String showTime = news.getString("showTime");
                            siteNews.setShowTime(DateTimeUtil.strToDate(showTime));
                            siteNews.setImgurl(imgUrl);
                            siteNews.setDescription(news.getString("description"));
                            siteNews.setContent(news.getString("content"));
                            siteNews.setStatus(1);
                            siteNews.setType(type);
                            siteNewsMapper.insert(siteNews);
                            k++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return k;
    }

}
