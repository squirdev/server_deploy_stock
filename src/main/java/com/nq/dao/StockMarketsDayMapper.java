package com.nq.dao;

import com.nq.pojo.StockMarketsDay;
import java.math.BigDecimal;
import org.apache.ibatis.annotations.Param;

public interface StockMarketsDayMapper {
  int deleteByPrimaryKey(Integer paramInteger);

  int insert(StockMarketsDay paramStockMarketsDay);

  int insertSelective(StockMarketsDay paramStockMarketsDay);

  StockMarketsDay selectByPrimaryKey(Integer paramInteger);

  int updateByPrimaryKeySelective(StockMarketsDay paramStockMarketsDay);

  int updateByPrimaryKey(StockMarketsDay paramStockMarketsDay);

  BigDecimal selectRateByDaysAndStockCode(@Param("stockId") Integer paramInteger1, @Param("days") Integer paramInteger2);

  BigDecimal selectBusinessBalanceByStockCode(@Param("stockCode") String paramInteger1);
}
