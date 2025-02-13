package com.nq.utils.task.news;

import com.nq.service.ISiteArticleService;
import com.nq.service.ISiteNewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
//import org.springframework.scheduling.annotation.Scheduled;


@Component
public class NewsTask {
    private static final Logger log = LoggerFactory.getLogger(NewsTask.class);

    @Autowired
    ISiteNewsService iSiteNewsService;

    @Autowired
    ISiteArticleService iSiteArticleService;

    /*
    * 新聞資訊抓取
    * */
   @Scheduled(cron = "0 0/30 9-20 * * ?")
//    @Scheduled(cron = "0 55 18 * * ?")
    //@Scheduled(cron = "0/2 * * * * ?")
    public void NewsInfoTask() {
        this.iSiteNewsService.grabNews();
        log.info("新聞資訊抓取完成");
    }






    /*
     * 新聞公告抓取
     * */
    @Scheduled(cron = "0 0/30 9-20 * * ?")
    public void ArtInfoTask() {
        this.iSiteArticleService.grabArticle();
    }




}

