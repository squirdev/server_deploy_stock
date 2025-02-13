package com.nq.controller.backend;


import com.nq.common.ServerResponse;
import com.nq.pojo.SiteInfo;
import com.nq.service.ISiteBannerService;
import com.nq.service.ISiteInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/admin/info/"})
public class AdminSiteInfoController {
    private static final Logger log = LoggerFactory.getLogger(AdminSiteInfoController.class);

    @Autowired
    ISiteInfoService iSiteInfoService;

    @Autowired
    ISiteBannerService iSiteBannerService;

    @RequestMapping({"add.do"})
    @ResponseBody
    public ServerResponse add(SiteInfo siteInfo) {
        return this.iSiteInfoService.add(siteInfo);
    }

    //修改系统基本设置信息
    @RequestMapping({"update.do"})
    @ResponseBody
    public ServerResponse update(SiteInfo siteInfo) {
        return this.iSiteInfoService.update(siteInfo);
    }

    @RequestMapping({"get.do"})
    @ResponseBody
    public ServerResponse get() {
        return this.iSiteInfoService.get();
    }

    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return this.iSiteBannerService.listByAdmin(pageNum, pageSize);
    }
}

