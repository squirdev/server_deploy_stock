package com.nq.service;


import com.nq.common.ServerResponse;
import com.nq.pojo.SiteBanner;

public interface ISiteBannerService {
  ServerResponse add(SiteBanner paramSiteBanner);
  
  ServerResponse listByAdmin(int paramInt1, int paramInt2);
  
  ServerResponse update(SiteBanner paramSiteBanner);
  
  ServerResponse delete(Integer paramInteger);
  
  ServerResponse getBannerByPlat(String paramString);
}
