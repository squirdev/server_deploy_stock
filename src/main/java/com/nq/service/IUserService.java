package com.nq.service;


import com.nq.common.ServerResponse;
import com.nq.pojo.User;
import com.nq.vo.position.ForceSellPositonDTO;

import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public interface IUserService {
  ServerResponse reg( String paramString2, String paramString3, String paramString4, String uid, HttpServletRequest paramHttpServletRequest);

  ServerResponse login(String paramString1, String paramString2, HttpServletRequest paramHttpServletRequest);

  User getCurrentUser(HttpServletRequest paramHttpServletRequest);

  User getCurrentRefreshUser(HttpServletRequest paramHttpServletRequest);

  ServerResponse addOption(String paramString, HttpServletRequest paramHttpServletRequest);

  ServerResponse delOption(String paramString, HttpServletRequest paramHttpServletRequest);

  ServerResponse isOption(String paramString, HttpServletRequest paramHttpServletRequest);

  ServerResponse getUserInfo(HttpServletRequest paramHttpServletRequest);

  ServerResponse updatePwd(String paramString1, String paramString2, HttpServletRequest paramHttpServletRequest);

  ServerResponse checkPhone(String paramString);

  ServerResponse updatePwd(String paramString1, String paramString2, String paramString3);

  ServerResponse update(User paramUser);

  ServerResponse auth(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, HttpServletRequest paramHttpServletRequest);

  ServerResponse transAmt(Integer paramInteger1, Integer paramInteger2, HttpServletRequest paramHttpServletRequest);

  void ForceSellTask();
  void ForceSellOneStockTask();
  void ForceSellOneStockTaskV2();
  void ForceSellMessageTask();

  void ForceSellIndexTask();
  void ForceSellIndexsMessageTask();

  void ForceSellFuturesTask();
  void ForceSellFuturesMessageTask();

  void qh1();

  void zs1();

  ServerResponse listByAgent(String paramString1, String paramString2, Integer paramInteger1, Integer paramInteger2, int paramInt1, int paramInt2, HttpServletRequest paramHttpServletRequest);

  ServerResponse addSimulatedAccount(Integer paramInteger1, String paramString1, String paramString2, String paramString3, Integer paramInteger2, HttpServletRequest paramHttpServletRequest);

  ServerResponse listByAdmin(String paramString1, String paramString2, Integer paramInteger1, Integer paramInteger2, int paramInt1, int paramInt2, HttpServletRequest paramHttpServletRequest);

  ServerResponse findByUserId(Integer paramInteger);

  ServerResponse updateLock(Integer paramInteger);

  ServerResponse updateAmt(Integer paramInteger1, String paramInteger2, Integer paramInteger3);

  ServerResponse delete(Integer paramInteger, HttpServletRequest paramHttpServletRequest);

  int CountUserSize(Integer paramInteger);

  BigDecimal CountUserAmt(Integer paramInteger);

  BigDecimal CountEnableAmt(Integer paramInteger);

  ServerResponse authByAdmin(Integer paramInteger1, Integer paramInteger2, String paramString);

  ServerResponse findIdWithPwd(HttpServletRequest paramHttpServletRequest);

  ServerResponse updateWithPwd(String paramString1, HttpServletRequest paramHttpServletRequest);

  void updateUserAmt(Double amt, Integer user_id);


  void forceSellTipTask();

  List<ForceSellPositonDTO> getForceList();

}
