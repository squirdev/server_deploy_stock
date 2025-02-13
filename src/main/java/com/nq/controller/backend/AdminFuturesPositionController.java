package com.nq.controller.backend;


import com.nq.common.ServerResponse;
import com.nq.service.IUserFuturesPositionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/admin/futures/position/"})
public class AdminFuturesPositionController {
    private static final Logger log = LoggerFactory.getLogger(AdminFuturesPositionController.class);


    @Autowired
    IUserFuturesPositionService iUserFuturesPositionService;

    //分页查询持仓管理 期货持仓单/平仓单 及模糊查询
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "positionType", required = false) Integer positionType, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "positionSn", required = false) String positionSn, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize) {
        return this.iUserFuturesPositionService.listByAdmin(agentId, positionType, state, userId, positionSn, beginTime, endTime, pageNum, pageSize);
    }

    @RequestMapping({"sell.do"})
    @ResponseBody
    public ServerResponse sell(String positionSn) throws Exception {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserFuturesPositionService.sellFutures(positionSn, 0);
        } catch (Exception e) {
            log.error("强制平仓 期货 异常信息 = {}", e);
        }
        return serverResponse;
    }

    @RequestMapping({"del.do"})
    @ResponseBody
    public ServerResponse del(@RequestParam("positionId") Integer positionId) {
        return this.iUserFuturesPositionService.del(positionId);
    }

    @RequestMapping({"lock.do"})
    @ResponseBody
    public ServerResponse lock(@RequestParam("positionId") Integer positionId, @RequestParam("state") Integer state, @RequestParam(value = "lockMsg", required = false) String lockMsg) {
        return this.iUserFuturesPositionService.lock(positionId, state, lockMsg);
    }
}

