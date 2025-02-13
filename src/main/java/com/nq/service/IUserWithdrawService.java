package com.nq.service;


import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.pojo.UserWithdraw;

import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public interface IUserWithdrawService {
  ServerResponse outMoney(String paramString,String with_Pwd, HttpServletRequest paramHttpServletRequest) throws Exception;
  
  ServerResponse<PageInfo> findUserWithList(String paramString, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);
  
  ServerResponse userCancel(Integer paramInteger);
  
  ServerResponse listByAgent(Integer paramInteger1, String paramString, Integer paramInteger2, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);
  
  ServerResponse<PageInfo> listByAdmin(Integer paramInteger1, Integer paramInteger2, String paramString1, Integer paramInteger3, String paramString2, String paramString3, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);
  
  ServerResponse updateState(Integer paramInteger1, Integer paramInteger2, String paramString) throws Exception;
  
  int deleteByUserId(Integer paramInteger);
  
  BigDecimal CountSpWithSumAmtByState(Integer paramInteger);

  BigDecimal CountSpWithSumAmTodaytByState(Integer paramInteger);

  ServerResponse deleteWithdraw(Integer withdrawId);

  List<UserWithdraw> exportByAdmin( Integer agentId, Integer userId, String realName, Integer state, String beginTime, String endTime, HttpServletRequest request);
}
