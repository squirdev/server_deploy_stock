package com.nq.utils.task.stock;

import com.nq.service.IStockService;
import com.nq.utils.DateTimeUtil;
import com.nq.utils.stock.BuyAndSellUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
//import org.springframework.scheduling.annotation.Scheduled;


//@Component
public class StockShTask {
    @Autowired
    IStockService stockService;

    private static final Logger log = LoggerFactory.getLogger(StockTask.class);

@Scheduled(cron = "0 0/1 5-15 * * ?")
    public void h1() {
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
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.h1();
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }
@Scheduled(cron = "0 0/1 5-15 * * ?")
    public void h11() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("h11-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.h11();
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }
@Scheduled(cron = "0 0/1 5-15 * * ?")
    public void h12() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("h12-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.h12();
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

@Scheduled(cron = "0 0/1 9-15 * * ?")
    public void h2() {
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
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.h2();
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }
@Scheduled(cron = "0 0/1 9-15 * * ?")
    public void h21() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("h21-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.h21();
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }
@Scheduled(cron = "0 0/1 9-15 * * ?")
    public void h22() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("h22-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.h22();
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

@Scheduled(cron = "0 0/1 9-15 * * ?")
    public void h3() {
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
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.h3();
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }
@Scheduled(cron = "0 0/1 9-15 * * ?")
    public void h31() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("h31-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.h31();
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }
@Scheduled(cron = "0 0/1 9-15 * * ?")
    public void h32() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("h32-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.h32();
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }

    /**
     * bj 实时数据 走势图
     */
    @Scheduled(cron = "0 0/1 9-15 * * ?")
    public void bj1() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("h32-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
            this.stockService.bj1();
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }
}
