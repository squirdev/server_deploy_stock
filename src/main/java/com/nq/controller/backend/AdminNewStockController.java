package com.nq.controller.backend;

import com.nq.service.IStockSubscribeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName : AdminNewStockController  //新股
 * @Description :   //描述
 * @Author :  tf //作者
 * @Date: 2022/10/24  22:53
 */
@RestController
@RequestMapping({"/admin/newstock/"})
public class AdminNewStockController {
    @Autowired
    IStockSubscribeService iStockSubscribeService;
    /**
    * @Description:  //新股列表
    * @Param:
    * @return:
    * @Author: tf
    * @Date: 2022/10/24
    */
    @RequestMapping({"list.do"})
    public void list(){

    }


}
