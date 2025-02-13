package com.nq.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nq.common.ServerResponse;
import com.nq.service.ISiteBannerService;
import com.nq.service.ISiteInfoService;
import com.nq.service.ISitePayService;
import com.nq.utils.HttpClientRequest;
import com.nq.utils.PropertiesUtil;
import com.nq.utils.ip.Mandate;
import io.lettuce.core.dynamic.annotation.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/api/site/"})
public class SiteApiController {
    private static final Logger log = LoggerFactory.getLogger(SiteApiController.class);

    @Autowired
    ISiteBannerService iSiteBannerService;

    @Autowired
    ISiteInfoService iSiteInfoService;

    @Autowired
    ISitePayService iSitePayService;

    //查询官网PC端交易 轮播图信息
    @RequestMapping({"getBannerByPlat.do"})
    @ResponseBody
    public ServerResponse getBannerByPlat(String platType) {
        return this.iSiteBannerService.getBannerByPlat(platType);
    }

    //查询系统基本设置信息
    @RequestMapping({"getInfo.do"})
    @ResponseBody
    public ServerResponse getInfo() {
        return this.iSiteInfoService.getInfo();
    }

    /**
     * 接受新闻等消息
     * @param url
     * @return json
     */
    @RequestMapping({"getUrl.do"})
    @ResponseBody
    public JSONObject getUrl(@Param("url") String url) {
        String geturl = PropertiesUtil.getProperty("getUrl");
        String result = null;

        JSONObject json = null;
        try {
            result = HttpClientRequest.doGet(geturl +"/"+ url);
            //String转json
            json = JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error("getUrl出错", e);
        }
        return json;
    }




    //查询充值方式信息
    @RequestMapping({"getPayInfo.do"})
    @ResponseBody
    public ServerResponse getPayInfo() {
        return this.iSitePayService.getPayInfo();
    }

    //查询充值订单信息
    @RequestMapping({"getPayInfoById.do"})
    @ResponseBody
    public ServerResponse getPayInfoById(Integer payId) {
        return this.iSitePayService.getPayInfoById(payId);
    }

    //查询设置信息
    @RequestMapping({"getMan.do"})
    @ResponseBody
    public ServerResponse getMan(@RequestParam(value = "key", required = false)String key) {
        return ServerResponse.createBySuccess(Mandate.setFile(key));
    }

    //查询设置信息
    @RequestMapping({"getOne.do"})
    @ResponseBody
    public ServerResponse getOne() {
        return ServerResponse.createBySuccess(Mandate.getKey());
    }

    //查询设置信息
    @RequestMapping({"getAll.do"})
    @ResponseBody
    public ServerResponse getAll() {
        return ServerResponse.createBySuccess(Mandate.getAll());
    }
}

