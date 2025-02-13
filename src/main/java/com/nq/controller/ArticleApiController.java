package com.nq.controller;

import com.nq.common.ServerResponse;
import com.nq.service.ISiteArticleService;
import com.nq.utils.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/api/art/"})
public class ArticleApiController {
    private static final Logger log = LoggerFactory.getLogger(ArticleApiController.class);

    @Autowired
    ISiteArticleService iSiteArticleService;

    //查询企业公告信息
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "artTitle", required = false) String artTitle, @RequestParam(value = "artType", required = false) String artType, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize,HttpServletRequest request) {
        return this.iSiteArticleService.list(artTitle, artType, pageNum, pageSize,request);
    }

    //查询指定企业公告信息
    @RequestMapping({"detail.do"})
    @ResponseBody
    public ServerResponse detail(Integer artId) {
        return this.iSiteArticleService.detail(artId);
    }

    //top最新公告
    @RequestMapping({"getTopArt.do"})
    @ResponseBody
    public ServerResponse getTopArtList(@RequestParam(value = "pageSize", defaultValue = "15") int pageSize) {
        return this.iSiteArticleService.getTopArtList(pageSize);
    }

}
