package com.nq.service;


import com.nq.common.ServerResponse;
import com.nq.pojo.SiteInfo;

public interface ISiteInfoService {
  ServerResponse get();
  
  ServerResponse add(SiteInfo paramSiteInfo);
  
  ServerResponse update(SiteInfo paramSiteInfo);
  
  ServerResponse getInfo();
}
