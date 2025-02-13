package com.nq.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nq.dao.*;
import com.nq.pojo.*;
import com.nq.service.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.nq.common.ServerResponse;
import com.nq.utils.*;
import com.nq.utils.redis.RedisShardedPoolUtils;
import com.nq.utils.stock.BuyAndSellUtils;
import com.nq.utils.stock.GeneratePosition;
import com.nq.utils.stock.GetStayDays;
import com.nq.utils.stock.biying.ByStockApi;
import com.nq.utils.stock.pinyin.GetPyByChinese;
import com.nq.utils.stock.sina.SinaStockApi;
import com.nq.utils.stock.wangji.WjStockApi;
import com.nq.utils.stock.wangji.vo.WjStockExtInfoDTO;
import com.nq.vo.agent.AgentIncomeVO;
import com.nq.vo.foreigncurrency.ExchangeVO;
import com.nq.vo.position.AdminPositionVO;
import com.nq.vo.position.AgentPositionVO;
import com.nq.vo.position.PositionProfitVO;
import com.nq.vo.position.PositionVO;
import com.nq.vo.position.UserPositionVO;
import com.nq.vo.stock.StockDTO;
import com.nq.vo.stock.StockListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static com.nq.utils.DateTimeUtil.getCurrentTimeMiao;
import static com.nq.utils.DateTimeUtil.getCurrentTimeMiaoZero;
import static com.nq.utils.DateTimeUtil.isCanSell;

@Service("iUserPositionService")
public class UserPositionServiceImpl implements IUserPositionService {

    private static final Logger log = LoggerFactory.getLogger(UserPositionServiceImpl.class);

    @Autowired
    UserPositionMapper userPositionMapper;

    @Autowired
    IUserService iUserService;

    @Autowired
    ISiteSettingService iSiteSettingService;

    @Autowired
    ISiteSpreadService iSiteSpreadService;

    @Autowired
    IStockService iStockService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserCashDetailMapper userCashDetailMapper;
    @Autowired
    IAgentUserService iAgentUserService;
    @Autowired
    AgentUserMapper agentUserMapper;
    @Autowired
    SiteTaskLogMapper siteTaskLogMapper;
    @Autowired
    StockMapper stockMapper;
    @Autowired
    AgentAgencyFeeMapper agentAgencyFeeMapper;
    @Autowired
    IAgentAgencyFeeService iAgentAgencyFeeService;
    @Autowired
    ISiteProductService iSiteProductService;

    @Autowired
    FundsApplyMapper fundsApplyMapper;
    @Autowired
    UserStockSubscribeMapper userStockSubscribeMapper;
    @Autowired
    StockSubscribeMapper stockSubscribeMapper;
    @Autowired
    UserIndexPositionMapper userIndexPositionMapper;


    @Autowired
    IStockFuturesService iStockFuturesService;
    @Autowired
    IStockCoinService iStockCoinService;
    @Autowired
    CurrencyUtils currencyUtils;
    @Autowired
    StockDzMapper stockDzMapper;
    @Autowired
    StockMarketsDayMapper stockMarketsDayMapper;

    @Autowired
    private ByStockApi byStockApi;

    @Autowired
    private WjStockApi wjStockApi;

    @Autowired
    private UserPostionDetailMapper postionDetailMapper;


    @Transactional
    public ServerResponse buy(Integer stockId, Integer buyNum, Integer buyType, Integer lever,BigDecimal profitTarget,BigDecimal stopTarget, HttpServletRequest request) throws Exception {

        // 判断周末不能买
        Date today = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        /*int weekday = c.get(Calendar.DAY_OF_WEEK);
        if (weekday == 1) {
            return ServerResponse.createByErrorMsg("周末不能购买！");
        }
        if (weekday == 7) {
            return ServerResponse.createByErrorMsg("周末不能购买！");
        }*/

        User user = this.iUserService.getCurrentRefreshUser(request);

        Stock stock = null;
        ServerResponse stock_res = this.iStockService.findStockById(stockId);
        if (!stock_res.isSuccess()) {
            return ServerResponse.createByErrorMsg("下单失败，股票代码错误");
        }
        stock = (Stock) stock_res.getData();

        UserPosition existPos = userPositionMapper.findPositionByStockCode(user.getId(), stock.getStockCode());

        if (existPos != null) {
            return buy2(existPos.getPositionSn(), buyNum, request);
        }

        /*实名认证开关开启*/
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        if (siteProduct.getRealNameDisplay() && (StringUtils.isBlank(user.getRealName()) || StringUtils.isBlank(user.getIdCard()))) {
            return ServerResponse.createByErrorMsg("下单失败，请先实名认证");
        }
        BigDecimal user_enable_amt = user.getEnableAmt();
        log.info("用户 {} 下单，股票id = {} ，数量 = {} , 方向 = {} , 杠杆 = {}", new Object[]{user
                .getId(), stockId, buyNum, buyType, lever});
        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
            return ServerResponse.createByErrorMsg("下单失败，账户已被锁定");
        }


        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("下单出错，网站设置表不存在");
            return ServerResponse.createByErrorMsg("下单失败，系统设置错误");
        }

        if (Objects.equals(stock.getStockType(), "us")) {
            String am_begin = siteSetting.getTransAmBeginUs();
            String am_end = siteSetting.getTransAmEndUs();
            String pm_begin = siteSetting.getTransPmBeginUs();
            String pm_end = siteSetting.getTransPmEndUs();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("下单失败，不在美股交易时段内");
            }

        }else if(Objects.equals(stock.getStockType(), "hk")){
            String am_begin = siteSetting.getTransAmBeginhk();
            String am_end = siteSetting.getTransAmEndhk();
            String pm_begin = siteSetting.getTransPmBeginhk();
            String pm_end = siteSetting.getTransPmEndhk();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("下单失败，不在港股股交易时段内");
            }
            if (siteProduct.getHolidayDisplay()) {
                return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
            }
        }else {
            String am_begin = siteSetting.getTransAmBegin();
            String am_end = siteSetting.getTransAmEnd();
            String pm_begin = siteSetting.getTransPmBegin();
            String pm_end = siteSetting.getTransPmEnd();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("下单失败，不在交易时段内");
            }
            if (siteProduct.getHolidayDisplay()) {
                return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
            }
        }



        if (stock.getIsLock().intValue() != 0) {
            return ServerResponse.createByErrorMsg("下单失败，当前股票不能交易");
        }

        List dbPosition = findPositionByStockCodeAndTimes(siteSetting.getBuySameTimes().intValue(), stock
                .getStockCode(), user.getId());
        if (dbPosition.size() >= siteSetting.getBuySameNums().intValue()) {
            return ServerResponse.createByErrorMsg("频繁交易," + siteSetting.getBuySameTimes() + "分钟内同一股票持仓不得超过" + siteSetting
                    .getBuySameNums() + "条");
        }

        Integer transNum = findPositionNumByTimes(siteSetting.getBuyNumTimes().intValue(), user.getId());
        if (transNum.intValue() / 100 >= siteSetting.getBuyNumLots().intValue()) {
            return ServerResponse.createByErrorMsg("频繁交易," + siteSetting
                    .getBuyNumTimes() + "分钟内不能超过" + siteSetting.getBuyNumLots() + "手");
        }

        if (buyNum.intValue() < siteSetting.getBuyMinNum().intValue()) {
            return ServerResponse.createByErrorMsg("下单失败，购买数量小于" + siteSetting
                    .getBuyMinNum() + "股");
        }
        if (buyNum.intValue() > siteSetting.getBuyMaxNum().intValue()) {
            return ServerResponse.createByErrorMsg("下单失败，购买数量大于" + siteSetting
                    .getBuyMaxNum() + "股");
        }
        BigDecimal now_price;
        StockListVO stockListVO = new StockListVO();
        StockCoin stockCoin = new StockCoin();
        //股票类型 现价 数据源的处理

        stockListVO = wjStockApi.getStockListVo(stock);
//        stockListVO = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(stock.getStockGid()));
        now_price = new BigDecimal(stockListVO.getNowPrice());
//        if (stock.getDataBase()!=0){
//             String date = getCurrentTimeMiaoZero();
//            String result = HttpClientRequest.doGet(PropertiesUtil.getProperty("changePrice.url")+"?cat_time="+date+"&stock_gid="+stock.getStockGid()+"&price="+stockListVO.getNowPrice());
//            JSONObject jsonObject = JSONObject.fromObject(result);
//            String nowPrice = jsonObject.getJSONObject("data").getString("new_price");
//            if (nowPrice!=null){
//                if ("us".equals(stock.getStockType())){
//                    ExchangeVO exchangeVO = this.iStockFuturesService.queryExchangeVO("USD").getData();
//                    now_price =new BigDecimal(nowPrice).multiply(new BigDecimal(exchangeVO.getNowPrice()));
//                } else if ("hk".equals(stock.getStockType())){
//                    ExchangeVO exchangeVO = this.iStockFuturesService.queryExchangeVO("HKD").getData();
//                    now_price =new BigDecimal(nowPrice).multiply(new BigDecimal(exchangeVO.getNowPrice()));
//                } else {
//                    now_price = new BigDecimal(nowPrice);
//                }
//
//            }else {
//                stockListVO.setNowPrice(stockListVO.getNowPrice());
//                if ("us".equals(stock.getStockType())){
//                    ExchangeVO exchangeVO = this.iStockFuturesService.queryExchangeVO("USD").getData();
//                    now_price =new BigDecimal(nowPrice).multiply(new BigDecimal(exchangeVO.getNowPrice()));
//                } else if ("hk".equals(stock.getStockType())){
//                    ExchangeVO exchangeVO = this.iStockFuturesService.queryExchangeVO("HKD").getData();
//                    now_price =new BigDecimal(nowPrice).multiply(new BigDecimal(exchangeVO.getNowPrice()));
//                } else {
//                    now_price = new BigDecimal(nowPrice);
//                }
//            }
//        }
//            String stockOther = RedisShardedPoolUtils.get(stock.getStockName(), 8);





        if (now_price.compareTo(new BigDecimal("0")) == 0) {
            return ServerResponse.createByErrorMsg("报价0，请稍后再试");
        }


        double stock_crease = stockListVO.getHcrate().doubleValue();


        BigDecimal maxRisePercent = new BigDecimal("0");
        if (stock.getStockPlate() != null) {

            maxRisePercent = new BigDecimal("0.2");
            log.info("【科创股票】");
        } else {
            maxRisePercent = new BigDecimal("0.1");
            log.info("【普通A股】");
        }

        if (stockListVO.getName().startsWith("ST") || stockListVO.getName().endsWith("退")) {
            return ServerResponse.createByErrorMsg("ST和已退市的股票不能入仓");
        }

        BigDecimal zsPrice = stockListVO.getYc();

        BigDecimal ztPrice = zsPrice.multiply(maxRisePercent).add(zsPrice);
        ztPrice = ztPrice.setScale(2, 4);
        BigDecimal chaPrice = ztPrice.subtract(zsPrice);

        BigDecimal ztRate = chaPrice.multiply(new BigDecimal("100")).divide(zsPrice, 2, 4);

        log.info("当前涨跌幅 = {} % , 涨停幅度 = {} %", Double.valueOf(stock_crease), ztRate);
        String nowDate = DateTimeUtil.stampToDate(String.valueOf(System.currentTimeMillis()));
        StockSubscribe stockSubscribeListQc = this.stockSubscribeMapper.selectOne(new QueryWrapper<StockSubscribe>().eq("code",stock.getStockCode()).eq("list_date",nowDate));
        if(stockSubscribeListQc == null) {
            if ((new BigDecimal(String.valueOf(stock_crease))).compareTo(ztRate) == 0 && buyType
                    .intValue() == 0) {
                return ServerResponse.createByErrorMsg("当前股票已涨停不能买涨");
            }

        }
        if (stock.getStockPlate() == null || StringUtils.isEmpty(stock.getStockPlate())) {

            int maxcrease = siteSetting.getCreaseMaxPercent().intValue();
            if (stock_crease > 0.0D &&
                    stock_crease >= maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，股票当前涨幅:" + stock_crease + ",大于最大涨幅:" + maxcrease);
            }


            if (stock_crease < 0.0D &&
                    -stock_crease > maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，股票当前跌幅:" + stock_crease + ",大于最大跌幅:" + maxcrease);

            }

        } else if ("创业".equals(stock.getStockPlate())) {

            int maxcrease = siteSetting.getCyCreaseMaxPercent().intValue();
            if (stock_crease > 0.0D &&
                    stock_crease >= maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，创业股当前涨幅:" + stock_crease + ",大于最大涨幅:" + maxcrease);
            }


            if (stock_crease < 0.0D &&
                    -stock_crease > maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，创业股当前跌幅:" + stock_crease + ",大于最大跌幅:" + maxcrease);
            }
        } else {

            int maxcrease = siteSetting.getKcCreaseMaxPercent().intValue();
            if (stock_crease > 0.0D &&
                    stock_crease >= maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，科创股当前涨幅:" + stock_crease + ",大于最大涨幅:" + maxcrease);
            }


            if (stock_crease < 0.0D &&
                    -stock_crease > maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，科创股当前跌幅:" + stock_crease + ",大于最大跌幅:" + maxcrease);
            }
        }


        ServerResponse serverResponse = this.iStockService.selectRateByDaysAndStockCode(stock
                .getStockCode(), siteSetting.getStockDays().intValue());
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        BigDecimal daysRate = (BigDecimal) serverResponse.getData();
        log.info("股票 {} ， {} 天内 涨幅 {} , 设置的涨幅 = {}", new Object[]{stock.getStockCode(), siteSetting
                .getStockDays(), daysRate, siteSetting.getStockRate()});

        if (daysRate != null &&
                siteSetting.getStockRate().compareTo(daysRate) == -1) {
            return serverResponse.createByErrorMsg(siteSetting.getStockDays() + "天内涨幅超过 " + siteSetting
                    .getStockRate() + "不能交易");
        }


        //BigDecimal buy_amt = now_price.multiply(new BigDecimal(buyNum.intValue())).divide(new BigDecimal(lever.intValue())).setScale(2, 4);
        BigDecimal buy_amt = now_price.multiply(new BigDecimal(buyNum.intValue()));


        //BigDecimal buy_amt_autual = now_price.multiply(new BigDecimal(buyNum.intValue())).divide(new BigDecimal(lever.intValue()), 2, 4);
        BigDecimal buy_amt_autual = buy_amt.divide(new BigDecimal(lever.intValue()), 2, 4);


        int compareInt = buy_amt_autual.compareTo(new BigDecimal(siteSetting.getBuyMinAmt().intValue()));
        if (compareInt == -1) {
            return ServerResponse.createByErrorMsg("下单失败，购买金额小于" + siteSetting
                    .getBuyMinAmt() + "元");
        }


        BigDecimal max_buy_amt = user_enable_amt.multiply(siteSetting.getBuyMaxAmtPercent());
        int compareCwInt = buy_amt_autual.compareTo(max_buy_amt);
        if (compareCwInt == 1) {
            return ServerResponse.createByErrorMsg("下单失败，不能超过可用资金的" + siteSetting
                    .getBuyMaxAmtPercent().multiply(new BigDecimal("100")) + "%");
        }


        int compareUserAmtInt = user_enable_amt.compareTo(buy_amt_autual);
        log.info("用户可用金额 = {}  实际购买金额 =  {}", user_enable_amt, buy_amt_autual);
        log.info("比较 用户金额 和 实际 购买金额 =  {}", Integer.valueOf(compareUserAmtInt));
        if (compareUserAmtInt == -1) {
            return ServerResponse.createByErrorMsg("下单失败，融资可用金额小于" + buy_amt_autual + "元");
        }

        if (user.getUserIndexAmt().compareTo(new BigDecimal("0")) == -1) {
            return ServerResponse.createByErrorMsg("失败，指数总资金小于0");
        }
