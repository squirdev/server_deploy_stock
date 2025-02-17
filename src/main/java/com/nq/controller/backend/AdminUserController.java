package com.nq.controller.backend;


import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nq.common.ServerResponse;
import com.nq.pojo.User;
import com.nq.pojo.UserBank;
import com.nq.service.IUserBankService;
import com.nq.service.IUserService;


@Controller
@RequestMapping({"/admin/user/"})
public class AdminUserController {
    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    @Autowired
    IUserService iUserService;

    @Autowired
    private StringRedisTemplate redisTemplate; // Inject Redis Template

    @Autowired
    IUserBankService iUserBankService;

    //分页查询所有用户列表信息 及模糊查询用户信息
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(
        @RequestParam(value = "realName", required = false) String realName,
        @RequestParam(value = "phone", required = false) String phone,
        @RequestParam(value = "agentId", required = false) Integer agentId, 
        @RequestParam(value = "accountType", required = false) Integer accountType, 
        @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, 
        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, 
        HttpServletRequest request) {



        // 🔹 Fetch users from database
        ServerResponse<List<User>> result = this.iUserService.listByAdmin(realName, phone, agentId, accountType, pageNum, pageSize, request);
        if (!result.isSuccess()) {
            return result; // Return error if fetching users failed
        }
        
        // List<User> userList = result.getData(); // Get user list

        // // 🔹 Fetch online users from Redis
        // Set<String> onlineUserKeys = redisTemplate.keys("user:online:*");
        // Set<Integer> onlineUserIds = onlineUserKeys.stream()
        //     .map(key -> Integer.parseInt(key.replace("user:online:", "")))
        //     .collect(Collectors.toSet());

        // // 🔹 Add online status to users
        // userList.forEach(user -> user.setIsOnline(onlineUserIds.contains(user.getId())));

        // 🔹 Return the updated list
        // return ServerResponse.createBySuccess(userList);

        return result;




        // ServerResponse result = this.iUserService.listByAdmin(realName, phone, agentId, accountType, pageNum, pageSize, request);
        // System.out.print(result);
        // return result;
    }

    //查询用户信息是否存在
    @RequestMapping({"detail.do"})
    @ResponseBody
    public ServerResponse detail(Integer userId) {
        return this.iUserService.findByUserId(userId);
    }

    @RequestMapping({"updateLock.do"})
    @ResponseBody
    public ServerResponse updateLock(Integer userId) {
        return this.iUserService.updateLock(userId);
    }

    //修改用户列表 用户资金入款/扣款
    @RequestMapping({"updateAmt.do"})
    @ResponseBody
    public ServerResponse updateAmt(Integer userId, String amt, Integer direction) {
        //amt转Integer

        return this.iUserService.updateAmt(userId, amt, direction);
    }

    //修改用户列表 用户信息
    @RequestMapping({"update.do"})
    @ResponseBody
    public ServerResponse update(User user) {
        return this.iUserService.update(user);
    }

    //修改用户列表 银行卡信息
    @RequestMapping({"updateBank.do"})
    @ResponseBody
    public ServerResponse updateBank(UserBank userBank) {
        return this.iUserBankService.updateBankByAdmin(userBank);
    }

    //添加用户列表 用户信息
    @RequestMapping({"addSimulatedAccount.do"})
    @ResponseBody
    public ServerResponse addSimulatedAccount(HttpServletRequest request, @RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam("phone") String phone, @RequestParam("amt") String amt, @RequestParam("accountType") Integer accountType, @RequestParam("pwd") String pwd) {
        return this.iUserService.addSimulatedAccount(agentId, phone, pwd, amt, accountType, request);
    }

    @RequestMapping({"authByAdmin.do"})
    @ResponseBody
    public ServerResponse authByAdmin(Integer userId, Integer state, String authMsg) {
        return this.iUserService.authByAdmin(userId, state, authMsg);
    }

    //查看指定 用户列表的用户信息
    @RequestMapping({"getBank.do"})
    @ResponseBody
    public ServerResponse getBank(Integer userId) {
        return this.iUserBankService.getBank(userId);
    }

    //删除用户列表 用户信息
    @RequestMapping({"delete.do"})
    @ResponseBody
    public ServerResponse delete(Integer userId, HttpServletRequest request) {
        return this.iUserService.delete(userId, request);
    }
}
