package com.nq.service;

import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.pojo.FundsSecuritiesInfo;

import javax.servlet.http.HttpServletRequest;

/**
 * 配资证券信息
 * @author lr
 * @date 2020/07/24
 */
public interface IFundsSecuritiesInfoService {

    /**
     * 新增
     */
    int insert(FundsSecuritiesInfo model);

    /**
     * 更新
     */
    int update(FundsSecuritiesInfo model);

    /**
     * 配资券商机构-保存
     */
    ServerResponse save(FundsSecuritiesInfo model);

    /**
     * 配资券商机构-列表查询
     */
    ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, HttpServletRequest request);

    /**
     * 配资券商机构-查询详情
     */
    ServerResponse getDetail(int id);

    /**
     * [查询] 查询可用的证券信息
     * @author lr
     * @date 2020/07/24
     **/
    ServerResponse<PageInfo> getEnabledList();

}