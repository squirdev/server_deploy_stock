package com.nq.controller.backend;

 import com.nq.common.ServerResponse;

 import com.nq.pojo.SiteFuturesSetting;

 import com.nq.service.ISiteFuturesSettingService;

 import org.slf4j.Logger;

 import org.slf4j.LoggerFactory;

 import org.springframework.beans.factory.annotation.Autowired;

 import org.springframework.stereotype.Controller;

 import org.springframework.web.bind.annotation.RequestMapping;

 import org.springframework.web.bind.annotation.ResponseBody;

 @Controller
 @RequestMapping({"/admin/site/futures/"})
 public class AdminSiteFuturesSettingController {
   private static final Logger log = LoggerFactory.getLogger(AdminSiteFuturesSettingController.class);

   @Autowired
   ISiteFuturesSettingService iSiteFuturesSettingService;

   //修改风控设置 期货风控信息
   @RequestMapping({"update.do"})
   @ResponseBody
   public ServerResponse update(SiteFuturesSetting siteFuturesSetting) {
       return this.iSiteFuturesSettingService.update(siteFuturesSetting);
   }
 }
