package com.nq.config;

import com.nq.dao.RealTimeMapper;
import com.nq.dao.StockFuturesMapper;
import com.nq.dao.StockIndexMapper;
import com.nq.dao.StockMapper;
import com.nq.pojo.Stock;
import com.nq.pojo.StockFutures;
import com.nq.pojo.StockIndex;
import com.nq.utils.stock.BuyAndSellUtils;
import com.nq.utils.stock.sina.SinaStockApi;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class StockPoll {
    @Resource
    StockMapper stockMapper;

    @Resource
    RealTimeMapper realTimeMapper;

    @Resource
    StockFuturesMapper stockFuturesMapper;

    @Resource
    StockIndexMapper stockIndexMapper;

    private ThreadPoolExecutor pool;

    @PostConstruct
    public void initPool() {
        this.pool = new ThreadPoolExecutor(50, 70, 20L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(240));
        this.pool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    public void test(String stockType, Integer stock_num, Integer stock_nums) {
        List<Stock> stockCodes = this.stockMapper.findStockCode(stockType, stock_num, stock_nums);
        System.out.println("stockCodes" + stockCodes.size() + "--stockCodes");
        for (Stock stock : stockCodes) {
            String stockGid = stock.getStockGid();
            String sinaStock = SinaStockApi.getSinaStock("s_" + stockGid);
            String[] arrayOfString = sinaStock.split(",");
            StockTask stockTask = new StockTask();
            stockTask.splits((Object[])arrayOfString);
            stockTask.stockGid(stockGid);

            /*//处理均价
            String details = SinaStockApi.getSinaStock(stockGid);
            String averagePrice = details.split(",")[29];
            stockTask.averagePrice(Double.parseDouble(averagePrice));*/

            stockTask.averagePrice(Double.parseDouble("0"));
            stockTask.StockPoll(this);
            stockTask.RealTimeMapper(this.realTimeMapper);

            this.pool.submit(stockTask);
        }
    }

    //0-540
    public void z1() {
        test("sz", Integer.valueOf(0), Integer.valueOf(300));
    }
    public void z11() {
        test("sz", Integer.valueOf(300), Integer.valueOf(300));
    }
    public void z12() {
        test("sz", Integer.valueOf(600), Integer.valueOf(200));
    }

    //540-540
    public void z2() {
        test("sz", Integer.valueOf(800), Integer.valueOf(300));
    }
    public void z21() {
        test("sz", Integer.valueOf(1100), Integer.valueOf(300));
    }
    public void z22() {
        test("sz", Integer.valueOf(1400), Integer.valueOf(200));
    }

    //1080 - 540
    public void z3() {
        test("sz", Integer.valueOf(1600), Integer.valueOf(300));
    }
    public void z31() {
        test("sz", Integer.valueOf(1900), Integer.valueOf(300));
    }
    public void z32() {
        test("sz", Integer.valueOf(2200), Integer.valueOf(200));
    }

    //1620 - 539
    public void z4() {
        test("sz", Integer.valueOf(2400), Integer.valueOf(200));
    }
    public void z41() {
        test("sz", Integer.valueOf(2600), Integer.valueOf(100));
    }
    public void z42() {
        test("sz", Integer.valueOf(2700), Integer.valueOf(100));
    }

    //0-484
    public void h1() {
        test("sh", Integer.valueOf(0), Integer.valueOf(300));
    }
    public void h11() {
        test("sh", Integer.valueOf(300), Integer.valueOf(300));
    }
    public void h12() {
        test("sh", Integer.valueOf(600), Integer.valueOf(100));
    }

    //484-484
    public void h2() {
        test("sh", Integer.valueOf(700), Integer.valueOf(300));
    }
    public void h21() {
        test("sh", Integer.valueOf(1000), Integer.valueOf(300));
    }
    public void h22() {
        test("sh", Integer.valueOf(1300), Integer.valueOf(100));
    }

    //968-485
    public void h3() {
        test("sh", Integer.valueOf(1400), Integer.valueOf(300));
    }
    public void h31() {
        test("sh", Integer.valueOf(1700), Integer.valueOf(300));
    }
    public void h32() {
        test("sh", Integer.valueOf(2000), Integer.valueOf(200));
    }

    //bj 0-121
    public void bj1() {
        test("bj", Integer.valueOf(0), Integer.valueOf(200));
    }

    public void qh1() {
        qhDataGrab("qh", Integer.valueOf(0), Integer.valueOf(540));
    }

    /*期货k线分时数据抓取*/
    public void qhDataGrab(String stockType, Integer stock_num, Integer stock_nums) {
        List<StockFutures> stockCodes = this.stockFuturesMapper.queryList();
        System.out.println("qh-stockCodes" + stockCodes.size() + "--stockCodes");
        boolean am = false;
        boolean pm = false;
        boolean pm2 = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:15", "12:00");
            pm = BuyAndSellUtils.isTransTime("13:00", "16:30");
            pm2 = BuyAndSellUtils.isTransTime("17:15", "23:45");
        } catch (Exception e) {
        }
        for (StockFutures stock : stockCodes) {
            String stockGid = stock.getFuturesGid();
            //恒生指数特殊处理，不在指定时间段跳过
            if("hf_HSI".equals(stockGid) && !am && !pm && !pm2){
                continue;
            }
            String sinaStock = SinaStockApi.getSinaStock(stockGid);
            if(sinaStock.length()>10) {
                String[] arrayOfString = sinaStock.split(",");
                //伦敦金格式不正确，特殊处理
                if (arrayOfString.length <= 14) {
                    sinaStock = sinaStock.replace("\"", ",1\"");
                    arrayOfString = sinaStock.split(",");
                }
                //拼接需要的字符串：1价格+3涨跌幅度+4成交量+5成交额
                double rates = 0;
                BigDecimal b1 = new BigDecimal(arrayOfString[3].toString());
                BigDecimal b2 = new BigDecimal(arrayOfString[4].toString());
                BigDecimal b3 = b1.subtract(b2);
                String s = arrayOfString[14].toString();
                int index = s.indexOf("\"");
                String substring = s.substring(0, index);
                rates = b3.multiply(new BigDecimal("100")).divide(b1, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                String qhstr = "0," + arrayOfString[0].toString() + ",0," + rates + "," + substring + "," + arrayOfString[9].split("\\.")[0].toString() + "\"";
                String[] arrayqh = qhstr.split(",");
                StockTask stockTask = new StockTask();
                stockTask.splits((Object[]) arrayqh);
                stockTask.stockGid(stockGid);
                stockTask.averagePrice(new Double("0"));
                stockTask.StockPoll(this);
                stockTask.RealTimeMapper(this.realTimeMapper);
                this.pool.submit(stockTask);
            }
        }
    }

    public void zs1() {
        zsDataGrab();
    }

    /*指数k线分时数据抓取*/
    public void zsDataGrab() {
        List<StockIndex> stockCodes = this.stockIndexMapper.queryListIndex();
        System.out.println("zs-stockCodes" + stockCodes.size() + "--stockCodes");
        for (StockIndex stock : stockCodes) {
            String stockGid = stock.getIndexGid();
            String sinaStock = SinaStockApi.getSinaStock("s_" + stockGid);
            String[] arrayOfString = sinaStock.split(",");
            StockTask stockTask = new StockTask();
            stockTask.splits((Object[])arrayOfString);
            stockTask.stockGid(stockGid);
            stockTask.StockPoll(this);
            stockTask.RealTimeMapper(this.realTimeMapper);
            this.pool.submit(stockTask);
        }
    }
}