//        if (user.getUserFutAmt().compareTo(new BigDecimal("0")) == -1) {
//            return ServerResponse.createByErrorMsg("失败，期货总资金小于0");
//        }
        UserPosition userPosition = new UserPosition();

        if (profitTarget != null && profitTarget.compareTo(new BigDecimal("0")) > 0) {
            userPosition.setProfitTargetPrice(profitTarget);
        }
        if (stopTarget != null && stopTarget.compareTo(new BigDecimal("0")) > 0) {
            userPosition.setStopTargetPrice(stopTarget);
        }


        userPosition.setPositionType(user.getAccountType());
        userPosition.setPositionSn(KeyUtils.getUniqueKey());
        userPosition.setUserId(user.getId());
        userPosition.setNickName(user.getRealName());
        userPosition.setAgentId(user.getAgentId());
        userPosition.setStockCode(stock.getStockCode());
        userPosition.setStockName(stock.getStockName());
        userPosition.setStockGid(stock.getStockGid());
        userPosition.setStockSpell(stock.getStockSpell());
        userPosition.setBuyOrderId(GeneratePosition.getPositionId());
        userPosition.setBuyOrderTime(new Date());
        userPosition.setBuyOrderPrice(now_price);
        userPosition.setOrderDirection((buyType.intValue() == 0) ? "买涨" : "买跌");

        userPosition.setOrderNum(buyNum);


        if (stock.getStockPlate() != null) {
            userPosition.setStockPlate(stock.getStockPlate());
        }


        userPosition.setIsLock(Integer.valueOf(0));


        userPosition.setOrderLever(lever);


        userPosition.setOrderTotalPrice(buy_amt);

        //递延费特殊处理
        BigDecimal stayFee = userPosition.getOrderTotalPrice().multiply(siteSetting.getStayFee());
        BigDecimal allStayFee = stayFee.multiply(new BigDecimal(1));
        userPosition.setOrderStayFee(allStayFee);
        userPosition.setOrderStayDays(1);


        BigDecimal buy_fee_amt = buy_amt.multiply(siteSetting.getBuyFee()).setScale(2, 4);
        log.info("用户购买手续费（配资后总资金 * 百分比） = {}", buy_fee_amt);
        userPosition.setOrderFee(buy_fee_amt);


        BigDecimal buy_yhs_amt = buy_amt.multiply(siteSetting.getDutyFee()).setScale(2, 4);
        log.info("用户购买印花税（配资后总资金 * 百分比） = {}", buy_yhs_amt);
        userPosition.setOrderSpread(buy_yhs_amt);

        SiteSpread siteSpread = iSiteSpreadService.findSpreadRateOne(new BigDecimal(stock_crease), buy_amt, stock.getStockCode(), now_price);
        BigDecimal spread_rate_amt = new BigDecimal("0");
        if (siteSpread != null) {
            spread_rate_amt = buy_amt.multiply(siteSpread.getSpreadRate()).setScale(2, 4);
            log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", siteSpread.getSpreadRate(), spread_rate_amt);
        } else {
            log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", "设置异常", spread_rate_amt);
        }

        userPosition.setSpreadRatePrice(spread_rate_amt);


        BigDecimal profit_and_lose = new BigDecimal("0");
        userPosition.setProfitAndLose(profit_and_lose);


        BigDecimal all_profit_and_lose = profit_and_lose.subtract(buy_fee_amt).subtract(buy_yhs_amt).subtract(spread_rate_amt);
        userPosition.setAllProfitAndLose(all_profit_and_lose);


        userPosition.setOrderStayDays(Integer.valueOf(0));
        userPosition.setOrderStayFee(new BigDecimal("0"));

        userPosition.setSellOrderFee(BigDecimal.ZERO);

        int insertPositionCount = 0;
        this.userPositionMapper.insert(userPosition);

        UserPostionDetail userPostionDetail = new UserPostionDetail();
        userPostionDetail.setPosId(userPosition.getId());
        userPostionDetail.setBuyOrderTime(userPosition.getBuyOrderTime());
        userPostionDetail.setOrderNum(userPosition.getOrderNum());
        userPostionDetail.setBuyOrderPrice(userPosition.getBuyOrderPrice());

        postionDetailMapper.insert(userPostionDetail);

        insertPositionCount = userPosition.getId();
        if (insertPositionCount > 0) {
            //修改用户可用余额= 当前余额-下单金额-买入手续费-印花税-点差费
            //BigDecimal reckon_enable = user_enable_amt.subtract(buy_amt_autual).subtract(buy_fee_amt).subtract(buy_yhs_amt).subtract(spread_rate_amt);
            //修改用户可用余额= 当前余额-下单总金额
            BigDecimal reckon_enable = user_enable_amt.subtract(buy_amt_autual);
            user.setEnableAmt(reckon_enable);
            int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateUserCount > 0) {
                log.info("【用户交易下单】修改用户金额成功");

                UserCashDetail ucd = new UserCashDetail();
                ucd.setPositionId(userPosition.getId());
                ucd.setAgentId(user.getAgentId());
                ucd.setAgentName(user.getAgentName());
                ucd.setUserId(user.getId());
                ucd.setUserName(user.getRealName());
                ucd.setDeType("买入股票");
                ucd.setDeAmt(buy_amt_autual);
//                ucd.setDeSummary(String.format("买入股票，%s/%s,保证金：%s,买入价格：%s，买入股数：%d，买入手续费：%s,印花税：%s",
//                        userPosition.getStockCode(), userPosition.getStockName(), BigDecimalUtil.format(buy_amt_autual),
//                        BigDecimalUtil.format(now_price), buyNum, BigDecimalUtil.format(buy_fee_amt),
//                        BigDecimalUtil.format(buy_yhs_amt)));
                ucd.setDeSummary(String.format("买入股票，%s/%s,保证金：%s,买入价格：%s，买入股数：%d，买入手续费：%s",
                        userPosition.getStockCode(), userPosition.getStockName(), BigDecimalUtil.format(buy_amt_autual),
                        BigDecimalUtil.format(now_price), buyNum, BigDecimalUtil.format(buy_fee_amt)));
                ucd.setAddTime(new Date());
                ucd.setIsRead(Integer.valueOf(0));

                int insertSxfCount = this.userCashDetailMapper.insert(ucd);


            } else {
                log.error("用户交易下单】修改用户金额出错");
                throw new Exception("用户交易下单】修改用户金额出错");
            }
            //核算代理收入-入仓手续费
            iAgentAgencyFeeService.AgencyFeeIncome(1, userPosition.getPositionSn());
            log.info("【用户交易下单】保存持仓记录成功");
        } else {
            log.error("用户交易下单】保存持仓记录出错");
            throw new Exception("用户交易下单】保存持仓记录出错");
        }

        return ServerResponse.createBySuccess("下单成功");
    }


    /**
     * 用户修改止盈止损
     *
     */
    @Override
    public ServerResponse updateProfitTarget (String positionSn, Integer profitTarget, Integer
            stopTarget, HttpServletRequest request){
        int update = 0;
        if (positionSn.contains("index")) {
            UserIndexPosition userIndexPosition = userIndexPositionMapper.selectIndexPositionBySn(positionSn.replace("index", ""));
            if (userIndexPosition == null) {
                return ServerResponse.createByErrorMsg("指数持仓单不存在");
            }
            if (profitTarget != null && profitTarget > 0) {
                userIndexPosition.setProfitTargetPrice(BigDecimal.valueOf(profitTarget));
            }
            if (stopTarget != null && stopTarget > 0) {
                userIndexPosition.setStopTargetPrice(BigDecimal.valueOf(stopTarget));
            }
            log.info("指数止盈线" + profitTarget + "-------指数止损线" + stopTarget);
            update = this.userIndexPositionMapper.updateByPrimaryKeySelective(userIndexPosition);
        } else {
            UserPosition userPosition = this.userPositionMapper.findPositionBySn(positionSn);

            if (userPosition == null) {
                return ServerResponse.createByErrorMsg("持仓记录不存在");
            }
            if (profitTarget != null && profitTarget > 0) {
                userPosition.setProfitTargetPrice(BigDecimal.valueOf(profitTarget));
            }
            if (stopTarget != null && stopTarget > 0) {
                userPosition.setStopTargetPrice(BigDecimal.valueOf(stopTarget));
            }
            log.info("止盈线" + profitTarget + "-------止损线" + stopTarget);
            update = this.userPositionMapper.updateByPrimaryKeySelective(userPosition);
        }
        if (update > 0) {
            return ServerResponse.createBySuccessMsg("修改成功");
        } else {
            return ServerResponse.createByErrorMsg("修改失败");
        }
    }



    public ServerResponse sell(String positionSn, int doType) throws Exception {
        log.info("【用户交易平仓】 positionSn = {} ， dotype = {}", positionSn, Integer.valueOf(doType));
        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("平仓出错，网站设置表不存在");
            return ServerResponse.createByErrorMsg("下单失败，系统设置错误");
        }
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        UserPosition userPosition = this.userPositionMapper.findPositionBySn(positionSn);
        if (doType != 0) {
            if (userPosition.getStockGid().contains("us")){
                String am_begin = siteSetting.getTransAmBeginUs();
                String am_end = siteSetting.getTransAmEndUs();
                String pm_begin = siteSetting.getTransPmBeginUs();
                String pm_end = siteSetting.getTransPmEndUs();
                boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
                boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
                log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

                if (!am_flag && !pm_flag) {
                    return ServerResponse.createByErrorMsg("平仓失败，不在交易时段内");
                }
            }else if(userPosition.getStockGid().contains("hk")){
                String am_begin = siteSetting.getTransAmBeginhk();
                String am_end = siteSetting.getTransAmEndhk();
                String pm_begin = siteSetting.getTransPmBeginhk();
                String pm_end = siteSetting.getTransPmEndhk();
                boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
                boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
                log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

                if (!am_flag && !pm_flag) {
                    return ServerResponse.createByErrorMsg("下单失败，不在港股股交易时段内");
                }
            }else {
                String am_begin = siteSetting.getTransAmBegin();
                String am_end = siteSetting.getTransAmEnd();
                String pm_begin = siteSetting.getTransPmBegin();
                String pm_end = siteSetting.getTransPmEnd();
                boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
                boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
                log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));
                if (!am_flag && !pm_flag) {
                    return ServerResponse.createByErrorMsg("平仓失败，不在交易时段内");
                }
            }
            if(siteProduct.getHolidayDisplay()){
                return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
            }
            if (!DateTimeUtil.isCanSell(userPosition.getBuyOrderTime(), siteSetting.getCantSellTimes().intValue())) {
//                return ServerResponse.createByErrorMsg(siteSetting.getCantSellTimes() + "分钟内不能卖出");
                return ServerResponse.createByErrorMsg("当天入仓的股票需要隔天才能出仓");
            }

        }

        if (userPosition == null) {
            return ServerResponse.createByErrorMsg("平仓失败，订单不存在");
        }

        User user = this.userMapper.selectByPrimaryKey(userPosition.getUserId());
        if (user == null){
            return ServerResponse.createByErrorMsg("平仓失败，用户不存在");
        }

        /*实名认证开关开启*/
        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
            return ServerResponse.createByErrorMsg("平仓失败，用户已被锁定");
        }

        if (userPosition.getSellOrderId() != null) {
            return ServerResponse.createByErrorMsg("平仓失败，此订单已平仓");
        }

        if (1 == userPosition.getIsLock().intValue()) {
            return ServerResponse.createByErrorMsg("平仓失败 " + userPosition.getLockMsg());
        }

        if (userPosition.getOrderNum() > getCanSellNum(userPosition)) {
            return ServerResponse.createByErrorMsg("当前可卖股数不足");
        }
//                if (DateTimeUtil.sameDate(DateTimeUtil.getCurrentDate(),userPosition.getBuyOrderTime())) {
//            return ServerResponse.createByErrorMsg("当天入仓的股票需要隔天才能出仓");
//       }
        BigDecimal now_price;
        StockListVO stockListVO = new StockListVO();
        StockCoin stockCoin = new StockCoin();
        Stock stock  = stockMapper.selectOne(new QueryWrapper<Stock>().eq("stock_gid",userPosition.getStockGid()));
        //股票卖出的 价格 数据源
        stockListVO = wjStockApi.getStockListVo(stock);
//        stockListVO = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(userPosition.getStockGid()));
        now_price = new BigDecimal(stockListVO.getNowPrice());

        if (stockListVO.getNowPrice() == null) {
            return ServerResponse.createByErrorMsg("平仓失败，获取股票信息失败");
        }
        if (now_price.compareTo(new BigDecimal("0")) != 1) {
            log.error("股票 = {} 收到报价 = {}", userPosition.getStockName(), now_price);
            return ServerResponse.createByErrorMsg("报价0，平仓失败，请稍后再试");
        }

        double stock_crease = stockListVO.getHcrate().doubleValue();

        BigDecimal zsPrice = stockListVO.getYc();

        BigDecimal ztPrice = zsPrice.multiply(new BigDecimal("0.1")).add(zsPrice);
        ztPrice = ztPrice.setScale(2, 4);
        BigDecimal chaPrice = ztPrice.subtract(zsPrice);

        BigDecimal ztRate = chaPrice.multiply(new BigDecimal("100")).divide(zsPrice, 2, 4);

        ztRate = ztRate.negate();
        log.info("股票当前涨跌幅 = {} 跌停幅度 = {}", Double.valueOf(stock_crease), ztRate);
        if ((new BigDecimal(String.valueOf(stock_crease))).compareTo(ztRate) == 0 && "买涨"
                .equals(userPosition.getOrderDirection())) {
            return ServerResponse.createByErrorMsg("当前股票已跌停不能卖出");
        }

        Integer buy_num = userPosition.getOrderNum();

        BigDecimal all_buy_amt = userPosition.getOrderTotalPrice();
        //BigDecimal all_sell_amt = now_price.multiply(new BigDecimal(buy_num.intValue())).divide(new BigDecimal(userPosition.getOrderLever())).setScale(2,4);
        BigDecimal all_sell_amt = now_price.multiply(new BigDecimal(buy_num.intValue()));

        BigDecimal profitLoss = new BigDecimal("0");
        if ("买涨".equals(userPosition.getOrderDirection())) {
            log.info("买卖方向：{}", "涨");

            profitLoss = all_sell_amt.subtract(all_buy_amt);
        } else {
            log.info("买卖方向：{}", "跌");
            profitLoss = all_buy_amt.subtract(all_sell_amt);
        }
        log.info("买入总金额 = {} , 卖出总金额 = {} , 盈亏 = {}", new Object[]{all_buy_amt, all_sell_amt, profitLoss});

        BigDecimal user_all_amt = user.getUserAmt();
        BigDecimal user_enable_amt = user.getEnableAmt();
        log.info("用户原本总资金 = {} , 可用 = {}", user_all_amt, user_enable_amt);

        BigDecimal buy_fee_amt = userPosition.getOrderFee();
        log.info("买入手续费 = {}", buy_fee_amt);

        BigDecimal orderSpread = userPosition.getOrderSpread();
        log.info("印花税 = {}", orderSpread);


        Integer days = GetStayDays.durationDays(userPosition.getBuyOrderTime());
        BigDecimal orderStayFee = this.getStayFee(userPosition);
        log.info("递延费2 = {}", orderStayFee);

        BigDecimal spreadRatePrice = userPosition.getSpreadRatePrice();
        log.info("点差费 = {}", spreadRatePrice);

        BigDecimal sell_fee_amt = all_sell_amt.multiply(siteSetting.getSellFee()).setScale(2, 4);
        log.info("卖出手续费 = {}", sell_fee_amt);


        //总手续费= 买入手续费+卖出手续费+印花税+递延费+点差费
//        BigDecimal all_fee_amt = buy_fee_amt.add(sell_fee_amt).add(orderSpread).add(orderStayFee).add(spreadRatePrice);
        BigDecimal all_fee_amt = buy_fee_amt.add(sell_fee_amt).add(orderSpread).add(spreadRatePrice);
        log.info("总的手续费费用 = {}", all_fee_amt);

        userPosition.setSellOrderId(GeneratePosition.getPositionId());
        userPosition.setSellOrderPrice(now_price);
        userPosition.setSellOrderTime(new Date());

        userPosition.setOrderStayDays(days);
        userPosition.setOrderStayFee(orderStayFee);

