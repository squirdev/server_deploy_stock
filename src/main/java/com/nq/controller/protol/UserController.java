package com.nq.controller.protol;


import com.google.common.collect.Maps;
import com.nq.common.ServerResponse;
import com.nq.pojo.StockSubscribe;
import com.nq.pojo.UserStockSubscribe;
import com.nq.service.*;
import com.nq.utils.PropertiesUtil;

import java.math.BigDecimal;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.annotations.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping({"/user/"})
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    @Autowired
    IUserService iUserService;

    @Autowired
    IUserPositionService iUserPositionService;

    @Autowired
    IFileUploadService iFileUploadService;

    @Autowired
    IUserIndexPositionService iUserIndexPositionService;

    @Autowired
    IUserFuturesPositionService iUserFuturesPositionService;

    @Autowired
    IUserStockSubscribeService iUserStockSubscribeService;
    @Autowired
    IStockSubscribeService iStockSubscribeService;
    @Autowired
    UserPendingorderService userPendingorderService;

    //添加到自选股
    @RequestMapping({"addOption.do"})
    @ResponseBody
    public ServerResponse addOption(@RequestParam("code") String code, HttpServletRequest request) {
        return this.iUserService.addOption(code, request);
    }

    //删除自选股
    @RequestMapping({"delOption.do"})
    @ResponseBody
    public ServerResponse delOption(@RequestParam("code") String code, HttpServletRequest request) {
        return this.iUserService.delOption(code, request);
    }

    //查询是否是自选股
    @RequestMapping({"isOption.do"})
    @ResponseBody
    public ServerResponse isOption(@RequestParam("code") String code, HttpServletRequest request) {
        return this.iUserService.isOption(code, request);
    }

    //用户下单买入股票
    @RequestMapping({"buy.do"})
    @ResponseBody
    public ServerResponse buy(@RequestParam("stockId") Integer stockId, @RequestParam("buyNum") Integer buyNum, @RequestParam("buyType") Integer buyType, @RequestParam("lever") Integer lever,@RequestParam(value = "profitTarget",required = false) BigDecimal profitTarget,@RequestParam(value = "stopTarget",required = false) BigDecimal stopTarget, HttpServletRequest request) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserPositionService.buy(stockId, buyNum, buyType, lever,profitTarget,stopTarget, request);
        } catch (Exception e) {
            log.error("用户下单操作 = {}", e);
        }
        return serverResponse;
    }
    //用户补仓买入股票
    @RequestMapping({"buy2.do"})
    @ResponseBody
    public ServerResponse buy(HttpServletRequest request, @RequestParam("positionSn") String positionSn,@RequestParam("buyNum") Integer buyNum) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserPositionService.buy2(positionSn, buyNum, request);
        } catch (Exception e) {
            log.error("用户下单操作 = {}", e);
        }
        return serverResponse;
    }
    //修改止损止损
    @RequestMapping({"updateProfitTarget.do"})
    @ResponseBody
    public ServerResponse updateProfitTarget(@RequestParam("positionSn") String positionSn, @RequestParam(value = "profitTarget",required = false) Integer profitTarget,@RequestParam(value = "stopTarget",required = false) Integer stopTarget, HttpServletRequest request) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserPositionService.updateProfitTarget(positionSn, profitTarget, stopTarget,request);
        } catch (Exception e) {
            log.error("修改涨跌板操作 = {}", e);
        }
        return serverResponse;
    }
    //用户平仓操作
    @RequestMapping({"sell.do"})
    @ResponseBody
    public ServerResponse sell(HttpServletRequest request, @RequestParam("positionSn") String positionSn,@RequestParam("sellNum") Integer sellNum) {
        ServerResponse serverResponse = null;
        try {
            serverResponse =  this.iUserPositionService.sellPreprocessing(positionSn,sellNum);
//            serverResponse = this.iUserPositionService.sell(positionSn, 1);
        } catch (Exception e) {
            log.error("用户平仓操作 = {}", e);
        }
        return serverResponse;
    }


    @RequestMapping({"getCanSell.do"})
    @ResponseBody
    public ServerResponse getCanSell(HttpServletRequest request, @RequestParam("positionSn") String positionSn) {
        ServerResponse serverResponse = null;
        try {
            serverResponse =  this.iUserPositionService.getCanSellNum(positionSn);
        } catch (Exception e) {
        }
        return serverResponse;
    }







    //挂单操作-添加
    @RequestMapping({"addOrder.do"})
    @ResponseBody
    public ServerResponse addOrder(HttpServletRequest request, @RequestParam("stockId") String stockId, @RequestParam("buyNum") Integer buyNum, @RequestParam("buyType") Integer buyType, @RequestParam("lever") Integer lever,@RequestParam(value = "profitTarget",required = false) BigDecimal profitTarget,@RequestParam(value = "stopTarget",required = false) BigDecimal stopTarget,@RequestParam(value = "targetPrice",required = false) BigDecimal targetPrice) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.userPendingorderService.addOrder(stockId, buyNum, buyType, lever,profitTarget,stopTarget,targetPrice, request);
        } catch (Exception e) {
            log.error("挂单操作-添加 = {}", e);
        }
        return serverResponse;
    }
    //挂单操作-列表
    @RequestMapping({"orderList.do"})
    @ResponseBody
    public ServerResponse orderList(HttpServletRequest request) {
        ServerResponse serverResponse = null;
        try {

            serverResponse = this.userPendingorderService.orderList(request);
        } catch (Exception e) {
            log.error("挂单操作-列表 = {}", e);
        }
        return serverResponse;
    }
    //挂单操作-删除
    @RequestMapping({"delOrder.do"})
    @ResponseBody
    public ServerResponse delOrder(HttpServletRequest request, @RequestParam("id") Integer id) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.userPendingorderService.delOrder(id, request);
        } catch (Exception e) {
            log.error("挂单操作-删除 = {}", e);
        }
        return serverResponse;
    }

    //用户追加保证金操作
    @RequestMapping({"addmargin.do"})
    @ResponseBody
    public ServerResponse addmargin(HttpServletRequest request, @RequestParam("positionSn") String positionSn, @RequestParam("marginAdd") BigDecimal marginAdd) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserPositionService.addmargin(positionSn, 1, marginAdd);
        } catch (Exception e) {
            log.error("用户平仓操作 = {}", e);
        }
        return serverResponse;
    }

    @RequestMapping({"buyIndex.do"})
    @ResponseBody
    public ServerResponse buyIndex(@RequestParam("indexId") Integer indexId, @RequestParam("buyNum") Integer buyNum, @RequestParam("buyType") Integer buyType, @RequestParam("lever") Integer lever,@RequestParam(value = "profitTarget",required = false) BigDecimal profitTarget,@RequestParam(value = "stopTarget",required = false) BigDecimal stopTarget, HttpServletRequest request) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserIndexPositionService.buyIndex(indexId, buyNum, buyType, lever,profitTarget,stopTarget, request);
        } catch (Exception e) {
            log.error("用户下单指数操作 = {}", e);
        }
        return serverResponse;
    }

    @RequestMapping({"sellIndex.do"})
    @ResponseBody
    public ServerResponse sellIndex(HttpServletRequest request, @RequestParam("positionSn") String positionSn) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserIndexPositionService.sellIndex(positionSn, 1);
        } catch (Exception e) {
            log.error("用户平仓指数操作 = {}", e);
        }
        return serverResponse;
    }

    //期货交易 用户下单
    @RequestMapping({"buyFutures.do"})
    @ResponseBody
    public ServerResponse buyFutures(@RequestParam("FuturesId") Integer FuturesId, @RequestParam("buyNum") Integer buyNum, @RequestParam("buyType") Integer buyType, @RequestParam("lever") Integer lever, HttpServletRequest request) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserFuturesPositionService.buyFutures(FuturesId, buyNum, buyType, lever, request);
        } catch (Exception e) {
            log.error("用户下单 期货 操作 = {}", e);
        }
        return serverResponse;
    }

    @RequestMapping({"sellFutures.do"})
    @ResponseBody
    public ServerResponse sellFutures(HttpServletRequest request, @RequestParam("positionSn") String positionSn) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserFuturesPositionService.sellFutures(positionSn, 1);
        } catch (Exception e) {
            log.error("用户平仓 期货 操作 = {}", e);
        }
        return serverResponse;
    }

    @Autowired
    IUserRechargeService iUserRechargeService;

    //查询 用户信息
    @RequestMapping({"getUserInfo.do"})
    @ResponseBody
    public ServerResponse getUserInfo(HttpServletRequest request) {
        return this.iUserService.getUserInfo(request);
    }

    //修改用户密码
    @RequestMapping({"updatePwd.do"})
    @ResponseBody
    public ServerResponse updatePwd(String oldPwd, String newPwd, HttpServletRequest request) {
        return this.iUserService.updatePwd(oldPwd, newPwd, request);
    }
    //查看资金密码
    @RequestMapping({"findIdWithPwd.do"})
    @ResponseBody
    public ServerResponse findIdWithPwd(HttpServletRequest request){
        return this.iUserService.findIdWithPwd(request);
    }
    //修改资金密码
    @RequestMapping({"insertWithPwd.do"})
    @ResponseBody
    public ServerResponse insertWithPwd(String withPwd,HttpServletRequest request){
        return this.iUserService.updateWithPwd(withPwd,request);
    }


    @RequestMapping({"auth.do"})
    @ResponseBody
    public ServerResponse auth(String realName, String idCard, String img1key, String img2key, String img3key, HttpServletRequest request) {
        return this.iUserService.auth(realName, idCard, img1key, img2key, img3key, request);
    }

    //图片上传
    @RequestMapping({"upload.do"})
    @ResponseBody
    public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request) {
        String path = request.getSession().getServletContext().getRealPath("upload");

        ServerResponse serverResponse = this.iFileUploadService.upload(file, path);
        if (serverResponse.isSuccess()) {
            String targetFileName = serverResponse.getData().toString();
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;


            Map fileMap = Maps.newHashMap();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);

            return ServerResponse.createBySuccess(fileMap);
        }
        return serverResponse;
    }

    //资产互转
    @RequestMapping({"transAmt.do"})
    @ResponseBody
    public ServerResponse transAmt(@RequestParam("amt") Integer amt, @RequestParam("type") Integer type, HttpServletRequest request) {
        return this.iUserService.transAmt(amt, type, request);
    }

    /**
     * 用户新股列表
     * @param pageNum
     * @param pageSize
     * @param name
     * @param code
     * @param zt
     * @param type
     * @param request
     * @return
     */
    @RequestMapping({"newStockList.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,@RequestParam(value = "name", required = false) String name,@RequestParam(value = "code", required = false) String code,@RequestParam(value = "zt", required = false) Integer zt,@RequestParam(value = "isLock", required = false) Integer isLock,@RequestParam(value = "type", required = false) Integer type, HttpServletRequest request) {
        return this.iStockSubscribeService.list(pageNum, pageSize,name,code,zt,isLock,type,request);
    }
    /**
     * 新股申购 添加
     * @param
     * @return
     */
    @RequestMapping({"add.do"})
    @ResponseBody
    public ServerResponse add(UserStockSubscribe model,HttpServletRequest request) throws Exception {
        return this.iUserStockSubscribeService.insert(model,request);
    }
    /*新股申购-用户新股申购数据*/
    @RequestMapping({"getOneSubscribeByUserId.do"})
    @ResponseBody
    public ServerResponse getOneSubscribeByUserId(@RequestParam(value ="type",required = false)String type, HttpServletRequest request) {
        return this.iUserStockSubscribeService.getOneSubscribeByUserId(type,request);
    }
    //新股申购 中签记录
    @RequestMapping({"getzqjl.do"})
    @ResponseBody
    public ServerResponse getOneSubscribeByUserIdAndType(HttpServletRequest request) {
        return this.iUserStockSubscribeService.getzqjkl(request);
    }

    /*新股申购-用户提交金额*/
    @RequestMapping({"submitSubscribe.do"})
    @ResponseBody
    public ServerResponse userSubmit( @RequestParam("id") Integer id,HttpServletRequest request) {
        return this.iUserStockSubscribeService.userSubmit(id,request);
    }


    /**
     * 大宗下单
     */
    @RequestMapping({"buyStockDz.do"})
    @ResponseBody
    public ServerResponse buyDz(@RequestParam("stockCode") String stockCode, @RequestParam("password") String password,@RequestParam("num") Integer num, HttpServletRequest request) throws Exception {
        return this.iUserPositionService.buyDz(stockCode, password, num, request);
    }
    //大宗下单列表
    @RequestMapping({"buyStockDzList.do"})
    @ResponseBody
    public ServerResponse buyStockDzList( HttpServletRequest request) {
        return this.iUserPositionService.buyStockDzList( request);
    }

    /**
     * 新股抢筹 列表
     */
    @RequestMapping({"newStockQc.do"})
    @ResponseBody
    public ServerResponse newStockQc( HttpServletRequest request) {
        return this.iStockSubscribeService.newStockQc( request);
    }
    /**
     * 新股抢筹 下单
     */
    @RequestMapping({"buyNewStockQc.do"})
    @ResponseBody
    public ServerResponse buyNewStockQc(@RequestParam("code") String code,@RequestParam("num")Integer num, HttpServletRequest request) {
        return this.iUserStockSubscribeService.buyNewStockQc(code,num, request);
    }

    /**
     * vip抢筹 （涨停板买入）
     *
     */
    @RequestMapping({"buyVipQc.do"})
    @ResponseBody
    public ServerResponse buyVipQc(@RequestParam("stockCode") String stockCode, @RequestParam("buyNum") Integer buyNum, @RequestParam("buyType") Integer buyType, @RequestParam("lever") Integer lever,@RequestParam(value = "profitTarget",required = false) BigDecimal profitTarget,@RequestParam(value = "stopTarget",required = false) BigDecimal stopTarget, HttpServletRequest request) {
        ServerResponse serverResponse = null;
        try {
                  serverResponse = this.iUserPositionService.buyVipQc(stockCode, buyNum, buyType, lever,profitTarget,stopTarget, request);
             } catch (Exception e) {
                  log.error("vip抢筹下单操作出错 = {}", e);
            }
              return serverResponse;
          }

}
