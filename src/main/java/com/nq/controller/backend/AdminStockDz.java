package com.nq.controller.backend;

import com.nq.common.ServerResponse;
import com.nq.pojo.StockDz;
import com.nq.service.StockDzService;
import com.nq.vo.stock.StockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping({"/admin/stockDz/"})
public class AdminStockDz {
    @Autowired
    StockDzService stockDzService;

    /**
     * @Description:  获取大宗列表
     * @Param:
     * @return:
     */
    @RequestMapping({"getDzListByAdmin.do"})
    public ServerResponse getDzListByAdmin(String keywords) {
        return stockDzService.getDzListByAdmin(keywords);
    }
    /**
     * @Description:  新增大宗
     * @Param:
     * @return:
     */
    @RequestMapping({"addByAdmin.do"})
    public ServerResponse addByAdmin(@RequestParam(value = "stockCode")String stockCode,
                                     @RequestParam(value = "stockNum")String stockNum,
                                     @RequestParam(value = "password")String password,
                                     @RequestParam(value = "startTime")String startTime,
                                     @RequestParam(value = "endTime")String endTime,
                                     @RequestParam(value = "discount") String discount) {
        return stockDzService.addByAdmin(stockCode,stockNum,password,startTime,endTime,discount);
    }
    /**
     * 删除大宗
     */
    @RequestMapping({"deleteByAdmin.do"})
    public ServerResponse deleteByAdmin(@RequestParam(value = "id") String id) {
        return stockDzService.deleteByAdmin(id);
    }
    /**
     * 修改大宗
     */
    @RequestMapping({"updateByAdmin.do"})
    public ServerResponse updateByAdmin(StockDz model) {
        return stockDzService.updByAdmin(model);
    }

}
