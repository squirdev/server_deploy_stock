package com.nq.service;


import com.nq.common.ServerResponse;
import com.nq.pojo.SiteFuturesSetting;

public interface ISiteFuturesSettingService {
  SiteFuturesSetting getSetting();
  
  ServerResponse update(SiteFuturesSetting paramSiteFuturesSetting);
}