//        BigDecimal order_fee_all = buy_fee_amt.add(sell_fee_amt);
//        userPosition.setOrderFee(order_fee_all);

        userPosition.setProfitAndLose(profitLoss);

        BigDecimal all_profit = profitLoss.subtract(all_fee_amt);
        userPosition.setAllProfitAndLose(all_profit);
        userPosition.setSellOrderFee(sell_fee_amt);

        int updatePositionCount = this.userPositionMapper.updateByPrimaryKeySelective(userPosition);
        if (updatePositionCount > 0) {
            log.info("【用户平仓】修改浮动盈亏记录成功");
        } else {
            log.error("用户平仓】修改浮动盈亏记录出错");
            throw new Exception("用户平仓】修改浮动盈亏记录出错");
        }

        BigDecimal freez_amt = all_buy_amt.divide(new BigDecimal(userPosition.getOrderLever().intValue()), 2, 4);
        //BigDecimal freez_amt = all_buy_amt;

        BigDecimal reckon_all = user_all_amt.add(all_profit);
        //修改用户可用余额=当前可用余额+总盈亏+买入总金额+追加保证金
        BigDecimal reckon_enable = user_enable_amt.add(all_profit).add(freez_amt).add(userPosition.getMarginAdd());

        log.info("用户平仓后的总资金  = {} , 可用资金 = {}", reckon_all, reckon_enable);
        user.setUserAmt(reckon_all);
        user.setEnableAmt(reckon_enable);
        int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateUserCount > 0) {
            log.info("【用户平仓】修改用户金额成功");
        } else {
            log.error("用户平仓】修改用户金额出错");
            throw new Exception("用户平仓】修改用户金额出错");
        }

        UserCashDetail ucd = new UserCashDetail();
        ucd.setPositionId(userPosition.getId());
        ucd.setAgentId(user.getAgentId());
        ucd.setAgentName(user.getAgentName());
        ucd.setUserId(user.getId());
        ucd.setUserName(user.getRealName());
        ucd.setDeType("总盈亏");
        ucd.setDeAmt(all_profit);
        ucd.setDeSummary("卖出股票，" + userPosition.getStockCode() + "/" + userPosition.getStockName() + ",占用本金：" + freez_amt + ",总手续费：" + all_fee_amt + ",卖出手续费：" + sell_fee_amt+ ",印花税：" + orderSpread+ ",盈亏：" + profitLoss + "，总盈亏：" + all_profit);

        ucd.setAddTime(new Date());
        ucd.setIsRead(Integer.valueOf(0));

        int insertSxfCount = this.userCashDetailMapper.insert(ucd);
        if (insertSxfCount > 0) {
            //核算代理收入-平仓手续费
            iAgentAgencyFeeService.AgencyFeeIncome(2,userPosition.getPositionSn());
            //核算代理收入-分红
            iAgentAgencyFeeService.AgencyFeeIncome(4,userPosition.getPositionSn());
            log.info("【用户平仓】保存明细记录成功");
        } else {
            log.error("用户平仓】保存明细记录出错");
            throw new Exception("用户平仓】保存明细记录出错");
        }

        return ServerResponse.createBySuccess("平仓成功！", userPosition);
    }

    //用户追加保证金操作
    public ServerResponse addmargin(String positionSn, int doType, BigDecimal marginAdd) throws Exception {
        log.info("【用户追加保证金】 positionSn = {} ， dotype = {}", positionSn, Integer.valueOf(doType));

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("追加保证金出错，网站设置表不存在");
            return ServerResponse.createByErrorMsg("追加失败，系统设置错误");
        }

        if (doType != 0) {
            /*String am_begin = siteSetting.getTransAmBegin();
            String am_end = siteSetting.getTransAmEnd();
            String pm_begin = siteSetting.getTransPmBegin();
            String pm_end = siteSetting.getTransPmEnd();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));
            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("追加失败，不在交易时段内");
            }*/
        }

        UserPosition userPosition = this.userPositionMapper.findPositionBySn(positionSn);
        if (userPosition == null) {
            return ServerResponse.createByErrorMsg("追加失败，订单不存在");
        }

        User user = this.userMapper.selectByPrimaryKey(userPosition.getUserId());
        /*实名认证开关开启*/
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        if (!siteProduct.getStockMarginDisplay()) {
            return ServerResponse.createByErrorMsg("不允许追加，请联系管理员");
        }

        if(siteProduct.getHolidayDisplay()){
            return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
        }

        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
            return ServerResponse.createByErrorMsg("追加失败，用户已被锁定");
        }

        if (1 == userPosition.getIsLock().intValue()) {
            return ServerResponse.createByErrorMsg("追加失败 " + userPosition.getLockMsg());
        }

        BigDecimal user_all_amt = user.getUserAmt();
        BigDecimal user_enable_amt = user.getEnableAmt();
        int compareUserAmtInt = user_enable_amt.compareTo(marginAdd);
        log.info("用户可用金额 = {}  追加金额 =  {}", user_enable_amt, marginAdd);
        log.info("比较 用户金额 和 实际 购买金额 =  {}", Integer.valueOf(compareUserAmtInt));
        if (compareUserAmtInt == -1) {
            return ServerResponse.createByErrorMsg("追加失败，融资可用金额小于" + marginAdd + "元");
        }


        userPosition.setMarginAdd(userPosition.getMarginAdd().add(marginAdd));

        int updatePositionCount = this.userPositionMapper.updateByPrimaryKeySelective(userPosition);
        if (updatePositionCount > 0) {
            log.info("【用户追加保证金】追加保证金成功");
        } else {
            log.error("用户追加保证金】追加保证金出错");
            throw new Exception("用户追加保证金】追加保证金出错");
        }

        //修改用户可用余额=当前可用余额-追加金额
        BigDecimal reckon_enable = user_enable_amt.subtract(marginAdd);

        log.info("用户追加保证金后的总资金  = {} , 可用资金 = {}", user_all_amt, reckon_enable);
        user.setEnableAmt(reckon_enable);
        int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateUserCount > 0) {
            log.info("【用户平仓】修改用户金额成功");
        } else {
            log.error("用户平仓】修改用户金额出错");
            throw new Exception("用户平仓】修改用户金额出错");
        }

        UserCashDetail ucd = new UserCashDetail();
        ucd.setPositionId(userPosition.getId());
        ucd.setAgentId(user.getAgentId());
        ucd.setAgentName(user.getAgentName());
        ucd.setUserId(user.getId());
        ucd.setUserName(user.getRealName());
        ucd.setDeType("追加保证金");
        ucd.setDeAmt(marginAdd.multiply(new BigDecimal("-1")));
        ucd.setDeSummary("追加股票，" + userPosition.getStockCode() + "/" + userPosition.getStockName() + ",追加金额：" + marginAdd );

        ucd.setAddTime(new Date());
        ucd.setIsRead(Integer.valueOf(0));

        int insertSxfCount = this.userCashDetailMapper.insert(ucd);
        if (insertSxfCount > 0) {
            log.info("【用户平仓】保存明细记录成功");
        } else {
            log.error("用户平仓】保存明细记录出错");
            throw new Exception("用户平仓】保存明细记录出错");
        }

        return ServerResponse.createBySuccessMsg("追加成功！");
    }


    public ServerResponse lock(Integer positionId, Integer state, String lockMsg) {
        if (positionId == null || state == null) {
            return ServerResponse.createByErrorMsg("参数不能为空");
        }

        UserPosition position = this.userPositionMapper.selectByPrimaryKey(positionId);
        if (position == null) {
            return ServerResponse.createByErrorMsg("持仓不存在");
        }

        if (position.getSellOrderId() != null) {
            return ServerResponse.createByErrorMsg("平仓单不能锁仓");
        }

        if (state.intValue() == 1 &&
                StringUtils.isBlank(lockMsg)) {
            return ServerResponse.createByErrorMsg("锁仓提示信息必填");
        }


        if (state.intValue() == 1) {
            position.setIsLock(Integer.valueOf(1));
            position.setLockMsg(lockMsg);
        } else {
            position.setIsLock(Integer.valueOf(0));
        }

        int updateCount = this.userPositionMapper.updateByPrimaryKeySelective(position);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失败");
    }

    public ServerResponse del(Integer positionId) {
        if (positionId == null) {
            return ServerResponse.createByErrorMsg("id不能为空");
        }
        UserPosition position = this.userPositionMapper.selectByPrimaryKey(positionId);
        if (position == null) {
            ServerResponse.createByErrorMsg("该持仓不存在");
        }
        /*if (position.getSellOrderId() == null) {
            return ServerResponse.createByErrorMsg("持仓单不能删除！");
        }*/
        int updateCount = this.userPositionMapper.deleteByPrimaryKey(positionId);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("删除成功");
        }
        return ServerResponse.createByErrorMsg("删除失败");
    }

    public ServerResponse findMyPositionByCodeAndSpell(String stockCode, String stockSpell, Integer state, HttpServletRequest request, int pageNum, int pageSize) {
        User user = this.iUserService.getCurrentUser(request);

        PageHelper.startPage(pageNum, pageSize);


        List<UserPosition> userPositions = this.userPositionMapper.findMyPositionByCodeAndSpell(user.getId(), stockCode, stockSpell, state);

        List<UserPositionVO> userPositionVOS = Lists.newArrayList();
        if (userPositions.size() > 0) {
            for (UserPosition position : userPositions) {
                String stockCode1 = position.getStockCode();
//                BigDecimal businessBalance = stockMarketsDayMapper.selectBusinessBalanceByStockCode(stockCode1);
                UserPositionVO userPositionVO = assembleUserPositionVO(position);
//                userPositionVO.setBusinessBalance(businessBalance);
                userPositionVOS.add(userPositionVO);
            }
        }

        PageInfo pageInfo = new PageInfo(userPositions);
        pageInfo.setList(userPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }

    public PositionVO findUserPositionAllProfitAndLose(Integer userId) {
        List<UserPosition> userPositions = this.userPositionMapper.findPositionByUserIdAndSellIdIsNull(userId);

        BigDecimal allProfitAndLose = new BigDecimal("0");
        BigDecimal allFreezAmt = new BigDecimal("0");
        for (UserPosition position : userPositions) {
                PositionProfitVO positionProfitVO = getPositionProfitVO(position);

                allProfitAndLose = allProfitAndLose.add(positionProfitVO.getAllProfitAndLose());

                BigDecimal position_freez = position.getOrderTotalPrice().divide(new BigDecimal(position.getOrderLever().intValue()), 2, 4);
                //BigDecimal position_freez = position.getOrderTotalPrice();
                allFreezAmt = allFreezAmt.add(position_freez).add(position.getMarginAdd());
                log.info("查询所有持仓单的总盈亏，现价返回0，当前为集合竞价");
        }

        //加上分仓交易保证金
        List<FundsApply> fundsApplyList = fundsApplyMapper.getUserMarginList(userId);
        for (FundsApply fundsApply : fundsApplyList){
            allFreezAmt = allFreezAmt.add(fundsApply.getMargin());
        }


        PositionVO positionVO = new PositionVO();
        positionVO.setAllProfitAndLose(allProfitAndLose);
        positionVO.setAllFreezAmt(allFreezAmt);
        return positionVO;
    }

    public List<UserPosition> findPositionByUserIdAndSellIdIsNull(Integer userId) {
        return this.userPositionMapper.findPositionByUserIdAndSellIdIsNull(userId);
    }

    public List<UserPosition> findPositionByStockCodeAndTimes(int minuteTimes, String stockCode, Integer userId) {
        Date paramTimes = null;
        paramTimes = DateTimeUtil.parseToDateByMinute(minuteTimes);

        return this.userPositionMapper.findPositionByStockCodeAndTimes(paramTimes, stockCode, userId);
    }

    public Integer findPositionNumByTimes(int minuteTimes, Integer userId) {
        Date beginDate = DateTimeUtil.parseToDateByMinute(minuteTimes);
        Integer transNum = this.userPositionMapper.findPositionNumByTimes(beginDate, userId);
        log.info("用户 {} 在 {} 分钟之内 交易手数 {}", new Object[]{userId, Integer.valueOf(minuteTimes), transNum});
        return transNum;
    }

    public ServerResponse listByAgent(Integer positionType, Integer state, Integer userId, Integer agentId, String positionSn, String beginTime, String endTime, HttpServletRequest request, int pageNum, int pageSize) {
        AgentUser currentAgent = this.iAgentUserService.getCurrentAgent(request);


        if (agentId != null) {
            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
            if (agentUser!=null && agentUser.getParentId() != currentAgent.getId()) {
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


        List<UserPosition> userPositions = this.userPositionMapper.listByAgent(positionType, state, userId, searchId, positionSn, begin_time, end_time);

        List<AgentPositionVO> agentPositionVOS = Lists.newArrayList();
        for (UserPosition position : userPositions) {
            AgentPositionVO agentPositionVO = assembleAgentPositionVO(position);
            agentPositionVOS.add(agentPositionVO);
        }

        PageInfo pageInfo = new PageInfo(userPositions);
        pageInfo.setList(agentPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse getIncome(Integer agentId, Integer positionType, String beginTime, String endTime) {
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


        List<UserPosition> userPositions = this.userPositionMapper.listByAgent(positionType, Integer.valueOf(1), null, agentId, null, begin_time, end_time);


        BigDecimal order_fee_amt = new BigDecimal("0");
        BigDecimal order_profit_and_lose = new BigDecimal("0");
        BigDecimal order_profit_and_lose_all = new BigDecimal("0");

        for (UserPosition position : userPositions) {

            order_fee_amt = order_fee_amt.add(position.getOrderFee()).add(position.getOrderSpread()).add(position.getOrderStayFee());

            order_profit_and_lose = order_profit_and_lose.add(position.getProfitAndLose());

            order_profit_and_lose_all = order_profit_and_lose_all.add(position.getAllProfitAndLose());
        }

        AgentIncomeVO agentIncomeVO = new AgentIncomeVO();
        agentIncomeVO.setOrderSize(Integer.valueOf(userPositions.size()));
        agentIncomeVO.setOrderFeeAmt(order_fee_amt);
        agentIncomeVO.setOrderProfitAndLose(order_profit_and_lose);
        agentIncomeVO.setOrderAllAmt(order_profit_and_lose_all);

        return ServerResponse.createBySuccess(agentIncomeVO);
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


        List<UserPosition> userPositions = this.userPositionMapper.listByAgent(positionType, state, userId, agentId, positionSn, begin_time, end_time);

        List<AdminPositionVO> adminPositionVOS = Lists.newArrayList();
        for (UserPosition position : userPositions) {
            AdminPositionVO adminPositionVO = assembleAdminPositionVO(position);
            adminPositionVOS.add(adminPositionVO);
        }

        PageInfo pageInfo = new PageInfo(userPositions);
        pageInfo.setList(adminPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }

    public int CountPositionNum(Integer state, Integer accountType) {
        return this.userPositionMapper.CountPositionNum(state, accountType);
    }

    public BigDecimal CountPositionProfitAndLose() {
        return this.userPositionMapper.CountPositionProfitAndLose();
    }

    public BigDecimal CountPositionAllProfitAndLose() {
        return this.userPositionMapper.CountPositionAllProfitAndLose();
    }

    @Override
    public BigDecimal CountPositionAllProfitAndLoseByUser(Integer userId) {
        return this.userPositionMapper.CountPositionAllProfitAndLoseByUserId(userId);
    }

    public ServerResponse create(Integer userId, String stockCode, String buyPrice, String buyTime, Integer buyNum, Integer buyType, Integer lever, BigDecimal profitTarget, BigDecimal stopTarget) {
        if (userId == null || StringUtils.isBlank(buyPrice) || StringUtils.isBlank(stockCode) ||
                StringUtils.isBlank(buyTime) || buyNum == null || buyType == null || lever == null) {
            log.info("参数为空");
            return ServerResponse.createByErrorMsg("参数不能为空");

        }

        User user = this.userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            log.info("用户不存在");
            return ServerResponse.createByErrorMsg("用户不存在");

        }
//        if (user.getAccountType().intValue() != 1) {
//            return ServerResponse.createByErrorMsg("正式用户不能生成持仓单");
//        }
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
            log.info("下单失败，账户已被锁定");
            return ServerResponse.createByErrorMsg("下单失败，账户已被锁定");

        }
        Stock stock = (Stock) this.iStockService.findStockByCode(stockCode).getData();
        if (stock == null) {
            log.info("股票不存在");
            return ServerResponse.createByErrorMsg("股票不存在");

        }


        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("下单出错，网站设置表不存在");
            return ServerResponse.createByErrorMsg("下单失败，系统设置错误");
        }


        BigDecimal user_enable_amt = user.getEnableAmt();

        BigDecimal buy_amt = (new BigDecimal(buyPrice)).multiply(new BigDecimal(buyNum.intValue()));
        BigDecimal buy_amt_autual = buy_amt.divide(new BigDecimal(lever.intValue()), 2, 4);


        int compareUserAmtInt = user_enable_amt.compareTo(buy_amt_autual);
        log.info("用户可用金额 = {}  实际购买金额 =  {}", user_enable_amt, buy_amt_autual);
        log.info("比较 用户金额 和 实际 购买金额 =  {}", Integer.valueOf(compareUserAmtInt));
        if (compareUserAmtInt == -1) {
            log.info("下单失败，用户可用金额小于" + buy_amt_autual + "元");
            return ServerResponse.createByErrorMsg("下单失败，用户可用金额小于" + buy_amt_autual + "元");

        }

        if (user.getUserIndexAmt().compareTo(new BigDecimal("0")) == -1) {
            log.info("失败，指数总资金小于0");
            return ServerResponse.createByErrorMsg("失败，指数总资金小于0");

        }



        UserPosition userPosition = new UserPosition();

        if (profitTarget != null && profitTarget.compareTo(new BigDecimal("0")) > 0) {
            userPosition.setProfitTargetPrice(profitTarget);
        }
        if (stopTarget != null && stopTarget.compareTo(new BigDecimal("0")) > 0) {
            userPosition.setStopTargetPrice(stopTarget);
        }



        userPosition.setPositionType(user.getAccountType());
        userPosition.setPositionSn(KeyUtils.getUniqueKey());
        userPosition.setUserId(user.getId());
        userPosition.setNickName(user.getRealName());
        userPosition.setAgentId(user.getAgentId());
        userPosition.setStockCode(stock.getStockCode());
        userPosition.setStockName(stock.getStockName());
        userPosition.setStockGid(stock.getStockGid());
        userPosition.setStockSpell(stock.getStockSpell());
        userPosition.setBuyOrderId(GeneratePosition.getPositionId());
        userPosition.setBuyOrderTime(DateTimeUtil.strToDate(buyTime));
        userPosition.setBuyOrderPrice(new BigDecimal(buyPrice));
        userPosition.setOrderDirection((buyType.intValue() == 0) ? "买涨" : "买跌");

        userPosition.setOrderNum(buyNum);




        if (stock.getStockPlate() != null) {
            userPosition.setStockPlate(stock.getStockPlate());
        }


        userPosition.setIsLock(Integer.valueOf(0));


        userPosition.setOrderLever(lever);


        userPosition.setOrderTotalPrice(buy_amt);

        //递延费特殊处理
        BigDecimal stayFee = userPosition.getOrderTotalPrice().multiply(siteSetting.getStayFee());
        BigDecimal allStayFee = stayFee.multiply(new BigDecimal(1));
        userPosition.setOrderStayFee(allStayFee);
        userPosition.setOrderStayDays(1);


        BigDecimal buy_fee_amt = buy_amt.multiply(siteSetting.getBuyFee()).setScale(2, 4);
        log.info("创建模拟持仓 手续费（配资后总资金 * 百分比） = {}", buy_fee_amt);
        userPosition.setOrderFee(buy_fee_amt);


        BigDecimal buy_yhs_amt = buy_amt.multiply(siteSetting.getDutyFee()).setScale(2, 4);
        log.info("创建模拟持仓 印花税（配资后总资金 * 百分比） = {}", buy_yhs_amt);
        userPosition.setOrderSpread(buy_yhs_amt);
        StockListVO stockListVO = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(stock.getStockGid()));
        BigDecimal now_price = new BigDecimal(stockListVO.getNowPrice());

        if (now_price.compareTo(new BigDecimal("0")) == 0) {
            log.info(stock.getStockGid()+"报价0，");
            return ServerResponse.createByErrorMsg("报价0，请稍后再试");

        }

        double stock_crease = stockListVO.getHcrate().doubleValue();
        SiteSpread siteSpread = iSiteSpreadService.findSpreadRateOne(new BigDecimal(stock_crease), buy_amt, stock.getStockCode(), now_price);
        BigDecimal spread_rate_amt = new BigDecimal("0");
        if(siteSpread != null){
            spread_rate_amt = buy_amt.multiply(siteSpread.getSpreadRate()).setScale(2, 4);
            log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", siteSpread.getSpreadRate(), spread_rate_amt);
        } else{
            log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", "设置异常", spread_rate_amt);
        }

        userPosition.setSpreadRatePrice(spread_rate_amt);


        BigDecimal profit_and_lose = new BigDecimal("0");
        userPosition.setProfitAndLose(profit_and_lose);


        BigDecimal all_profit_and_lose = profit_and_lose.subtract(buy_fee_amt).subtract(buy_yhs_amt).subtract(spread_rate_amt);
        userPosition.setAllProfitAndLose(all_profit_and_lose);


        userPosition.setOrderStayDays(Integer.valueOf(0));
        userPosition.setOrderStayFee(new BigDecimal("0"));
        userPosition.setSpreadRatePrice(new BigDecimal("0"));
        userPosition.setSellOrderFee(BigDecimal.ZERO);

        int insertPositionCount = this.userPositionMapper.insert(userPosition);
        if (insertPositionCount > 0) {
            log.info("【创建持仓】保存记录成功");
        } else {
            log.error("【创建持仓】保存记录出错");
        }
        BigDecimal reckon_enable = user_enable_amt.subtract(buy_amt_autual);
        user.setEnableAmt(reckon_enable);
        int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateUserCount > 0) {
            log.info("【用户交易下单】修改用户金额成功");
        } else {
            log.error("用户交易下单】修改用户金额出错");

        }
        iAgentAgencyFeeService.AgencyFeeIncome(1,userPosition.getPositionSn());
        return ServerResponse.createBySuccess("生成持仓成功");
    }



    public int deleteByUserId(Integer userId) {
        return this.userPositionMapper.deleteByUserId(userId);
    }

    public void doClosingStayTask() {
        List<UserPosition> userPositions = this.userPositionMapper.findAllStayPosition();


        if (userPositions.size() > 0) {
            log.info("查询到正在持仓的订单数量 = {}", Integer.valueOf(userPositions.size()));

            SiteProduct siteProduct = iSiteProductService.getProductSetting();
            if(!siteProduct.getHolidayDisplay()) {
                for (UserPosition position : userPositions) {
                    int stayDays = GetStayDays.getDays(GetStayDays.getBeginDate(position.getBuyOrderTime()));
                    //递延费特殊处理
                    stayDays = stayDays + 1;

                    log.info("");
                    log.info("开始处理 持仓订单id = {} 订单号 = {} 用户id = {} realName = {} 留仓天数 = {}", new Object[]{position
                            .getId(), position.getPositionSn(), position.getUserId(), position
                            .getNickName(), Integer.valueOf(stayDays)});

                    if (stayDays != 0) {
                        log.info(" 开始收取 {} 天 留仓费", Integer.valueOf(stayDays));
                        try {
                            closingStayTask(position, Integer.valueOf(stayDays));
                        } catch (Exception e) {
                            log.error("doClosingStayTask = ", e);


                        }


                    } else {


                        log.info("持仓订单 = {} ,持仓天数0天,不需要处理...", position.getId());
                    }

                    log.info("修改留仓费 处理结束。");
                    log.info("");
                }

                SiteTaskLog stl = new SiteTaskLog();
                stl.setTaskType("扣除留仓费");
                stl.setAddTime(new Date());
                stl.setIsSuccess(Integer.valueOf(0));
                stl.setTaskTarget("扣除留仓费，订单数量为" + userPositions.size());
                this.siteTaskLogMapper.insert(stl);
            }
        } else {
            log.info("doClosingStayTask没有正在持仓的订单");
        }
    }

    /*留仓到期强制平仓，每天15点执行*/
    public void expireStayUnwindTask() {
        List<UserPosition> userPositions = this.userPositionMapper.findAllStayPosition();


        if (userPositions.size() > 0) {
            log.info("查询到正在持仓的订单数量 = {}", Integer.valueOf(userPositions.size()));

            SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
            for (UserPosition position : userPositions) {
                int stayDays = GetStayDays.getDays(GetStayDays.getBeginDate(position.getBuyOrderTime()));

                log.info("");
                log.info("开始处理 持仓订单id = {} 订单号 = {} 用户id = {} realName = {} 留仓天数 = {}", new Object[]{position
                        .getId(), position.getPositionSn(), position.getUserId(), position
                        .getNickName(), Integer.valueOf(stayDays)});

                //留仓达到最大天数
                if (stayDays >= siteSetting.getStayMaxDays().intValue()) {
                    log.info(" 开始强平 {} 天", Integer.valueOf(stayDays));
                    try {
                        this.sell(position.getPositionSn(),0);
                    } catch (Exception e) {
                        log.error("expireStayUnwindTask = ", e);
                    }
                } else {
                    log.info("持仓订单 = {} ,持仓天数0天,不需要处理...", position.getId());
                }
            }
        } else {
            log.info("doClosingStayTask没有正在持仓的订单");
        }
    }

    @Transactional
    public ServerResponse closingStayTask(UserPosition position, Integer stayDays) throws Exception {
        log.info("=================closingStayTask====================");
        log.info("修改留仓费，持仓id={},持仓天数={}", position.getId(), stayDays);

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("修改留仓费出错，网站设置表不存在");
            return ServerResponse.createByErrorMsg("修改留仓费出错，网站设置表不存在");
        }


        BigDecimal stayFee = position.getOrderTotalPrice().multiply(siteSetting.getStayFee());

        BigDecimal allStayFee = stayFee.multiply(new BigDecimal(stayDays.intValue()));

        log.info("总留仓费 = {}", allStayFee);


        position.setOrderStayFee(position.getOrderStayFee().add(position.getOrderTotalPrice().multiply(new BigDecimal("0.0025"))));
        position.setOrderStayDays(stayDays);

//        BigDecimal all_profit = position.getAllProfitAndLose().subtract(allStayFee);
        BigDecimal all_profit = position.getAllProfitAndLose();
        position.setAllProfitAndLose(all_profit);

        int updateCount = this.userPositionMapper.updateByPrimaryKeySelective(position);
        if (updateCount > 0) {
            //核算代理收入-延递费
            iAgentAgencyFeeService.AgencyFeeIncome(3,position.getPositionSn());
            log.info("【closingStayTask收持仓费】修改持仓记录成功");
        } else {
            log.error("【closingStayTask收持仓费】修改持仓记录出错");
            throw new Exception("【closingStayTask收持仓费】修改持仓记录出错");
        }


        log.info("=======================================================");
        return ServerResponse.createBySuccess();
    }

    public List<Integer> findDistinctUserIdList() {
        return this.userPositionMapper.findDistinctUserIdList();
    }

    private AdminPositionVO assembleAdminPositionVO(UserPosition position) {
        AdminPositionVO adminPositionVO = new AdminPositionVO();

        adminPositionVO.setId(position.getId());
        adminPositionVO.setPositionSn(position.getPositionSn());
        adminPositionVO.setPositionType(position.getPositionType());
        adminPositionVO.setUserId(position.getUserId());
        adminPositionVO.setNickName(position.getNickName());
        adminPositionVO.setAgentId(position.getAgentId());
        adminPositionVO.setStockName(position.getStockName());
        adminPositionVO.setStockCode(position.getStockCode());
        adminPositionVO.setStockGid(position.getStockGid());
        adminPositionVO.setStockSpell(position.getStockSpell());
        adminPositionVO.setBuyOrderId(position.getBuyOrderId());
        adminPositionVO.setBuyOrderTime(position.getBuyOrderTime());
        adminPositionVO.setBuyOrderPrice(position.getBuyOrderPrice());
        adminPositionVO.setSellOrderId(position.getSellOrderId());
        adminPositionVO.setSellOrderTime(position.getSellOrderTime());
        adminPositionVO.setSellOrderPrice(position.getSellOrderPrice());
        adminPositionVO.setOrderDirection(position.getOrderDirection());
        adminPositionVO.setOrderNum(position.getOrderNum());
        adminPositionVO.setOrderLever(position.getOrderLever());
        adminPositionVO.setOrderTotalPrice(position.getOrderTotalPrice());
        adminPositionVO.setOrderFee(position.getOrderFee());
        adminPositionVO.setOrderSpread(position.getOrderSpread());
        adminPositionVO.setOrderStayFee(position.getOrderStayFee());
        adminPositionVO.setOrderStayDays(position.getOrderStayDays());

        adminPositionVO.setIsLock(position.getIsLock());
        adminPositionVO.setLockMsg(position.getLockMsg());

        adminPositionVO.setStockPlate(position.getStockPlate());

        PositionProfitVO positionProfitVO = getPositionProfitVO(position);
        adminPositionVO.setProfitAndLose(positionProfitVO.getProfitAndLose());
        adminPositionVO.setAllProfitAndLose(positionProfitVO.getAllProfitAndLose());
        adminPositionVO.setNow_price(positionProfitVO.getNowPrice());
        adminPositionVO.setOrderStayDays(positionProfitVO.getOrderStayDays());
        adminPositionVO.setAllProfitRate(positionProfitVO.getAllProfitRate());

        return adminPositionVO;
    }

    private AgentPositionVO assembleAgentPositionVO(UserPosition position) {
        AgentPositionVO agentPositionVO = new AgentPositionVO();

        agentPositionVO.setId(position.getId());
        agentPositionVO.setPositionSn(position.getPositionSn());
        agentPositionVO.setPositionType(position.getPositionType());
        agentPositionVO.setUserId(position.getUserId());
        agentPositionVO.setNickName(position.getNickName());
        agentPositionVO.setAgentId(position.getAgentId());
        agentPositionVO.setStockName(position.getStockName());
        agentPositionVO.setStockCode(position.getStockCode());
        agentPositionVO.setStockGid(position.getStockGid());
        agentPositionVO.setStockSpell(position.getStockSpell());
        agentPositionVO.setBuyOrderId(position.getBuyOrderId());
        agentPositionVO.setBuyOrderTime(position.getBuyOrderTime());
        agentPositionVO.setBuyOrderPrice(position.getBuyOrderPrice());
        agentPositionVO.setSellOrderId(position.getSellOrderId());
        agentPositionVO.setSellOrderTime(position.getSellOrderTime());
        agentPositionVO.setSellOrderPrice(position.getSellOrderPrice());
        agentPositionVO.setOrderDirection(position.getOrderDirection());
        agentPositionVO.setOrderNum(position.getOrderNum());
        agentPositionVO.setOrderLever(position.getOrderLever());
        agentPositionVO.setOrderTotalPrice(position.getOrderTotalPrice());
        agentPositionVO.setOrderFee(position.getOrderFee());
        agentPositionVO.setOrderSpread(position.getOrderSpread());
        agentPositionVO.setOrderStayFee(position.getOrderStayFee());
        agentPositionVO.setOrderStayDays(position.getOrderStayDays());

        agentPositionVO.setIsLock(position.getIsLock());
        agentPositionVO.setLockMsg(position.getLockMsg());

        agentPositionVO.setStockPlate(position.getStockPlate());

        PositionProfitVO positionProfitVO = getPositionProfitVO(position);
        agentPositionVO.setProfitAndLose(positionProfitVO.getProfitAndLose());
        agentPositionVO.setAllProfitAndLose(positionProfitVO.getAllProfitAndLose());
        agentPositionVO.setNow_price(positionProfitVO.getNowPrice());


        return agentPositionVO;
    }

    private UserPositionVO assembleUserPositionVO(UserPosition position) {
        UserPositionVO userPositionVO = new UserPositionVO();

        userPositionVO.setId(position.getId());
        userPositionVO.setPositionType(position.getPositionType());
        userPositionVO.setPositionSn(position.getPositionSn());
        userPositionVO.setUserId(position.getUserId());
        userPositionVO.setNickName(position.getNickName());
        userPositionVO.setAgentId(position.getAgentId());
        userPositionVO.setStockName(position.getStockName());
        userPositionVO.setStockCode(position.getStockCode());
        userPositionVO.setStockGid(position.getStockGid());
        userPositionVO.setStockSpell(position.getStockSpell());
        userPositionVO.setBuyOrderId(position.getBuyOrderId());
        userPositionVO.setBuyOrderTime(position.getBuyOrderTime());
        userPositionVO.setBuyOrderPrice(position.getBuyOrderPrice());
        userPositionVO.setSellOrderId(position.getSellOrderId());
        userPositionVO.setSellOrderTime(position.getSellOrderTime());
        userPositionVO.setSellOrderPrice(position.getSellOrderPrice());
        userPositionVO.setProfitTargetPrice(position.getProfitTargetPrice());
        userPositionVO.setStopTargetPrice(position.getStopTargetPrice());
        userPositionVO.setOrderDirection(position.getOrderDirection());
        userPositionVO.setOrderNum(position.getOrderNum());
        userPositionVO.setOrderLever(position.getOrderLever());
        userPositionVO.setOrderTotalPrice(position.getOrderTotalPrice());
        userPositionVO.setOrderFee(position.getOrderFee());
        userPositionVO.setSellOrderFee(position.getSellOrderFee());
        userPositionVO.setOrderSpread(position.getOrderSpread());
        userPositionVO.setOrderStayFee(position.getOrderStayFee());
        userPositionVO.setOrderStayDays(position.getOrderStayDays());
        userPositionVO.setMarginAdd(position.getMarginAdd());

        userPositionVO.setStockPlate(position.getStockPlate());
        userPositionVO.setSpreadRatePrice(position.getSpreadRatePrice());

        PositionProfitVO positionProfitVO = getPositionProfitVO(position);
        userPositionVO.setProfitAndLose(positionProfitVO.getProfitAndLose());
        userPositionVO.setAllProfitAndLose(positionProfitVO.getAllProfitAndLose());
        userPositionVO.setNow_price(positionProfitVO.getNowPrice());
        userPositionVO.setOrderStayDays(positionProfitVO.getOrderStayDays());
        userPositionVO.setOrderStayFee(positionProfitVO.getStayFee());

        return userPositionVO;
    }

    public PositionProfitVO getPositionProfitVO(UserPosition position) {
        BigDecimal profitAndLose = new BigDecimal("0");
        BigDecimal allProfitAndLose = new BigDecimal("0");
        String nowPrice = "";
        BigDecimal stadyFee = BigDecimal.ZERO;

        Integer orderStayDays = 0;

        BigDecimal bzj = position.getOrderTotalPrice().divide(new BigDecimal(position.getOrderLever())).add(position.getMarginAdd());

        if (position.getSellOrderId() != null) {
            nowPrice = position.getSellOrderPrice().toString();
            BigDecimal subPrice = position.getSellOrderPrice().subtract(position.getBuyOrderPrice());
            //profitAndLose = subPrice.multiply(new BigDecimal(position.getOrderNum().intValue())).multiply(new BigDecimal(position.getOrderLever())).setScale(2,4);
            profitAndLose = subPrice.multiply(new BigDecimal(position.getOrderNum().intValue()));
            if ("买跌".equals(position.getOrderDirection())) {
                profitAndLose = profitAndLose.negate();
            }
            stadyFee = position.getOrderStayFee();
            orderStayDays = position.getOrderStayDays();
            allProfitAndLose = profitAndLose.subtract(position.getOrderFee()).subtract(position.getOrderSpread()).subtract(position.getSpreadRatePrice()).subtract(position.getSellOrderFee());
        } else {
            StockListVO stockListVO = new StockListVO();
            StockCoin stockCoin = new StockCoin();
            WjStockExtInfoDTO realData = wjStockApi.getRealData(position.getStockGid());
            nowPrice = realData.getPrice().toString();
          //  nowPrice = String.valueOf(0);
            if (nowPrice == null){
                nowPrice = String.valueOf(0);
              //  nowPrice= realData.getPreclose().toString();
            }
            orderStayDays = GetStayDays.durationDays(position.getBuyOrderTime());
            stadyFee = getStayFee(position);

            BigDecimal subPrice = (new BigDecimal(nowPrice)).subtract(position.getBuyOrderPrice());
            //profitAndLose = subPrice.multiply(new BigDecimal(position.getOrderNum().intValue())).multiply(new BigDecimal(position.getOrderLever())).setScale(2,4);
            profitAndLose = subPrice.multiply(new BigDecimal(position.getOrderNum().intValue()))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            if ("买跌".equals(position.getOrderDirection())) {
                profitAndLose = profitAndLose.negate();
            }

            //总盈亏= 浮动盈亏 – 手续费 – 印花税 – 留仓费 – 点差费
            allProfitAndLose = profitAndLose.subtract(position.getOrderFee()).subtract(position.getOrderSpread()).subtract(position.getSpreadRatePrice());
        }
        PositionProfitVO positionProfitVO = new PositionProfitVO();
        positionProfitVO.setProfitAndLose(profitAndLose);
        positionProfitVO.setAllProfitAndLose(allProfitAndLose.setScale(2, BigDecimal.ROUND_HALF_UP));
        positionProfitVO.setNowPrice(nowPrice);
        positionProfitVO.setStayFee(stadyFee);
        positionProfitVO.setOrderStayDays(orderStayDays);


//        log.info("pos id is : {}, now price is : {}, buy price: {}", position.getId(),
//                nowPrice, position.getBuyOrderPrice());

        BigDecimal rate = new BigDecimal(nowPrice).multiply(new BigDecimal("100"))
                .divide(position.getBuyOrderPrice(), BigDecimal.ROUND_HALF_UP)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        positionProfitVO.setAllProfitRate(rate);

        return positionProfitVO;
    }



    private BigDecimal getStayFee(UserPosition position) {
        Integer days = GetStayDays.durationDays(position.getBuyOrderTime());

        if (days == 0L) {
            return BigDecimal.ZERO;
        }

        if (position.getOrderLever() == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal bzj_amt = position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum()))
                .divide(new BigDecimal(position.getOrderLever()), BigDecimal.ROUND_HALF_UP);

        return bzj_amt.multiply(new BigDecimal(days)).multiply(new BigDecimal("0.00025"))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }



    /*股票入仓最新top列表*/
    public ServerResponse findPositionTopList(Integer pageSize) {
        List<UserPosition> userPositions = this.userPositionMapper.findPositionTopList(pageSize);

        List<UserPositionVO> userPositionVOS = Lists.newArrayList();
        if (userPositions.size() > 0) {
            for (UserPosition position : userPositions) {

                UserPositionVO userPositionVO = assembleUserPositionVO(position);
                userPositionVOS.add(userPositionVO);
            }
        }

        PageInfo pageInfo = new PageInfo(userPositions);
        pageInfo.setList(userPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }

    /*根据股票代码查询用户最早入仓股票*/
    public ServerResponse findUserPositionByCode(HttpServletRequest request, String stockCode) {
        User user = this.iUserService.getCurrentRefreshUser(request);
        UserPosition position = this.userPositionMapper.findUserPositionByCode(user.getId(), stockCode);

        List<UserPositionVO> userPositionVOS = Lists.newArrayList();
        UserPositionVO userPositionVO = null;
        if(position != null){
            userPositionVO = assembleUserPositionVO(position);
        }
        userPositionVOS.add(userPositionVO);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setList(userPositionVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }
    /**
     * @Description:  新股转持仓
     * @Param:
     * @return:
     * @Author: tf
     * @Date: 2022/10/26
     */
    @Override
    public ServerResponse newStockToPosition(Integer id) {
        UserStockSubscribe userStockSubscribe =userStockSubscribeMapper.load(id);
        if(userStockSubscribe==null){
            return ServerResponse.createByErrorMsg("无该申购记录");
        }
        StockSubscribe stockSubscribe = stockSubscribeMapper.selectOne(new QueryWrapper<StockSubscribe>().eq("code", userStockSubscribe.getNewCode()));
        if( userStockSubscribe == null){
            return ServerResponse.createByErrorMsg("该新股不存在");
        }
        if (userStockSubscribe.getApplyNumber() ==null){
            return ServerResponse.createByErrorMsg("中签数量为空，转持仓失败");
        }
        Stock stock = stockMapper.selectOne(new QueryWrapper<Stock>().eq("stock_code", userStockSubscribe.getNewCode()));
        if (stock == null){
            return ServerResponse.createByErrorMsg("该股不存在，每天8点30分更新，如果时间已过并且此新股确保上市，请手动添加");
        }

        if(userStockSubscribe.getStatus() == 4||userStockSubscribe.getStatus() == 3){

            String sinaStock = SinaStockApi.getSinaStock(stock.getStockGid());
            String[] arrayOfString = sinaStock.split(",");
//                 if (arrayOfString.length < 10){
//                 return ServerResponse.createByErrorMsg("数据源无该新股数据，转持仓失败");
//                 }
            UserPosition userPosition = new UserPosition();
            userPosition.setPositionType(1);
            userPosition.setPositionSn(KeyUtils.getUniqueKey());
            userPosition.setUserId(userStockSubscribe.getUserId());
            userPosition.setNickName(userStockSubscribe.getRealName());
            userPosition.setAgentId(userStockSubscribe.getAgentId());
            userPosition.setStockCode(userStockSubscribe.getNewCode());
            userPosition.setStockName(userStockSubscribe.getNewName());
            userPosition.setStockGid(stock.getStockGid());

            userPosition.setBuyOrderId(GeneratePosition.getPositionId());
            userPosition.setBuyOrderTime(new Date());
            userPosition.setBuyOrderPrice(userStockSubscribe.getBuyPrice());
            userPosition.setOrderDirection("买涨");
            userPosition.setOrderNum(userStockSubscribe.getApplyNumber());


            userPosition.setIsLock(Integer.valueOf(0));


            userPosition.setOrderLever(1);


            //递延费特殊处理
            //            BigDecimal stayFee = userPosition.getOrderTotalPrice().multiply(siteSetting.getStayFee());
            BigDecimal stayFee = new BigDecimal(0);
            BigDecimal allStayFee = stayFee.multiply(new BigDecimal(1));
            userPosition.setOrderStayFee(allStayFee);
            userPosition.setOrderStayDays(1);
            userPosition.setOrderTotalPrice(userStockSubscribe.getBond());

            //            BigDecimal buy_fee_amt = buy_amt.multiply(siteSetting.getBuyFee()).setScale(2, 4);
            BigDecimal buy_fee_amt = new BigDecimal(0);
            log.info("用户购买手续费（配资后总资金 * 百分比） = {}", buy_fee_amt);
            userPosition.setOrderFee(buy_fee_amt);


            //            BigDecimal buy_yhs_amt = buy_amt.multiply(siteSetting.getDutyFee()).setScale(2, 4);
            BigDecimal buy_yhs_amt = new BigDecimal(0);
            log.info("用户购买印花税（配资后总资金 * 百分比） = {}", buy_yhs_amt);
            userPosition.setOrderSpread(buy_yhs_amt);

            //            SiteSpread siteSpread = iSiteSpreadService.findSpreadRateOne(new BigDecimal(stock_crease), buy_amt, stock.getStockCode(), now_price);
            //            BigDecimal spread_rate_amt = new BigDecimal("0");
            //            if(siteSpread != null){
            //                spread_rate_amt = buy_amt.multiply(siteSpread.getSpreadRate()).setScale(2, 4);
            //                log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", siteSpread.getSpreadRate(), spread_rate_amt);
            //            } else{
            //                log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", "设置异常", spread_rate_amt);
            //            }
            BigDecimal spread_rate_amt = new BigDecimal(0);
            userPosition.setSpreadRatePrice(spread_rate_amt);


            BigDecimal profit_and_lose = new BigDecimal("0");
            userPosition.setProfitAndLose(profit_and_lose);


            BigDecimal all_profit_and_lose = profit_and_lose.subtract(buy_fee_amt).subtract(buy_yhs_amt).subtract(spread_rate_amt);
            userPosition.setAllProfitAndLose(all_profit_and_lose);


            userPosition.setOrderStayDays(Integer.valueOf(0));
            userPosition.setOrderStayFee(new BigDecimal("0"));

            int ret = 0;
            ret = this.userPositionMapper.insert(userPosition);

            if(ret > 0){
                userStockSubscribe.setStatus(5);
                userStockSubscribeMapper.update1(userStockSubscribe);
                if (userStockSubscribe.getType() == 1||userStockSubscribe.getType() == 2){
                    User user = userMapper.selectByPrimaryKey(userStockSubscribe.getUserId());
                    user.setDjzj(user.getDjzj().subtract(userStockSubscribe.getBond()));
                    ret = userMapper.updateByPrimaryKey(user);
                }
                if(ret > 0) {
                    return ServerResponse.createBySuccessMsg("新股转持仓成功");
                } else {
                    return ServerResponse.createByErrorMsg("新股转持仓失败");
                }
            } else{
                return ServerResponse.createByErrorMsg("新股转持仓失败");
            }
        }
        return ServerResponse.createByErrorMsg("新股转持仓失败");
    }

    @Override
    public ServerResponse buyStockDzList( HttpServletRequest request) {
        User user = this.iUserService.getCurrentRefreshUser(request);
        if(user == null){
            return null;
        }
        List<UserPosition> dzList = userPositionMapper.selectList(new QueryWrapper<UserPosition>().eq("user_id",user.getId()).eq("position_type",3).orderByDesc("buy_order_time"));
        return ServerResponse.createBySuccess(dzList);

    }

    @Override
    public ServerResponse sellPreprocessing(String positionSn, Integer sellNum) throws Exception {
        UserPosition userPosition = this.userPositionMapper.findPositionBySn(positionSn);
        Integer orderNum = userPosition.getOrderNum();
        if (sellNum > orderNum){
            return ServerResponse.createByErrorMsg("数量不合法,请重新输入,最大卖出量" + orderNum);
        }

        if (sellNum % 1 != 0) {
            return ServerResponse.createByErrorMsg("只能卖出整数");
        }
        if (sellNum.equals(orderNum)){
            return this.sell(positionSn, 1);
        }
        if (sellNum < 100) {
            return ServerResponse.createByErrorMsg("最少卖出100股");
        }
        return this.dividingWarehouses(userPosition,sellNum);
    }


    private ServerResponse dividingWarehouses(UserPosition userPosition,Integer sellNum) throws Exception {
        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("平仓出错，网站设置表不存在");
            return ServerResponse.createByErrorMsg("下单失败，系统设置错误");
        }
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        if (userPosition.getStockGid().contains("us")){
            String am_begin = siteSetting.getTransAmBeginUs();
            String am_end = siteSetting.getTransAmEndUs();
            String pm_begin = siteSetting.getTransPmBeginUs();
            String pm_end = siteSetting.getTransPmEndUs();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("平仓失败，不在交易时段内");
            }
        }else if(userPosition.getStockGid().contains("hk")){
            String am_begin = siteSetting.getTransAmBeginhk();
            String am_end = siteSetting.getTransAmEndhk();
            String pm_begin = siteSetting.getTransPmBeginhk();
            String pm_end = siteSetting.getTransPmEndhk();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("下单失败，不在港股股交易时段内");
            }
        }else {
            String am_begin = siteSetting.getTransAmBegin();
            String am_end = siteSetting.getTransAmEnd();
            String pm_begin = siteSetting.getTransPmBegin();
            String pm_end = siteSetting.getTransPmEnd();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));
            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("平仓失败，不在交易时段内");
            }
        }
        if(siteProduct.getHolidayDisplay()){
            return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
        }


        if (userPosition == null) {
            return ServerResponse.createByErrorMsg("平仓失败，订单不存在");
        }

        User user = this.userMapper.selectByPrimaryKey(userPosition.getUserId());
        if (user == null){
            return ServerResponse.createByErrorMsg("平仓失败，用户不存在");
        }

        /*实名认证开关开启*/
        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
            return ServerResponse.createByErrorMsg("平仓失败，用户已被锁定");
        }

        if (userPosition.getSellOrderId() != null) {
            return ServerResponse.createByErrorMsg("平仓失败，此订单已平仓");
        }

        if (1 == userPosition.getIsLock().intValue()) {
            return ServerResponse.createByErrorMsg("平仓失败 " + userPosition.getLockMsg());
        }


        if (sellNum > getCanSellNum(userPosition)) {
            return ServerResponse.createByErrorMsg("当前可卖股数不足");
        }

        BigDecimal now_price;
        StockListVO stockListVO = new StockListVO();
        StockCoin stockCoin = new StockCoin();
        Stock stock  = stockMapper.selectOne(new QueryWrapper<Stock>().eq("stock_gid",userPosition.getStockGid()));
        //股票卖出的 价格 数据源
        stockListVO = wjStockApi.getStockListVo(stock);
        now_price = new BigDecimal(stockListVO.getNowPrice());

        if (stockListVO.getNowPrice() == null) {
            return ServerResponse.createByErrorMsg("平仓失败，获取股票信息失败");
        }
        if (now_price.compareTo(new BigDecimal("0")) != 1) {
            log.error("股票 = {} 收到报价 = {}", userPosition.getStockName(), now_price);
            return ServerResponse.createByErrorMsg("报价0，平仓失败，请稍后再试");
        }

        double stock_crease = stockListVO.getHcrate().doubleValue();

        BigDecimal zsPrice = stockListVO.getYc();

        BigDecimal ztPrice = zsPrice.multiply(new BigDecimal("0.1")).add(zsPrice);
        ztPrice = ztPrice.setScale(2, 4);
        BigDecimal chaPrice = ztPrice.subtract(zsPrice);

        BigDecimal ztRate = chaPrice.multiply(new BigDecimal("100")).divide(zsPrice, 2, 4);

        ztRate = ztRate.negate();
        log.info("股票当前涨跌幅 = {} 跌停幅度 = {}", Double.valueOf(stock_crease), ztRate);
        if ((new BigDecimal(String.valueOf(stock_crease))).compareTo(ztRate) == 0 && "买涨"
                .equals(userPosition.getOrderDirection())) {
            return ServerResponse.createByErrorMsg("当前股票已跌停不能卖出");
        }

        //从这里开始计算
        //买入数量
        Integer buy_num = userPosition.getOrderNum();
        //买入价格
        BigDecimal buyOrderPrice = userPosition.getBuyOrderPrice();
        //计算盈亏
        BigDecimal sellNumToBig = new BigDecimal(sellNum);
        //卖出当前总价总价
        BigDecimal sellNowPirce = sellNumToBig.multiply(now_price);
        //购入总价
        BigDecimal sellBuyPirce = sellNumToBig.multiply(buyOrderPrice);
        //总盈亏
        BigDecimal Profitloss = sellNowPirce.subtract(sellBuyPirce);

        BigDecimal user_all_amt = user.getUserAmt();
        BigDecimal user_enable_amt = user.getEnableAmt();
        log.info("用户原本总资金 = {} , 可用 = {}", user_all_amt, user_enable_amt);
        BigDecimal totalBigDecimal = new BigDecimal(buy_num);
        BigDecimal soldBigDecimal = new BigDecimal(sellNum);
        //计算卖出百分百
        BigDecimal percentage = soldBigDecimal.divide(totalBigDecimal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

        BigDecimal buy_fee_amt = percentageCalculation(userPosition.getOrderFee(),percentage);
        log.info("买入手续费 = {}", buy_fee_amt);

        BigDecimal orderSpread = percentageCalculation(userPosition.getOrderSpread(),percentage);
        log.info("印花税 = {}", orderSpread);

        Integer days = GetStayDays.durationDays(userPosition.getBuyOrderTime());
        BigDecimal allOrderStayFee = this.getStayFee(userPosition);

        BigDecimal orderStayFee = percentageCalculation(allOrderStayFee, percentage);
        log.info("递延费1 = {}", orderStayFee);

        BigDecimal spreadRatePrice = percentageCalculation(userPosition.getSpreadRatePrice(),percentage);
        log.info("点差费 = {}", spreadRatePrice);

        BigDecimal sell_fee_amt = sellNowPirce.multiply(siteSetting.getSellFee()).setScale(2, 4);
        log.info("卖出手续费 = {}", sell_fee_amt);

        //总手续费= 买入手续费+卖出手续费+印花税+递延费+点差费
//        BigDecimal all_fee_amt = buy_fee_amt.add(sell_fee_amt).add(orderSpread).add(orderStayFee).add(spreadRatePrice);
        BigDecimal all_fee_amt = buy_fee_amt.add(sell_fee_amt).add(orderSpread).add(spreadRatePrice);
        log.info("总的手续费费用 = {}", all_fee_amt);
        //插入一个平仓记录
        BigDecimal all_profit = Profitloss.subtract(all_fee_amt);
        UserPosition instPostion = new UserPosition();

        instPostion.setPositionType(userPosition.getPositionType());
        instPostion.setPositionSn(GeneratePosition.getPositionId());
        instPostion.setBuyOrderPrice(userPosition.getBuyOrderPrice());
        instPostion.setUserId(userPosition.getUserId());
        instPostion.setNickName(userPosition.getNickName());
        instPostion.setAgentId(userPosition.getAgentId());
        instPostion.setStockName(userPosition.getStockName());
        instPostion.setStockCode(userPosition.getStockCode());
        instPostion.setStockGid(userPosition.getStockGid());
        instPostion.setStockSpell(userPosition.getStockSpell());
        instPostion.setSellOrderId(GeneratePosition.getPositionId());
        instPostion.setSellOrderPrice(now_price);
        instPostion.setSellOrderTime(new Date());
        instPostion.setOrderDirection(userPosition.getOrderDirection());
        instPostion.setOrderNum(sellNum);
        instPostion.setOrderLever(userPosition.getOrderLever());
        instPostion.setOrderTotalPrice(sellBuyPirce);
        instPostion.setOrderFee(buy_fee_amt);
        instPostion.setSellOrderFee(sell_fee_amt);
        instPostion.setOrderSpread(orderSpread);
        instPostion.setOrderStayFee(orderStayFee);
        instPostion.setSpreadRatePrice(spreadRatePrice);
        instPostion.setOrderStayDays(days);
        instPostion.setProfitAndLose(Profitloss);
        instPostion.setAllProfitAndLose(all_profit);
        instPostion.setStockPlate(userPosition.getStockPlate());
        instPostion.setMarginAdd(userPosition.getMarginAdd());
        instPostion.setBuyOrderId(GeneratePosition.getPositionId());
        instPostion.setBuyOrderTime(userPosition.getBuyOrderTime());
        this.userPositionMapper.insert(instPostion);
        userPosition.setOrderNum(buy_num - sellNum);
        userPosition.setOrderFee(userPosition.getOrderFee().subtract(buy_fee_amt));
        userPosition.setOrderSpread(userPosition.getOrderSpread().subtract(orderSpread));
        userPosition.setOrderStayFee(allOrderStayFee.subtract(orderStayFee));
        userPosition.setOrderStayDays(days);
        userPosition.setOrderTotalPrice(userPosition.getOrderTotalPrice().subtract(sellBuyPirce));
        userPosition.setSpreadRatePrice(userPosition.getSpreadRatePrice().subtract(spreadRatePrice));
        //更新

        int updatePositionCount = this.userPositionMapper.updateByPrimaryKeySelective(userPosition);
        if (updatePositionCount > 0) {
            log.info("【用户平仓】修改浮动盈亏记录成功");
        } else {
            log.error("用户平仓】修改浮动盈亏记录出错");
            throw new Exception("用户平仓】修改浮动盈亏记录出错");
        }

        BigDecimal freez_amt = sellBuyPirce.divide(new BigDecimal(userPosition.getOrderLever().intValue()), 2, 4);

        BigDecimal reckon_all = user_all_amt.add(all_profit);
        //修改用户可用余额=当前可用余额+总盈亏+买入总金额+追加保证金
        BigDecimal reckon_enable = user_enable_amt.add(all_profit).add(freez_amt).add(userPosition.getMarginAdd());

        log.info("用户平仓后的总资金  = {} , 可用资金 = {}", reckon_all, reckon_enable);
        user.setUserAmt(reckon_all);
        user.setEnableAmt(reckon_enable);

        int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateUserCount > 0) {
            log.info("【用户平仓】修改用户金额成功");
        } else {
            log.error("用户平仓】修改用户金额出错");
            throw new Exception("用户平仓】修改用户金额出错");
        }
        UserCashDetail ucd = new UserCashDetail();
        ucd.setPositionId(userPosition.getId());
        ucd.setAgentId(user.getAgentId());
        ucd.setAgentName(user.getAgentName());
        ucd.setUserId(user.getId());
        ucd.setUserName(user.getRealName());
        ucd.setDeType("总盈亏");
        ucd.setDeAmt(all_profit);
        ucd.setDeSummary("卖出股票，" + userPosition.getStockCode() + "/" + userPosition.getStockName() + ",占用本金：" + freez_amt + ",总手续费：" + all_fee_amt + ",卖出手续费：" + sell_fee_amt+ ",印花税：" + orderSpread + ",盈亏：" + Profitloss + "，总盈亏：" + all_profit);

        ucd.setAddTime(new Date());
        ucd.setIsRead(Integer.valueOf(0));

        int insertSxfCount = this.userCashDetailMapper.insert(ucd);
        if (insertSxfCount > 0) {
            //核算代理收入-平仓手续费
            iAgentAgencyFeeService.AgencyFeeIncome(2,userPosition.getPositionSn());
            //核算代理收入-分红
            iAgentAgencyFeeService.AgencyFeeIncome(4,userPosition.getPositionSn());
            log.info("【用户平仓】保存明细记录成功");
        } else {
            log.error("用户平仓】保存明细记录出错");
            throw new Exception("用户平仓】保存明细记录出错");
        }

        return ServerResponse.createBySuccess("平仓成功！", userPosition);
    }


    private BigDecimal percentageCalculation(BigDecimal total,BigDecimal rate){
        return total.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }


    /**
     * 大宗下单
     * @param stockCode
     * @param password
     * @param num
     * @param request
     * @return
     */
    @Transactional
    public ServerResponse buyDz(String stockCode, String password, Integer num, HttpServletRequest request) throws Exception {

        /*实名认证开关开启*/
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (siteProduct.getRealNameDisplay() && (StringUtils.isBlank(user.getRealName()) || StringUtils.isBlank(user.getIdCard()))) {
            return ServerResponse.createByErrorMsg("下单失败，请先实名认证");
        }
        BigDecimal user_enable_amt = user.getEnableAmt();
        log.info("用户 {} 下单，股票code = {} ，数量 = {}", new Object[]{user
                .getId(), stockCode, num });
        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
            return ServerResponse.createByErrorMsg("下单失败，账户已被锁定");
        }

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("下单出错，网站设置表不存在");
            return ServerResponse.createByErrorMsg("下单失败，系统设置错误");
        }
        StockDz stockDz = null;
        stockDz = this.stockDzMapper.selectOne(new QueryWrapper<StockDz>().eq("stock_code", stockCode));
        String am_begin = siteSetting.getTransAmBeginhk();
        String am_end = siteSetting.getTransAmEndhk();
        String pm_begin = siteSetting.getTransPmBeginhk();
        String pm_end = siteSetting.getTransPmEndhk();
        boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
        boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
        log.info("是否在大宗交易时间 = {}", Boolean.valueOf(pm_flag));
        //15-15:30
