package com.nq.service;

import com.nq.common.ServerResponse;
import com.nq.pojo.StockDz;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nq.vo.stock.StockVO;

import java.math.BigDecimal;

/**
* @author Administrator
* @description 针对表【stock_dz】的数据库操作Service
* @createDate 2022-12-03 17:09:01
*/
public interface StockDzService extends IService<StockDz> {

    ServerResponse getDzList();

    ServerResponse addByAdmin(String stockCode, String stockNum, String password, String startTime, String endTime, String discount);

    ServerResponse getDzListByAdmin(String keywords);

    ServerResponse updByAdmin(StockDz model);

    ServerResponse deleteByAdmin(String id);
}
