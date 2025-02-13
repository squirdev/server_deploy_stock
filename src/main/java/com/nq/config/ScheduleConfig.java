//package com.nq.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.SchedulingConfigurer;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.scheduling.config.ScheduledTaskRegistrar;
//
//import java.util.concurrent.Executors;
//
///**
// * spring boot 多线程并发定时任务
// * 		所有的任务都在同一个线程池但不同线程中完成
// * @ClassName:：ScheduleConfig
// * @author leon
// * @createDate	2018年10月15日 下午4:02:38
// * @version	v1.0
// * @classRemarks TODO
// */
//@Configuration
//public class ScheduleConfig implements SchedulingConfigurer {
//
//    @Autowired
//    ThreadPoolTaskExecutor threadPoolTaskExecutor;
//
//    @Override
//    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(8,threadPoolTaskExecutor));
//    }
//}