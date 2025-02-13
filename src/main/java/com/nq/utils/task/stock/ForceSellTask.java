package com.nq.utils.task.stock;


import com.nq.service.IUserIndexPositionService;
import com.nq.service.IUserService;
import com.nq.service.UserPendingorderService;
import com.nq.utils.DateTimeUtil;
import com.nq.utils.stock.BuyAndSellUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class ForceSellTask {

    private static final Logger log = LoggerFactory.getLogger(ForceSellTask.class);


    @Autowired
    IUserService iUserService;
    @Autowired
    UserPendingorderService userPendingorderService;
    @Autowired
    IUserIndexPositionService iUserIndexPositionService;


    private static final String am_begin = "9:30";


    private static final String am_end = "11:30";


    private static final String pm_begin = "13:00";


    private static final String pm_end = "15:00";


    @Scheduled(cron = "0 * 9-15 ? * MON-FRI")
    public void qiangpingTipTask() {
        this.iUserService.forceSellTipTask();
    }




    /*用戶持倉單-用戶總金額-強平定時*/
//@Scheduled(cron = "0 0/3 9-15 ? * MON-FRI")
    public void banlanceUserPositionTaskV1() {

        boolean am = false;

        boolean pm = false;

        try {

            am = BuyAndSellUtils.isTransTime("9:30", "11:30");

            pm = BuyAndSellUtils.isTransTime("13:00", "15:00");

        } catch (Exception e) {

            log.error("執行定時任務出錯，e = {}", e);

        }


        log.info("當前 am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));

        if (am || pm) {

            log.info("=====掃描用戶持倉執行，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));

            dotask();

            log.info("=====掃描用戶持倉結束，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));

        } else {

            log.info("當前時間不為周一至周五，或者不在交易時間內，不執行（強平）定時任務");

        }

    }


    public void dotask() {
        this.iUserService.ForceSellTask();
    }

    /*用戶持倉單-單支股票盈虧-強平定時*/
//    @Scheduled(cron = "0 0/1 * * * *")
//    @Scheduled(cron = "0 0/1 9-15 ? * MON-FRI")
    public void stockProfitLossOneTask() {
        boolean am = false;
        boolean pm = false;
        //todo 測試完成需要關閉註釋
        try {
            am = BuyAndSellUtils.isTransTime("9:30", "11:30");
            pm = BuyAndSellUtils.isTransTime("13:00", "15:00");
        } catch (Exception e) {
            log.error("執行定時任務出錯，e = {}", e);
        }

        log.info("當前 am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("=====掃描單支股票盈虧執行，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
            this.iUserService.ForceSellOneStockTask();
            log.info("=====掃描單支股票盈虧結束，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));

        } else {
            log.info("當前時間不為周一至周五，或者不在交易時間內，不執行（強平）單支股票盈虧定時任務");
        }

    }

    /**
     * 用户持仓单-单支股票止损止盈线-强平定时
     */
//    @Scheduled(cron = "0 0/1 * ? * MON-FRI")
    public void stockProfitLossOneTaskV2() {
//        boolean am = false;
//        boolean pm = false;
//        //todo 測試完成需要關閉註釋
//        try {
//            am = BuyAndSellUtils.isTransTime("9:30", "11:30");
//            pm = BuyAndSellUtils.isTransTime("13:00", "15:00");
//        } catch (Exception e) {
//            log.error("執行定時任務出錯，e = {}", e);
//        }
//
//        log.info("當前 am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
//        if (am || pm) {
            log.info("=====掃描單支股票盈虧執行，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
            this.iUserService.ForceSellOneStockTaskV2();
            log.info("=====掃描單支股票盈虧結束，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));

//        } else {
//            log.info("當前時間不為周一至周五，或者不在交易時間內，不執行（強平）單支股票盈虧定時任務");
        }


    /**
     * 用户指数持仓单-指数止损止盈线-强平定时
     */
//    @Scheduled(cron = "0 0/1 * ? * MON-FRI")
    public void indexPosition() {
//        boolean am = false;
//        boolean pm = false;
//        //todo 測試完成需要關閉註釋
//        try {
//            am = BuyAndSellUtils.isTransTime("9:30", "11:30");
//            pm = BuyAndSellUtils.isTransTime("13:00", "15:00");
//        } catch (Exception e) {
//            log.error("執行定時任務出錯，e = {}", e);
//        }
//
//        log.info("當前 am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
//        if (am || pm) {
            log.info("=====掃描單支指数盈虧執行，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
            this.iUserIndexPositionService.indexPositiontask();
            log.info("=====掃描單支指数盈虧結束，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));

//        } else {
//            log.info("當前時間不為周一至周五，或者不在交易時間內，不執行（強平）指数止盈止损定時任務");
//        }

    }
    /**
     * 挂单定时任务
     *
     */
//    @Scheduled(cron = "0 0/1 * ? * MON-FRI")
    public void orderTask() {
        boolean am = false;
        boolean pm = false;
        boolean ln = false;
        //todo 測試完成需要關閉註釋
        try {
            am = BuyAndSellUtils.isTransTime("9:30", "16:00");
            pm = BuyAndSellUtils.isTransTime("21:30", "23:59");
            ln= BuyAndSellUtils.isTransTime("0:00", "5:00");
        } catch (Exception e) {
            log.error("執行定時任務出錯，e = {}", e);
        }

        log.info("挂单当前 am = {}  pm = {} ln = {}", Boolean.valueOf(am), Boolean.valueOf(pm),Boolean.valueOf(ln));
        if (am || pm||ln) {
            log.info("=====掃描挂單執行，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
            this.userPendingorderService.orderTask();
            log.info("=====掃描挂單結束，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
        } else {
            log.info("當前時間不為周一至周五，或者不在交易時間內，不執行<< 挂单 >>定時任務");
        }

    }

    /*用戶股票持倉單-強平提醒推送消息定時*/
//    @Scheduled(cron = "0 0/1 * * * *")
    public void banlanceUserPositionMessage() {

        boolean am = false;

        boolean pm = false;

        try {

            am = BuyAndSellUtils.isTransTime("9:30", "11:30");

            pm = BuyAndSellUtils.isTransTime("13:00", "15:00");

        } catch (Exception e) {

            log.error("執行定時任務出錯，e = {}", e);

        }


        log.info("當前 am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));

        if (am || pm) {

            log.info("=====掃描用戶持倉執行，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));

            this.iUserService.ForceSellMessageTask();

            log.info("=====掃描用戶持倉結束，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));

        } else {

            log.info("當前時間不為周一至周五，或者不在交易時間內，不執行（強平）提醒推送消息任務");

        }

    }


}
