package com.nq.service;


import com.nq.common.ServerResponse;
import com.nq.pojo.SiteAdminIndex;
import com.nq.pojo.SiteSetting;

public interface ISiteSettingService {
  SiteSetting getSiteSetting();
  
  ServerResponse update(SiteSetting paramSiteSetting);


}