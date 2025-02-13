package com.nq.controller;


import com.nq.common.ServerResponse;
import com.nq.service.IStockIndexService;
import com.nq.utils.CacheUtil;
import com.nq.vo.stock.MarketVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping({"/api/index/"})
public class StockIndexApiController {
    private static final Logger log = LoggerFactory.getLogger(StockIndexApiController.class);

    @Autowired
    IStockIndexService iStockIndexService;


    @RequestMapping({"queryHomeIndex.do"})
    @ResponseBody
    public ServerResponse queryHomeIndex() {
        return this.iStockIndexService.queryHomeIndex();
    }
    //查询指数信息
    @RequestMapping({"queryListIndex.do"})
    @ResponseBody
    public ServerResponse queryListIndex(HttpServletRequest request) {
//        ServerResponse serverResponse = (ServerResponse) CacheUtil.get("queryListIndex");
//        if (serverResponse == null) {
//            serverResponse = this.iStockIndexService.queryListIndex(request);
//            CacheUtil.set("queryListIndex", serverResponse, 6000);
//        }
        return this.iStockIndexService.queryListIndex(request);
    }

    @RequestMapping({"queryCustIndex.do"})
    @ResponseBody
    public ServerResponse queryCustIndex(HttpServletRequest request) {
        return this.iStockIndexService.queryCustIndex();
    }

    @RequestMapping({"queryTransIndex.do"})
    @ResponseBody
    public ServerResponse queryTransIndex(@RequestParam("indexId") Integer indexId) {
        return this.iStockIndexService.queryTransIndex(indexId);
    }

    @RequestMapping({"querySingleIndex.do"})
    @ResponseBody
    public ServerResponse querySingleIndex(@RequestParam("indexCode") String indexCode) {
        MarketVO marketVO = this.iStockIndexService.querySingleIndex(indexCode);
        return ServerResponse.createBySuccess(marketVO);
    }
    //指数新闻
    @RequestMapping({"queryIndexNews.do"})
    @ResponseBody
    public ServerResponse queryIndexNews() {
        return this.iStockIndexService.queryIndexNews();
    }
}
