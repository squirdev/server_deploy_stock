package com.nq.service;


import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.pojo.SiteNews;

import javax.servlet.http.HttpServletRequest;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 新闻资讯
 * @author lr
 * @date 2020/07/24
 */
public interface ISiteNewsService {

    /**
     * 新增
     */
    int insert(SiteNews model);

    /**
     * 更新
     */
    int update(SiteNews model);

    /**
     * 新闻资讯-保存
     */
    ServerResponse save(SiteNews model);

    /**
     * 新闻资讯-列表查询
     */
    ServerResponse<PageInfo> getList(int pageNum, int pageSize, Integer type, String sort, String keyword, HttpServletRequest request);

    /**
     * 新闻资讯-查询详情
     */
    ServerResponse getDetail(int id, HttpServletRequest request);

    /**
     * 抓取新闻
     */
    int grabNews();

    /**
     * 新闻资讯-修改新闻浏览量
     */
    ServerResponse updateViews(Integer id);

    /**
     * 新闻资讯-top最新新闻资讯
     */
    ServerResponse getTopNewsList(int pageSize);

}
