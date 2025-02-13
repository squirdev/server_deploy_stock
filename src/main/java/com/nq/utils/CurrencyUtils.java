package com.nq.utils;

import com.alibaba.fastjson2.JSONObject;
import com.nq.StockApplication;
import com.nq.service.IStockFuturesService;
import com.nq.service.IStockIndexService;
import com.nq.utils.redis.RedisShardedPoolUtils;
import com.nq.utils.stock.sina.SinaStockApi;
import com.nq.vo.foreigncurrency.ExchangeVO;
import com.nq.vo.stock.MarketVO;
import com.nq.vo.stock.StockListVO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
@Component
public class CurrencyUtils {
//    private static IStockFuturesService iStockFuturesService;

    @Autowired
    IStockFuturesService iStockFuturesService;
    @Autowired
    IStockIndexService iStockIndexService;
    //股票
    public String getNowStockPrice(String stockGid, String coinCode) {
        StockListVO stockListVO = new StockListVO();
        BigDecimal price = new BigDecimal(0);
        if (stockGid.contains("us")) {
            String us = RedisShardedPoolUtils.get(stockGid, 2);
            stockListVO = SinaStockApi.otherStockListVO(us);
            ExchangeVO exchangeVO = iStockFuturesService.queryExchangeVO(coinCode).getData();
            price = new BigDecimal(stockListVO.getNowPrice()).multiply(new BigDecimal(exchangeVO.getNowPrice()));
        }else if (stockGid.contains("hk")) {
            String hk = RedisShardedPoolUtils.get(stockGid, 1);
            stockListVO = SinaStockApi.otherStockListVO(hk);
            ExchangeVO exchangeVO = iStockFuturesService.queryExchangeVO(coinCode).getData();
            price = new BigDecimal(stockListVO.getNowPrice()).multiply(new BigDecimal(exchangeVO.getNowPrice()));
        }
        return price.toString();
    }
    //指数
    public  BigDecimal getNowIndexPrice(String indexGid,String coinCode) {

        MarketVO marketVO = this.iStockIndexService.querySingleIndex(indexGid);
        ExchangeVO exchangeVO = iStockFuturesService.queryExchangeVO(coinCode).getData();
        BigDecimal  price = new BigDecimal(marketVO.getNowPrice()).multiply(new BigDecimal(exchangeVO.getNowPrice()));

        return price;
    }
}
