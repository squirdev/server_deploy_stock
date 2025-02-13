package com.nq.service;

import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.pojo.FundsLever;

import javax.servlet.http.HttpServletRequest;

/**
 * funds_lever
 * @author lr
 * @date 2020/07/23
 */
public interface IFundsLeverService {

    /**
     * 新增
     */
    int insert(FundsLever fundsLever);

    /**
     * 删除
     */
    int delete(int id);

    /**
     * 更新
     */
    int update(FundsLever fundsLever);

    /*查询配资杠杆列表*/
    ServerResponse<PageInfo> getFundsLeverList(int pageNum, int pageSize, HttpServletRequest request);

    /**
     * 配资杠杆列表保存
     */
    ServerResponse saveFundsLever(FundsLever fundsLever);

    /**
     * [查询] 查询配资类型杠杆
     * @author lr
     * @date 2020/07/23
     **/
    ServerResponse getFundsTypeList(Integer cycleType);

    /**
     * 配资杠杆-查询杠杆费率
     */
    ServerResponse getLeverRateInfo(Integer cycleType, Integer lever);

}
