package com.nq.service;


import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.pojo.SiteArticle;

import javax.servlet.http.HttpServletRequest;

public interface ISiteArticleService {
  ServerResponse<PageInfo> listByAdmin(String paramString1, String paramString2, int paramInt1, int paramInt2);
  
  ServerResponse add(SiteArticle paramSiteArticle);
  
  ServerResponse update(SiteArticle paramSiteArticle);
  
  ServerResponse detail(Integer paramInteger);
  
  ServerResponse list(String paramString1, String paramString2, int paramInt1, int paramInt2, HttpServletRequest request);

  /**
   * top最新公告
   */
  ServerResponse getTopArtList(int pageSize);

  /**
   * 抓取公告
   */
  int grabArticle();

  ServerResponse del(Integer artId);

}
