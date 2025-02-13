package com.nq.utils.stock.sina;


import com.alibaba.fastjson2.JSONObject;
import com.nq.common.ServerResponse;
import com.nq.pojo.Stock;
import com.nq.pojo.StockFutures;
import com.nq.pojo.StockIndex;
import com.nq.utils.DateTimeUtil;
import com.nq.utils.HttpClientRequest;
import com.nq.utils.PropertiesUtil;
import com.nq.utils.redis.JsonUtil;
import com.nq.utils.stock.sina.vo.SinaStockMinData;
import com.nq.vo.stock.StockListVO;
import com.nq.vo.stock.StockUsVO;
import com.nq.vo.stock.StockVO;
import com.nq.vo.stock.k.MinDataVO;
import com.nq.vo.stock.k.echarts.EchartsDataVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SinaStockApi {
    public static final String sina_url = PropertiesUtil.getProperty("sina.single.stock.url");
    private static final Logger log = LoggerFactory.getLogger(SinaStockApi.class);

    public static String getSinaStock(String stockGid) {
        String sina_result = "";
        try {
            sina_result = HttpClientRequest.doGet(sina_url + stockGid);
        } catch (Exception e) {
            log.error("获取股票行情出错，错误信息 = {}", e);
        }
        return sina_result.substring(sina_result.indexOf("=") + 2);
    }


    public static StockListVO assembleStockListVO(String sinaResult) {
        StockListVO stockListVO = new StockListVO();

        String[] hqarr = sinaResult.split(",");

        if (hqarr.length > 1) {

            stockListVO.setName(hqarr[0]);

            stockListVO.setNowPrice(hqarr[3]);

            BigDecimal chang_rate = new BigDecimal("0");
            if ((new BigDecimal(hqarr[2])).compareTo(new BigDecimal("0")) != 0 && new BigDecimal(hqarr[3]).compareTo(new BigDecimal("0")) != 0) {

                chang_rate = (new BigDecimal(hqarr[3])).subtract(new BigDecimal(hqarr[2]));

                chang_rate = chang_rate.multiply(new BigDecimal("100")).divide(new BigDecimal(hqarr[2]), 2, RoundingMode.HALF_UP);
            }
            stockListVO.setHcrate(chang_rate);

            stockListVO.setToday_max(hqarr[4]);

            stockListVO.setToday_min(hqarr[5]);

            stockListVO.setBusiness_amount(hqarr[8]);

            stockListVO.setBusiness_balance(hqarr[9]);

            stockListVO.setPreclose_px(hqarr[2]);

            stockListVO.setOpen_px(hqarr[1]);
        }

        return stockListVO;
    }
    //hk us 数据转换
    public static StockListVO otherStockListVO(String Result) {
        StockListVO stockListVO = new StockListVO();
        JSONObject jsonObject = JSONObject.parseObject(Result);


            stockListVO.setName(jsonObject.getString("f14"));
//            log.info(!Objects.equals(jsonObject.getString("f2"), "-") ?jsonObject.getString("f2"):"0");
            stockListVO.setNowPrice(!Objects.equals(jsonObject.getString("f2"), "-") ?jsonObject.getString("f2"):"0");


            stockListVO.setHcrate(new BigDecimal(!Objects.equals(jsonObject.getString("f3"), "-") ? jsonObject.getString("f3"):"0"));

            stockListVO.setToday_max(!Objects.equals(jsonObject.getString("f15"), "-") ? jsonObject.getString("f15") : "0");

            stockListVO.setToday_min(!Objects.equals(jsonObject.getString("f16"), "-") ? jsonObject.getString("f16") : "0");

            stockListVO.setBusiness_amount(!Objects.equals(jsonObject.getString("f6"), "-") ? jsonObject.getString("f6") : "0");

            stockListVO.setBusiness_balance(!Objects.equals(jsonObject.getString("f5"), "-") ? jsonObject.getString("f5") : "0");

            stockListVO.setPreclose_px(!Objects.equals(jsonObject.getString("f18"), "-") ? jsonObject.getString("f18") : "0");

            stockListVO.setOpen_px(!Objects.equals(jsonObject.getString("f17"), "-") ? jsonObject.getString("f17") : "0");
            stockListVO.setType(jsonObject.getString("f13"));

        return stockListVO;
    }

    public static StockVO assembleStockVO(String sinaResult) {
        StockVO stockVO = new StockVO();

        String[] hqarr = sinaResult.split(",");

        stockVO.setName(hqarr[0]);

        stockVO.setNowPrice(hqarr[3]);

        BigDecimal chang_rate = new BigDecimal("0");
        if ((new BigDecimal(hqarr[2])).compareTo(new BigDecimal("0")) != 0 && new BigDecimal(hqarr[3]).compareTo(new BigDecimal("0")) != 0) {

            chang_rate = (new BigDecimal(hqarr[3])).subtract(new BigDecimal(hqarr[2]));

            chang_rate = chang_rate.multiply(new BigDecimal("100")).divide(new BigDecimal(hqarr[2]), 2, RoundingMode.HALF_UP);
        }
        stockVO.setHcrate(chang_rate);

        stockVO.setToday_max(hqarr[4]);

        stockVO.setToday_min(hqarr[5]);

        stockVO.setBusiness_amount(hqarr[8]);

        stockVO.setBusiness_balance(hqarr[9]);

        stockVO.setPreclose_px(hqarr[2]);

        stockVO.setOpen_px(hqarr[1]);

        stockVO.setBuy1(hqarr[6]);
        stockVO.setBuy2(hqarr[13]);
        stockVO.setBuy3(hqarr[15]);
        stockVO.setBuy4(hqarr[17]);
        stockVO.setBuy5(hqarr[19]);

        stockVO.setSell1(hqarr[7]);
        stockVO.setSell2(hqarr[23]);
        stockVO.setSell3(hqarr[25]);
        stockVO.setSell4(hqarr[27]);
        stockVO.setSell5(hqarr[29]);

        stockVO.setBuy1_num(hqarr[10]);
        stockVO.setBuy2_num(hqarr[12]);
        stockVO.setBuy3_num(hqarr[14]);
        stockVO.setBuy4_num(hqarr[16]);
        stockVO.setBuy5_num(hqarr[18]);

        stockVO.setSell1_num(hqarr[20]);
        stockVO.setSell2_num(hqarr[22]);
        stockVO.setSell3_num(hqarr[24]);
        stockVO.setSell4_num(hqarr[26]);
        stockVO.setSell5_num(hqarr[28]);

        return stockVO;
    }
    //hk  数据转换
    public static StockVO otherStockVO(String Result) {
        StockVO stockVO = new StockVO();
        //判断是否是
        JSONObject jsonObject = JSONObject.parseObject(Result);
        stockVO.setName(jsonObject.getString("f14"));

        stockVO.setNowPrice(!Objects.equals(jsonObject.getString("f2"), "-") ? jsonObject.getString("f2") : "0");

        stockVO.setHcrate(new BigDecimal(!Objects.equals(jsonObject.getString("f3"), "-") ? jsonObject.getString("f3"): "0"));

        stockVO.setToday_max(!Objects.equals(jsonObject.getString("f15"), "-") ? jsonObject.getString("f15") : "0");

        stockVO.setToday_min(!Objects.equals(jsonObject.getString("f16"), "-") ? jsonObject.getString("f16") : "0");

        stockVO.setBusiness_amount(!Objects.equals(jsonObject.getString("f6"), "-") ? jsonObject.getString("f6") : "0");

        stockVO.setBusiness_balance(!Objects.equals(jsonObject.getString("f5"), "-") ? jsonObject.getString("f5") : "0");

        stockVO.setPreclose_px(!Objects.equals(jsonObject.getString("f18"), "-") ? jsonObject.getString("f18") : "0");

        stockVO.setOpen_px(!Objects.equals(jsonObject.getString("f17"), "-") ? jsonObject.getString("f17") : "0");
        stockVO.setType(jsonObject.getString("f13"));
        String buy = "0";
        stockVO.setBuy1(buy);
        stockVO.setBuy2(buy);
        stockVO.setBuy3(buy);
        stockVO.setBuy4(buy);
        stockVO.setBuy5(buy);

        stockVO.setSell1(buy);
        stockVO.setSell2(buy);
        stockVO.setSell3(buy);
        stockVO.setSell4(buy);
        stockVO.setSell5(buy);

        stockVO.setBuy1_num(buy);
        stockVO.setBuy2_num(buy);
        stockVO.setBuy3_num(buy);
        stockVO.setBuy4_num(buy);
        stockVO.setBuy5_num(buy);

        stockVO.setSell1_num(buy);
        stockVO.setSell2_num(buy);
        stockVO.setSell3_num(buy);
        stockVO.setSell4_num(buy);
        stockVO.setSell5_num(buy);

        return stockVO;
    }
    //us  数据转换
    public static StockUsVO otherUsStockVO(String Result) {
        StockUsVO stockVO = new StockUsVO();
        //判断是否是
        JSONObject jsonObject = JSONObject.parseObject(Result);
        stockVO.setName(jsonObject.getString("f14"));

        stockVO.setNowPrice(jsonObject.getString("f2") != "-" ? jsonObject.getString("f2") : "0");

        stockVO.setHcrate(new BigDecimal(!Objects.equals(jsonObject.getString("f3"), "-") ? jsonObject.getString("f3"): "0"));

        stockVO.setToday_max(jsonObject.getString("f15") != "-" ? jsonObject.getString("f15") : "0");

        stockVO.setToday_min(jsonObject.getString("f16") != "-" ? jsonObject.getString("f16") : "0");

        stockVO.setBusiness_amount(jsonObject.getString("f6") != "-" ? jsonObject.getString("f6") : "0");

        stockVO.setBusiness_balance(jsonObject.getString("f5") != "-" ? jsonObject.getString("f5") : "0");

        stockVO.setPreclose_px(jsonObject.getString("f18") != "-" ? jsonObject.getString("f18") : "0");

        stockVO.setOpen_px(jsonObject.getString("f17") != "-" ? jsonObject.getString("f17") : "0");
        stockVO.setType(jsonObject.getString("f13") != "-" ? jsonObject.getString("f13") : "0");
        return stockVO;
    }

    /*期货详情转换*/
    public static StockVO assembleStockFuturesVO(String sinaResult) {
        StockVO stockVO = new StockVO();

        String[] hqarr = sinaResult.split(",");
        //伦敦金格式不正确，特殊处理
        if(hqarr.length<=14){
            String sinaResulttemp = sinaResult.replace("\"",",1\"");
            hqarr = sinaResulttemp.split(",");
        }
        stockVO.setName(hqarr[13]);

        stockVO.setNowPrice(hqarr[0]);

        BigDecimal rates = new BigDecimal("0");
        BigDecimal b1 = new BigDecimal(hqarr[3].toString());
        BigDecimal b2 = new BigDecimal(hqarr[2].toString());
        BigDecimal b3 = b1.subtract(b2);
        String s = hqarr[14].toString();
        int index = s.indexOf("\"");
        String substring = s.substring(0, index);
        rates = b3.multiply(new BigDecimal("100")).divide(b1,2,BigDecimal.ROUND_HALF_UP);
        stockVO.setHcrate(rates);

        stockVO.setToday_max(hqarr[7]);

        stockVO.setToday_min(hqarr[8]);

        stockVO.setBusiness_amount(substring);

        stockVO.setBusiness_balance(hqarr[9]);

        stockVO.setPreclose_px(hqarr[11]);

        stockVO.setOpen_px(hqarr[10]);

        stockVO.setBuy1(hqarr[2]);
        stockVO.setBuy2("0");
        stockVO.setBuy3("0");
        stockVO.setBuy4("0");
        stockVO.setBuy5("0");

        stockVO.setSell1(hqarr[3]);
        stockVO.setSell2("0");
        stockVO.setSell3("0");
        stockVO.setSell4("0");
        stockVO.setSell5("0");

        stockVO.setBuy1_num(hqarr[10]);
        stockVO.setBuy2_num("0");
        stockVO.setBuy3_num("0");
        stockVO.setBuy4_num("0");
        stockVO.setBuy5_num("0");

        stockVO.setSell1_num(hqarr[11]);
        stockVO.setSell2_num("0");
        stockVO.setSell3_num("0");
        stockVO.setSell4_num("0");
        stockVO.setSell5_num("0");

        return stockVO;
    }


    public static ServerResponse<MinDataVO> getStockMinK(StockIndex stock, int time, int ma, int size) {
        int maxSize = Integer.parseInt(PropertiesUtil.getProperty("sina.k.min.max.size"));
        if (size > maxSize) {
            size = maxSize;
        }

        String minUrl = PropertiesUtil.getProperty("sina.k.min.url");
        minUrl = minUrl + "?symbol=" + stock.getIndexGid() + "&scale=" + time + "&ma=" + ma + "&datalen=" + size;

        String hqstr = "";
        try {
            hqstr = HttpClientRequest.doGet(minUrl);
        } catch (Exception e) {
            log.error("获取股票K线分时图出错，错误信息 = {}", e);
        }

        log.info(" time = {} ma = {} size = {}", new Object[]{Integer.valueOf(time), Integer.valueOf(ma), Integer.valueOf(size)});


        hqstr = hqstr.replace("day", "\"day\"").replace("open", "\"open\"").replace("high", "\"high\"").replace("low", "\"low\"").replace("close", "\"close\"");

        if (ma == 5) {

            hqstr = hqstr.replace("ma_volume5", "\"ma_volume\"").replace(",volume", ",\"volume\"").replace("ma_price5", "\"ma_price\"");
        } else if (ma == 10) {


            hqstr = hqstr.replace("ma_volume10", "\"ma_volume\"").replace(",volume", ",\"volume\"").replace("ma_price10", "\"ma_price\"");
        } else if (ma == 15) {

            hqstr = hqstr.replace("ma_volume15", "\"ma_volume\"").replace(",volume", ",\"volume\"").replace("ma_price15", "\"ma_price\"");
        } else {
            return ServerResponse.createByErrorMsg("ma 取值 5，10，15");
        }


        if (StringUtils.isBlank(hqstr)) {
            return ServerResponse.createByErrorMsg("没有查询到行情数据");
        }

        MinDataVO minDataVO = new MinDataVO();
        minDataVO.setStockName(stock.getIndexName());
        minDataVO.setStockCode(stock.getIndexCode());
        minDataVO.setGid(stock.getIndexGid());

        hqstr = hqstr.replaceAll("\"\"", "\"");

        List<SinaStockMinData> list = (List<SinaStockMinData>)JsonUtil.string2Obj(hqstr, new TypeReference<List<SinaStockMinData>>(){});
        log.info("需要查询的行情size为： {}", Integer.valueOf(size));

        minDataVO.setData(list);

        return ServerResponse.createBySuccess(minDataVO);
    }

    /*期货分时-k线
     * stock：期货代码
     * time：5、15、30、60，单位分钟
     * */
    public static ServerResponse<MinDataVO> getFuturesMinK(StockFutures stock, int time, int size) {
        String minUrl = PropertiesUtil.getProperty("sina.futures.k.min.url").replace("{code}",stock.getFuturesCode()).replace("{time}",String.valueOf(time));
        String stamp = String.valueOf(new Date().getTime());// new Date()为获取当前系统时间
        minUrl = minUrl.replace("{stamp}",stamp);

        String hqstr = "";
        try {
            hqstr = HttpClientRequest.doGet(minUrl);
        } catch (Exception e) {
            log.error("获取股票K线分时图出错，错误信息 = {}", e);
        }

        log.info("期货分时 - time = {} ", time);

        hqstr = hqstr.split("\\[")[1].replace("]);","");
        hqstr = hqstr.replace("d","\"day\"");
        hqstr = hqstr.replace("o","\"open\"");
        hqstr = hqstr.replace("h","\"high\"");
        hqstr = hqstr.replace("l","\"low\"");
        hqstr = hqstr.replace("c","\"close\"");
        hqstr = hqstr.replace("v","\"volume\"");

        if (StringUtils.isBlank(hqstr)) {
            return ServerResponse.createByErrorMsg("没有查询到行情数据");
        }

        MinDataVO minDataVO = new MinDataVO();
        minDataVO.setStockName(stock.getFuturesName());
        minDataVO.setStockCode(stock.getFuturesCode());
        minDataVO.setGid(stock.getFuturesGid());

        hqstr = hqstr.replaceAll("\"\"", "\"");
        hqstr = "[" + hqstr + "]";

        List<SinaStockMinData> list = (List<SinaStockMinData>)JsonUtil.string2Obj(hqstr, new TypeReference<List<SinaStockMinData>>(){});
        //int size = Integer.valueOf(PropertiesUtil.getProperty("sina.futures.k.min.max.size"));
        if(list.size()>size){
            list = list.subList((list.size()-size-1),list.size());
        }
        minDataVO.setData(list);
        return ServerResponse.createBySuccess(minDataVO);
    }

    public static EchartsDataVO assembleEchartsDataVO(MinDataVO minDataVO) {
        EchartsDataVO echartsDataVO = new EchartsDataVO();
        echartsDataVO.setStockName(minDataVO.getStockName());
        echartsDataVO.setStockCode(minDataVO.getStockCode());

        List<SinaStockMinData> minDataList = minDataVO.getData();


        double[][] values = (double[][]) null;
        Object[][] volumes = (Object[][]) null;
        String[] date = null;

        if (minDataList.size() > 0) {

            values = new double[minDataList.size()][6];

            volumes = new Object[minDataList.size()][3];

            date = new String[minDataList.size()];

            for (int i = 0; i < minDataList.size(); i++) {
                SinaStockMinData sinaStockMinData = (SinaStockMinData) minDataList.get(i);

                for (int j = 0; j < values[i].length; j++) {
                    String time = DateTimeUtil.dateToStamp(sinaStockMinData.getDay());
                    values[i][0] = Double.valueOf(sinaStockMinData.getOpen()).doubleValue();
                    values[i][1] = Double.valueOf(sinaStockMinData.getClose()).doubleValue();
                    values[i][2] = Double.valueOf(sinaStockMinData.getLow()).doubleValue();
                    values[i][3] = Double.valueOf(sinaStockMinData.getHigh()).doubleValue();
                    values[i][4] = Double.valueOf(sinaStockMinData.getVolume()).doubleValue();
                    values[i][5] = Double.valueOf(time).doubleValue();
                }
                for (int k = 0; k < volumes[i].length; k++) {
                    volumes[i][0] = Integer.valueOf(i);
                    volumes[i][1] = Double.valueOf(sinaStockMinData.getVolume());

                    if ((new BigDecimal(sinaStockMinData.getClose()))
                            .compareTo(new BigDecimal(sinaStockMinData.getOpen())) == 1) {
                        volumes[i][2] = Integer.valueOf(1);
                    } else {
                        volumes[i][2] = Integer.valueOf(-1);
                    }
                }

                date[i] = sinaStockMinData.getDay();
            }
        }

        echartsDataVO.setValues(values);
        echartsDataVO.setVolumes(volumes);
        echartsDataVO.setDate(date);

        return echartsDataVO;
    }

    /*股票日线*/
    public static ServerResponse<MinDataVO> getStockDayK(Stock stock, int time, int ma, int size) {
        int maxSize = Integer.parseInt(PropertiesUtil.getProperty("sina.k.min.max.size"));
        if (size > maxSize) {
            size = maxSize;
        }

        String minUrl = PropertiesUtil.getProperty("sina.k.min.url");
        minUrl = minUrl + "?symbol=" + stock.getStockGid() + "&scale=" + time + "&ma=" + ma + "&datalen=" + size;

        String hqstr = "";
        try {
            hqstr = HttpClientRequest.doGet(minUrl);
        } catch (Exception e) {
            log.error("获取股票K线分时图出错，错误信息 = {}", e);
        }

        log.info(" time = {} ma = {} size = {}", new Object[]{Integer.valueOf(time), Integer.valueOf(ma), Integer.valueOf(size)});


        hqstr = hqstr.replace("day", "\"day\"").replace("open", "\"open\"").replace("high", "\"high\"").replace("low", "\"low\"").replace("close", "\"close\"");

        if (ma == 5) {

            hqstr = hqstr.replace("ma_volume5", "\"ma_volume\"").replace(",volume", ",\"volume\"").replace("ma_price5", "\"ma_price\"");
        } else if (ma == 10) {


            hqstr = hqstr.replace("ma_volume10", "\"ma_volume\"").replace(",volume", ",\"volume\"").replace("ma_price10", "\"ma_price\"");
        } else if (ma == 15) {

            hqstr = hqstr.replace("ma_volume15", "\"ma_volume\"").replace(",volume", ",\"volume\"").replace("ma_price15", "\"ma_price\"");
        } else {
            return ServerResponse.createByErrorMsg("ma 取值 5，10，15");
        }


        if (StringUtils.isBlank(hqstr)) {
            return ServerResponse.createByErrorMsg("没有查询到行情数据");
        }

        MinDataVO minDataVO = new MinDataVO();
        minDataVO.setStockName(stock.getStockName());
        minDataVO.setStockCode(stock.getStockCode());
        minDataVO.setGid(stock.getStockGid());

        hqstr = hqstr.replaceAll("\"\"", "\"");

        List<SinaStockMinData> list = (List<SinaStockMinData>)JsonUtil.string2Obj(hqstr, new TypeReference<List<SinaStockMinData>>(){});
        log.info("需要查询的行情size为： {}", Integer.valueOf(size));

        minDataVO.setData(list);

        return ServerResponse.createBySuccess(minDataVO);
    }

    /*指数分时-k线
     * stock：指数代码
     * time：5、15、30、60，单位分钟
     * */
    public static ServerResponse<MinDataVO> getIndexMinK(StockIndex stock, int time, int size) {
        String minUrl = PropertiesUtil.getProperty("sina.index.k.min.url").replace("{code}",stock.getIndexGid()).replace("{time}",String.valueOf(time));
        String stamp = String.valueOf(new Date().getTime());// new Date()为获取当前系统时间
        minUrl = minUrl.replace("{stamp}",stamp);

        String hqstr = "";
        try {
            hqstr = HttpClientRequest.doGet(minUrl);
        } catch (Exception e) {
            log.error("获取股票K线分时图出错，错误信息 = {}", e);
        }

        log.info("期货分时 - time = {} ", time);

        hqstr = hqstr.split("\\[")[1].replace("]);","");
        /*hqstr = hqstr.replace("d","\"day\"");
        hqstr = hqstr.replace("o","\"open\"");
        hqstr = hqstr.replace("h","\"high\"");
        hqstr = hqstr.replace("l","\"low\"");
        hqstr = hqstr.replace("c","\"close\"");
        hqstr = hqstr.replace("v","\"volume\"");*/

        if (StringUtils.isBlank(hqstr)) {
            return ServerResponse.createByErrorMsg("没有查询到行情数据");
        }

        MinDataVO minDataVO = new MinDataVO();
        minDataVO.setStockName(stock.getIndexName());
        minDataVO.setStockCode(stock.getIndexCode());
        minDataVO.setGid(stock.getIndexGid());

        hqstr = hqstr.replaceAll("\"\"", "\"");
        hqstr = "[" + hqstr + "]";

        List<SinaStockMinData> list = (List<SinaStockMinData>)JsonUtil.string2Obj(hqstr, new TypeReference<List<SinaStockMinData>>(){});
        //int size = Integer.valueOf(PropertiesUtil.getProperty("sina.index.k.min.max.size"));
        if(list.size()>size){
            list = list.subList((list.size()-size-1),list.size());
        }
        minDataVO.setData(list);
        return ServerResponse.createBySuccess(minDataVO);
    }


    public static void main(String[] args) {
//        List list = Lists.newArrayList();
//        list.add(Integer.valueOf(1));
//        list.add(Integer.valueOf(2));
//        list.add(Integer.valueOf(3));
//        System.out.println(list.size());
//
//        String[][] values = new String[list.size()][5];
//
//        System.out.println("[]" + values.length);
//        System.out.println("[][]" + values[1].length);
//
//        System.out.println(getSinaStock("sh601318"));
//
//        String sss = "[\n{\n\"day\": \"2019-03-05 14:50:00\",\n\"open\": \"13.020\",\n\"high\": \"13.040\",\n\"low\": \"13.000\",\n\"close\": \"13.040\",\n\"volume\": \"2611513\",\n\"ma_price5\": 13.01,\n\"ma_volume5\": 3216535\n},\n{\n\"day\": \"2019-03-05 14:55:00\",\n\"open\": \"13.040\",\n\"high\": \"13.040\",\n\"low\": \"13.010\",\n\"close\": \"13.030\",\n\"volume\": \"2296000\",\n\"ma_price5\": 13.016,\n\"ma_volume5\": 3044839\n}\n]";
//
//        sss = sss.substring(1, sss.length() - 1);
//
//        sss = "{" + sss + "}";
        getSinaStock("s_sh600090");
    }
}

