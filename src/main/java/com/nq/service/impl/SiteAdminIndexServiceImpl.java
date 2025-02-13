package com.nq.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.nq.common.ServerResponse;
import com.nq.dao.SiteInfoMapper;
import com.nq.pojo.SiteAdminIndex;
import com.nq.pojo.SiteInfo;
import com.nq.service.SiteAdminIndexService;
import com.nq.dao.SiteAdminIndexMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;

/**
* @author 11527
* @description 针对表【site_admin_index】的数据库操作Service实现
* @createDate 2022-11-03 01:39:06
*/
@Service("siteAdminIndexService")
@Slf4j
public class SiteAdminIndexServiceImpl extends ServiceImpl<SiteAdminIndexMapper, SiteAdminIndex>
    implements SiteAdminIndexService{
    @Autowired
    SiteAdminIndexMapper siteAdminIndexMapper;
    @Autowired
    SiteInfoMapper siteInfoMapper;
    @Override
    public ServerResponse setSiteStyle(SiteAdminIndex model) {
        log.info("model"+model);
        if (model != null) {

            int res = siteAdminIndexMapper.updateById(model);
            if (res > 0) {
                return ServerResponse.createBySuccessMsg("保存成功");
            }
            return ServerResponse.createByErrorMsg("保存失败");
        }
        return null;
    }

    @Override
    public ServerResponse getSiteStyle(Integer id) {
        SiteAdminIndex siteAdminIndex = siteAdminIndexMapper.selectById(id);

        SiteInfo siteInfo = siteInfoMapper.selectByPrimaryKey(id);
        Map map = Maps.newHashMap();
        if (siteAdminIndex != null&&siteInfo!=null) {
            map.put("siteAdminIndex", siteAdminIndex);
            map.put("siteInfo", siteInfo);
            return ServerResponse.createBySuccess(map);
        }
        return ServerResponse.createByErrorMsg("查询失败");
    }

}




