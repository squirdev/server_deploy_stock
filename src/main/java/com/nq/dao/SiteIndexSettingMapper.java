package com.nq.dao;

import com.nq.pojo.SiteIndexSetting;
import java.util.List;

public interface SiteIndexSettingMapper {
  int deleteByPrimaryKey(Integer paramInteger);
  
  int insert(SiteIndexSetting paramSiteIndexSetting);
  
  int insertSelective(SiteIndexSetting paramSiteIndexSetting);
  
  SiteIndexSetting selectByPrimaryKey(Integer paramInteger);
  
  int updateByPrimaryKeySelective(SiteIndexSetting paramSiteIndexSetting);
  
  int updateByPrimaryKey(SiteIndexSetting paramSiteIndexSetting);
  
  List selectAllSiteIndexSetting();
}