//        if (!pm_flag) {
//            return ServerResponse.createByErrorMsg("下单失败，不在交易时段内");
//        }
        if (siteProduct.getHolidayDisplay()) {
            return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
        }
        if(!Objects.equals(stockDz.getPassword(), password)){
            return ServerResponse.createByErrorMsg("下单失败，密钥错误");
        }

        if (stockDz.getIsLock().intValue() != 0) {
            return ServerResponse.createByErrorMsg("下单失败，当前股票不能交易");
        }

        List dbPosition = findPositionByStockCodeAndTimes(siteSetting.getBuySameTimes().intValue(), stockDz.getStockCode(), user.getId());
        if (dbPosition.size() >= siteSetting.getBuySameNums().intValue()) {
            return ServerResponse.createByErrorMsg("频繁交易," + siteSetting.getBuySameTimes() + "分钟内同一股票持仓不得超过" + siteSetting
                    .getBuySameNums() + "条");
        }

        Integer transNum = findPositionNumByTimes(siteSetting.getBuyNumTimes().intValue(), user.getId());
        if (transNum.intValue() / 100 >= siteSetting.getBuyNumLots().intValue()) {
            return ServerResponse.createByErrorMsg("频繁交易," + siteSetting
                    .getBuyNumTimes() + "分钟内不能超过" + siteSetting.getBuyNumLots() + "手");
        }

        if (num < stockDz.getStockNum().intValue()) {
            return ServerResponse.createByErrorMsg("下单失败，购买数量最小为" + stockDz.getStockNum() + "股");
        }
        if (num > siteSetting.getBuyMaxNum()) {
            return ServerResponse.createByErrorMsg("下单失败，购买数量大于" + siteSetting.getBuyMaxNum() + "股");
        }
        BigDecimal now_price;
        StockListVO stockListVO = new StockListVO();
        //价格处理
        stockListVO = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(stockDz.getStockGid()));
        now_price = new BigDecimal(stockListVO.getNowPrice()).multiply(stockDz.getDiscount());

        if (now_price.compareTo(new BigDecimal("0")) == 0) {
            return ServerResponse.createByErrorMsg("报价0，请稍后再试");
        }


        double stock_crease = stockListVO.getHcrate().doubleValue();


        BigDecimal maxRisePercent = new BigDecimal("0");
        if (stockDz.getStockPlate() != null) {

            maxRisePercent = new BigDecimal("0.2");
            log.info("【科创股票】");
        } else {
            maxRisePercent = new BigDecimal("0.1");
            log.info("【普通A股】");
        }

        if (stockListVO.getName().startsWith("ST") || stockListVO.getName().endsWith("退")) {
            return ServerResponse.createByErrorMsg("ST和已退市的股票不能入仓");
        }

        BigDecimal zsPrice = new BigDecimal(stockListVO.getPreclose_px());

        BigDecimal ztPrice = zsPrice.multiply(maxRisePercent).add(zsPrice);
        ztPrice = ztPrice.setScale(2, 4);
        BigDecimal chaPrice = ztPrice.subtract(zsPrice);

        BigDecimal ztRate = chaPrice.multiply(new BigDecimal("100")).divide(zsPrice, 2, 4);

        log.info("当前涨跌幅 = {} % , 涨停幅度 = {} %", Double.valueOf(stock_crease), ztRate);


        if (stockDz.getStockPlate() == null || StringUtils.isEmpty(stockDz.getStockPlate())) {

            int maxcrease = siteSetting.getCreaseMaxPercent().intValue();
            if (stock_crease > 0.0D &&
                    stock_crease >= maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，股票当前涨幅:" + stock_crease + ",大于最大涨幅:" + maxcrease);
            }

            if (stock_crease < 0.0D &&
                    -stock_crease > maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，股票当前跌幅:" + stock_crease + ",大于最大跌幅:" + maxcrease);

            }

        } else if ("创业".equals(stockDz.getStockPlate())) {

            int maxcrease = siteSetting.getCyCreaseMaxPercent().intValue();
            if (stock_crease > 0.0D &&
                    stock_crease >= maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，创业股当前涨幅:" + stock_crease + ",大于最大涨幅:" + maxcrease);
            }

            if (stock_crease < 0.0D &&
                    -stock_crease > maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，创业股当前跌幅:" + stock_crease + ",大于最大跌幅:" + maxcrease);
            }
        } else {

            int maxcrease = siteSetting.getKcCreaseMaxPercent().intValue();
            if (stock_crease > 0.0D &&
                    stock_crease >= maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，科创股当前涨幅:" + stock_crease + ",大于最大涨幅:" + maxcrease);
            }

            if (stock_crease < 0.0D &&
                    -stock_crease > maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，科创股当前跌幅:" + stock_crease + ",大于最大跌幅:" + maxcrease);
            }
        }


        ServerResponse serverResponse = this.iStockService.selectRateByDaysAndStockCode(stockDz.getStockCode(), siteSetting.getStockDays().intValue());
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        BigDecimal daysRate = (BigDecimal) serverResponse.getData();
        log.info("股票 {} ， {} 天内 涨幅 {} , 设置的涨幅 = {}", new Object[]{stockDz.getStockCode(), siteSetting
                .getStockDays(), daysRate, siteSetting.getStockRate()});

        if (daysRate != null && siteSetting.getStockRate().compareTo(daysRate) == -1) {
            return serverResponse.createByErrorMsg(siteSetting.getStockDays() + "天内涨幅超过 " + siteSetting
                    .getStockRate() + "不能交易");
        }


        //BigDecimal buy_amt = now_price.multiply(new BigDecimal(buyNum.intValue())).divide(new BigDecimal(lever.intValue())).setScale(2, 4);
        BigDecimal buy_amt = now_price.multiply(new BigDecimal(num.intValue()));


        //BigDecimal buy_amt_autual = now_price.multiply(new BigDecimal(buyNum.intValue())).divide(new BigDecimal(lever.intValue()), 2, 4);
        BigDecimal buy_amt_autual = buy_amt.divide(new BigDecimal(1), 2, 4);


        int compareInt = buy_amt_autual.compareTo(new BigDecimal(siteSetting.getBuyMinAmt().intValue()));
        if (compareInt == -1) {
            return ServerResponse.createByErrorMsg("下单失败，购买金额小于" + siteSetting
                    .getBuyMinAmt() + "元");
        }


        BigDecimal max_buy_amt = user_enable_amt.multiply(siteSetting.getBuyMaxAmtPercent());
        int compareCwInt = buy_amt_autual.compareTo(max_buy_amt);
        if (compareCwInt == 1) {
            return ServerResponse.createByErrorMsg("下单失败，不能超过可用资金的" + siteSetting
                    .getBuyMaxAmtPercent().multiply(new BigDecimal("100")) + "%");
        }


        int compareUserAmtInt = user_enable_amt.compareTo(buy_amt_autual);
        log.info("用户可用金额 = {}  实际购买金额 =  {}", user_enable_amt, buy_amt_autual);
        log.info("比较 用户金额 和 实际 购买金额 =  {}", Integer.valueOf(compareUserAmtInt));
        if (compareUserAmtInt == -1) {
            return ServerResponse.createByErrorMsg("下单失败，融资可用金额小于" + buy_amt_autual + "元");
        }

