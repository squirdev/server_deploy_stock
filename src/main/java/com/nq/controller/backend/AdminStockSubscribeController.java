package com.nq.controller.backend;


import com.nq.common.ServerResponse;
import com.nq.dao.StockSubscribeMapper;
import com.nq.pojo.StockSubscribe;
import com.nq.pojo.UserStockSubscribe;
import com.nq.service.IStockSubscribeService;
import com.nq.service.IUserStockSubscribeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/admin/subscribe/"})
public class AdminStockSubscribeController {
    private static final Logger log = LoggerFactory.getLogger(AdminStockSubscribeController.class);
    @Autowired
    IUserStockSubscribeService iUserStockSubscribeService;
    @Autowired
    IStockSubscribeService iStockSubscribeService;

    /**
     * 
     * @param pageNum
     * @param pageSize
     * @param request
     * @return
     */
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,@RequestParam(value = "name", required = false) String name,@RequestParam(value = "code", required = false) String code,@RequestParam(value = "zt", required = false) Integer zt,@RequestParam(value = "isLock", required = false) Integer isLock,@RequestParam(value = "type", required = false) Integer type, HttpServletRequest request) {
        return this.iStockSubscribeService.listbyadmin(pageNum, pageSize,name,code,zt,isLock,type,request);
    }
    /** 
    * @Description:  新增新股
    * @Param:  
    * @return:  
    * @Author: tf
    * @Date: 2022/10/25 
    */
    @RequestMapping({"add.do"})
    @ResponseBody
    public ServerResponse add(StockSubscribe model, HttpServletRequest request) {
        return this.iStockSubscribeService.add(model, request);

    }
    /**
    * @Description:  修改新股
    * @Param:
    * @return:
    * @Author: tf
    * @Date: 2022/10/25
    */
    @RequestMapping({"update.do"})
    @ResponseBody
    public ServerResponse update(StockSubscribe model, HttpServletRequest request) {
        return this.iStockSubscribeService.update(model, request);

    }
    /**
    * @Description:  删除新股
    * @Param:
    * @return:
    * @Author: tf
    * @Date: 2022/10/25
    */
    @RequestMapping({"del.do"})
    @ResponseBody
    public ServerResponse del(@RequestParam("id") Integer id, HttpServletRequest request) {
        return this.iStockSubscribeService.del(id, request);

    }

    //申购信息列表查询
    @RequestMapping({"getStockSubscribeList.do"})
    @ResponseBody
    public ServerResponse getStockSubscribeList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, @RequestParam(value = "keyword", defaultValue = "") String keyword, HttpServletRequest request) {
        return this.iUserStockSubscribeService.getList(pageNum, pageSize, keyword, request);
    }

    //申购信息-添加 修改
    @RequestMapping({"saveStockSubscribe.do"})
    @ResponseBody
    public ServerResponse saveStockSubscribe(UserStockSubscribe model, HttpServletRequest request) {
        return this.iUserStockSubscribeService.save(model, request);
    }

    //发送站内信
    @RequestMapping({"sendMsg.do"})
    @ResponseBody
    public ServerResponse sendMsg(UserStockSubscribe model, HttpServletRequest request) {
        return this.iUserStockSubscribeService.sendMsg(model, request);
    }
    //新股申购-删除
    @RequestMapping({"delStockSubscribe.do"})
    @ResponseBody
    public ServerResponse delStockSubscribe(@RequestParam("id") int id, HttpServletRequest request) {
        return this.iUserStockSubscribeService.del(id, request);
    }
    //新股抢筹 股票列表
    @RequestMapping({"getStockQcList.do"})
    @ResponseBody
    public ServerResponse getStockQcList( HttpServletRequest request) {
        return this.iUserStockSubscribeService.getStockQcList( request);
    }

    //新股抢筹 记录列表
    @RequestMapping({"getStockSubscribeQcListByAdmin.do"})
    @ResponseBody
    public ServerResponse getStockSubscribeQcList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "keyword", required = false) String keyword, HttpServletRequest request) {
        return this.iUserStockSubscribeService.getQcList(pageNum, pageSize, keyword, request);
    }
    //新股抢筹-审核（修改）
    @RequestMapping({"updateStockSubscribeQcByAdmin.do"})
    @ResponseBody
    public ServerResponse updateStockSubscribeQc(@RequestParam(value = "id")String id,@RequestParam(value = "status")String status,@RequestParam(value = "num")String num, HttpServletRequest request) {
        return this.iUserStockSubscribeService.updateQcByAdmin(id,status,num, request);
    }
    //新股抢筹-记录添加
    @RequestMapping({"addStockSubscribeQcByAdmin.do"})
    @ResponseBody
    public ServerResponse addStockSubscribeQc(@RequestParam("userPhone")String phone,@RequestParam("newCode")String code,@RequestParam("num")String num, HttpServletRequest request) {
        return this.iUserStockSubscribeService.addQcByAdmin(phone,code,num, request);
    }

}
