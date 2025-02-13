package com.nq.service;

import com.nq.common.ServerResponse;
import com.nq.pojo.UserPendingorder;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
* @author Administrator
* @description 针对表【user_pendingorder】的数据库操作Service
* @createDate 2022-11-10 06:10:40
*/
public interface UserPendingorderService extends IService<UserPendingorder> {

    ServerResponse addOrder(String stockId, Integer buyNum, Integer buyType, Integer lever, BigDecimal profitTarget, BigDecimal stopTarget, BigDecimal targetPrice, HttpServletRequest request);

    ServerResponse orderList( HttpServletRequest request);

    void orderTask();

    ServerResponse delOrder(Integer id, HttpServletRequest request);
}
