package com.nq.service;


import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.pojo.SiteAdmin;
import com.nq.vo.user.AlarmDTO;

import javax.servlet.http.HttpServletRequest;

public interface ISiteAdminService {
  ServerResponse login(String paramString1, String paramString2, String paramString3, HttpServletRequest paramHttpServletRequest);
  
  ServerResponse<PageInfo> listByAdmin(String paramString1, String paramString2, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);
  
  ServerResponse authCharge(String paramString1, Integer paramInteger, String paramString2);
  
  ServerResponse updateLock(Integer paramInteger);
  
  ServerResponse add(SiteAdmin paramSiteAdmin);
  
  ServerResponse update(SiteAdmin paramSiteAdmin);
  
  SiteAdmin findAdminByName(String paramString);
  
  SiteAdmin findAdminByPhone(String paramString);
  
  ServerResponse count();

  ServerResponse deleteAdmin(Integer adminId);


  void alarm(String type, Integer bizId);

  AlarmDTO getAlarm(Integer adminUserId);

  AlarmDTO getAlarmByLoginToken(String loginToken);

}
