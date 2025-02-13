package com.nq.utils.task.stock;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nq.dao.StockMapper;
import com.nq.pojo.Stock;
import com.nq.service.IStockService;
import com.nq.utils.DateTimeUtil;
import com.nq.utils.HttpClientRequest;
import com.nq.utils.PropertiesUtil;
import com.nq.utils.redis.RedisShardedPoolUtils;
import com.nq.utils.stock.BuyAndSellUtils;
import com.nq.utils.stock.pinyin.GetPyByChinese;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
//import org.springframework.scheduling.annotation.Scheduled;


@Component
public class StockTask {
    @Autowired
    IStockService stockService;
    @Autowired
    StockMapper stockMapper;

    private static final Logger log = LoggerFactory.getLogger(StockTask.class);

    public void time(boolean am, boolean pm) {
        am = false;
        pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
    }

    /*股票走勢圖定時任務-15*/
    @Scheduled(cron = "0 0/1 9-15 * * ?")
    public void z1() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("=====z1={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.z1();
            log.info("=====z1={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

    @Scheduled(cron = "0 0/1 9-15 * * ?")
    public void z11() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info(" am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====z11={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.z11();
            log.info("====z11={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

    @Scheduled(cron = "0 0/1 9-15 * * ?")
    public void z12() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("z12 am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====z12={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.z12();
            log.info("====z12={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

    @Scheduled(cron = "0 0/1 9-15 * * MON-FRI")
    public void z2() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("=====z2{} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.z2();
            log.info("=====z2{} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

    @Scheduled(cron = "0 0/1 9-15 * * MON-FRI")
    public void z21() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("z21-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("===z21=={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.z21();
            log.info("===z21=={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

    @Scheduled(cron = "0 0/1 9-15 * * MON-FRI")
    public void z22() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("z22-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====z22={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.z22();
            log.info("====z22={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

    @Scheduled(cron = "0 0/1 9-15 * * MON-FRI")
    public void z3() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====z3={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.z3();
            log.info("====z3={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

    @Scheduled(cron = "0 0/1 9-15 * * MON-FRI")
    public void z31() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("z31-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====z31={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.z31();
            log.info("===z31=={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

    @Scheduled(cron = "0 0/1 9-15 * * MON-FRI")
    public void z32() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("z32-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====z32={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.z32();
            log.info("====z32={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

    @Scheduled(cron = "0 0/1 9-15 * * MON-FRI")
    public void z4() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====z4={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.z4();
            log.info("====z4={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

    @Scheduled(cron = "0 0/1 9-15 * * MON-FRI")
    public void z41() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("z41-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====z41={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.z41();
            log.info("====z41={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

    @Scheduled(cron = "0 0/1 9-15 * * MON-FRI")
    public void z42() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("z42-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====z42={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.z42();
            log.info("===z42=={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

    //每天24点执行 同步股票数据 查漏补缺
    @Scheduled(cron = "0 0 9 * * ?")
//    @Scheduled(cron = "0/1 * * * * ?")

//    @Scheduled(cron = "0 7 0 * * ?")
    public void syncStockData() throws InterruptedException {

        /**
         * sh_a https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=1&num=80&sort=symbol&asc=1&node=sh_a&symbol=&_s_r_a=init
         * sz_a https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=1&num=80&sort=symbol&asc=1&node=sz_a&symbol=&_s_r_a=init
         * hs_bjs https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=1&num=80&sort=symbol&asc=1&node=hs_bjs&symbol=&_s_r_a=init
         * cyb https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=1&num=80&sort=symbol&asc=1&node=cyb&symbol=&_s_r_a=init
         * kcb https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=1&num=80&sort=symbol&asc=1&node=kcb&symbol=&_s_r_a=init
         * 总条数 https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeStockCount?node=hs_bjs
         */
        log.info("====={股票同步任务开启} =====", DateTimeUtil.dateToStr(new Date()));
        String[] keys = {"sh_a", "sz_a", "hs_bjs", "cyb", "kcb"};
        for (String key : keys) {
            StringBuilder sb = new StringBuilder();
            int pageNumber = 34;
            String num = null;
            try {
                num = HttpClientRequest.doGet("https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeStockCount?node=" + key);
            } catch (Exception e) {
                log.error("计算页码请求出错", e);
                Thread.sleep(30000L);
                num = HttpClientRequest.doGet("https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeStockCount?node=" + key);
            }
            if (num != null) {
                num = num.substring(1, num.length() - 1);
                int total = Integer.parseInt(num);
                if (total % 80 == 0) {
                    pageNumber = total / 80;
                } else {
                    pageNumber = total / 80 + 1;
                }
            }

            try {
                String result = null;
                try {
                    result = HttpClientRequest.doGet("https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=" + pageNumber + "&num=80&sort=symbol&asc=1&node=" + key + "&symbol=&_s_r_a=init");
                } catch (Exception e) {
                    log.error("请求失败" + e);
                    Thread.sleep(30000L);
                    result = HttpClientRequest.doGet("https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=" + pageNumber + "&num=80&sort=symbol&asc=1&node=" + key + "&symbol=&_s_r_a=init");
                }

                JSONArray hsArray = JSONObject.parseArray(result);
//                    log.info("类型"+key +"数量"+hsArray.size()+"页码"+pageNumber+"内容"+hsArray);
                for (Object o : hsArray) {
                    JSONObject hsjson = JSONObject.parseObject(o.toString());
//                    log.info("代碼"+hsjson.getString("code"));
                    Stock stock = stockMapper.findStockByCode(hsjson.getString("code"));
//                        log.info("stock = {}", stock);
                    if (stock == null) {
                        stock = new Stock();
                        String type = hsjson.getString("symbol").substring(0, 2);
                        String spell = GetPyByChinese.converterToFirstSpell(hsjson.getString("name"));
                        stock.setStockCode(hsjson.getString("code"));
                        stock.setStockName(hsjson.getString("name"));
                        stock.setStockType(type);
                        stock.setStockGid(hsjson.getString("symbol"));
                        stock.setStockSpell(spell);
                        stock.setIsLock(0);
                        stock.setIsShow(0);
                        stock.setDataBase(0);
                        stock.setAddTime(new Date());
                        stockMapper.insert1(stock);
                        sb.append("新增股票：").append(stock.getStockName()).append(stock.getStockCode()).append("/n");
                    }

                    if ("cyb".equals(key) && stock.getStockPlate() == null) {
                        stock.setStockPlate("创业");
                        stockMapper.updateById(stock);
                    } else if ("kcb".equals(key) && stock.getStockPlate() == null) {
                        stock.setStockPlate("科创");
                        stockMapper.updateById(stock);
                    }

                }

            } catch (Exception e) {
                log.error("同步出错", e);


            }
            log.info(key + "股新增股票：{}", sb.toString());
        }
        log.info("====={股票同步任务结束} =====", DateTimeUtil.dateToStr(new Date()));

    }

    @Scheduled(cron = "0 0 16 * * ?")
    public void stockTask() throws InterruptedException {
        this.syncStockData();
    }


    /**
     * 美股港股同步
     */
//    @Scheduled(cron = "0 5 0 * * ?")
    public void syncOtherStockData() {
        String[] keys = {"hk", "us"};
        long now = System.currentTimeMillis();
        String url;
        String result = null;
        com.alibaba.fastjson2.JSONObject json;
        for (String key : keys) {
            StringBuilder sb = new StringBuilder();
            if ("hk".equals(key)) {
                url = PropertiesUtil.getProperty("hk.stock.url") + now;
                result = HttpClientRequest.doGet(url);
                json = com.alibaba.fastjson2.JSONObject.parseObject(result);
            } else {
                url = PropertiesUtil.getProperty("us.stock.url") + now;
                result = HttpClientRequest.doGet(url);
                String da = result.substring(result.indexOf("(") + 1, result.indexOf(");"));
                json = com.alibaba.fastjson2.JSONObject.parseObject(da);
            }
            ArrayList diff = (ArrayList) json.getJSONObject("data").get("diff");
//            log.info("开始存库");
            for (Object o : diff) {
                com.alibaba.fastjson2.JSONObject data = (com.alibaba.fastjson2.JSONObject) o;
                String stockCode = data.getString("f12");

                Stock stock = stockMapper.selectOne(new QueryWrapper<Stock>().eq("stock_code", stockCode));
                if (stock == null) {
                    stock = new Stock();
                    String spell = GetPyByChinese.converterToFirstSpell(data.getString("f14"));
                    stock.setStockCode(data.getString("f12"));
                    stock.setStockName(data.getString("f14"));
                    stock.setStockType(key);
                    stock.setStockSpell(spell);
                    stock.setStockGid(key + data.getString("f12"));
                    stock.setIsLock(0);
                    stock.setIsShow(0);
                    stock.setAddTime(new Date());
                    stockMapper.insert(stock);
                    sb.append("新增股票：").append(stock.getStockName()).append(stock.getStockCode()).append("/n");
                }
            }
            log.info(key + "股新增股票：{}", sb.toString());
        }

    }
}