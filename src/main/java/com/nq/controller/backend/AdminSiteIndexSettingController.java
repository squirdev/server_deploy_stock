package com.nq.controller.backend;

import com.nq.common.ServerResponse;
import com.nq.pojo.SiteIndexSetting;
import com.nq.service.ISiteIndexSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/admin/site/index/"})
public class AdminSiteIndexSettingController {
    private static final Logger log = LoggerFactory.getLogger(AdminSiteIndexSettingController.class);

    @Autowired
    ISiteIndexSettingService iSiteIndexSettingService;

    //修改风控设置 指数风控信息
    @RequestMapping({"update.do"})
    @ResponseBody
    public ServerResponse update(SiteIndexSetting siteIndexSetting) {
        return this.iSiteIndexSettingService.update(siteIndexSetting);
    }
}
