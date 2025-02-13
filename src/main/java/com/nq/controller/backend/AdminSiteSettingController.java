 package com.nq.controller.backend;


 import com.nq.common.ServerResponse;
 import com.nq.pojo.SiteSetting;
 import com.nq.service.ISiteSettingService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;


 @Controller
 @RequestMapping({"/admin/set/"})
 public class AdminSiteSettingController {
     private static final Logger log = LoggerFactory.getLogger(AdminSiteSettingController.class);

     @Autowired
     ISiteSettingService iSiteSettingService;

     //修改风控设置 股票风控信息
     @RequestMapping({"update.do"})
     @ResponseBody
     public ServerResponse update(SiteSetting siteSetting) {
         return this.iSiteSettingService.update(siteSetting);
     }
 }