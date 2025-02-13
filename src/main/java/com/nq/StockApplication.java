package com.nq;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.InetAddress;
import java.net.UnknownHostException;

@MapperScan(basePackages = "com.nq.dao")
@EnableScheduling
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableTransactionManagement
@Slf4j
@EnableAsync
public class StockApplication {

    public static void main(String[] args) throws UnknownHostException {


        SpringApplication app = new SpringApplication(StockApplication.class);
        ConfigurableApplicationContext ctx = app.run(args);

        ConfigurableEnvironment env = ctx.getEnvironment();

        log.info("--------------------------------------------------------");
        log.info("Application '{}' is running! Access URLs:", env.getProperty("spring.application.name"));
        log.info("Local: \thttp://localhost:{}", env.getProperty("server.port"));
        log.info("External: \thttp://{}:{}", InetAddress.getLocalHost().getHostAddress(), env.getProperty("server.port"));
        log.info("Profile: \t{}", env.getActiveProfiles());
        log.info("--------------------------------------------------------");

    }

}
