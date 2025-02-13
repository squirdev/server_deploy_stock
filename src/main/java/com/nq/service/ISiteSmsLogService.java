package com.nq.service;


import com.nq.common.ServerResponse;
import com.nq.pojo.SiteSmsLog;

import javax.servlet.http.HttpServletRequest;

public interface ISiteSmsLogService {
  ServerResponse smsList(String paramString, int paramInt1, int paramInt2);

  void addData(SiteSmsLog siteSmsLog);

  ServerResponse del(Integer id, HttpServletRequest request);

}
