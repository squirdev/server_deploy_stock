package com.nq.utils.task.stock;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nq.dao.StockMapper;
import com.nq.pojo.Stock;
import com.nq.service.IStockService;
import com.nq.utils.HttpClientRequest;
import com.nq.utils.PropertiesUtil;
import com.nq.utils.redis.RedisShardedPoolUtils;
import com.nq.utils.stock.BuyAndSellUtils;
import com.nq.utils.stock.pinyin.GetPyByChinese;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;

import static com.nq.utils.DateTimeUtil.*;


@Component
public class StockOthersTask {
    @Autowired
    IStockService stockService;
    @Autowired
    StockMapper stockMapper;
    private static final Logger log = LoggerFactory.getLogger(StockOthersTask.class);

    //9点到15点每隔10s执行一次
//    @Scheduled(cron = "0 0/1 9-15 * * MON-FRI")
    //redis存股票信息
//    @Scheduled(cron = "0/30 0/1 9-16 * * MON-FRI")
    public void HkStockTask() throws UnsupportedEncodingException {

        //当前毫秒时间戳
        long now = System.currentTimeMillis();
        ArrayList diff = null;

            String url = PropertiesUtil.getProperty("hk.stock.url") + now;
            String result = null;
        try {
             result = HttpClientRequest.doGet(url);
        } catch (Exception e) {
            log.error("获取港股信息失败，重新获取", e);
            result = HttpClientRequest.doGet(url);
        }
        if (result == null) {
            log.error("获取港股信息失败");
            return;
        }
            JSONObject json = JSONObject.parseObject(result);
            //取出data里的diff数组
            diff = (ArrayList) json.getJSONObject("data").get("diff");


        for (Object o : diff) {
            //取第一条数据存入redisDB1
            JSONObject data = (JSONObject) o;
            String stockCode = data.getString("f12");
            //存入redis
            RedisShardedPoolUtils.set("hk" + stockCode, String.valueOf(data), 1);
        }
        log.info("港股信息存入redis成功");

    }

    /**
     * 美股存redis 22:00-05:00
     */
//    @Scheduled(cron = "0/20 0/1 22-23 * * ?")
//    @Scheduled(cron = "0/25 0/1 * * * ?")

    public void UsStockTask() {
    boolean am = false;
    boolean pm = false;
    try {
        am = BuyAndSellUtils.isTransTime("21:00", "0:00");
        pm = BuyAndSellUtils.isTransTime("0:00", "5:00");
    } catch (Exception e) {
        log.error("= {}", e);
    }
//        log.info("美股UsStock-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
    if (am || pm) {
        //当前毫秒时间戳
       this.usStockTask2();
    }
    }

    /**
     * 美股信息启动加载
     *
     */
    public void usStockTask2(){
        long now = System.currentTimeMillis();
        ArrayList diff = null;
        String result = null;
        String url = PropertiesUtil.getProperty("us.stock.url") + now;
        try {
            result = HttpClientRequest.doGet(url);
        } catch (Exception e) {
            log.error("获取美股信息失败,尝试重新获取", e);
            result = HttpClientRequest.doGet(url);
        }
        if (result == null) {
            log.error("获取美股信息失败");
            return;
        }
        //取第一个（后到最后的数据
        String da = null;
        try {
            da = result.substring(result.indexOf("(") + 1, result.indexOf(");"));
        } catch (Exception e) {
            log.error("此次美股信息获取失败", e);
            return;
        }
        JSONObject json = JSONObject.parseObject(da);
        //取出data里的diff数组
        diff = (ArrayList) json.getJSONObject("data").get("diff");

        for (Object o : diff) {
            //取第一条数据存入redisDB1
            JSONObject data = (JSONObject) o;
            String stockCode = data.getString("f12");
            //存入redis

            RedisShardedPoolUtils.set("us" + stockCode, String.valueOf(data), 2);
        }
        log.info("美股信息存入redis成功");
    }

    /**
     * 美股港股指数
     */
//    @Scheduled(cron = "0/30 0/1 * * * ?")
    public void HkIndexTask() throws UnsupportedEncodingException {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:00", "0:00");
            pm = BuyAndSellUtils.isTransTime("0:00", "5:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
//        log.info("HkIndex-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            String[] keys = {"hk", "us"};
            //当前毫秒时间戳
            long now = System.currentTimeMillis();
            for (String key : keys) {
                String url = null;
                if ("hk".equals(key)) {
                    url = PropertiesUtil.getProperty("hk.index.url") + now;
                } else {
                    url = PropertiesUtil.getProperty("us.index.url") + now;
                }

                String result = null;
                try {
                    result = HttpClientRequest.doGet(url);
                } catch (Exception e) {
                    log.error("获取指数信息失败,尝试重新获取", e);
                    result = HttpClientRequest.doGet(url);
                }

                JSONObject json = JSONObject.parseObject(result);

                //取出data里的diff数组
                ArrayList diff = null;
                try {
                    diff = (ArrayList) json.getJSONObject("data").get("diff");
                } catch (Exception e) {
                    log.error("获取指数信息失败", e);
                    return;
                }

                for (int i = 0; i < diff.size(); i++) {
                    JSONObject data = (JSONObject) diff.get(i);
                    String stockCode = data.getString("f12");
                    //存入redis
                    RedisShardedPoolUtils.set(key + stockCode, String.valueOf(data), 3);
                }
            }

            log.info("美股港股指数信息存入redis成功");
        }
    }

    /**
     * @Description:   股票数据启动后初始化
     * @param
     * @throws Exception
     */
//    @PostConstruct
//    public void init() {
//        try {
//            this.HkStockTask();
//        } catch (UnsupportedEncodingException e) {
//            log.error("初始化港股数据加载失败"+e);
//        }
//        this.usStockTask2();
//        try {
//            this.HkIndexTask();
//        } catch (UnsupportedEncodingException e) {
//            log.error("初始化美港指数数据加载失败"+e);
//        }
//        log.info("=======================数据加载完毕============================");
//    }
    @Test
    public void test() throws UnsupportedEncodingException {
        //当前系统分钟时间戳

        String date = getCurrentTimeMiaoZero();
        //?cat_time=1669089660&stock_gid=sz000001&price=10
        log.info("date = {}", date);
        String result = HttpClientRequest.doGet("http://116.124.132.181/stock/base/cx_gp?cat_time="+date+"&stock_gid=sz000001&price=10");
        log.info("result = {}", result);
//        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(result);
//        String nowPrice = jsonObject.getJSONObject("data").getString("new_price");
        System.out.println(result);
    }
    }
