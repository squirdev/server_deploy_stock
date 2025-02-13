package com.nq.utils.stock.wangji;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.nq.pojo.Stock;
import com.nq.utils.stock.wangji.vo.HsPageRet;
import com.nq.utils.stock.wangji.vo.HsQueryParam;
import com.nq.utils.stock.wangji.vo.KLineParam;
import com.nq.utils.stock.wangji.vo.QueryParam;
import com.nq.utils.stock.wangji.vo.WjKLineDataRet;
import com.nq.utils.stock.wangji.vo.WjPageData;
import com.nq.utils.stock.wangji.vo.WjPageRet;
import com.nq.utils.stock.wangji.vo.WjStockDetailDTO;
import com.nq.utils.stock.wangji.vo.WjStockExtInfoDTO;
import com.nq.utils.stock.wangji.vo.WjStockExtInfoRet;
import com.nq.utils.stock.wangji.vo.WjStockInfoDTO;
import com.nq.utils.stock.wangji.vo.WjStockInfoRet;
import com.nq.vo.stock.StockDTO;
import com.nq.vo.stock.StockListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WjStockApi {


    @Autowired
    private RestTemplate restTemplate;

    @Value("${wj.base}")
    private String baseUrl;

    @Value("${wj.appCode}")
    private String appCode;



    public WjPageRet getPageList(QueryParam queryParam) {

        return get("/hs_rank?type={type}&sort_field={sort_field}&order_by={order_by}&limit={limit}&page={page}",
                WjPageRet.class, queryParam.toParamsMap());

    }


    public List<WjStockExtInfoDTO> getHsPageList(HsQueryParam param) {
        HsPageRet ret = get("/hs/rank?sort={sort}&asc={asc}&page={page}&limit={limit}&market={market}",
                HsPageRet.class, param.toParamsMap());

        List<WjStockExtInfoDTO> data = ret.getData();

        return data;
    }


    public WjStockInfoDTO getStock(String stockCode, String stockType) {
        log.info("股票代码:{}", stockCode,stockType);
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("code", String.format("%s.%s", stockCode, stockType.toUpperCase()));
        log.info("携带参数:{}", params);
        WjStockInfoRet stockInfoRet = get("/hs_info?code={code}", WjStockInfoRet.class, params);
        log.info("查询url:{}", stockInfoRet);
        WjStockInfoDTO infoDTO = stockInfoRet.getData();
        log.info("查询结果:{}", infoDTO);
        return infoDTO;

    }

    public WjStockExtInfoDTO getRealData(String stockGid) {

        HashMap<String, Object> params = Maps.newHashMap();
        params.put("symbol", StringUtils.join(stockGid, ","));

        WjStockExtInfoRet ret = get("/hs/real?symbol={symbol}", WjStockExtInfoRet.class, params);

        Map<String, WjStockExtInfoDTO> data = ret.getData();

//        log.info("gerRealData: {}", JSONObject.toJSONString(data));

        return data.get(stockGid);

    }

    public Map<String, WjStockExtInfoDTO> getStockBatch(List<String> stockGid) {

        HashMap<String, Object> params = Maps.newHashMap();
        params.put("symbol", StringUtils.join(stockGid, ","));

        WjStockExtInfoRet ret = get("/hs/real?symbol={symbol}", WjStockExtInfoRet.class, params);

        Map<String, WjStockExtInfoDTO> data = ret.getData();

        return data;

    }


    public List<WjStockDetailDTO> getKLine(KLineParam param) {

        WjKLineDataRet wjKLineDataRet = get("/hs/kline?symbol={symbol}&type={type}&limit={limit}",
                WjKLineDataRet.class, param.toParamsMap());

        return wjKLineDataRet.getData();

    }



    public static StockListVO assembleStockListVO(WjStockExtInfoDTO stock) {

        StockListVO stockListVO = new StockListVO();

        stockListVO.setNowPrice(stock.getPrice().toString());
        stockListVO.setPreclose(stock.getPreclose().toString());

        stockListVO.setHcrate(stock.getChangeRate().setScale(2, BigDecimal.ROUND_HALF_UP));
        stockListVO.setUd(stock.getChange());

//        stockListVO.setToday_max(hqarr[4]);
//
//        stockListVO.setToday_min(hqarr[5]);
//
//        stockListVO.setBusiness_amount(hqarr[8]);
//
//        stockListVO.setBusiness_balance(hqarr[9]);
//
//        stockListVO.setPreclose_px(stock.getPreclose().toString());
        stockListVO.setYc(stock.getPreclose());
//
//        stockListVO.setOpen_px(hqarr[1]);

        return stockListVO;
    }






    public <T> T get(String url, Class<T> clazz) {
        return get(url, clazz, Maps.newHashMap());
    }

    public <T> T get(String url, Class<T> clazz, Map<String, Object> params) {
        url = String.format("%s%s", baseUrl, url);

        //指定header
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("APPCODE %s", appCode));
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class, params);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return null;
        }
        return JSONObject.parseObject(responseEntity.getBody(), clazz);
    }

    public <T> List<T> getList(String url, Class<T> clazz) {
        return getList(url, clazz, Maps.newHashMap());
    }
    public <T> List<T> getList(String url, Class<T> clazz, Map<String, Object> params) {
        String ret = get(url, String.class, params);
        return JSONArray.parseArray(ret, clazz);
    }

    public StockListVO getStockListVo(Stock stock) {

        WjStockExtInfoDTO realData = getRealData(stock.getStockGid());
        StockListVO stockListVO = assembleStockListVO(realData);
        stockListVO.setName(stock.getStockName());
        return stockListVO;
    }


    public static void main(String[] args) {
        BigDecimal bigDecimal = new BigDecimal("");
        System.out.println(bigDecimal);
    }
}
