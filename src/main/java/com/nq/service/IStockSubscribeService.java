package com.nq.service;

import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.pojo.StockSubscribe;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.xml.crypto.Data;

/**
* @author 11527
* @description 针对表【stock_subscribe(新股)】的数据库操作Service
* @createDate 2022-10-24 23:27:27
*/
public interface IStockSubscribeService extends IService<StockSubscribe> {
    ServerResponse<PageInfo> list( int paramInt1, int paramInt2,String name,String code,Integer zt,Integer isLock,Integer type, HttpServletRequest paramHttpServletRequest);

    ServerResponse<PageInfo> listbyadmin( int paramInt1, int paramInt2,String name,String code,Integer zt,Integer isLock,Integer type, HttpServletRequest paramHttpServletRequest);

    ServerResponse add(StockSubscribe model, HttpServletRequest request);

    ServerResponse update(StockSubscribe model, HttpServletRequest request);

    ServerResponse del(Integer id, HttpServletRequest request);

    ServerResponse newStockQc(HttpServletRequest request);
}