//        if (user.getUserIndexAmt().compareTo(new BigDecimal("0")) == -1) {
//            return ServerResponse.createByErrorMsg("失败，指数总资金小于0");
//        }
//
        UserPosition userPosition = new UserPosition();
        userPosition.setPositionType(3);
        userPosition.setPositionSn(KeyUtils.getUniqueKey());
        userPosition.setUserId(user.getId());
        userPosition.setNickName(user.getRealName());
        userPosition.setAgentId(user.getAgentId());
        userPosition.setStockCode(stockDz.getStockCode());
        userPosition.setStockName(stockDz.getStockName());
        userPosition.setStockGid(stockDz.getStockGid());
        userPosition.setStockSpell(GetPyByChinese.converterToFirstSpell(stockDz.getStockName()));
        userPosition.setBuyOrderId(GeneratePosition.getPositionId());
        userPosition.setBuyOrderTime(new Date());
        userPosition.setBuyOrderPrice(now_price);
        userPosition.setOrderDirection( "买涨");
        userPosition.setOrderNum(num);
        if (stockDz.getStockPlate() != null) {
            userPosition.setStockPlate(stockDz.getStockPlate());
        }
        userPosition.setIsLock(Integer.valueOf(0));
        userPosition.setOrderLever(1);
        userPosition.setOrderTotalPrice(buy_amt);

        //递延费特殊处理
        BigDecimal stayFee = userPosition.getOrderTotalPrice().multiply(siteSetting.getStayFee());
        BigDecimal allStayFee = stayFee.multiply(new BigDecimal(1));
        userPosition.setOrderStayFee(allStayFee);
        userPosition.setOrderStayDays(1);


        BigDecimal buy_fee_amt = buy_amt.multiply(siteSetting.getBuyFee()).setScale(2, 4);
        log.info("用户购买手续费（配资后总资金 * 百分比） = {}", buy_fee_amt);
        userPosition.setOrderFee(buy_fee_amt);


        BigDecimal buy_yhs_amt = buy_amt.multiply(siteSetting.getDutyFee()).setScale(2, 4);
        log.info("用户购买印花税（配资后总资金 * 百分比） = {}", buy_yhs_amt);
        userPosition.setOrderSpread(buy_yhs_amt);

        SiteSpread siteSpread = iSiteSpreadService.findSpreadRateOne(new BigDecimal(stock_crease), buy_amt, stockDz.getStockCode(), now_price);
        BigDecimal spread_rate_amt = new BigDecimal("0");
        if (siteSpread != null) {
            spread_rate_amt = buy_amt.multiply(siteSpread.getSpreadRate()).setScale(2, 4);
            log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", siteSpread.getSpreadRate(), spread_rate_amt);
        } else {
            log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", "设置异常", spread_rate_amt);
        }

        userPosition.setSpreadRatePrice(spread_rate_amt);


        BigDecimal profit_and_lose = new BigDecimal("0");
        userPosition.setProfitAndLose(profit_and_lose);


        BigDecimal all_profit_and_lose = profit_and_lose.subtract(buy_fee_amt).subtract(buy_yhs_amt).subtract(spread_rate_amt);
        userPosition.setAllProfitAndLose(all_profit_and_lose);


        userPosition.setOrderStayDays(Integer.valueOf(0));
        userPosition.setOrderStayFee(new BigDecimal("0"));

        int insertPositionCount = 0;
        this.userPositionMapper.insert(userPosition);
        insertPositionCount = userPosition.getId();
        if (insertPositionCount > 0) {
            //修改用户可用余额= 当前余额-下单金额-买入手续费-印花税-点差费
            //BigDecimal reckon_enable = user_enable_amt.subtract(buy_amt_autual).subtract(buy_fee_amt).subtract(buy_yhs_amt).subtract(spread_rate_amt);
            //修改用户可用余额= 当前余额-下单总金额
            BigDecimal reckon_enable = user_enable_amt.subtract(buy_amt_autual);
            user.setEnableAmt(reckon_enable);
            int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateUserCount > 0) {
                log.info("【用户交易下单】修改用户金额成功");
            } else {
                log.error("用户交易下单】修改用户金额出错");
                throw new Exception("用户交易下单】修改用户金额出错");
            }
            //核算代理收入-入仓手续费
            iAgentAgencyFeeService.AgencyFeeIncome(1, userPosition.getPositionSn());
            log.info("【用户交易下单】保存持仓记录成功");
        } else {
            log.error("用户交易下单】保存持仓记录出错");
            throw new Exception("用户交易下单】保存持仓记录出错");
        }

        return ServerResponse.createBySuccess("大宗交易下单成功");
    }

    /**
     * vip抢筹
     * @param stockId
     * @param buyNum
     * @param buyType
     * @param lever
     * @param profitTarget
     * @param stopTarget
     * @param request
     * @return
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public ServerResponse buyVipQc(String stockCode, Integer buyNum, Integer buyType, Integer lever, BigDecimal profitTarget, BigDecimal stopTarget, HttpServletRequest request) throws Exception {

        /*实名认证开关开启*/
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        User user = this.iUserService.getCurrentRefreshUser(request);

        if (siteProduct.getRealNameDisplay() && (StringUtils.isBlank(user.getRealName()) || StringUtils.isBlank(user.getIdCard()))) {
            return ServerResponse.createByErrorMsg("下单失败，请先实名认证");
        }
        BigDecimal user_enable_amt = user.getEnableAmt();

        log.info("用户 {} 下单，股票id = {} ，数量 = {} , 方向 = {} , 杠杆 = {}", new Object[]{user
                .getId(), stockCode, buyNum, buyType, lever});
        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
            return ServerResponse.createByErrorMsg("下单失败，账户已被锁定");
        }


        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("下单出错，网站设置表不存在");
            return ServerResponse.createByErrorMsg("下单失败，系统设置错误");
        }
        if (siteSetting.getVipQcMaxAmt().compareTo(user_enable_amt) > 0) {
            return ServerResponse.createByErrorMsg("下单失败，可用余额小于"+siteSetting.getVipQcMaxAmt());
        }
        Stock stock = null;
        ServerResponse stock_res = this.iStockService.findStockByCode(stockCode);
        if (!stock_res.isSuccess()) {
            return ServerResponse.createByErrorMsg("下单失败，股票代码错误");
        }
        stock = (Stock) stock_res.getData();

        String am_begin = siteSetting.getTransAmBegin();
        String am_end = siteSetting.getTransAmEnd();
        String pm_begin = siteSetting.getTransPmBegin();
        String pm_end = siteSetting.getTransPmEnd();
        boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
        boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
        log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

        if (!am_flag && !pm_flag) {
            return ServerResponse.createByErrorMsg("下单失败，不在交易时段内");
        }
        if (siteProduct.getHolidayDisplay()) {
            return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
        }



        if (stock.getIsLock().intValue() != 0) {
            return ServerResponse.createByErrorMsg("下单失败，当前股票不能交易");
        }

        List dbPosition = findPositionByStockCodeAndTimes(siteSetting.getBuySameTimes().intValue(), stock
                .getStockCode(), user.getId());
        if (dbPosition.size() >= siteSetting.getBuySameNums().intValue()) {
            return ServerResponse.createByErrorMsg("频繁交易," + siteSetting.getBuySameTimes() + "分钟内同一股票持仓不得超过" + siteSetting
                    .getBuySameNums() + "条");
        }

        Integer transNum = findPositionNumByTimes(siteSetting.getBuyNumTimes().intValue(), user.getId());
        if (transNum.intValue() / 100 >= siteSetting.getBuyNumLots().intValue()) {
            return ServerResponse.createByErrorMsg("频繁交易," + siteSetting
                    .getBuyNumTimes() + "分钟内不能超过" + siteSetting.getBuyNumLots() + "手");
        }

        if (buyNum.intValue() < siteSetting.getBuyMinNum().intValue()) {
            return ServerResponse.createByErrorMsg("下单失败，购买数量小于" + siteSetting
                    .getBuyMinNum() + "股");
        }
        if (buyNum.intValue() > siteSetting.getBuyMaxNum().intValue()) {
            return ServerResponse.createByErrorMsg("下单失败，购买数量大于" + siteSetting
                    .getBuyMaxNum() + "股");
        }
        BigDecimal now_price;
        StockListVO stockListVO = new StockListVO();
        StockCoin stockCoin = new StockCoin();
        //股票
        stockListVO = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(stock.getStockGid()));
        now_price = new BigDecimal(stockListVO.getNowPrice());
        if (now_price.compareTo(new BigDecimal("0")) == 0) {
            return ServerResponse.createByErrorMsg("报价0，请稍后再试");
        }


        double stock_crease = stockListVO.getHcrate().doubleValue();


        BigDecimal maxRisePercent = new BigDecimal("0");
        if (stock.getStockPlate() != null) {
            maxRisePercent = new BigDecimal("0.2");
            //maxRisePercent=siteSetting.getKcCreaseMaxPercent().divide(new BigDecimal("100"),2, RoundingMode.DOWN);
            log.info("【科创股票】");
        } else {
            maxRisePercent = new BigDecimal("0.1");
            //maxRisePercent = siteSetting.getCreaseMaxPercent().divide(new BigDecimal("100"),2, RoundingMode.DOWN);
            log.info("【普通A股】");
        }

        if (stockListVO.getName().startsWith("ST") || stockListVO.getName().endsWith("退")) {
            return ServerResponse.createByErrorMsg("ST和已退市的股票不能入仓");
        }

        BigDecimal zsPrice = new BigDecimal(stockListVO.getPreclose_px());

        BigDecimal ztPrice = zsPrice.multiply(maxRisePercent).add(zsPrice);
        ztPrice = ztPrice.setScale(2, 4);
        BigDecimal chaPrice = ztPrice.subtract(zsPrice);

        BigDecimal ztRate = chaPrice.multiply(new BigDecimal("100")).divide(zsPrice, 2, 4);

        log.info("当前涨跌幅 = {} % , 涨停幅度 = {} %", Double.valueOf(stock_crease), ztRate);
