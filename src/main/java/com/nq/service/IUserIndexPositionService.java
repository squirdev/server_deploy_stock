package com.nq.service;


import com.nq.common.ServerResponse;
import com.nq.pojo.UserIndexPosition;
import com.nq.vo.indexposition.IndexPositionVO;

import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public interface IUserIndexPositionService {
  ServerResponse buyIndex(Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, Integer lever, BigDecimal profitTarget,BigDecimal stopTarget, HttpServletRequest paramHttpServletRequest) throws Exception;

  ServerResponse del(Integer paramInteger);

  ServerResponse sellIndex(String paramString, int paramInt) throws Exception;
  
  ServerResponse lock(Integer paramInteger1, Integer paramInteger2, String paramString);
  
  ServerResponse listByAgent(Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, Integer paramInteger4, String paramString1, String paramString2, String paramString3, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);
  
  ServerResponse listByAdmin(Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, Integer paramInteger4, String paramString1, String paramString2, String paramString3, int paramInt1, int paramInt2);
  
  ServerResponse findMyIndexPositionByNameAndCode(String paramString1, String paramString2, Integer paramInteger, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);
  
  IndexPositionVO findUserIndexPositionAllProfitAndLose(Integer paramInteger);
  
  List<Integer> findDistinctUserIdList();
  
  List<UserIndexPosition> findIndexPositionByUserIdAndSellPriceIsNull(Integer paramInteger);
  
  ServerResponse getIndexIncome(Integer paramInteger1, Integer paramInteger2, String paramString1, String paramString2);

  ServerResponse findUserIndexPositionByCode(HttpServletRequest paramHttpServletRequest, String indexGid);
  ServerResponse buyIndexOrder(Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, Integer lever,BigDecimal profitTarget,BigDecimal stopTarget, Integer userId) throws Exception;

  ServerResponse indexPositiontask();
}
