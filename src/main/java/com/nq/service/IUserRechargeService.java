package com.nq.service;


import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.pojo.UserRecharge;

import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public interface IUserRechargeService {
  ServerResponse checkInMoney(int paramInt, Integer paramInteger);

  ServerResponse inMoney(String paramString1, String payUsername,Integer payId,
                         String channelAccount,
                         String channelType,
                         String channelName, String payUrl,String paramString2,String password, HttpServletRequest paramHttpServletRequest);

    ServerResponse inMoneyByAdmin(String amt, String payType, Integer uid);

    ServerResponse findUserRechargeByOrderSn(String paramString);

  ServerResponse chargeSuccess(UserRecharge paramUserRecharge) throws Exception;

  ServerResponse chargeFail(UserRecharge paramUserRecharge) throws Exception;

  ServerResponse chargeCancel(UserRecharge paramUserRecharge) throws Exception;

  ServerResponse<PageInfo> findUserChargeList(String paramString1, String paramString2, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);

  ServerResponse<PageInfo> listByAgent(Integer paramInteger1, String paramString1, String paramString2, Integer paramInteger2, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);

  ServerResponse listByAdmin(Integer paramInteger1, Integer paramInteger2, String paramString1, Integer paramInteger3, String paramString2, String paramString3, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);

  ServerResponse updateState(Integer paramInteger1, Integer paramInteger2) throws Exception;

  ServerResponse createOrder(Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, String paramString);

  ServerResponse del(Integer paramInteger);

  int deleteByUserId(Integer paramInteger);

  BigDecimal CountChargeSumAmt(Integer paramInteger);

  BigDecimal CountTotalRechargeAmountByTime(Integer paramInteger);

  List<UserRecharge> exportByAdmin( Integer agentId, Integer userId, String realName, Integer state, String beginTime, String endTime, HttpServletRequest request);
}
