package com.nq.dao;

import com.nq.pojo.SiteFuturesSetting;
import java.util.List;

public interface SiteFuturesSettingMapper {
  int deleteByPrimaryKey(Integer paramInteger);
  
  int insert(SiteFuturesSetting paramSiteFuturesSetting);
  
  int insertSelective(SiteFuturesSetting paramSiteFuturesSetting);
  
  SiteFuturesSetting selectByPrimaryKey(Integer paramInteger);
  
  int updateByPrimaryKeySelective(SiteFuturesSetting paramSiteFuturesSetting);
  
  int updateByPrimaryKey(SiteFuturesSetting paramSiteFuturesSetting);
  
  List selectAllSiteSetting();
}
