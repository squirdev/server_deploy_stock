package com.nq.service.impl;

import com.github.pagehelper.PageHelper;

import com.github.pagehelper.PageInfo;

import com.nq.common.ServerResponse;

import com.nq.dao.SiteAmtTransLogMapper;

import com.nq.pojo.SiteAmtTransLog;

import com.nq.service.ISiteAmtTransLogService;

import java.util.List;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

@Service("iSiteAmtTransLogService")

public class SiteAmtTransLogServiceImpl

        implements ISiteAmtTransLogService {

    private static final Logger log = LoggerFactory.getLogger(SiteAmtTransLogServiceImpl.class);

    @Autowired
    SiteAmtTransLogMapper siteAmtTransLogMapper;

    public ServerResponse<PageInfo> transList(Integer userId, String realName, int pageNum, int pageSize) {

        PageHelper.startPage(pageNum, pageSize);

        List<SiteAmtTransLog> siteAmtTransLogs = this.siteAmtTransLogMapper.transList(userId, realName);

        PageInfo pageInfo = new PageInfo(siteAmtTransLogs);

        return ServerResponse.createBySuccess(pageInfo);

    }

}
