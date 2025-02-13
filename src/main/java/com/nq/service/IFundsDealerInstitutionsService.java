package com.nq.service;

import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.pojo.FundsDealerInstitutions;

import javax.servlet.http.HttpServletRequest;

/**
 * 配资券商机构
 * @author lr
 * @date 2020/07/24
 */
public interface IFundsDealerInstitutionsService {

    /**
     * 新增
     */
    int insert(FundsDealerInstitutions model);

    /**
     * 更新
     */
    int update(FundsDealerInstitutions model);

    /**
     * 配资券商机构-保存
     */
    ServerResponse save(FundsDealerInstitutions model);

    /**
     * 配资券商机构-列表查询
     */
    ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, Integer status, HttpServletRequest request);

    /**
     * 配资券商机构-查询详情
     */
    ServerResponse getDetail(int id);

}
