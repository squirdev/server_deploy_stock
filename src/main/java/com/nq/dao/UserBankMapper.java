package com.nq.dao;


import com.nq.pojo.UserBank;

public interface UserBankMapper {
  int deleteByPrimaryKey(Integer paramInteger);
  
  int insert(UserBank paramUserBank);
  
  int insertSelective(UserBank paramUserBank);
  
  UserBank selectByPrimaryKey(Integer paramInteger);
  
  int updateByPrimaryKeySelective(UserBank paramUserBank);
  
  int updateByPrimaryKey(UserBank paramUserBank);
  
  UserBank findUserBankByUserId(Integer paramInteger);
}
