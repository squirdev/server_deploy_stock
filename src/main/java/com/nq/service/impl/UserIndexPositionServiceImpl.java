package com.nq.service.impl;


import com.nq.dao.*;
import com.nq.pojo.*;
import com.nq.service.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.nq.common.ServerResponse;
import com.nq.utils.CurrencyUtils;
import com.nq.utils.DateTimeUtil;
import com.nq.utils.KeyUtils;
import com.nq.utils.redis.JsonUtil;
import com.nq.utils.stock.BuyAndSellUtils;
import com.nq.utils.stock.biying.ByStockApi;
import com.nq.utils.stock.sina.SinaStockApi;
import com.nq.vo.agent.AgentIncomeVO;
import com.nq.vo.indexposition.AdminIndexPositionVO;
import com.nq.vo.indexposition.AgentIndexPositionVO;
import com.nq.vo.indexposition.IndexPositionProfitVO;
import com.nq.vo.indexposition.IndexPositionVO;
import com.nq.vo.indexposition.UserIndexPositionVO;
import com.nq.vo.position.PositionProfitVO;
import com.nq.vo.stock.MarketVO;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.nq.vo.stock.StockDTO;
import com.nq.vo.stock.StockListVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("iUserIndexPositionService")
public class UserIndexPositionServiceImpl implements IUserIndexPositionService {
    private static final Logger log = LoggerFactory.getLogger(UserIndexPositionServiceImpl.class);

    @Autowired
    UserIndexPositionMapper userIndexPositionMapper;

    @Autowired
    IUserService iUserService;

    @Autowired
    IStockIndexService iStockIndexService;

    @Autowired
    ISiteIndexSettingService iSiteIndexSettingService;

    @Autowired
    UserMapper userMapper;
    @Autowired
    UserCashDetailMapper userCashDetailMapper;
    @Autowired
    IAgentUserService iAgentUserService;
    @Autowired
    AgentUserMapper agentUserMapper;
    @Autowired
    ISiteProductService iSiteProductService;
    @Autowired
    IUserIndexPositionService iUserIndexPositionService;
    @Autowired
    SiteTaskLogMapper siteTaskLogMapper;
    @Autowired
    CurrencyUtils currencyUtils;


    @Autowired
    private ByStockApi byStockApi;

    @Transactional
    public ServerResponse buyIndex(Integer indexId, Integer buyNum, Integer buyType, Integer lever,BigDecimal profitTarget,BigDecimal stopTarget, HttpServletRequest request) throws Exception {
        if (indexId == null || buyNum == null || buyType == null) {
            return ServerResponse.createByErrorMsg("参数不能为空");
        }

        User user = this.iUserService.getCurrentRefreshUser(request);
        /*实名认证开关开启*/
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        if (siteProduct.getRealNameDisplay() && (StringUtils.isBlank(user.getRealName()) || StringUtils.isBlank(user.getIdCard()))) {
            return ServerResponse.createByErrorMsg("下单失败，请先实名认证");
        }

        if(siteProduct.getHolidayDisplay()){
            return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
        }

        log.info("用户 {} 下单, 指数id = {} ，数量 = {} 手 , 方向 = {} ， 杠杆 = {}", new Object[]{user
                .getId(), indexId, buyNum, buyType, lever});

        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
            return ServerResponse.createByErrorMsg("下单失败，账户已被锁定");
        }


        SiteIndexSetting siteIndexSetting = this.iSiteIndexSettingService.getSiteIndexSetting();
        if (siteIndexSetting == null) {
            log.error("下单出错，指数设置表不存在");
            return ServerResponse.createByErrorMsg("下单失败，系统设置错误");
        }
        StockIndex stockIndex = this.iStockIndexService.selectIndexById(indexId);

