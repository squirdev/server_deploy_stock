package com.nq.utils.task.stock;

import com.nq.service.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class StockDataUpdateTask {


    @Autowired
    private IStockService stockService;


    @Scheduled(cron = "0 0 16 ? * MON-FRI")
    public void updateStockData() {
        dotask();
    }

    public void dotask() {
        this.stockService.updateStockData();
    }

    @PostConstruct
    public void init() {
        log.info("update stock name.");
//        this.stockService.updateStockData();
    }
}