//        if ((new BigDecimal(String.valueOf(stock_crease))).compareTo(ztRate) == 0 && buyType
//                .intValue() == 0) {
//            return ServerResponse.createByErrorMsg("当前股票已涨停不能买涨");
//        }


        if (stock.getStockPlate() == null || StringUtils.isEmpty(stock.getStockPlate())) {

            int maxcrease = siteSetting.getCreaseMaxPercent().intValue();
            if (stock_crease > 0.0D &&
                    stock_crease >= maxcrease) {
                return  ServerResponse.createBySuccess("当前交易不可买入");
                //return ServerResponse.createByErrorMsg("下单失败，股票当前涨幅:" + stock_crease + ",大于最大涨幅:" + maxcrease);
            }


            if (stock_crease < 0.0D &&
                    -stock_crease > maxcrease) {
                //return ServerResponse.createByErrorMsg("下单失败，股票当前跌幅:" + stock_crease + ",大于最大跌幅:" + maxcrease);
                return  ServerResponse.createBySuccess("当前交易不可买入");
            }

        } else if ("创业".equals(stock.getStockPlate())) {

            int maxcrease = siteSetting.getCyCreaseMaxPercent().intValue();
            if (stock_crease > 0.0D &&
                    stock_crease >= maxcrease) {
                return  ServerResponse.createBySuccess("当前交易不可买入");
                //return ServerResponse.createByErrorMsg("下单失败，创业股当前涨幅:" + stock_crease + ",大于最大涨幅:" + maxcrease);
            }


            if (stock_crease < 0.0D &&
                    -stock_crease > maxcrease) {
                return  ServerResponse.createBySuccess("当前交易不可买入");
                // return ServerResponse.createByErrorMsg("下单失败，创业股当前跌幅:" + stock_crease + ",大于最大跌幅:" + maxcrease);
            }
        } else {

            int maxcrease = siteSetting.getKcCreaseMaxPercent().intValue();
            if (stock_crease > 0.0D &&
                    stock_crease >= maxcrease) {
                return  ServerResponse.createBySuccess("当前交易不可买入");
                //   return ServerResponse.createByErrorMsg("下单失败，科创股当前涨幅:" + stock_crease + ",大于最大涨幅:" + maxcrease);
            }


            if (stock_crease < 0.0D &&
                    -stock_crease > maxcrease) {
                return  ServerResponse.createBySuccess("当前交易不可买入");
                // return ServerResponse.createByErrorMsg("下单失败，科创股当前跌幅:" + stock_crease + ",大于最大跌幅:" + maxcrease);
            }
        }


        ServerResponse serverResponse = this.iStockService.selectRateByDaysAndStockCode(stock
                .getStockCode(), siteSetting.getStockDays().intValue());
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        BigDecimal daysRate = (BigDecimal) serverResponse.getData();
        log.info("股票 {} ， {} 天内 涨幅 {} , 设置的涨幅 = {}", new Object[]{stock.getStockCode(), siteSetting
                .getStockDays(), daysRate, siteSetting.getStockRate()});

        if (daysRate != null &&
                siteSetting.getStockRate().compareTo(daysRate) == -1) {
            return serverResponse.createByErrorMsg(siteSetting.getStockDays() + "天内涨幅超过 " + siteSetting
                    .getStockRate() + "不能交易");
        }


        //BigDecimal buy_amt = now_price.multiply(new BigDecimal(buyNum.intValue())).divide(new BigDecimal(lever.intValue())).setScale(2, 4);
        BigDecimal buy_amt = now_price.multiply(new BigDecimal(buyNum.intValue()));


        //BigDecimal buy_amt_autual = now_price.multiply(new BigDecimal(buyNum.intValue())).divide(new BigDecimal(lever.intValue()), 2, 4);
        BigDecimal buy_amt_autual = buy_amt.divide(new BigDecimal(lever.intValue()), 2, 4);


        int compareInt = buy_amt_autual.compareTo(new BigDecimal(siteSetting.getBuyMinAmt().intValue()));
        if (compareInt == -1) {
            return ServerResponse.createByErrorMsg("下单失败，购买金额小于" + siteSetting
                    .getBuyMinAmt() + "元");
        }


        BigDecimal max_buy_amt = user_enable_amt.multiply(siteSetting.getBuyMaxAmtPercent());
        int compareCwInt = buy_amt_autual.compareTo(max_buy_amt);
        if (compareCwInt == 1) {
            return ServerResponse.createByErrorMsg("下单失败，不能超过可用资金的" + siteSetting
                    .getBuyMaxAmtPercent().multiply(new BigDecimal("100")) + "%");
        }


        int compareUserAmtInt = user_enable_amt.compareTo(buy_amt_autual);
        log.info("用户可用金额 = {}  实际购买金额 =  {}", user_enable_amt, buy_amt_autual);
        log.info("比较 用户金额 和 实际 购买金额 =  {}", Integer.valueOf(compareUserAmtInt));
        if (compareUserAmtInt == -1) {
            return ServerResponse.createByErrorMsg("下单失败，可用金额小于" + buy_amt_autual + "元");
        }