        if (stockIndex.getIndexGid().contains("us")){
            String am_begin = siteIndexSetting.getTransAmBeginUs();
            String am_end = siteIndexSetting.getTransAmEndUs();
            String pm_begin = siteIndexSetting.getTransPmBeginUs();
            String pm_end = siteIndexSetting.getTransPmEndUs();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("下单失败，不在交易时段内");
            }
        }else if(stockIndex.getIndexGid().contains("hk")){
            String am_begin = siteIndexSetting.getTransAmBeginhk();
            String am_end = siteIndexSetting.getTransAmEndhk();
            String pm_begin = siteIndexSetting.getTransPmBeginhk();
            String pm_end = siteIndexSetting.getTransPmEndhk();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("下单失败，不在交易时段内");
            }
        }else {
            String am_begin = siteIndexSetting.getTransAmBegin();
            String am_end = siteIndexSetting.getTransAmEnd();
            String pm_begin = siteIndexSetting.getTransPmBegin();
            String pm_end = siteIndexSetting.getTransPmEnd();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));
            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("下单失败，不在交易时段内");
            }
        }

        if (stockIndex == null) {
            return ServerResponse.createByErrorMsg("指数不存在");
        }
        if (1 != stockIndex.getTransState().intValue()) {
            return ServerResponse.createByErrorMsg("该指数不能交易");
        }

        //保证金= 指数保证金*数量/杠杆倍数
        BigDecimal all_deposit_amt = (new BigDecimal(stockIndex.getDepositAmt().intValue())).multiply(new BigDecimal(buyNum.intValue())).divide(new BigDecimal(lever)).setScale(4,2);

        if (user.getEnableIndexAmt().compareTo(all_deposit_amt) == -1) {
            return ServerResponse.createByErrorMsg("指数账户资金不足");
        }

        BigDecimal max_buy_amt = user.getEnableIndexAmt().multiply(siteIndexSetting.getBuyMaxPercent());
        if (max_buy_amt.compareTo(all_deposit_amt) == -1) {
            return ServerResponse.createByErrorMsg("不能超过最大购买比例");
        }
        if (user.getUserAmt().compareTo(new BigDecimal("0")) == -1) {
            return ServerResponse.createByErrorMsg("失败，融资总资金小于0");
        }
        if (user.getUserFutAmt().compareTo(new BigDecimal("0")) == -1) {
            return ServerResponse.createByErrorMsg("失败，期货总资金小于0");
        }


        MarketVO marketVO = this.iStockIndexService.querySingleIndex(stockIndex.getIndexGid());
        log.info("当前指数行情 = {}", JsonUtil.obj2StringPretty(marketVO));

        BigDecimal increaseRate = new BigDecimal(marketVO.getIncreaseRate());
        if (increaseRate.compareTo(new BigDecimal("0")) == 1) {

            if (siteIndexSetting.getRiseLimit().multiply(new BigDecimal("100"))
                    .compareTo(increaseRate) == -1 && buyType
                    .intValue() == 0) {
                return ServerResponse.createByErrorMsg("当前指数涨幅:" + increaseRate + ",不能买涨");
            }
        } else {

            increaseRate = increaseRate.negate();
            if (siteIndexSetting.getRiseLimit().multiply(new BigDecimal("100"))
                    .compareTo(increaseRate) == -1 && buyType
                    .intValue() == 1) {
                return ServerResponse.createByErrorMsg("当前指数跌幅:" + increaseRate + ",不能买跌");
            }
        }


        BigDecimal reckon_enable = user.getEnableIndexAmt().subtract(all_deposit_amt);
        user.setEnableIndexAmt(reckon_enable);
        int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateUserCount > 0) {
            log.info("【用户交易指数下单】修改用户金额成功");
        } else {
            log.error("用户交易指数下单】修改用户金额出错");
            throw new Exception("用户交易指数下单】修改用户金额出错");
        }


        UserIndexPosition userIndexPosition = new UserIndexPosition();
        if (profitTarget != null && profitTarget.compareTo(new BigDecimal("0")) > 0) {
            userIndexPosition.setProfitTargetPrice(profitTarget);
        }
        if (stopTarget != null && stopTarget.compareTo(new BigDecimal("0")) > 0) {
            userIndexPosition.setStopTargetPrice(stopTarget);
        }
        BigDecimal buyPrice = new BigDecimal("0");
            buyPrice = new BigDecimal(marketVO.getNowPrice());
        userIndexPosition.setPositionType(user.getAccountType());
        userIndexPosition.setPositionSn(KeyUtils.getUniqueKey());
        userIndexPosition.setUserId(user.getId());
        userIndexPosition.setRealName(user.getRealName());
        userIndexPosition.setAgentId(user.getAgentId());
        userIndexPosition.setIndexName(stockIndex.getIndexName());
        userIndexPosition.setIndexCode(stockIndex.getIndexCode());
        userIndexPosition.setIndexGid(stockIndex.getIndexGid());
        userIndexPosition.setBuyOrderTime(new Date());
        userIndexPosition.setBuyOrderPrice(buyPrice);
        userIndexPosition.setOrderDirection((buyType.intValue() == 0) ? "买涨" : "买跌");

        userIndexPosition.setOrderNum(buyNum);


        userIndexPosition.setIsLock(Integer.valueOf(0));

        userIndexPosition.setAllDepositAmt(all_deposit_amt);
        userIndexPosition.setOrderFee((new BigDecimal(stockIndex.getTransFee().intValue()))
                .multiply(new BigDecimal(buyNum.intValue())));
        userIndexPosition.setOrderStayDays(Integer.valueOf(0));
        userIndexPosition.setEachPoint(new BigDecimal(stockIndex.getEachPoint().intValue()));
        userIndexPosition.setProfitAndLose(new BigDecimal("0"));
        userIndexPosition.setAllProfitAndLose(new BigDecimal("0"));
        userIndexPosition.setOrderLever(lever);

        int insertPositionCount = this.userIndexPositionMapper.insert(userIndexPosition);
        if (insertPositionCount > 0) {
            log.info("【用户交易指数下单】保存持仓记录成功");
        } else {
            log.error("用户交易指数下单】保存持仓记录出错");
            throw new Exception("用户交易指数下单】保存持仓记录出错");
        }

        return ServerResponse.createBySuccess("下单成功");
    }

    /**
     * 挂单指数
     * @param indexId
     * @param buyNum
     * @param buyType
     * @param lever
     * @param userId
     * @return
     * @throws Exception
     */
    @Transactional
    public ServerResponse buyIndexOrder(Integer indexId, Integer buyNum, Integer buyType, Integer lever,BigDecimal profitTarget,BigDecimal stopTarget,Integer userId) throws Exception {
        if (indexId == null || buyNum == null || buyType == null) {
            return ServerResponse.createByErrorMsg("参数不能为空");
        }

        User user = this.userMapper.selectByPrimaryKey(userId);
        /*实名认证开关开启*/
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        if (siteProduct.getRealNameDisplay() && (StringUtils.isBlank(user.getRealName()) || StringUtils.isBlank(user.getIdCard()))) {
            return ServerResponse.createByErrorMsg("下单失败，请先实名认证");
        }

        if(siteProduct.getHolidayDisplay()){
            return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
        }

        log.info("用户 {} 下单, 指数id = {} ，数量 = {} 手 , 方向 = {} ， 杠杆 = {}", new Object[]{user
                .getId(), indexId, buyNum, buyType, lever});

        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
            log.error("用户 {} 已被锁定", user.getId());
            return ServerResponse.createByErrorMsg("下单失败，账户已被锁定");
        }


        SiteIndexSetting siteIndexSetting = this.iSiteIndexSettingService.getSiteIndexSetting();
        if (siteIndexSetting == null) {
            log.error("下单出错，指数设置表不存在");
            return ServerResponse.createByErrorMsg("下单失败，系统设置错误");
        }
        StockIndex stockIndex = this.iStockIndexService.selectIndexById(indexId);
        if (stockIndex.getIndexGid().contains("us")){
            String am_begin = siteIndexSetting.getTransAmBeginUs();
            String am_end = siteIndexSetting.getTransAmEndUs();
            String pm_begin = siteIndexSetting.getTransPmBeginUs();
            String pm_end = siteIndexSetting.getTransPmEndUs();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("下单失败，不在交易时段内");
            }
        }else {
            String am_begin = siteIndexSetting.getTransAmBegin();
            String am_end = siteIndexSetting.getTransAmEnd();
            String pm_begin = siteIndexSetting.getTransPmBegin();
            String pm_end = siteIndexSetting.getTransPmEnd();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));
            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("下单失败，不在交易时段内");
            }
        }


        if (stockIndex == null) {
            log.error("下单出错，指数不存在");
            return ServerResponse.createByErrorMsg("指数不存在");
        }
        if (1 != stockIndex.getTransState().intValue()) {
            log.error("下单出错，指数未开启交易");
            return ServerResponse.createByErrorMsg("该指数不能交易");
        }

        //保证金= 指数保证金*数量/杠杆倍数
        BigDecimal all_deposit_amt = (new BigDecimal(stockIndex.getDepositAmt().intValue())).multiply(new BigDecimal(buyNum.intValue())).divide(new BigDecimal(lever)).setScale(4,2);

        if (user.getEnableIndexAmt().compareTo(all_deposit_amt) == -1) {
            log.error("用户 {} 余额不足", user.getId());
            return ServerResponse.createByErrorMsg("指数账户资金不足");
        }

        BigDecimal max_buy_amt = user.getEnableIndexAmt().multiply(siteIndexSetting.getBuyMaxPercent());
        if (max_buy_amt.compareTo(all_deposit_amt) == -1) {
            log.error("不能超过最大购买比例");
            return ServerResponse.createByErrorMsg("不能超过最大购买比例");
        }
        if (user.getUserAmt().compareTo(new BigDecimal("0")) == -1) {
            log.error("用户 {} 余额不足", user.getId());
            return ServerResponse.createByErrorMsg("失败，融资总资金小于0");
        }
