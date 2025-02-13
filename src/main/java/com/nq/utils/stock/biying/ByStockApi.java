package com.nq.utils.stock.biying;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.nq.pojo.Stock;
import com.nq.pojo.StockIndex;
import com.nq.utils.DateTimeUtil;
import com.nq.vo.stock.StockDTO;
import com.nq.vo.stock.StockListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

@Component
@Slf4j
public class ByStockApi {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${by.base}")
    private String baseUrl;

    @Value("${by.license}")
    private String license;

    public static String convertTime(int time) {
        return String.format("%dm", time);
    }

    public static void main(String[] args) {
        System.out.println(convertTime(5));
    }

    public static StockListVO assembleStockListVO(StockDTO stock) {

        StockListVO stockListVO = new StockListVO();

        stockListVO.setNowPrice(stock.getP().toString());

        stockListVO.setHcrate(stock.getPc());
        stockListVO.setUd(stock.getUd());

//        stockListVO.setToday_max(hqarr[4]);
//
//        stockListVO.setToday_min(hqarr[5]);
//
//        stockListVO.setBusiness_amount(hqarr[8]);
//
//        stockListVO.setBusiness_balance(hqarr[9]);
//
        stockListVO.setPreclose_px(stock.getYc().toString());
        stockListVO.setYc(stock.getYc());
//
//        stockListVO.setOpen_px(hqarr[1]);

        return stockListVO;
    }


    public List<StockDTO> getKLine(String code, String time) {
        String url = String.format("/hszbl/fsjy/%s/%s", code, time);
        List<StockDTO> list = getList(url, StockDTO.class);
        return list;

    }


    public StockDTO getStock(String stockCode) {
        String url = String.format("/hsrl/ssjy/%s", stockCode);
        return get(url, StockDTO.class);

    }

    public StockDTO getStockIndex(String indexCode) {
        try {
            String url = String.format("/zs/sssj/%s", indexCode);
            return get(url, StockDTO.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }


    }

    public JSONObject getMmwp(String stockCode) {
        String url = String.format("/hsrl/mmwp/%s", stockCode);
        JSONObject jsonObject = get(url, JSONObject.class);
        return jsonObject;
    }

    public List<StockDTO> getStockList() {
        String url = "/hslt/list";
        return getList(url, StockDTO.class);
    }



    public <T> T get(String url, Class<T> clazz) {

        url = String.format("%s%s/%s", baseUrl, url, license);

        ResponseEntity<T> responseEntity = restTemplate.getForEntity(url, clazz);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return null;
        }
        return responseEntity.getBody();
    }



    public <T> List<T> getList(String url, Class<T> clazz) {

        String ret = get(url, String.class);

        return JSONArray.parseArray(ret, clazz);
    }


    public StockListVO getStockListVo(Stock stock) {

        StockDTO stockDTO = getStock(stock.getStockCode());

        StockListVO stockListVO = assembleStockListVO(stockDTO);

        stockListVO.setName(stock.getStockName());

        return stockListVO;
    }

    public List<StockDTO> getDailyZt(String day) {

        String url = String.format("/hslt/ztgc/%s", day);

        List<StockDTO> list = getList(url, StockDTO.class);

        return list;

    }

    public List<StockDTO> getQsgc() {


        LocalDate date = LocalDate.now();

        int count = 10;

        while (count > 0) {

            date = DateTimeUtil.getWeekDay(date);
            String url = String.format("/hslt/qsgc/%s", date.format(DateTimeFormatter.ofPattern(DateTimeUtil.YMD_FORMAT)));

            try {
                return getList(url, StockDTO.class);
            } catch (HttpServerErrorException e) {
                log.error(e.getMessage(), e);
                count--;
                date = date.minusDays(1L);
            }
        }
        return null;
    }
}
