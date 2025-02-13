package com.nq.controller.protol;


import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.service.IUserRechargeService;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/user/recharge/"})
public class UserRechargeController {
    private static final Logger log = LoggerFactory.getLogger(UserRechargeController.class);

    @Autowired
    IUserRechargeService iUserRechargeService;

    //分页查询所有充值记录
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "payChannel", required = false) String payChannel, @RequestParam(value = "orderStatus", required = false) String orderStatus, HttpServletRequest request) {
        return this.iUserRechargeService.findUserChargeList(payChannel, orderStatus, request, pageNum, pageSize);
    }

    //账户线下充值转账 创建充值订单
    @RequestMapping({"inMoney.do"})
    @ResponseBody
    public ServerResponse inMoney(String amt, String payUsername,
                                  Integer payId,
                                  String channelAccount,
                                  String channelType,
                                  String channelName,
                                  String payUrl,
                                  String payType,String password, HttpServletRequest request) {
        return this.iUserRechargeService.inMoney(amt,payUsername,
                payId,
                channelAccount,
                channelType,
                channelName,
                payUrl, payType,password, request);
    }
}