//        if (user.getUserFutAmt().compareTo(new BigDecimal("0")) == -1) {
//            return ServerResponse.createByErrorMsg("失败，期货总资金小于0");
//        }


        MarketVO marketVO = this.iStockIndexService.querySingleIndex(stockIndex.getIndexGid());
        log.info("当前指数行情 = {}", JsonUtil.obj2StringPretty(marketVO));


        BigDecimal increaseRate = new BigDecimal(marketVO.getIncreaseRate());
        if (increaseRate.compareTo(new BigDecimal("0")) == 1) {

            if (siteIndexSetting.getRiseLimit().multiply(new BigDecimal("100"))
                    .compareTo(increaseRate) == -1 && buyType
                    .intValue() == 0) {
                return ServerResponse.createByErrorMsg("当前指数涨幅:" + increaseRate + ",不能买涨");
            }
        } else {

            increaseRate = increaseRate.negate();
            if (siteIndexSetting.getRiseLimit().multiply(new BigDecimal("100"))
                    .compareTo(increaseRate) == -1 && buyType
                    .intValue() == 1) {
                return ServerResponse.createByErrorMsg("当前指数跌幅:" + increaseRate + ",不能买跌");
            }
        }


        BigDecimal reckon_enable = user.getEnableIndexAmt().subtract(all_deposit_amt);
        user.setEnableIndexAmt(reckon_enable);
        int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateUserCount > 0) {
            log.info("【用户交易指数下单】修改用户金额成功");
        } else {
            log.error("用户交易指数下单】修改用户金额出错");
            throw new Exception("用户交易指数下单】修改用户金额出错");
        }


        UserIndexPosition userIndexPosition = new UserIndexPosition();
        if (profitTarget != null && profitTarget.compareTo(new BigDecimal("0")) > 0) {
            userIndexPosition.setProfitTargetPrice(profitTarget);
        }
        if (stopTarget != null && stopTarget.compareTo(new BigDecimal("0")) > 0) {
            userIndexPosition.setStopTargetPrice(stopTarget);
        }
        BigDecimal buyPrice = new BigDecimal("0");
            buyPrice = new BigDecimal(marketVO.getNowPrice());
        userIndexPosition.setPositionType(user.getAccountType());
        userIndexPosition.setPositionSn(KeyUtils.getUniqueKey());
        userIndexPosition.setUserId(user.getId());
        userIndexPosition.setRealName(user.getRealName());
        userIndexPosition.setAgentId(user.getAgentId());
        userIndexPosition.setIndexName(stockIndex.getIndexName());
        userIndexPosition.setIndexCode(stockIndex.getIndexCode());
        userIndexPosition.setIndexGid(stockIndex.getIndexGid());
        userIndexPosition.setBuyOrderTime(new Date());
        userIndexPosition.setBuyOrderPrice(buyPrice);
        userIndexPosition.setOrderDirection((buyType.intValue() == 0) ? "买涨" : "买跌");

        userIndexPosition.setOrderNum(buyNum);


        userIndexPosition.setIsLock(Integer.valueOf(0));

        userIndexPosition.setAllDepositAmt(all_deposit_amt);
        userIndexPosition.setOrderFee((new BigDecimal(stockIndex.getTransFee().intValue()))
                .multiply(new BigDecimal(buyNum.intValue())));
        userIndexPosition.setOrderStayDays(Integer.valueOf(0));
        userIndexPosition.setEachPoint(new BigDecimal(stockIndex.getEachPoint().intValue()));
        userIndexPosition.setProfitAndLose(new BigDecimal("0"));
        userIndexPosition.setAllProfitAndLose(new BigDecimal("0"));
        userIndexPosition.setOrderLever(lever);

        int insertPositionCount = this.userIndexPositionMapper.insert(userIndexPosition);
        if (insertPositionCount > 0) {
            log.info("【用户交易指数下单】保存持仓记录成功");
        } else {
            log.error("用户交易指数下单】保存持仓记录出错");
            throw new Exception("用户交易指数下单】保存持仓记录出错");
        }

        return ServerResponse.createBySuccess("下单成功");
    }
    /**
     * 指数交易止盈止损平仓
     *
     *
     * @return
     */
    @Override
    public ServerResponse indexPositiontask(){
        List<Integer> userIdList = this.userIndexPositionMapper.findDistinctUserIdList();
        log.info("当前有指数持仓单的用户数量 为 {}", Integer.valueOf(userIdList.size()));
        SiteIndexSetting SiteIndexSetting = this.iSiteIndexSettingService.getSiteIndexSetting();
        BigDecimal buyMaxPercent = SiteIndexSetting.getBuyMaxPercent();
        BigDecimal forceSellPercent = SiteIndexSetting.getForceSellPercent();
//        BigDecimal force_stop_percent = SiteIndexSetting.ge();
        for (int i = 0; i < userIdList.size(); i++) {
            log.info("=====================");
            Integer userId = (Integer)userIdList.get(i);
            User user = this.userMapper.selectByPrimaryKey(userId);
            if(user == null){
                continue;
            }
            List<UserIndexPosition> userindexPositions = this.iUserIndexPositionService.findIndexPositionByUserIdAndSellPriceIsNull(userId);
            log.info("用户id = {} 姓名 = {} 指数持仓中订单数： {}", new Object[] { userId, user.getRealName(), Integer.valueOf(userindexPositions.size()) });

            BigDecimal enable_user_amt = user.getEnableAmt();
            BigDecimal all_freez_amt = new BigDecimal("0");
            for (UserIndexPosition userIndexPosition : userindexPositions) {

//                BigDecimal profitAndLose = positionProfitVO.getProfitAndLose();
                MarketVO marketVO = this.iStockIndexService.querySingleIndex(userIndexPosition.getIndexGid());
                String nowPrice = marketVO.getNowPrice();
                if (nowPrice == null){
                    nowPrice = String.valueOf(0);
                }

                if(userIndexPosition.getProfitTargetPrice()!=null && userIndexPosition.getProfitTargetPrice().compareTo(new BigDecimal(nowPrice)) <= 0 ||userIndexPosition.getStopTargetPrice() != null && userIndexPosition.getStopTargetPrice().compareTo(new BigDecimal(nowPrice)) >= 0) {

                    try {
                        this.iUserIndexPositionService.sellIndex(userIndexPosition.getPositionSn(), 0);
                        User user1 = this.userMapper.selectByPrimaryKey(userId);
                        BigDecimal enable_user_amt1 = user1.getEnableAmt();
                        SiteTaskLog siteTaskLog = new SiteTaskLog();
                        siteTaskLog.setTaskType("指数止盈止损强平任务-指数持仓");
                        String accountType = (user.getAccountType().intValue() == 0) ? "正式用户" : "模拟用户";
                        String taskcnt = accountType + "-" + user.getRealName() + "被强平[达到目标盈亏] 用户id = " + user.getId() + ", 扣款可用资金 = " + enable_user_amt + "扣款后可用资金 = " + enable_user_amt1 + ",现价"+nowPrice+ ", 目标止盈价格:" + userIndexPosition.getProfitTargetPrice()+ ", 目标止损价格:" + userIndexPosition.getStopTargetPrice();
                        siteTaskLog.setTaskCnt(taskcnt);
                        String tasktarget = "此次强平订单号为：" + userIndexPosition.getPositionSn();
                        siteTaskLog.setTaskTarget(tasktarget);
                        siteTaskLog.setAddTime(new Date());
                        siteTaskLog.setIsSuccess(Integer.valueOf(0));
                        siteTaskLog.setErrorMsg("");
                        int insertTaskCount = this.siteTaskLogMapper.insert(siteTaskLog);
                        if (insertTaskCount > 0) {
                            log.info("指数止盈止损强平任务-指数持仓 task任务成功");
                        } else {
                            log.info("指数止盈止损强平任务-指数持仓 task任务保存失败");
                        }
                    } catch (Exception e) {
                        log.error("【指数止盈止损强平任务】强制平仓失败...");
                    }

                }

            }
            log.info("=========止盈止损定时任务============");
        }
        return null;
    }




    @Override
    public ServerResponse del(Integer positionId) {
        if (positionId == null) {
            return ServerResponse.createByErrorMsg("id不能为空");
        }
        UserIndexPosition position = this.userIndexPositionMapper.selectByPrimaryKey(positionId);

        if (position == null) {
            ServerResponse.createByErrorMsg("该持仓不存在");
        }

        int updateCount = this.userIndexPositionMapper.deleteByPrimaryKey(positionId);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("删除成功");
        }
        return ServerResponse.createByErrorMsg("删除失败");
    }

    @Transactional
    public ServerResponse sellIndex(String positionSn, int doType) throws Exception {
        log.info("【用户交易平仓指数】 positionSn = {} ， dotype = {}", positionSn, Integer.valueOf(doType));


        SiteIndexSetting siteIndexSetting = this.iSiteIndexSettingService.getSiteIndexSetting();
        if (siteIndexSetting == null) {
            log.error("平仓出错，网站指数设置表不存在");
            return ServerResponse.createByErrorMsg("下单失败，系统设置错误");
        }
        UserIndexPosition userIndexPosition = this.userIndexPositionMapper.selectIndexPositionBySn(positionSn);

        if (doType != 0) {
            if (userIndexPosition.getIndexGid().contains("us")){
                String am_begin = siteIndexSetting.getTransAmBeginUs();
                String am_end = siteIndexSetting.getTransAmEndUs();
                String pm_begin = siteIndexSetting.getTransPmBeginUs();
                String pm_end = siteIndexSetting.getTransPmEndUs();
                boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
                boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
                log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));
                if (!am_flag && !pm_flag) {
                    return ServerResponse.createByErrorMsg("平仓失败，不在交易时段内");
                }
            }else if(userIndexPosition.getIndexGid().contains("hk")){
                String am_begin = siteIndexSetting.getTransAmBeginhk();
                String am_end = siteIndexSetting.getTransAmEndhk();
                String pm_begin = siteIndexSetting.getTransPmBeginhk();
                String pm_end = siteIndexSetting.getTransPmEndhk();
                boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
                boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
                log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));
                if (!am_flag && !pm_flag) {
                    return ServerResponse.createByErrorMsg("下单失败，不在交易时段内");
                }
            }else {
                String am_begin = siteIndexSetting.getTransAmBegin();
                String am_end = siteIndexSetting.getTransAmEnd();
                String pm_begin = siteIndexSetting.getTransPmBegin();
                String pm_end = siteIndexSetting.getTransPmEnd();
                boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
                boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
                log.info("【指数】是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));
                if (!am_flag && !pm_flag) {
                    return ServerResponse.createByErrorMsg("平仓失败，不在交易时段内");
                }
            }
        }



        if (userIndexPosition == null) {
            return ServerResponse.createByErrorMsg("平仓失败，指数持仓不存在");
        }


        User user = this.userMapper.selectByPrimaryKey(userIndexPosition.getUserId());
        /*实名认证开关开启*/
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
            return ServerResponse.createByErrorMsg("平仓失败，用户已被锁定");
        }
        if(siteProduct.getHolidayDisplay()){
            return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
        }
        if (userIndexPosition.getSellOrderPrice() != null) {
            return ServerResponse.createByErrorMsg("平仓失败，此订单已平仓");
        }

        if (1 == userIndexPosition.getIsLock().intValue()) {
            return ServerResponse.createByErrorMsg("平仓失败 " + userIndexPosition.getLockMsg());
        }


        MarketVO marketVO = this.iStockIndexService.querySingleIndex(userIndexPosition.getIndexGid());
        log.info("当前指数行情 = {}", JsonUtil.obj2StringPretty(marketVO));

        BigDecimal increaseRate = new BigDecimal(marketVO.getIncreaseRate());
        if (increaseRate.compareTo(new BigDecimal("0")) == 1) {

            if (siteIndexSetting.getRiseLimit().multiply(new BigDecimal("100"))
                    .compareTo(increaseRate) != 1 && "买跌"
                    .equals(userIndexPosition.getOrderDirection())) {
                return ServerResponse.createByErrorMsg("当前指数涨幅:" + increaseRate + ",不能卖出");
            }
        } else {

            increaseRate = increaseRate.negate();
            if (siteIndexSetting.getRiseLimit().multiply(new BigDecimal("100"))
                    .compareTo(increaseRate) == -1 && "买涨"
                    .equals(userIndexPosition.getOrderDirection())) {
                return ServerResponse.createByErrorMsg("当前指数跌幅:" + increaseRate + ",不能卖出");
            }
        }
        BigDecimal buyPrice = new BigDecimal("0");
            buyPrice = new BigDecimal(marketVO.getNowPrice());
        userIndexPosition.setSellOrderPrice((buyPrice));
        userIndexPosition.setSellOrderTime(new Date());

        BigDecimal point_sub = userIndexPosition.getSellOrderPrice().subtract(userIndexPosition.getBuyOrderPrice());

        BigDecimal profit_and_lose = point_sub.multiply(userIndexPosition.getEachPoint()).multiply(new BigDecimal(userIndexPosition.getOrderNum().intValue()));
        if ("买跌".equals(userIndexPosition.getOrderDirection())) {
            profit_and_lose = profit_and_lose.negate();
        }

        userIndexPosition.setProfitAndLose(profit_and_lose);

        BigDecimal all_profit = profit_and_lose.subtract(userIndexPosition.getOrderFee());
        userIndexPosition.setAllProfitAndLose(all_profit);


        int updateIndexPositionCount = this.userIndexPositionMapper.updateByPrimaryKeySelective(userIndexPosition);
        if (updateIndexPositionCount > 0) {
            log.info("【用户平仓指数】修改浮动盈亏记录成功");
        } else {
            log.error("【用户平仓指数】修改浮动盈亏记录出错");
            throw new Exception("用户平仓指数】修改浮动盈亏记录出错");
        }


        BigDecimal before_user_amt = user.getUserIndexAmt();
        BigDecimal before_enable_amt = user.getEnableIndexAmt();
        log.info("用户平仓之前 的 总资金  = {} , 可用资金 = {}", before_user_amt, before_enable_amt);

        BigDecimal user_index_amt = before_user_amt.add(all_profit);

        BigDecimal enable_index_amt = before_enable_amt.add(all_profit).add(userIndexPosition.getAllDepositAmt());

        log.info("用户平仓后的总资金  = {} , 可用资金 = {}", user_index_amt, enable_index_amt);
        user.setUserIndexAmt(user_index_amt);
        user.setEnableIndexAmt(enable_index_amt);

        int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateUserCount > 0) {
            log.info("【用户平仓指数】修改用户金额成功");
        } else {
            log.error("【用户平仓指数】修改用户金额出错");
            throw new Exception("【用户平仓指数】修改用户金额出错");
        }

        UserCashDetail ucd = new UserCashDetail();
        ucd.setPositionId(userIndexPosition.getId());
        ucd.setAgentId(user.getAgentId());
        ucd.setAgentName(user.getAgentName());
        ucd.setUserId(user.getId());
        ucd.setUserName(user.getRealName());
        ucd.setDeType("指数盈亏");
        ucd.setDeAmt(all_profit);
        ucd.setDeSummary("卖出指数，" + userIndexPosition.getIndexName() + "/" + userIndexPosition
                .getIndexCode() + "，总盈亏：" + all_profit);


        ucd.setAddTime(new Date());
        ucd.setIsRead(Integer.valueOf(0));

        int insertSxfCount = this.userCashDetailMapper.insert(ucd);
        if (insertSxfCount > 0) {
            log.info("【用户平仓指数】保存明细记录成功");
        } else {
            log.error("【用户平仓指数】保存明细记录出错");
            throw new Exception("【用户平仓指数】保存明细记录出错");
        }

        return ServerResponse.createBySuccessMsg("平仓成功！");
    }


    public ServerResponse lock(Integer positionId, Integer state, String lockMsg) {
        if (positionId == null || state == null) {
            return ServerResponse.createByErrorMsg("参数不能为空");
        }


        UserIndexPosition userIndexPosition = this.userIndexPositionMapper.selectByPrimaryKey(positionId);
        if (userIndexPosition == null) {
            return ServerResponse.createByErrorMsg("持仓不存在");
        }

        if (userIndexPosition.getSellOrderPrice() != null) {
            return ServerResponse.createByErrorMsg("平仓单不能锁仓");
        }

        if (state.intValue() == 1 &&
                StringUtils.isBlank(lockMsg)) {
            return ServerResponse.createByErrorMsg("锁仓提示信息必填");
        }


        if (state.intValue() == 1) {
            userIndexPosition.setIsLock(Integer.valueOf(1));
            userIndexPosition.setLockMsg(lockMsg);
        } else {
            userIndexPosition.setIsLock(Integer.valueOf(0));
        }

        int updateCount = this.userIndexPositionMapper.updateByPrimaryKeySelective(userIndexPosition);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失败");
    }


    public ServerResponse listByAgent(Integer positionType, Integer state, Integer userId, Integer agentId, String positionSn, String beginTime, String endTime, HttpServletRequest request, int pageNum, int pageSize) {
        AgentUser currentAgent = this.iAgentUserService.getCurrentAgent(request);


        if (agentId != null) {
            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
            if (agentUser.getParentId() != currentAgent.getId()) {
                return ServerResponse.createByErrorMsg("不能查询非下级代理用户持仓");
            }
        }

        Integer searchId = null;
        if (agentId == null) {
            searchId = currentAgent.getId();
        } else {
            searchId = agentId;
        }


        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }

        PageHelper.startPage(pageNum, pageSize);

        List<UserIndexPosition> userIndexPositions = this.userIndexPositionMapper.listByAgent(positionType, state, userId, searchId, positionSn, begin_time, end_time);


        List<AgentIndexPositionVO> agentIndexPositionVOS = Lists.newArrayList();
        for (UserIndexPosition userIndexPosition : userIndexPositions) {
            AgentIndexPositionVO agentIndexPositionVO = assembleAgentIndexPositionVO(userIndexPosition);
            agentIndexPositionVOS.add(agentIndexPositionVO);
        }

        PageInfo pageInfo = new PageInfo(userIndexPositions);
        pageInfo.setList(agentIndexPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }

    private AgentIndexPositionVO assembleAgentIndexPositionVO(UserIndexPosition userIndexPosition) {
        AgentIndexPositionVO agentIndexPositionVO = new AgentIndexPositionVO();

        agentIndexPositionVO.setId(userIndexPosition.getId());
        agentIndexPositionVO.setPositionSn(userIndexPosition.getPositionSn());
        agentIndexPositionVO.setPositionType(userIndexPosition.getPositionType());
        agentIndexPositionVO.setUserId(userIndexPosition.getUserId());
        agentIndexPositionVO.setRealName(userIndexPosition.getRealName());
        agentIndexPositionVO.setAgentId(userIndexPosition.getAgentId());
        agentIndexPositionVO.setIndexName(userIndexPosition.getIndexName());
        agentIndexPositionVO.setIndexCode(userIndexPosition.getIndexCode());
        agentIndexPositionVO.setIndexGid(userIndexPosition.getIndexGid());
        agentIndexPositionVO.setBuyOrderTime(userIndexPosition.getBuyOrderTime());
        agentIndexPositionVO.setBuyOrderPrice(userIndexPosition.getBuyOrderPrice());

        agentIndexPositionVO.setSellOrderTime(userIndexPosition.getSellOrderTime());
        agentIndexPositionVO.setSellOrderPrice(userIndexPosition.getSellOrderPrice());
        agentIndexPositionVO.setOrderDirection(userIndexPosition.getOrderDirection());
        agentIndexPositionVO.setOrderNum(userIndexPosition.getOrderNum());
        agentIndexPositionVO.setAllDepositAmt(userIndexPosition.getAllDepositAmt());
        agentIndexPositionVO.setOrderFee(userIndexPosition.getOrderFee());
        agentIndexPositionVO.setOrderStayDays(userIndexPosition.getOrderNum());
        agentIndexPositionVO.setEachPoint(userIndexPosition.getEachPoint());
        agentIndexPositionVO.setIsLock(userIndexPosition.getIsLock());
        agentIndexPositionVO.setLockMsg(userIndexPosition.getLockMsg());


        IndexPositionProfitVO indexPositionProfitVO = getIndexPositionProfitVO(userIndexPosition);
        agentIndexPositionVO.setProfitAndLose(indexPositionProfitVO.getProfitAndLose());
        agentIndexPositionVO.setAllProfitAndLose(indexPositionProfitVO.getAllProfitAndLose());
        agentIndexPositionVO.setNow_price(indexPositionProfitVO.getNowPrice());

        return agentIndexPositionVO;
    }


    public ServerResponse listByAdmin(Integer agentId, Integer positionType, Integer state, Integer userId, String positionSn, String beginTime, String endTime, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);


        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }


        List<UserIndexPosition> userIndexPositions = this.userIndexPositionMapper.listByAdmin(positionType, state, userId, agentId, positionSn, begin_time, end_time);


        List<AdminIndexPositionVO> adminIndexPositionVOS = Lists.newArrayList();
        for (UserIndexPosition userIndexPosition : userIndexPositions) {
            AdminIndexPositionVO adminIndexPositionVO = assembleAdminIndexPositionVO(userIndexPosition);
            adminIndexPositionVOS.add(adminIndexPositionVO);
        }

        PageInfo pageInfo = new PageInfo(userIndexPositions);
        pageInfo.setList(adminIndexPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }


    public ServerResponse findMyIndexPositionByNameAndCode(String indexName, String indexCode, Integer state, HttpServletRequest request, int pageNum, int pageSize) {
        User user = this.iUserService.getCurrentUser(request);

        PageHelper.startPage(pageNum, pageSize);


        List<UserIndexPosition> userIndexPositions = this.userIndexPositionMapper.findMyIndexPositionByNameAndCode(user.getId(), indexName, indexCode, state);

        List<UserIndexPositionVO> userIndexPositionVOS = Lists.newArrayList();
        if (userIndexPositions.size() > 0) {
            for (UserIndexPosition userIndexPosition : userIndexPositions) {
                UserIndexPositionVO userIndexPositionVO = assembleUserIndexPositionVO(userIndexPosition);
                userIndexPositionVOS.add(userIndexPositionVO);
            }
        }
        PageInfo pageInfo = new PageInfo(userIndexPositions);
        pageInfo.setList(userIndexPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }

    /*根据指数代码查询用户最早入仓股票*/
    public ServerResponse findUserIndexPositionByCode(HttpServletRequest request, String indexGid) {
        User user = this.iUserService.getCurrentRefreshUser(request);
        UserIndexPosition position = this.userIndexPositionMapper.findUserIndexPositionByCode(user.getId(), indexGid);

        List<UserIndexPositionVO> userPositionVOS = Lists.newArrayList();
        UserIndexPositionVO userPositionVO = null;
        if(position != null){
            userPositionVO = assembleUserIndexPositionVO(position);
        }
        userPositionVOS.add(userPositionVO);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setList(userPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }

    private UserIndexPositionVO assembleUserIndexPositionVO(UserIndexPosition userIndexPosition) {
        UserIndexPositionVO userIndexPositionVO = new UserIndexPositionVO();

        userIndexPositionVO.setId(userIndexPosition.getId());
        userIndexPositionVO.setPositionSn(userIndexPosition.getPositionSn());
        userIndexPositionVO.setPositionType(userIndexPosition.getPositionType());
        userIndexPositionVO.setUserId(userIndexPosition.getUserId());
        userIndexPositionVO.setRealName(userIndexPosition.getRealName());
        userIndexPositionVO.setAgentId(userIndexPosition.getAgentId());
        userIndexPositionVO.setIndexName(userIndexPosition.getIndexName());
        userIndexPositionVO.setIndexCode(userIndexPosition.getIndexCode());
        userIndexPositionVO.setIndexGid(userIndexPosition.getIndexGid());
        userIndexPositionVO.setBuyOrderTime(userIndexPosition.getBuyOrderTime());
        userIndexPositionVO.setBuyOrderPrice(userIndexPosition.getBuyOrderPrice());
        userIndexPositionVO.setSellOrderTime(userIndexPosition.getSellOrderTime());
        userIndexPositionVO.setSellOrderPrice(userIndexPosition.getSellOrderPrice());
        userIndexPositionVO.setOrderDirection(userIndexPosition.getOrderDirection());
        userIndexPositionVO.setOrderNum(userIndexPosition.getOrderNum());
        userIndexPositionVO.setAllDepositAmt(userIndexPosition.getAllDepositAmt());
        userIndexPositionVO.setOrderFee(userIndexPosition.getOrderFee());
        userIndexPositionVO.setOrderStayDays(userIndexPosition.getOrderNum());
        userIndexPositionVO.setEachPoint(userIndexPosition.getEachPoint());
        userIndexPositionVO.setIsLock(userIndexPosition.getIsLock());
        userIndexPositionVO.setLockMsg(userIndexPosition.getLockMsg());


        IndexPositionProfitVO indexPositionProfitVO = getIndexPositionProfitVO(userIndexPosition);
        userIndexPositionVO.setProfitAndLose(indexPositionProfitVO.getProfitAndLose());
        userIndexPositionVO.setAllProfitAndLose(indexPositionProfitVO.getAllProfitAndLose());
        userIndexPositionVO.setNow_price(indexPositionProfitVO.getNowPrice());

        return userIndexPositionVO;
    }


    public IndexPositionVO findUserIndexPositionAllProfitAndLose(Integer userId) {
        List<UserIndexPosition> userIndexPositions = this.userIndexPositionMapper.findPositionByUserIdAndSellPriceIsNull(userId);

        BigDecimal allProfitAndLose = new BigDecimal("0");
        BigDecimal allFreezAmt = new BigDecimal("0");

        for (UserIndexPosition userIndexPosition : userIndexPositions) {


            StockDTO stockDTO = byStockApi.getStockIndex(userIndexPosition.getIndexGid());
            log.info("IndexPositionVO 当前指数行情 = {}", JsonUtil.obj2StringPretty(stockDTO));
            BigDecimal nowPrice  = stockDTO.getP();

            if (nowPrice.compareTo(new BigDecimal("0")) != 0) {

                BigDecimal buyPrice = userIndexPosition.getBuyOrderPrice();
                BigDecimal subPrice = nowPrice.subtract(buyPrice);

                BigDecimal profit_and_lose = subPrice.multiply(userIndexPosition.getEachPoint()).multiply(new BigDecimal(userIndexPosition.getOrderNum().intValue()));
                if ("买跌".equals(userIndexPosition.getOrderDirection())) {
                    profit_and_lose = profit_and_lose.negate();
                }

                log.info("持仓指数 {} ,现价点数 = {} ,买入点数 = {} 差价 = {} 盈亏 = {}", new Object[]{userIndexPosition
                        .getIndexName(), nowPrice, buyPrice, subPrice, profit_and_lose});


                BigDecimal position_profit = profit_and_lose.subtract(userIndexPosition.getOrderFee());


                allProfitAndLose = allProfitAndLose.add(position_profit);


                BigDecimal position_freez = userIndexPosition.getAllDepositAmt();
                allFreezAmt = allFreezAmt.add(position_freez);
                continue;
            }
            log.info("查询所有持仓单的总盈亏，现价返回0，当前为集合竞价");
        }


        IndexPositionVO indexPositionVO = new IndexPositionVO();
        indexPositionVO.setAllIndexProfitAndLose(allProfitAndLose);
        indexPositionVO.setAllIndexFreezAmt(allFreezAmt);

        return indexPositionVO;
    }


    public List<Integer> findDistinctUserIdList() {
        return this.userIndexPositionMapper.findDistinctUserIdList();
    }


    public List<UserIndexPosition> findIndexPositionByUserIdAndSellPriceIsNull(Integer userId) {
        return this.userIndexPositionMapper.findPositionByUserIdAndSellPriceIsNull(userId);
    }


    public ServerResponse getIndexIncome(Integer agentId, Integer positionType, String beginTime, String endTime) {
        if (StringUtils.isBlank(beginTime) || StringUtils.isBlank(endTime)) {
            return ServerResponse.createByErrorMsg("时间不能为空");
        }
        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }


        List<UserIndexPosition> userIndexPositions = this.userIndexPositionMapper.listByAdmin(positionType, Integer.valueOf(1), null, agentId, null, begin_time, end_time);


        BigDecimal order_fee_amt = new BigDecimal("0");
        BigDecimal order_profit_and_lose = new BigDecimal("0");
        BigDecimal order_profit_and_lose_all = new BigDecimal("0");

        for (UserIndexPosition userIndexPosition : userIndexPositions) {
            order_fee_amt = order_fee_amt.add(userIndexPosition.getOrderFee());

            order_profit_and_lose = order_profit_and_lose.add(userIndexPosition.getProfitAndLose());

            order_profit_and_lose_all = order_profit_and_lose_all.add(userIndexPosition.getAllProfitAndLose());
        }

        AgentIncomeVO agentIncomeVO = new AgentIncomeVO();
        agentIncomeVO.setOrderSize(Integer.valueOf(userIndexPositions.size()));
        agentIncomeVO.setOrderFeeAmt(order_fee_amt);
        agentIncomeVO.setOrderProfitAndLose(order_profit_and_lose);
        agentIncomeVO.setOrderAllAmt(order_profit_and_lose_all);

        return ServerResponse.createBySuccess(agentIncomeVO);
    }

    private AdminIndexPositionVO assembleAdminIndexPositionVO(UserIndexPosition userIndexPosition) {
        AdminIndexPositionVO adminIndexPositionVO = new AdminIndexPositionVO();

        adminIndexPositionVO.setId(userIndexPosition.getId());
        adminIndexPositionVO.setPositionSn(userIndexPosition.getPositionSn());
        adminIndexPositionVO.setPositionType(userIndexPosition.getPositionType());
        adminIndexPositionVO.setUserId(userIndexPosition.getUserId());
        adminIndexPositionVO.setRealName(userIndexPosition.getRealName());
        adminIndexPositionVO.setAgentId(userIndexPosition.getAgentId());
        adminIndexPositionVO.setIndexName(userIndexPosition.getIndexName());
        adminIndexPositionVO.setIndexCode(userIndexPosition.getIndexCode());
        adminIndexPositionVO.setIndexGid(userIndexPosition.getIndexGid());
        adminIndexPositionVO.setBuyOrderTime(userIndexPosition.getBuyOrderTime());
        adminIndexPositionVO.setBuyOrderPrice(userIndexPosition.getBuyOrderPrice());

        adminIndexPositionVO.setSellOrderTime(userIndexPosition.getSellOrderTime());
        adminIndexPositionVO.setSellOrderPrice(userIndexPosition.getSellOrderPrice());
        adminIndexPositionVO.setOrderDirection(userIndexPosition.getOrderDirection());
        adminIndexPositionVO.setOrderNum(userIndexPosition.getOrderNum());
        adminIndexPositionVO.setAllDepositAmt(userIndexPosition.getAllDepositAmt());
        adminIndexPositionVO.setOrderFee(userIndexPosition.getOrderFee());
        adminIndexPositionVO.setOrderStayDays(userIndexPosition.getOrderNum());
        adminIndexPositionVO.setEachPoint(userIndexPosition.getEachPoint());
        adminIndexPositionVO.setIsLock(userIndexPosition.getIsLock());
        adminIndexPositionVO.setLockMsg(userIndexPosition.getLockMsg());


        IndexPositionProfitVO indexPositionProfitVO = getIndexPositionProfitVO(userIndexPosition);
        adminIndexPositionVO.setProfitAndLose(indexPositionProfitVO.getProfitAndLose());
        adminIndexPositionVO.setAllProfitAndLose(indexPositionProfitVO.getAllProfitAndLose());
        adminIndexPositionVO.setNow_price(indexPositionProfitVO.getNowPrice());

        return adminIndexPositionVO;
    }


    private IndexPositionProfitVO getIndexPositionProfitVO(UserIndexPosition userIndexPosition) {
        BigDecimal profitAndLose = new BigDecimal("0");
        BigDecimal allProfitAndLose = new BigDecimal("0");
        String nowPrice = "";

        if (userIndexPosition.getSellOrderPrice() != null) {


            BigDecimal subPrice = userIndexPosition.getSellOrderPrice().subtract(userIndexPosition.getBuyOrderPrice());

            profitAndLose = subPrice.multiply(userIndexPosition.getEachPoint()).multiply(new BigDecimal(userIndexPosition.getOrderNum().intValue()));
            if ("买跌".equals(userIndexPosition.getOrderDirection())) {
                profitAndLose = profitAndLose.negate();
            }

            allProfitAndLose = profitAndLose.subtract(userIndexPosition.getOrderFee());
        } else {

            MarketVO marketVO = this.iStockIndexService.querySingleIndex(userIndexPosition.getIndexGid());


            nowPrice = marketVO.getNowPrice();


            BigDecimal subPrice = (new BigDecimal(nowPrice)).subtract(userIndexPosition.getBuyOrderPrice());

            profitAndLose = subPrice.multiply(userIndexPosition.getEachPoint()).multiply(new BigDecimal(userIndexPosition.getOrderNum().intValue()));
            if ("买跌".equals(userIndexPosition.getOrderDirection())) {
                profitAndLose = profitAndLose.negate();
            }

            allProfitAndLose = profitAndLose.subtract(userIndexPosition.getOrderFee());
        }
        IndexPositionProfitVO indexPositionProfitVO = new IndexPositionProfitVO();
        indexPositionProfitVO.setProfitAndLose(profitAndLose);
        indexPositionProfitVO.setAllProfitAndLose(allProfitAndLose);
        indexPositionProfitVO.setNowPrice(nowPrice);

        return indexPositionProfitVO;
    }
}
