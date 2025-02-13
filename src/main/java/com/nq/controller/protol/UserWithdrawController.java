package com.nq.controller.protol;

import com.nq.common.ServerResponse;
import com.nq.pojo.User;
import com.nq.service.IUserService;
import com.nq.service.IUserWithdrawService;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/user/withdraw/"})
public class UserWithdrawController {
    private static final Logger log = LoggerFactory.getLogger(UserWithdrawController.class);

    @Autowired
    IUserWithdrawService iUserWithdrawService;

    @Autowired
    IUserService iUserService;

    //分页查询所有提现记录
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "8") int pageSize, @RequestParam(value = "withStatus", required = false) String withStatus, HttpServletRequest request) {
        return this.iUserWithdrawService.findUserWithList(withStatus, request, pageNum, pageSize);
    }

    //用户提现
    @RequestMapping({"outMoney.do"})
    @ResponseBody
    public ServerResponse outMoney(String amt,String withPwd, HttpServletRequest request) {
        ServerResponse serverResponse = null;
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (withPwd == null || !withPwd.equals(user.getWithPwd())) {
            return ServerResponse.createByErrorMsg("资金密码错误");
        }
        try {
            serverResponse = this.iUserWithdrawService.outMoney(amt, withPwd, request);
        } catch (Exception e) {
            log.error("出金异常 e = {}", e);
            serverResponse = ServerResponse.createByErrorMsg("出金异常，请稍后再试");
        }
        return serverResponse;
    }

    @RequestMapping({"cancel.do"})
    @ResponseBody
    public ServerResponse userCancel(Integer withId) {
        return this.iUserWithdrawService.userCancel(withId);
    }
}