package com.nq.service;

import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.pojo.UserPosition;
import com.nq.vo.position.PositionProfitVO;
import com.nq.vo.position.PositionVO;

import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public interface IUserPositionService {
  ServerResponse buy(Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, Integer paramInteger4,BigDecimal paramInteger5,BigDecimal paramInteger6, HttpServletRequest paramHttpServletRequest) throws Exception;
  
  ServerResponse sell(String paramString, int paramInt) throws Exception;
  
  ServerResponse lock(Integer paramInteger1, Integer paramInteger2, String paramString);
  
  ServerResponse del(Integer paramInteger);
  
  ServerResponse<PageInfo> findMyPositionByCodeAndSpell(String paramString1, String paramString2, Integer paramInteger, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);
  
  PositionVO findUserPositionAllProfitAndLose(Integer paramInteger);
  
  List<UserPosition> findPositionByUserIdAndSellIdIsNull(Integer paramInteger);
  
  List<UserPosition> findPositionByStockCodeAndTimes(int paramInt, String paramString, Integer paramInteger);
  
  Integer findPositionNumByTimes(int paramInt, Integer paramInteger);
  
  ServerResponse listByAgent(Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, Integer paramInteger4, String paramString1, String paramString2, String paramString3, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);
  
  ServerResponse getIncome(Integer paramInteger1, Integer paramInteger2, String paramString1, String paramString2);
  
  ServerResponse listByAdmin(Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, Integer paramInteger4, String paramString1, String paramString2, String paramString3, int paramInt1, int paramInt2);
  
  int CountPositionNum(Integer paramInteger1, Integer paramInteger2);
  
  BigDecimal CountPositionProfitAndLose();
  
  BigDecimal CountPositionAllProfitAndLose();
  BigDecimal CountPositionAllProfitAndLoseByUser(Integer userId);


  ServerResponse create(Integer paramInteger1, String paramString1, String paramString2, String paramString3, Integer paramInteger2, Integer paramInteger3, Integer paramInteger4,BigDecimal paramInteger5,BigDecimal paramInteger6) ;
  
  int deleteByUserId(Integer paramInteger);
  
  void doClosingStayTask();
  void expireStayUnwindTask();
  
  ServerResponse closingStayTask(UserPosition paramUserPosition, Integer paramInteger) throws Exception;
  
  List<Integer> findDistinctUserIdList();

  ServerResponse<PageInfo> findPositionTopList(Integer pageSize);

  ServerResponse findUserPositionByCode(HttpServletRequest paramHttpServletRequest, String stockCode);

  ServerResponse addmargin(String paramString, int paramInt, BigDecimal marginAdd) throws Exception;

  PositionProfitVO getPositionProfitVO(UserPosition position);


  ServerResponse newStockToPosition(Integer id);

  ServerResponse updateProfitTarget(String positionSn, Integer profitTarget, Integer stopTarget, HttpServletRequest request);

    ServerResponse buyDz(String stockCode, String password, Integer num, HttpServletRequest request) throws Exception;

  ServerResponse buyVipQc(String stockCode,Integer buyNum, Integer buyType, Integer lever, BigDecimal profitTarget, BigDecimal stopTarget, HttpServletRequest request) throws Exception;

  ServerResponse buyStockDzList( HttpServletRequest request);

  ServerResponse sellPreprocessing(String positionSn, Integer sellNum) throws Exception;

  ServerResponse buy2(String positionSn, Integer buyNum, HttpServletRequest request)  throws Exception;

  Integer getCanSellNum(UserPosition position);

  ServerResponse getCanSellNum(String positionSn);
}
