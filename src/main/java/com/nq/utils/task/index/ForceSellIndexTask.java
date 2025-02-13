package com.nq.utils.task.index;

import com.nq.service.IStockIndexService;
import com.nq.service.IUserService;
import com.nq.utils.DateTimeUtil;
import com.nq.utils.stock.BuyAndSellUtils;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class ForceSellIndexTask {
    private static final Logger log = LoggerFactory.getLogger(ForceSellIndexTask.class);


    @Autowired
    IUserService iUserService;
    @Autowired
    IStockIndexService iStockIndexService;


    private static final String am_begin = "9:30";

    private static final String am_end = "11:30";

    private static final String pm_begin = "13:00";

    private static final String pm_end = "15:00";


@Scheduled(cron = "0 0/3 9-15 ? * MON-FRI")
    public void banlanceUserIndexPositionTaskV1() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:30", "11:30");
            pm = BuyAndSellUtils.isTransTime("13:00", "15:00");
        } catch (Exception e) {
            log.error("執行定時任務（指數）出錯，e = {}", e);
        }

        log.info("當前 am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("=====掃描用戶（指數）持倉執行，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
            dotask();
            log.info("=====掃描用戶（指數）持倉結束，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
        } else {
            log.info("當前時間不為周一至周五，或者不在交易時間內，不執行（強平指數）定時任務");
        }
    }


    public void dotask() {
        this.iUserService.ForceSellIndexTask();
    }

    /*指數強平提醒推送消息，每分鐘檢測一次*/
@Scheduled(cron = "0 0/3 9-15 ? * MON-FRI")
    public void banlanceUserIndexPositionMessage() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:30", "11:30");
            pm = BuyAndSellUtils.isTransTime("13:00", "15:00");
        } catch (Exception e) {
            log.error("執行定時任務（指數）出錯，e = {}", e);
        }

        log.info("當前 am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("=====掃描用戶（指數）持倉執行，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
            this.iUserService.ForceSellIndexsMessageTask();
            log.info("=====掃描用戶（指數）持倉結束，當前時間 {} =====", DateTimeUtil.dateToStr(new Date()));
        } else {
            log.info("當前時間不為周一至周五，或者不在交易時間內，不執行（強平指數）定時任務");
        }
    }

    /*指數分時圖k線定時任務*/
@Scheduled(cron = "0 0/1 9-15 * * ?")
    public void zs1() {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("9:29", "11:31");
            pm = BuyAndSellUtils.isTransTime("12:59", "15:00");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("zs1-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
            this.iUserService.zs1();
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }
    //美股和港股指数 每天0点5分执行
//    @Scheduled(cron = "0 5 0 ? * MON-FRI")

    public void otherIndexTask() {
        log.info("====={美港指数同步开始} =====", DateTimeUtil.dateToStr(new Date()));
        this.iStockIndexService.otherIndexTask();
        log.info("====={美港指数同步结束} =====", DateTimeUtil.dateToStr(new Date()));
    }


}

