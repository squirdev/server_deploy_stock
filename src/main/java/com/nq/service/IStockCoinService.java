package com.nq.service;


import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.pojo.StockCoin;

import java.util.List;

public interface IStockCoinService {
  ServerResponse<PageInfo> listByAdmin(String paramString1, String paramString2, int paramInt1, int paramInt2);
  
  ServerResponse add(StockCoin paramStockCoin);
  
  ServerResponse update(StockCoin paramStockCoin);
  
  StockCoin selectCoinByCode(String paramString);
  
  List getSelectCoin();
}
