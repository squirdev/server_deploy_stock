package com.nq.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nq.common.ServerResponse;
import com.nq.service.ISiteNewsService;
import com.nq.service.IUserPositionService;
import com.nq.utils.HttpClientRequest;
import com.nq.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/api/news/"})
public class SiteNewsController {
    private static final Logger log = LoggerFactory.getLogger(SiteNewsController.class);
    @Autowired
    ISiteNewsService iSiteNewsService;

    @Autowired
    IUserPositionService iUserPositionService;

    //新闻资讯-列表查询
    @RequestMapping({"getNewsList.do"})
    @ResponseBody
    public ServerResponse getNewsList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "15") int pageSize, @RequestParam(value = "type", defaultValue = "0") Integer type, @RequestParam(value = "sort", defaultValue = "time1") String sort, @RequestParam(value = "keyword", required = false) String keyword, HttpServletRequest request) {
        return this.iSiteNewsService.getList(pageNum, pageSize, type, sort, keyword, request);
    }

    //新闻资讯-详情
    @RequestMapping({"getDetail.do"})
    @ResponseBody
    public ServerResponse getDetail(int id, HttpServletRequest request) {
        return this.iSiteNewsService.getDetail(id, request);
    }

    //新闻资讯-修改新闻浏览量
    @RequestMapping({"updateViews.do"})
    @ResponseBody
    public ServerResponse updateViews(Integer id) {
        return this.iSiteNewsService.updateViews(id);
    }

    //新闻资讯-列表查询
    @RequestMapping({"getTopNews.do"})
    @ResponseBody
    public ServerResponse getTopNewsList(@RequestParam(value = "pageSize", defaultValue = "15") int pageSize) {
        return this.iSiteNewsService.getTopNewsList(pageSize);
    }

    //新闻资讯-列表查询
    @RequestMapping({"getPositionTop.do"})
    @ResponseBody
    public ServerResponse findPositionTopList(@RequestParam(value = "pageSize", defaultValue = "15") int pageSize) {
        return this.iUserPositionService.findPositionTopList(pageSize);
    }

    /**
     * 个股咨询
     */
    @RequestMapping({"getStockNews.do"})
    @ResponseBody
    public ServerResponse getStockNews(@RequestParam(value = "code") String code, HttpServletRequest request) {

        String url = "https://xueqiu.com/statuses/stock_timeline.json?symbol_id="+code+"&count=20&source=%E8%87%AA%E9%80%89%E8%82%A1%E6%96%B0%E9%97%BB&page=1";
        String result = null;

        JSONObject json = null;
        try {
            result = HttpClientRequest.doGet(url);

            json = JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("个股咨询出错", e);
        }
        return ServerResponse.createBySuccess(json);
    }

}