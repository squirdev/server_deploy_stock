package com.nq.utils.task.stock;//package com.nq.utils.task.stock;


import com.nq.service.IUserPositionService;

import com.nq.utils.DateTimeUtil;

import java.util.Date;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;


@Component
public class ClosingStayTask {

    private static final Logger log = LoggerFactory.getLogger(ClosingStayTask.class);


    @Autowired
    IUserPositionService iUserPositionService;

    /*遞延費周一到周五，每天七點定時計算*/
//@Scheduled(cron = "0 0 7 * * ?")
//@Scheduled(cron = "0 15 9 ? * MON-FRI")
    public void closingStayV1() {

        log.info("=======================收盤收取留倉費任務開始 ===========================");

        log.info("收盤收取留倉費任務 開始時間 = {}", DateTimeUtil.dateToStr(new Date()));

        log.info("");

        dotask();

        log.info("");

        log.info("收盤收取留倉費任務 結束時間 = {}", DateTimeUtil.dateToStr(new Date()));

        log.info("=======================收盤收取留倉費任務結束 ===========================");

    }


    public void dotask() {
        this.iUserPositionService.doClosingStayTask();
    }

    /*留倉到期強制平倉，每天15點執行*/
@Scheduled(cron = "0 0 15 ? * MON-FRI")
    public void expireStayUnwind() {

        log.info("=======================留倉到期強制平倉任務開始 ===========================");
        log.info("留倉到期強制平倉 開始時間 = {}", DateTimeUtil.dateToStr(new Date()));

        this.iUserPositionService.expireStayUnwindTask();

        log.info("留倉到期強制平倉 結束時間 = {}", DateTimeUtil.dateToStr(new Date()));
        log.info("=======================留倉到期強制平倉任務結束 ===========================");

    }

}
