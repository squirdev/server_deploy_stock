package com.nq.utils.task.stock;


import com.nq.service.IStockMarketsDayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component

public class SaveDayMarketsTask {
    private static final Logger log = LoggerFactory.getLogger(SaveDayMarketsTask.class);

    @Autowired
    IStockMarketsDayService iStockMarketsDayService;

    /*每天16點股票數據定時存入數據庫（股票走勢圖數據存儲）*/
@Scheduled(cron = "0 0 16 ? * MON-FRI")
    public void banlanceUserPositionTaskV1() {
        dotask();
    }

    public void dotask() {
        this.iStockMarketsDayService.saveStockMarketDay();
    }

    /*每天1點同步節假日開關*/
//@Scheduled(cron = "0 0 1 * * ?")
    public void holidayTask() {
        this.iStockMarketsDayService.saveHoliday();
    }


}
