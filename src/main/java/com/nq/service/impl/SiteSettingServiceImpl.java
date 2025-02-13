package com.nq.service.impl;


import com.nq.common.ServerResponse;

import com.nq.dao.SiteSettingMapper;

import com.nq.dao.SiteAdminIndexMapper;
import com.nq.pojo.SiteSetting;

import com.nq.service.ISiteSettingService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;


@Service("iSiteSettingService")
public class SiteSettingServiceImpl implements ISiteSettingService {

    @Autowired
    SiteSettingMapper siteSettingMapper;
    @Autowired
    SiteAdminIndexMapper siteAdminIndexMapper;

    public SiteSetting getSiteSetting() {

        SiteSetting siteSetting = null;

        List list = this.siteSettingMapper.findAllSiteSetting();

        if (list.size() > 0) {

            siteSetting = (SiteSetting) list.get(0);

        }
        return siteSetting;
    }


    public ServerResponse update(SiteSetting setting) {
        if (setting.getId() == null) {
            return ServerResponse.createByErrorMsg("ID 不能为空");
        }
        SiteSetting siteSetting = this.siteSettingMapper.selectByPrimaryKey(setting.getId());
        if (siteSetting == null) {
            return ServerResponse.createByErrorMsg("查不到设置记录");
        }

        int updateCount = this.siteSettingMapper.updateByPrimaryKeySelective(setting);

        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("修改成功");
        }
        return ServerResponse.createByErrorMsg("修改失败");

    }



}
