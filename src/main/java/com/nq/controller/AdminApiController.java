package com.nq.controller;

import com.nq.common.ServerResponse;
import com.nq.pojo.SiteAdmin;
import com.nq.pojo.SiteAdminIndex;
import com.nq.pojo.SiteSpread;
import com.nq.service.*;
import com.nq.utils.PropertiesUtil;
import com.nq.utils.redis.CookieUtils;
import com.nq.utils.redis.JsonUtil;
import com.nq.utils.redis.RedisConst;
import com.nq.utils.redis.RedisShardedPoolUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"/api/admin/"})
public class AdminApiController {
    private static final Logger log = LoggerFactory.getLogger(AdminApiController.class);

    @Autowired
    ISiteAdminService iSiteAdminService;

    @Autowired
    ISiteSettingService iSiteSettingService;

    @Autowired
    ISiteIndexSettingService iSiteIndexSettingService;

    @Autowired
    ISiteFuturesSettingService iSiteFuturesSettingService;

    @Autowired
    ISiteProductService iSiteProductService;

    @Autowired
    ISiteSpreadService iSiteSpreadService;
    @Autowired
    SiteAdminIndexService siteAdminIndexService;

    //管理系统登录
    @RequestMapping({"login.do"})
    @ResponseBody
    public ServerResponse login(@RequestParam("adminPhone") String adminPhone, @RequestParam("adminPwd") String adminPwd, @RequestParam("verifyCode") String verifyCode, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        ServerResponse serverResponse = this.iSiteAdminService.login(adminPhone, adminPwd, verifyCode, request);

        return serverResponse;
    }

    //管理系统注销
    @RequestMapping({"logout.do"})
    @ResponseBody
    public ServerResponse logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String cookie_name = PropertiesUtil.getProperty("admin.cookie.name");
        String logintoken = CookieUtils.readLoginToken(httpServletRequest, cookie_name);
        log.info("管理员 token = {} ,退出登陆", logintoken);
        RedisShardedPoolUtils.del(logintoken);
        CookieUtils.delLoginToken(httpServletRequest, httpServletResponse, cookie_name);
        return ServerResponse.createBySuccess();
    }

    @RequestMapping({"authCharge.do"})
    @ResponseBody
    public ServerResponse authCharge(@RequestParam("token") String token, @RequestParam("state") Integer state, @RequestParam("orderSn") String orderSn) {
        return this.iSiteAdminService.authCharge(token, state, orderSn);
    }

    //查询风控设置 股票分控信息
    @RequestMapping({"getSetting.do"})
    @ResponseBody
    public ServerResponse getSetting() {
        return ServerResponse.createBySuccess(this.iSiteSettingService.getSiteSetting());
    }

    //查询风控设置 指数风控信息
    @RequestMapping({"getIndexSetting.do"})
    @ResponseBody
    public ServerResponse getIndexSetting() {
        return ServerResponse.createBySuccess(this.iSiteIndexSettingService.getSiteIndexSetting());
    }

    //查询风控设置 期货风控信息
    @RequestMapping({"getFuturesSetting.do"})
    @ResponseBody
    public ServerResponse getFuturesSetting() {
        return ServerResponse.createBySuccess(this.iSiteFuturesSettingService.getSetting());
    }

    //风控设置 显示产品配置信息
    @RequestMapping({"getProductSetting.do"})
    @ResponseBody
    public ServerResponse getProductSetting() {
        return ServerResponse.createBySuccess(this.iSiteProductService.getProductSetting());
    }

    //查询点差设置列表
    @RequestMapping({"getSiteSpreadList.do"})
    @ResponseBody
    public ServerResponse getSiteSpreadList(int pageNum, int pageSize, String typeName) {
        return ServerResponse.createBySuccess(this.iSiteSpreadService.pageList(pageNum, pageSize, typeName));
    }

    //添加点差设置
    @RequestMapping({"addSiteSpread.do"})
    @ResponseBody
    public ServerResponse addSiteSpread(SiteSpread siteSpread) {
        return ServerResponse.createBySuccess(this.iSiteSpreadService.insert(siteSpread));
    }

    //添加点差设置
    @RequestMapping({"updateSiteSpread.do"})
    @ResponseBody
    public ServerResponse updateSiteSpread(SiteSpread siteSpread) {
        return ServerResponse.createBySuccess(this.iSiteSpreadService.update(siteSpread));
    }
//    //页面样式设置
//    @RequestMapping({"setSiteStyle.do"})
//    @ResponseBody
//    public ServerResponse setSiteStyle(SiteAdminIndex siteAdminIndex) {
//        return ServerResponse.createBySuccess(this.siteAdminIndexService.setSiteStyle(siteAdminIndex));
//    }
//    //页面样式设置查询
//    @RequestMapping({"getSiteStyle.do"})
//    @ResponseBody
//    public ServerResponse getSiteStyle(@RequestParam("id") Integer id) {
//        return ServerResponse.createBySuccess(this.siteAdminIndexService.getSiteStyle(id));
//    }

    //获取提醒信息
    @RequestMapping({"getAlarm.do"})
    @ResponseBody
    public ServerResponse getAlarm(HttpServletRequest request) {

        String loginToken = request.getHeader(PropertiesUtil.getProperty("admin.cookie.name"));
        return ServerResponse.createBySuccess(this.iSiteAdminService.getAlarmByLoginToken(loginToken));
    }

}
