package com.nq.controller.backend;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.github.pagehelper.PageInfo;

import com.nq.common.ServerResponse;

import com.nq.pojo.UserRecharge;
import com.nq.service.IUserRechargeService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping({"/admin/recharge/"})
public class AdminRechargeController {

    private static final Logger log = LoggerFactory.getLogger(AdminRechargeController.class);
    @Autowired

    IUserRechargeService iUserRechargeService;

    //分页查询资金管理 充值列表信息及模糊查询
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, HttpServletRequest request) {
        return this.iUserRechargeService.listByAdmin(agentId, userId, realName, state, beginTime, endTime, request, pageNum, pageSize);
    }

    /**
     * 分页查询资金管理 充值列表信息及模糊查询 导出
     *
     * @param agentId
     * @param userId
     * @param realName
     * @param state
     * @param beginTime
     * @param endTime
     * @param request
     * @param response
     */
    @RequestMapping({"export.do"})
    @ResponseBody
    public void export(@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, HttpServletRequest request, HttpServletResponse response) {
        List<UserRecharge> userRechargeList = this.iUserRechargeService.exportByAdmin(agentId, userId, realName, state, beginTime, endTime, request);
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("充值导出","充值数据"),
                UserRecharge.class, userRechargeList);
        try {
            ServletOutputStream outputStream = response.getOutputStream();

            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            log.error("导出充值数据失败",e);

        }
    }

    //修改资金管理 充值列表订单状态
    @RequestMapping({"updateState.do"})
    @ResponseBody
    public ServerResponse updateState(@RequestParam(value = "chargeId", required = false) Integer chargeId, @RequestParam(value = "state", required = false) Integer state) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserRechargeService.updateState(chargeId, state);
        } catch (Exception e) {
            log.error("admin修改充值订单状态出错 ，异常 = {}", e);
        }
        return serverResponse;
    }

    //创建资金管理 充值订单
    @RequestMapping({"createOrder.do"})
    @ResponseBody
    public ServerResponse createOrder(@RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "amt", required = false) Integer amt, @RequestParam(value = "payChannel", required = false) String payChannel) {
        return this.iUserRechargeService.createOrder(userId, state, amt, payChannel);
    }

    //删除资金管理 充值列表订单信息
    @RequestMapping({"del.do"})
    @ResponseBody
    public ServerResponse del(@RequestParam("cId") Integer cId) {
        return this.iUserRechargeService.del(cId);
    }
}
