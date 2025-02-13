package com.nq.service;

import com.nq.common.ServerResponse;
import com.nq.pojo.SiteIndexSetting;

public interface ISiteIndexSettingService {
  SiteIndexSetting getSiteIndexSetting();
  
  ServerResponse update(SiteIndexSetting paramSiteIndexSetting);
}
