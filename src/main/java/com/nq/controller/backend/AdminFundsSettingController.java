package com.nq.controller.backend;

import com.nq.common.ServerResponse;
import com.nq.pojo.FundsLever;
import com.nq.pojo.FundsSetting;
import com.nq.service.IFundsLeverService;
import com.nq.service.IFundsSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/admin/funds/"})
public class AdminFundsSettingController {
    private static final Logger log = LoggerFactory.getLogger(AdminAgentController.class);

    @Autowired
    IFundsSettingService iFundsSettingService;

    @Autowired
    IFundsLeverService iFundsLeverService;

    //分仓配资设置信息保存
    @RequestMapping({"saveFundsSetting.do"})
    @ResponseBody
    public ServerResponse save(FundsSetting fundsSetting, HttpServletRequest request) {
        return this.iFundsSettingService.save(fundsSetting, request);
    }

    //分仓配资设置信息查询
    @RequestMapping({"getFundsSetting.do"})
    @ResponseBody
    public ServerResponse getFundsSetting() {
        return ServerResponse.createBySuccess(this.iFundsSettingService.getFundsSetting());
    }

    //配资杠杆列表查询
    @RequestMapping({"getFundsLeverList.do"})
    @ResponseBody
    public ServerResponse getFundsLeverList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, HttpServletRequest request) {
        return ServerResponse.createBySuccess(this.iFundsLeverService.getFundsLeverList(pageNum,pageSize,request));
    }

    //配资杠杆列表保存
    @RequestMapping({"saveFundsLever.do"})
    @ResponseBody
    public ServerResponse saveFundsLever(FundsLever fundsLever) {
        return ServerResponse.createBySuccess(this.iFundsLeverService.saveFundsLever(fundsLever));
    }

}
