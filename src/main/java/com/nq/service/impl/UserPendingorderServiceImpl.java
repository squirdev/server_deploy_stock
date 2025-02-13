package com.nq.service.impl;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nq.common.ServerResponse;
import com.nq.dao.*;
import com.nq.pojo.*;
import com.nq.service.*;
import com.nq.utils.DateTimeUtil;
import com.nq.utils.HttpClientRequest;
import com.nq.utils.PropertiesUtil;
import com.nq.utils.redis.JsonUtil;
import com.nq.utils.redis.RedisShardedPoolUtils;
import com.nq.utils.stock.sina.SinaStockApi;
import com.nq.vo.foreigncurrency.ExchangeVO;
import com.nq.vo.position.PositionProfitVO;
import com.nq.vo.position.UserPendingorderVO;
import com.nq.vo.stock.MarketVO;
import com.nq.vo.stock.StockListVO;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

import static com.nq.utils.DateTimeUtil.getCurrentTimeMiao;
import static com.nq.utils.DateTimeUtil.getCurrentTimeMiaoZero;


/**
* @author Administrator
* @description 针对表【user_pendingorder】的数据库操作Service实现
* @createDate 2022-11-10 06:10:40
*/
@Service
@Slf4j
public class UserPendingorderServiceImpl extends ServiceImpl<UserPendingorderMapper, UserPendingorder>
    implements UserPendingorderService {
    @Autowired
    private UserPendingorderMapper userPendingorderMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IUserPositionService iUserPositionService;
    @Autowired
    private SiteTaskLogMapper siteTaskLogMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StockIndexMapper stockIndexMapper;
    @Autowired
    private IStockIndexService iStockIndexService;
    @Autowired
    private IUserIndexPositionService iUserIndexPositionService;
    @Autowired
    private IStockCoinService iStockCoinService;
    @Autowired
    private IStockFuturesService iStockFuturesService;
    @Autowired
    private ISiteSettingService iSiteSettingService;
    @Autowired
    private UserPositionMapper userPositionMapper;
    @Override
    public ServerResponse addOrder(String stockId, Integer buyNum, Integer buyType, Integer lever, BigDecimal profitTarget, BigDecimal stopTarget, BigDecimal targetPrice, HttpServletRequest request) {
        User user = this.iUserService.getCurrentRefreshUser(request);

        if (user == null) {
            return ServerResponse.createByErrorMsg("請先登錄");
        }
        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (buyNum.intValue() < siteSetting.getBuyMinNum().intValue()) {
            return ServerResponse.createByErrorMsg("挂单失败，购买数量小于" + siteSetting
                    .getBuyMinNum() + "股");
        }
        if (buyNum.intValue() > siteSetting.getBuyMaxNum().intValue()) {
            return ServerResponse.createByErrorMsg("挂单失败，购买数量大于" + siteSetting
                    .getBuyMaxNum() + "股");
        }
        UserPendingorder userPendingorder = userPendingorderMapper.selectOne(new QueryWrapper<UserPendingorder>().eq("user_id", user.getId()).eq("stock_id", stockId).eq("status", 0));
        if(userPendingorder != null) {
            return ServerResponse.createByErrorMsg("请勿重复挂单");
        }

         userPendingorder = new UserPendingorder();
        userPendingorder.setUserId(user.getId());
        userPendingorder.setStockId(stockId);
        userPendingorder.setBuyNum(buyNum);
        userPendingorder.setBuyType(buyType);
        userPendingorder.setLever(lever);
        userPendingorder.setProfitTarget(profitTarget);
        userPendingorder.setStopTarget(stopTarget);
        userPendingorder.setNowPrice(new BigDecimal(0));
        userPendingorder.setTargetPrice(targetPrice);
        userPendingorder.setAddTime(new Date());
        userPendingorder.setStatus(0);
        int ret = userPendingorderMapper.insert(userPendingorder);
        if (ret > 0) {
            return ServerResponse.createBySuccessMsg("添加挂单成功，满足下单条件将自动下单");
        }
        return ServerResponse.createByErrorMsg("添加失败");

    }
    @Override
    public ServerResponse orderList(HttpServletRequest request) {

        String property = PropertiesUtil.getProperty("user.cookie.name");
        String header = request.getHeader(property);
//        log.info("header:{}",header);
        if (header != null) {
            String userJson = RedisShardedPoolUtils.get(header);
            User user = (User) JsonUtil.string2Obj(userJson, User.class);
//            log.info("user:{}",user);
            if (user != null) {
                List<UserPendingorder> userPendingorders = userPendingorderMapper.selectList(new QueryWrapper<UserPendingorder>().eq("user_id", user.getId()));

                List UserPendingorderList = new ArrayList();


                for (UserPendingorder userPendingorder : userPendingorders) {
                    UserPendingorderVO userPendingorderVO = new UserPendingorderVO();
                    //挂单-指数
                    if (userPendingorder.getStockId().contains("sh") || userPendingorder.getStockId().contains("sz")) {
                        StockIndex model = stockIndexMapper.selectIndexByCode(userPendingorder.getStockId().replace("sh", "").replace("sz", ""));

                        MarketVO marketVO = this.iStockIndexService.querySingleIndex(model.getIndexGid());
                        userPendingorderVO.setNowPrice(new BigDecimal(marketVO.getNowPrice()));
                        userPendingorderVO.setStockName(model.getIndexName());
                        userPendingorderVO.setStockId(model.getIndexGid());

                    } else {
                        //挂单-股票
                        Stock stock = stockMapper.findStockByCode(userPendingorder.getStockId());
                        StockListVO stockListVO = new StockListVO();
                            stockListVO = SinaStockApi.assembleStockListVO(
                                    SinaStockApi.getSinaStock(stock.getStockGid()));
                        String nowPrice = stockListVO.getNowPrice();
                        if (nowPrice == null) {
                            nowPrice = String.valueOf(0);
                        }
                        userPendingorderVO.setNowPrice(new BigDecimal(nowPrice));
                        userPendingorderVO.setStockName(stock.getStockName());
                        userPendingorderVO.setStockId(stock.getStockCode());
                    }
                    userPendingorderVO.setBuyNum(userPendingorder.getBuyNum());
                    userPendingorderVO.setBuyType(userPendingorder.getBuyType());
                    userPendingorderVO.setLever(userPendingorder.getLever());
                    userPendingorderVO.setProfitTarget(userPendingorder.getProfitTarget());
                    userPendingorderVO.setStopTarget(userPendingorder.getStopTarget());
                    userPendingorderVO.setTargetPrice(userPendingorder.getTargetPrice());
                    userPendingorderVO.setAddTime(userPendingorder.getAddTime());
                    userPendingorderVO.setStatus(userPendingorder.getStatus());
                    userPendingorderVO.setId(userPendingorder.getId());
                    UserPendingorderList.add(userPendingorderVO);
                }
                return ServerResponse.createBySuccess(UserPendingorderList);
            }
        }
//        else {
//            String admin = PropertiesUtil.getProperty("admin.cookie.name");
//            String adminHeader = request.getHeader(admin);
//            log.info("adminHeader:{}",adminHeader);
//            if (adminHeader!=null) {
//                String adminJson = RedisShardedPoolUtils.get(adminHeader);
//                SiteAdmin siteAdmin = JsonUtil.string2Obj(adminJson, SiteAdmin.class);
//                log.info("siteAdmin:{}",siteAdmin);
//                if (siteAdmin != null) {
//
//                    return ServerResponse.createBySuccess();
//                }
//            }
//
//        }

        return ServerResponse.createByErrorMsg("数据异常");

    }

    @Override
    public void orderTask() {

        List<UserPendingorder> userPendingorders = userPendingorderMapper.selectList(new QueryWrapper<UserPendingorder>().eq("status", 0));
        log.info("当前有挂单的用户数量 为 {}", Integer.valueOf(userPendingorders.size()));

        for (int i = 0; i < userPendingorders.size(); i++) {
//            log.info("=====================");
            Integer userId = (Integer) userPendingorders.get(i).getUserId();
            User user = this.userMapper.selectByPrimaryKey(userId);
            if (user == null) {
                continue;
            }
            List<UserPendingorder> userPendingorderList = userPendingorderMapper.selectList(new QueryWrapper<UserPendingorder>().eq("user_id", userId).eq("status", 0));
            if (userPendingorderList == null) {
                continue;
            }

            log.info("用户id = {} 姓名 = {} 已挂单数： {}", new Object[]{userId, user.getRealName(), Integer.valueOf(userPendingorders.size())});
            BigDecimal enable_user_amt = user.getEnableAmt();
            BigDecimal all_freez_amt = new BigDecimal("0");
            String nowPrice = "";
            String code = "";
            Integer indexId = null;
            StockListVO stockListVO = new StockListVO();
            StockCoin stockCoin = new StockCoin();
            for (UserPendingorder userPendingorder : userPendingorderList) {
                    //指数
                if (userPendingorder.getStockId().contains("sh") || userPendingorder.getStockId().contains("sz")) {
                    StockIndex model = stockIndexMapper.selectIndexByCode(userPendingorder.getStockId().replace("sh", "").replace("sz", ""));
                    all_freez_amt = (new BigDecimal(model.getDepositAmt().intValue())).multiply(new BigDecimal(userPendingorder.getBuyNum())).divide(new BigDecimal(userPendingorder.getLever())).setScale(4, 2);

//                    if (){
//
//                    }else {
                        MarketVO marketVO = this.iStockIndexService.querySingleIndex(model.getIndexGid());
                        nowPrice = marketVO.getNowPrice();
//                    }

                    indexId = model.getId();

                } else {
                    //股票
                    Stock stock = stockMapper.findStockByCode(userPendingorder.getStockId());
                            stockListVO = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(stock.getStockGid()));
                            nowPrice = stockListVO.getNowPrice();
//                        String stockOther = RedisShardedPoolUtils.get(stock.getStockName(), 8);

//                    if (stock.getDataBase() != 0) {
//
////                         String date = getCurrentTimeMiaoZero();
//                        String date = getCurrentTimeMiaoZero();
//
//                        String result = HttpClientRequest.doGet(PropertiesUtil.getProperty("changePrice.url")+"?cat_time="+date+"&stock_gid="+stock.getStockGid()+"&price="+stockListVO.getNowPrice());
//                        JSONObject jsonObject = JSONObject.fromObject(result);
//                        String nowPrice1 = jsonObject.getJSONObject("data").getString("new_price");
//                        if (nowPrice1!=null){
//                            if ("us".equals(stock.getStockType())){
//                                ExchangeVO exchangeVO = this.iStockFuturesService.queryExchangeVO("USD").getData();
//                                nowPrice = String.valueOf(new BigDecimal(nowPrice1).multiply(new BigDecimal(exchangeVO.getNowPrice())));
//                            } else if ("hk".equals(stock.getStockType())){
//                                ExchangeVO exchangeVO = this.iStockFuturesService.queryExchangeVO("HKD").getData();
//                                nowPrice = String.valueOf(new BigDecimal(nowPrice1).multiply(new BigDecimal(exchangeVO.getNowPrice())));
//                            } else {
//                                nowPrice = String.valueOf(new BigDecimal(nowPrice1));
//                            }
//
//                        }else {
//                            stockListVO.setNowPrice(stockListVO.getNowPrice());
//                            if ("us".equals(stock.getStockType())){
//                                ExchangeVO exchangeVO = this.iStockFuturesService.queryExchangeVO("USD").getData();
//                                nowPrice = String.valueOf(new BigDecimal(stockListVO.getNowPrice()).multiply(new BigDecimal(exchangeVO.getNowPrice())));
//                            } else if ("hk".equals(stock.getStockType())){
//                                ExchangeVO exchangeVO = this.iStockFuturesService.queryExchangeVO("HKD").getData();
//                                nowPrice = String.valueOf(new BigDecimal(stockListVO.getNowPrice()).multiply(new BigDecimal(exchangeVO.getNowPrice())));
//                            } else {
//                                nowPrice = String.valueOf(new BigDecimal(stockListVO.getNowPrice()));
//                            }
//                        }
//                    }


                    all_freez_amt = new BigDecimal(nowPrice).multiply(new BigDecimal(userPendingorder.getBuyNum())).divide(new BigDecimal(userPendingorder.getLever()), 2, 4);
                    code = stock.getStockCode();
                }
                if (nowPrice == null) {
                    nowPrice = String.valueOf(0);
                }
                if (userPendingorder.getUserId() != null && userPendingorder.getStockId() != null && userPendingorder.getBuyNum() != null && userPendingorder.getBuyType() != null && userPendingorder.getLever() != null && userPendingorder.getTargetPrice() != null) {
                    int ret = userPendingorder.getBuyType().intValue() == 0 ? userPendingorder.getTargetPrice().compareTo(new BigDecimal(nowPrice)) : new BigDecimal(nowPrice).compareTo(userPendingorder.getTargetPrice());
                    //当前时间String
                    String buyTime = DateTimeUtil.dateToStr(new Date());
                    if (ret <= 0) {
                        if (code != null && !"".equals(code)) {
                            try {
                                this.iUserPositionService.create(userPendingorder.getUserId(), code, nowPrice, buyTime, userPendingorder.getBuyNum(), userPendingorder.getBuyType(), userPendingorder.getLever(), userPendingorder.getProfitTarget(), userPendingorder.getStopTarget());
                                userPendingorder.setStatus(1);
                                this.userPendingorderMapper.updateById(userPendingorder);
                                SiteTaskLog siteTaskLog = new SiteTaskLog();
                                siteTaskLog.setTaskType("股票挂单转持仓");
                                String accountType = (user.getAccountType() == 0) ? "正式用户" : "模拟用户";
                                String taskcnt = accountType + "-" + user.getRealName() + "挂单[达到目标价格] 用户id = " + user.getId() + ", 可用资金 = " + enable_user_amt + "冻结保证金 = " + all_freez_amt + ", 目标价格 = " + userPendingorder.getTargetPrice() + ",现价" + nowPrice + ", 涨跌:" + (userPendingorder.getBuyType().intValue() == 0 ? "涨" : "跌");
                                siteTaskLog.setTaskCnt(taskcnt);
                                String tasktarget = "此次挂单买入id：" + userPendingorder.getId();
                                siteTaskLog.setTaskTarget(tasktarget);
                                siteTaskLog.setAddTime(new Date());
                                siteTaskLog.setIsSuccess(0);
                                siteTaskLog.setErrorMsg("");
                                int insertTaskCount = this.siteTaskLogMapper.insert(siteTaskLog);
                                if (insertTaskCount > 0) {
                                    log.info("挂单task任务成功");
                                } else {
                                    log.info("挂单task任务失败");
                                }
                            } catch (Exception e) {
                                log.error("股票挂单任务失败...");
                                userPendingorder.setStatus(2);
                                this.userPendingorderMapper.updateById(userPendingorder);
                            }
                        } else if (indexId != null) {
                            try {
                                this.iUserIndexPositionService.buyIndexOrder(indexId, userPendingorder.getBuyNum(), userPendingorder.getBuyType(), userPendingorder.getLever(), userPendingorder.getProfitTarget(), userPendingorder.getStopTarget(), userPendingorder.getUserId());
                                userPendingorder.setStatus(1);
                                this.userPendingorderMapper.updateById(userPendingorder);
                                SiteTaskLog siteTaskLog = new SiteTaskLog();
                                siteTaskLog.setTaskType("指数挂单转持仓");
                                String accountType = (user.getAccountType() == 0) ? "正式用户" : "模拟用户";
                                String taskcnt = accountType + "-" + user.getRealName() + "挂单[达到目标价格] 用户id = " + user.getId() + ", 可用资金 = " + enable_user_amt + "冻结保证金 = " + all_freez_amt + ", 目标价格 = " + userPendingorder.getTargetPrice() + ",现价" + nowPrice + ", 涨跌:" + (userPendingorder.getBuyType().intValue() == 0 ? "涨" : "跌");
                                siteTaskLog.setTaskCnt(taskcnt);
                                String tasktarget = "此次挂单买入id：" + userPendingorder.getId();
                                siteTaskLog.setTaskTarget(tasktarget);
                                siteTaskLog.setAddTime(new Date());
                                siteTaskLog.setIsSuccess(0);
                                siteTaskLog.setErrorMsg("");
                                int insertTaskCount = this.siteTaskLogMapper.insert(siteTaskLog);
                                if (insertTaskCount > 0) {
                                    log.info("挂单task任务成功");
                                    userPendingorder.setStatus(1);
                                } else {
                                    log.info("挂单task任务失败");
                                }
                            } catch (Exception e) {
                                log.error("指数挂单任务失败...");
                                userPendingorder.setStatus(2);
                                this.userPendingorderMapper.updateById(userPendingorder);
                            }
                        }

                    }

                }

            }

        }
        log.info("===========挂单结束==========");
    }

    //删除
    @Override
    public ServerResponse delOrder(Integer id, HttpServletRequest request) {
        String property = PropertiesUtil.getProperty("user.cookie.name");
        String header = request.getHeader(property);
        if (header != null) {
            String userJson = RedisShardedPoolUtils.get(header);
            User user = (User) JsonUtil.string2Obj(userJson, User.class);
            UserPendingorder userPendingorder = this.userPendingorderMapper.selectById(id);
            if (userPendingorder == null) {
                return ServerResponse.createByErrorMsg("该挂单不存在");
            }
            if (user.getId().intValue() != userPendingorder.getUserId().intValue()) {
                return ServerResponse.createByErrorMsg("该挂单不属于您");
            }
            int delCount = this.userPendingorderMapper.deleteById(id);
            if (delCount > 0) {
                return ServerResponse.createByErrorMsg("删除成功");
            }
            return ServerResponse.createByErrorMsg("删除失败");
        }

        return ServerResponse.createByErrorMsg("请登录");
    }

}


