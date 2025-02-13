package com.nq.service;


import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;

import javax.servlet.http.HttpServletRequest;

public interface IStockOptionService {
  ServerResponse<PageInfo> findMyStockOptions(String paramString, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);
  
  ServerResponse isOption(Integer paramInteger, String paramString);

  String isMyOption(Integer paramInteger, String paramString);

}
