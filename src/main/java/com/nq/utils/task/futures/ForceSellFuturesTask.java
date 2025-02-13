package com.nq.utils.task.futures;


import com.nq.service.IUserService;
import com.nq.utils.DateTimeUtil;
import com.nq.utils.stock.BuyAndSellUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ForceSellFuturesTask {
    private static final Logger log = LoggerFactory.getLogger(ForceSellFuturesTask.class);
    @Autowired
    IUserService iUserService;

    /*指數強制平倉，每分鐘執行一次*/
//@Scheduled(cron = "0 */1 * * * ?")
    public void banlanceUserFuturesPositionTaskV1() {
        dotask();
    }

    public void dotask() {
        this.iUserService.ForceSellFuturesTask();
    }

    /*指數強平提醒推送消息，每分鐘檢測一次*/
//@Scheduled(cron = "0 */1 * * * ?")
    public void banlanceUserFuturesPositioMessage() {
        this.iUserService.ForceSellFuturesMessageTask();
    }

    /*期貨走勢圖定時任務*/
//@Scheduled(cron = "0 0/1 0-23 * * MON-FRI")
    public void qh1() throws Exception {
        boolean am = false;
        boolean pm = false;
        try {
            am = BuyAndSellUtils.isTransTime("0:01", "12:59");
            pm = BuyAndSellUtils.isTransTime("12:59", "23:59");
        } catch (Exception e) {
            log.error("= {}", e);
        }
        log.info("qh1-am = {}  pm = {}", Boolean.valueOf(am), Boolean.valueOf(pm));
        if (am || pm) {
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
            this.iUserService.qh1();
            log.info("====={} =====", DateTimeUtil.dateToStr(new Date()));
        }
    }
}