//        if (user.getUserIndexAmt().compareTo(new BigDecimal("0")) == -1) {
//            return ServerResponse.createByErrorMsg("失败，指数总资金小于0");
//        }

        UserPosition userPosition = new UserPosition();

        if (profitTarget != null && profitTarget.compareTo(new BigDecimal("0")) > 0) {
            userPosition.setProfitTargetPrice(profitTarget);
        }
        if (stopTarget != null && stopTarget.compareTo(new BigDecimal("0")) > 0) {
            userPosition.setStopTargetPrice(stopTarget);
        }


        userPosition.setPositionType(user.getAccountType());
        userPosition.setPositionSn(KeyUtils.getUniqueKey());
        userPosition.setUserId(user.getId());
        userPosition.setNickName(user.getRealName());
        userPosition.setAgentId(user.getAgentId());
        userPosition.setStockCode(stock.getStockCode());
        userPosition.setStockName(stock.getStockName());
        userPosition.setStockGid(stock.getStockGid());
        userPosition.setStockSpell(stock.getStockSpell());
        userPosition.setBuyOrderId(GeneratePosition.getPositionId());
        userPosition.setBuyOrderTime(new Date());
        userPosition.setBuyOrderPrice(now_price);
        userPosition.setOrderDirection((buyType.intValue() == 0) ? "买涨" : "买跌");

        userPosition.setOrderNum(buyNum);


        if (stock.getStockPlate() != null) {
            userPosition.setStockPlate(stock.getStockPlate());
        }


        userPosition.setIsLock(Integer.valueOf(0));


        userPosition.setOrderLever(lever);


        userPosition.setOrderTotalPrice(buy_amt);

        //递延费特殊处理
        BigDecimal stayFee = userPosition.getOrderTotalPrice().multiply(siteSetting.getStayFee());
        BigDecimal allStayFee = stayFee.multiply(new BigDecimal(1));
        userPosition.setOrderStayFee(allStayFee);
        userPosition.setOrderStayDays(1);


        BigDecimal buy_fee_amt = buy_amt.multiply(siteSetting.getBuyFee()).setScale(2, 4);
        log.info("用户购买手续费（配资后总资金 * 百分比） = {}", buy_fee_amt);
        userPosition.setOrderFee(buy_fee_amt);


        BigDecimal buy_yhs_amt = buy_amt.multiply(siteSetting.getDutyFee()).setScale(2, 4);
        log.info("用户购买印花税（配资后总资金 * 百分比） = {}", buy_yhs_amt);
        userPosition.setOrderSpread(buy_yhs_amt);

        SiteSpread siteSpread = iSiteSpreadService.findSpreadRateOne(new BigDecimal(stock_crease), buy_amt, stock.getStockCode(), now_price);
        BigDecimal spread_rate_amt = new BigDecimal("0");
        if (siteSpread != null) {
            spread_rate_amt = buy_amt.multiply(siteSpread.getSpreadRate()).setScale(2, 4);
            log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", siteSpread.getSpreadRate(), spread_rate_amt);
        } else {
            log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", "设置异常", spread_rate_amt);
        }

        userPosition.setSpreadRatePrice(spread_rate_amt);


        BigDecimal profit_and_lose = new BigDecimal("0");
        userPosition.setProfitAndLose(profit_and_lose);


        BigDecimal all_profit_and_lose = profit_and_lose.subtract(buy_fee_amt).subtract(buy_yhs_amt).subtract(spread_rate_amt);
        userPosition.setAllProfitAndLose(all_profit_and_lose);


        userPosition.setOrderStayDays(Integer.valueOf(0));
        userPosition.setOrderStayFee(new BigDecimal("0"));

        int insertPositionCount = 0;
        this.userPositionMapper.insert(userPosition);
        insertPositionCount = userPosition.getId();
        if (insertPositionCount > 0) {
            //修改用户可用余额= 当前余额-下单金额-买入手续费-印花税-点差费
            //BigDecimal reckon_enable = user_enable_amt.subtract(buy_amt_autual).subtract(buy_fee_amt).subtract(buy_yhs_amt).subtract(spread_rate_amt);
            //修改用户可用余额= 当前余额-下单总金额
            BigDecimal reckon_enable = user_enable_amt.subtract(buy_amt_autual);
            user.setEnableAmt(reckon_enable);
            int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateUserCount > 0) {
                log.info("【用户交易下单】修改用户金额成功");
            } else {
                log.error("用户交易下单】修改用户金额出错");
                throw new Exception("用户交易下单】修改用户金额出错");
            }
            //核算代理收入-入仓手续费
            iAgentAgencyFeeService.AgencyFeeIncome(1, userPosition.getPositionSn());
            log.info("【用户交易下单】保存持仓记录成功");
        } else {
            log.error("用户交易下单】保存持仓记录出错");
            throw new Exception("用户交易下单】保存持仓记录出错");
        }

        return ServerResponse.createBySuccess("vip抢筹下单成功");
    }

    @Override
    public ServerResponse buy2(String positionSn, Integer buyNum, HttpServletRequest request) throws Exception  {
        // 判断周末不能买
        Date today = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        /*int weekday = c.get(Calendar.DAY_OF_WEEK);
        if (weekday == 1) {
            return ServerResponse.createByErrorMsg("周末不能购买！");
        }
        if (weekday == 7) {
            return ServerResponse.createByErrorMsg("周末不能购买！");
        }*/

        UserPosition currPosition = this.userPositionMapper.findPositionBySn(positionSn);

        Integer buyType = 0;

        if (StringUtils.equalsIgnoreCase(currPosition.getOrderDirection(), "买跌")) {
            buyType = 1;
        }

        Integer lever = currPosition.getOrderLever();

        ServerResponse<Stock> stock_res = this.iStockService.findStockByCode(currPosition.getStockCode());

        /*实名认证开关开启*/
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (siteProduct.getRealNameDisplay() && (StringUtils.isBlank(user.getRealName()) || StringUtils.isBlank(user.getIdCard()))) {
            return ServerResponse.createByErrorMsg("下单失败，请先实名认证");
        }
        BigDecimal user_enable_amt = user.getEnableAmt();
        log.info("用户 {} 下单，股票id = {} ，数量 = {} , 方向 = {} , 杠杆 = {}", new Object[]{user
                .getId(), currPosition.getStockCode(), buyNum, buyType, lever});
        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
            return ServerResponse.createByErrorMsg("下单失败，账户已被锁定");
        }


        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("下单出错，网站设置表不存在");
            return ServerResponse.createByErrorMsg("下单失败，系统设置错误");
        }
        Stock stock = null;
        if (!stock_res.isSuccess()) {
            return ServerResponse.createByErrorMsg("下单失败，股票代码错误");
        }
        stock = (Stock) stock_res.getData();
        if (Objects.equals(stock.getStockType(), "us")) {
            String am_begin = siteSetting.getTransAmBeginUs();
            String am_end = siteSetting.getTransAmEndUs();
            String pm_begin = siteSetting.getTransPmBeginUs();
            String pm_end = siteSetting.getTransPmEndUs();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("下单失败，不在美股交易时段内");
            }

        }else if(Objects.equals(stock.getStockType(), "hk")){
            String am_begin = siteSetting.getTransAmBeginhk();
            String am_end = siteSetting.getTransAmEndhk();
            String pm_begin = siteSetting.getTransPmBeginhk();
            String pm_end = siteSetting.getTransPmEndhk();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("下单失败，不在港股股交易时段内");
            }
            if (siteProduct.getHolidayDisplay()) {
                return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
            }
        }else {
            String am_begin = siteSetting.getTransAmBegin();
            String am_end = siteSetting.getTransAmEnd();
            String pm_begin = siteSetting.getTransPmBegin();
            String pm_end = siteSetting.getTransPmEnd();
            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
            boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
            log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));

            if (!am_flag && !pm_flag) {
                return ServerResponse.createByErrorMsg("下单失败，不在交易时段内");
            }
            if (siteProduct.getHolidayDisplay()) {
                return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
            }
        }



        if (stock.getIsLock().intValue() != 0) {
            return ServerResponse.createByErrorMsg("下单失败，当前股票不能交易");
        }

        List dbPosition = findPositionByStockCodeAndTimes(siteSetting.getBuySameTimes().intValue(), stock
                .getStockCode(), user.getId());
        if (dbPosition.size() >= siteSetting.getBuySameNums().intValue()) {
            return ServerResponse.createByErrorMsg("频繁交易," + siteSetting.getBuySameTimes() + "分钟内同一股票持仓不得超过" + siteSetting
                    .getBuySameNums() + "条");
        }

        Integer transNum = findPositionNumByTimes(siteSetting.getBuyNumTimes().intValue(), user.getId());
        if (transNum.intValue() / 100 >= siteSetting.getBuyNumLots().intValue()) {
            return ServerResponse.createByErrorMsg("频繁交易," + siteSetting
                    .getBuyNumTimes() + "分钟内不能超过" + siteSetting.getBuyNumLots() + "手");
        }

        if (buyNum.intValue() < siteSetting.getBuyMinNum().intValue()) {
            return ServerResponse.createByErrorMsg("下单失败，购买数量小于" + siteSetting
                    .getBuyMinNum() + "股");
        }
        if (buyNum.intValue() > siteSetting.getBuyMaxNum().intValue()) {
            return ServerResponse.createByErrorMsg("下单失败，购买数量大于" + siteSetting
                    .getBuyMaxNum() + "股");
        }
        BigDecimal now_price;
        StockListVO stockListVO = new StockListVO();
        StockCoin stockCoin = new StockCoin();
        //股票类型 现价 数据源的处理

        stockListVO = wjStockApi.getStockListVo(stock);
        now_price = new BigDecimal(stockListVO.getNowPrice());

        if (now_price.compareTo(new BigDecimal("0")) == 0) {
            return ServerResponse.createByErrorMsg("报价0，请稍后再试");
        }

        double stock_crease = stockListVO.getHcrate().doubleValue();

        BigDecimal maxRisePercent = new BigDecimal("0");
        if (stock.getStockPlate() != null) {

            maxRisePercent = new BigDecimal("0.2");
            log.info("【科创股票】");
        } else {
            maxRisePercent = new BigDecimal("0.1");
            log.info("【普通A股】");
        }

        if (stockListVO.getName().startsWith("ST") || stockListVO.getName().endsWith("退")) {
            return ServerResponse.createByErrorMsg("ST和已退市的股票不能入仓");
        }

        BigDecimal zsPrice = stockListVO.getYc();

        BigDecimal ztPrice = zsPrice.multiply(maxRisePercent).add(zsPrice);
        ztPrice = ztPrice.setScale(2, 4);
        BigDecimal chaPrice = ztPrice.subtract(zsPrice);

        BigDecimal ztRate = chaPrice.multiply(new BigDecimal("100")).divide(zsPrice, 2, 4);

        log.info("当前涨跌幅 = {} % , 涨停幅度 = {} %", Double.valueOf(stock_crease), ztRate);
        String nowDate = DateTimeUtil.stampToDate(String.valueOf(System.currentTimeMillis()));
        StockSubscribe stockSubscribeListQc = this.stockSubscribeMapper.selectOne(new QueryWrapper<StockSubscribe>().eq("code",stock.getStockCode()).eq("list_date",nowDate));
        if(stockSubscribeListQc == null) {
            if ((new BigDecimal(String.valueOf(stock_crease))).compareTo(ztRate) == 0 && buyType
                    .intValue() == 0) {
                return ServerResponse.createByErrorMsg("当前股票已涨停不能买涨");
            }

        }
        if (stock.getStockPlate() == null || StringUtils.isEmpty(stock.getStockPlate())) {

            int maxcrease = siteSetting.getCreaseMaxPercent().intValue();
            if (stock_crease > 0.0D &&
                    stock_crease >= maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，股票当前涨幅:" + stock_crease + ",大于最大涨幅:" + maxcrease);
            }


            if (stock_crease < 0.0D &&
                    -stock_crease > maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，股票当前跌幅:" + stock_crease + ",大于最大跌幅:" + maxcrease);

            }

        } else if ("创业".equals(stock.getStockPlate())) {

            int maxcrease = siteSetting.getCyCreaseMaxPercent().intValue();
            if (stock_crease > 0.0D &&
                    stock_crease >= maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，创业股当前涨幅:" + stock_crease + ",大于最大涨幅:" + maxcrease);
            }


            if (stock_crease < 0.0D &&
                    -stock_crease > maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，创业股当前跌幅:" + stock_crease + ",大于最大跌幅:" + maxcrease);
            }
        } else {

            int maxcrease = siteSetting.getKcCreaseMaxPercent().intValue();
            if (stock_crease > 0.0D &&
                    stock_crease >= maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，科创股当前涨幅:" + stock_crease + ",大于最大涨幅:" + maxcrease);
            }


            if (stock_crease < 0.0D &&
                    -stock_crease > maxcrease) {
                return ServerResponse.createByErrorMsg("下单失败，科创股当前跌幅:" + stock_crease + ",大于最大跌幅:" + maxcrease);
            }
        }


        ServerResponse serverResponse = this.iStockService.selectRateByDaysAndStockCode(stock
                .getStockCode(), siteSetting.getStockDays().intValue());
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        BigDecimal daysRate = (BigDecimal) serverResponse.getData();
        log.info("股票 {} ， {} 天内 涨幅 {} , 设置的涨幅 = {}", new Object[]{stock.getStockCode(), siteSetting
                .getStockDays(), daysRate, siteSetting.getStockRate()});

        if (daysRate != null &&
                siteSetting.getStockRate().compareTo(daysRate) == -1) {
            return serverResponse.createByErrorMsg(siteSetting.getStockDays() + "天内涨幅超过 " + siteSetting
                    .getStockRate() + "不能交易");
        }


        BigDecimal buy_amt = now_price.multiply(new BigDecimal(buyNum.intValue()));


        BigDecimal buy_amt_autual = buy_amt.divide(new BigDecimal(lever.intValue()), 2, 4);


        int compareInt = buy_amt_autual.compareTo(new BigDecimal(siteSetting.getBuyMinAmt().intValue()));
        if (compareInt == -1) {
            return ServerResponse.createByErrorMsg("下单失败，购买金额小于" + siteSetting
                    .getBuyMinAmt() + "元");
        }


        BigDecimal max_buy_amt = user_enable_amt.multiply(siteSetting.getBuyMaxAmtPercent());
        int compareCwInt = buy_amt_autual.compareTo(max_buy_amt);
        if (compareCwInt == 1) {
            return ServerResponse.createByErrorMsg("下单失败，不能超过可用资金的" + siteSetting
                    .getBuyMaxAmtPercent().multiply(new BigDecimal("100")) + "%");
        }


        int compareUserAmtInt = user_enable_amt.compareTo(buy_amt_autual);
        log.info("用户可用金额 = {}  实际购买金额 =  {}", user_enable_amt, buy_amt_autual);
        log.info("比较 用户金额 和 实际 购买金额 =  {}", Integer.valueOf(compareUserAmtInt));
        if (compareUserAmtInt == -1) {
            return ServerResponse.createByErrorMsg("下单失败，融资可用金额小于" + buy_amt_autual + "元");
        }

        if (user.getUserIndexAmt().compareTo(new BigDecimal("0")) == -1) {
            return ServerResponse.createByErrorMsg("失败，指数总资金小于0");
        }
