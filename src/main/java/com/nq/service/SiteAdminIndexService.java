package com.nq.service;

import com.nq.common.ServerResponse;
import com.nq.pojo.SiteAdminIndex;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 11527
* @description 针对表【site_admin_index】的数据库操作Service
* @createDate 2022-11-03 01:39:06
*/
public interface SiteAdminIndexService extends IService<SiteAdminIndex> {
    ServerResponse setSiteStyle(SiteAdminIndex siteAdminIndex);

    ServerResponse getSiteStyle(Integer id);
}
