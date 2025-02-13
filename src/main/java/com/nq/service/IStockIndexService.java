package com.nq.service;


import com.nq.common.ServerResponse;
import com.nq.pojo.StockIndex;
import com.nq.vo.stock.MarketVO;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpServletRequest;

public interface IStockIndexService {
  ServerResponse listByAdmin(Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, String paramString1, String paramString2, int paramInt1, int paramInt2, HttpServletRequest paramHttpServletRequest);
  
  ServerResponse updateIndex(StockIndex paramStockIndex);
  
  ServerResponse addIndex(StockIndex paramStockIndex);
  
  ServerResponse queryHomeIndex();
  
  ServerResponse queryListIndex(HttpServletRequest request);
  
  ServerResponse queryTransIndex(Integer paramInteger);
  
  MarketVO querySingleIndex(String paramString);
  
  StockIndex selectIndexById(Integer paramInteger);

  void otherIndexTask();

  ServerResponse queryIndexNews();

  StockIndex selectIndexByCode(String indexCode);

  ServerResponse queryCustIndex();

}