//        if (user.getUserFutAmt().compareTo(new BigDecimal("0")) == -1) {
//            return ServerResponse.createByErrorMsg("失败，期货总资金小于0");
//        }
        UserPosition updatePosition = new UserPosition();
        BeanUtils.copyProperties(currPosition, updatePosition);


        BigDecimal totalNum = BigDecimal.valueOf(currPosition.getOrderNum() + buyNum);
        BigDecimal totalAmt = currPosition.getOrderTotalPrice().add(buy_amt);
        BigDecimal avg_price = totalAmt.divide(totalNum, BigDecimal.ROUND_HALF_UP);


        updatePosition.setBuyOrderPrice(avg_price);
        updatePosition.setOrderNum(totalNum.intValue());
        updatePosition.setOrderTotalPrice(totalAmt);
        updatePosition.setBuyOrderTime(currPosition.getBuyOrderTime());

        updatePosition.setOrderLever(currPosition.getOrderLever());
        //递延费特殊处理
        Integer days = GetStayDays.durationDays(currPosition.getBuyOrderTime());
        BigDecimal orderStayFee = this.getStayFee(updatePosition);
        updatePosition.setOrderStayDays(days);
        updatePosition.setOrderStayFee(orderStayFee);



        BigDecimal buy_fee_amt = buy_amt.multiply(siteSetting.getBuyFee()).setScale(2, 4);
        log.info("用户购买手续费（配资后总资金 * 百分比） = {}", buy_fee_amt);
        updatePosition.setOrderFee(currPosition.getOrderFee().add(buy_fee_amt));


        BigDecimal buy_yhs_amt = buy_amt.multiply(siteSetting.getDutyFee()).setScale(2, 4);
        log.info("用户购买印花税（配资后总资金 * 百分比） = {}", buy_yhs_amt);
        updatePosition.setOrderSpread(currPosition.getOrderSpread().add(buy_yhs_amt));

        SiteSpread siteSpread = iSiteSpreadService.findSpreadRateOne(new BigDecimal(stock_crease), buy_amt, stock.getStockCode(), now_price);
        BigDecimal spread_rate_amt = new BigDecimal("0");
        if (siteSpread != null) {
            spread_rate_amt = buy_amt.multiply(siteSpread.getSpreadRate()).setScale(2, 4);
            log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", siteSpread.getSpreadRate(), spread_rate_amt);
        } else {
            log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", "设置异常", spread_rate_amt);
        }

        updatePosition.setSpreadRatePrice(currPosition.getSpreadRatePrice().add(spread_rate_amt));


        BigDecimal subPrice = (now_price).subtract(avg_price);
        BigDecimal profitAndLose = subPrice.multiply(totalNum);
        if ("买跌".equals(currPosition.getOrderDirection())) {
            profitAndLose = profitAndLose.negate();
        }

        //总盈亏= 浮动盈亏 – 手续费 – 印花税 – 留仓费 – 点差费
        BigDecimal allProfitAndLose = profitAndLose.subtract(updatePosition.getOrderFee())
                .subtract(updatePosition.getOrderSpread())
                .subtract(updatePosition.getSpreadRatePrice());

        updatePosition.setProfitAndLose(profitAndLose);


        updatePosition.setAllProfitAndLose(allProfitAndLose);

        updatePosition.setSellOrderFee(BigDecimal.ZERO);

        updatePosition.setId(currPosition.getId());

        int insertPositionCount = 0;
        insertPositionCount = this.userPositionMapper.updateByPrimaryKeySelective(updatePosition);


        UserPostionDetail userPostionDetail = new UserPostionDetail();
        userPostionDetail.setPosId(updatePosition.getId());
        userPostionDetail.setBuyOrderTime(new Date());
        userPostionDetail.setOrderNum(buyNum);
        userPostionDetail.setBuyOrderPrice(now_price);

        postionDetailMapper.insert(userPostionDetail);

        if (insertPositionCount > 0) {
            //修改用户可用余额= 当前余额-下单金额-买入手续费-印花税-点差费
            //BigDecimal reckon_enable = user_enable_amt.subtract(buy_amt_autual).subtract(buy_fee_amt).subtract(buy_yhs_amt).subtract(spread_rate_amt);
            //修改用户可用余额= 当前余额-下单总金额
            BigDecimal reckon_enable = user_enable_amt.subtract(buy_amt_autual);
            user.setEnableAmt(reckon_enable);
            int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateUserCount > 0) {
                log.info("【用户交易下单】修改用户金额成功");

                UserCashDetail ucd = new UserCashDetail();
                ucd.setPositionId(updatePosition.getId());
                ucd.setAgentId(user.getAgentId());
                ucd.setAgentName(user.getAgentName());
                ucd.setUserId(user.getId());
                ucd.setUserName(user.getRealName());
                ucd.setDeType("买入股票");
                ucd.setDeAmt(buy_amt_autual);
                ucd.setDeSummary(String.format("补仓，%s/%s,保证金：%s,买入价格：%s，买入股数：%d，买入手续费：%s,印花税：%s",
                        updatePosition.getStockCode(), updatePosition.getStockName(), BigDecimalUtil.format(buy_amt_autual),
                        BigDecimalUtil.format(now_price), buyNum, BigDecimalUtil.format(buy_fee_amt),
                        BigDecimalUtil.format(buy_yhs_amt)));
                ucd.setAddTime(new Date());
                ucd.setIsRead(Integer.valueOf(0));

                int insertSxfCount = this.userCashDetailMapper.insert(ucd);


            } else {
                log.error("用户交易下单】修改用户金额出错");
                throw new Exception("用户交易下单】修改用户金额出错");
            }
            //核算代理收入-入仓手续费
            iAgentAgencyFeeService.AgencyFeeIncome(5, currPosition.getPositionSn(), buy_fee_amt);
            log.info("【用户交易下单】保存持仓记录成功");
        } else {
            log.error("用户交易下单】保存持仓记录出错");
            throw new Exception("用户交易下单】保存持仓记录出错");
        }

        return ServerResponse.createBySuccess("下单成功", updatePosition);
    }

    public Integer getCanSellNum(UserPosition position) {
        LambdaQueryWrapper<UserPostionDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPostionDetail::getPosId, position.getId());
        List<UserPostionDetail> list = postionDetailMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(list)) {
            if (isCanSell(position.getBuyOrderTime())) {

                return position.getOrderNum();
            }
            return 0;
        }

         return position.getOrderNum() - list.stream()
                .filter(detail -> !isCanSell(detail.getBuyOrderTime()))
                .map(UserPostionDetail::getOrderNum)
                .reduce((a, b) -> a + b)
                .orElse(0);

    }

    @Override
    public ServerResponse getCanSellNum(String positionSn) {
        UserPosition userPosition = this.userPositionMapper.findPositionBySn(positionSn);

        Integer canSellNum = getCanSellNum(userPosition);

        return ServerResponse.createBySuccess(canSellNum);

    }

    public static void main(String[] args) throws ParseException {
        Date date = DateUtils.parseDate("2042-5-1", "yyyy-MM-dd");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

        System.out.println(date);
    }
}



