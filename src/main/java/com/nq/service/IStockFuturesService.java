package com.nq.service;


import com.nq.common.ServerResponse;
import com.nq.pojo.StockFutures;
import com.nq.vo.foreigncurrency.ExchangeVO;
import com.nq.vo.stockfutures.FuturesVO;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

public interface IStockFuturesService {
  ServerResponse listByAdmin(String paramString1, String paramString2,Integer homeShow,Integer listShow,Integer transState, int paramInt1, int paramInt2);
  
  ServerResponse add(StockFutures paramStockFutures);
  
  ServerResponse update(StockFutures paramStockFutures);
  
  ServerResponse queryHome();
  
  ServerResponse queryList(HttpServletRequest request);
  
  ServerResponse queryTrans(Integer paramInteger);
  
  FuturesVO querySingleMarket(String paramString);
  
  ServerResponse<ExchangeVO> queryExchangeVO(String paramString);
  
  ServerResponse<BigDecimal> getExchangeRate(String paramString);
  
  StockFutures selectFuturesById(Integer paramInteger);
  
  StockFutures selectFuturesByCode(String paramString);
}
