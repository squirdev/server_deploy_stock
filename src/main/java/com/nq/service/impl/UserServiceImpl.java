package com.nq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.nq.common.ServerResponse;
import com.nq.config.StockPoll;
import com.nq.dao.*;
import com.nq.pojo.*;
import com.nq.service.*;
import com.nq.utils.DateTimeUtil;
import com.nq.utils.PropertiesUtil;
import com.nq.utils.SymmetricCryptoUtil;
import com.nq.utils.ip.IpUtils;
import com.nq.utils.ip.JuheIpApi;
import com.nq.utils.redis.CookieUtils;
import com.nq.utils.redis.JsonUtil;
import com.nq.utils.redis.RedisShardedPoolUtils;
import com.nq.utils.stock.sina.SinaStockApi;
import com.nq.utils.stock.wangji.WjStockApi;
import com.nq.utils.stock.wangji.vo.WjStockExtInfoDTO;
import com.nq.vo.agent.AgentUserListVO;
import com.nq.vo.futuresposition.FuturesPositionVO;
import com.nq.vo.indexposition.IndexPositionVO;
import com.nq.vo.position.ForceSellPositonDTO;
import com.nq.vo.position.PositionProfitVO;
import com.nq.vo.position.PositionVO;
import com.nq.vo.stock.StockListVO;
import com.nq.vo.user.UserInfoVO;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserMapper userMapper;

    @Autowired
    IAgentUserService iAgentUserService;

    @Autowired
    ISiteLoginLogService iSiteLoginLogService;

    @Autowired
    StockOptionMapper stockOptionMapper;

    @Autowired
    StockMapper stockMapper;
    @Autowired
    IUserPositionService iUserPositionService;
    @Autowired
    IUserBankService iUserBankService;
    @Autowired
    AgentUserMapper agentUserMapper;
    @Autowired
    SiteTaskLogMapper siteTaskLogMapper;
    @Autowired
    IStockOptionService iStockOptionService;
    @Autowired
    ISiteSettingService iSiteSettingService;
    @Autowired
    IUserCashDetailService iUserCashDetailService;
    @Autowired
    IUserRechargeService iUserRechargeService;
    @Autowired
    IUserWithdrawService iUserWithdrawService;
    @Autowired
    IUserIndexPositionService iUserIndexPositionService;
    @Autowired
    ISiteIndexSettingService iSiteIndexSettingService;
    @Autowired
    StockPoll stockPoll;
    @Autowired
    SiteAmtTransLogMapper siteAmtTransLogMapper;
    @Autowired
    IUserFuturesPositionService iUserFuturesPositionService;
    @Autowired
    ISiteFuturesSettingService iSiteFuturesSettingService;
    @Autowired
    IStockFuturesService iStockFuturesService;
    @Autowired
    StockFuturesMapper stockFuturesMapper;
    @Autowired
    StockIndexMapper stockIndexMapper;
    @Autowired
    ISiteMessageService iSiteMessageService;
    @Autowired
    IUserService iUserService;

    @Autowired
    IUserRechargeService userRechargeService;

    @Autowired
    private ISiteAdminService siteAdminService;

    @Autowired
    private WjStockApi wjStockApi;

    public ServerResponse reg(String yzmCode, String phone, String userPwd, String uid, HttpServletRequest request) {
        if (StringUtils.isBlank(phone) ||
                StringUtils.isBlank(userPwd) || StringUtils.isBlank(yzmCode))
        {
            return ServerResponse.createByErrorMsg("注册失败, 参数不能为空");
        }

        String original = RedisShardedPoolUtils.get("code_" + uid);

        if (!yzmCode.equalsIgnoreCase(original)) {
            return ServerResponse.createByErrorMsg("验证码错误,请重新输入");
        }

//        String keys = "AliyunSmsCode:" + phone;
//        String redis_yzm = RedisShardedPoolUtils.get(keys);

//        log.info("redis_yzm = {},yzmCode = {}", redis_yzm, yzmCode);
//        if (!yzmCode.equals(redis_yzm) && !"6666".equals(yzmCode)) {
//            return ServerResponse.createByErrorMsg("注册失败, 验证码错误");
//        }


//        AgentUser agentUser = this.iAgentUserService.findByCode(agentCode);
//        if (agentUser == null) {
//            return ServerResponse.createByErrorMsg("注册失败, 代理不存在");
//        }
//        if (agentUser.getIsLock().intValue() == 1) {
//            return ServerResponse.createByErrorMsg("注册失败, 代理已被锁定");
//        }
//

        User dbuser = this.userMapper.findByPhone(phone);
        if (dbuser != null) {
            return ServerResponse.createByErrorMsg("注册失败, 手机号已注册");
        }


        User user = new User();
        user.setAgentId(0);
        user.setAgentName("无代理");
        user.setPhone(phone);
        user.setUserPwd(SymmetricCryptoUtil.encryptPassword(userPwd));


        user.setAccountType(Integer.valueOf(0));
        user.setIsLock(Integer.valueOf(1));
        user.setIsActive(Integer.valueOf(0));

        user.setRegTime(new Date());
        String uip = IpUtils.getIp(request);
        user.setRegIp(uip);
        String uadd = JuheIpApi.ip2Add(uip);
        user.setRegAddress(uadd);

        user.setIsLogin(Integer.valueOf(0));

        user.setUserAmt(new BigDecimal("0"));
        user.setEnableAmt(new BigDecimal("0"));

        user.setUserIndexAmt(new BigDecimal("0"));
        user.setEnableIndexAmt(new BigDecimal("0"));

        user.setUserFutAmt(new BigDecimal("0"));
        user.setEnableFutAmt(new BigDecimal("0"));

        user.setSumBuyAmt(new BigDecimal("0"));
        user.setSumChargeAmt(new BigDecimal("0"));
        user.setDjzj(new BigDecimal("0"));

        int insertCount = this.userMapper.insert(user);

        if (insertCount > 0) {
            log.info("用户注册成功 手机 {} , ip = {} 地址 = {}", new Object[] { phone, uip, uadd });
            return ServerResponse.createBySuccessMsg("注册成功.请登录");
        }
        return ServerResponse.createBySuccessMsg("注册出错, 请重试");
    }



    public ServerResponse login(String phone, String userPwd, HttpServletRequest request) {
        if (StringUtils.isBlank(phone) || StringUtils.isBlank(userPwd)) {
            return ServerResponse.createByErrorMsg("手机号和密码不能为空");
        }
        userPwd = SymmetricCryptoUtil.encryptPassword(userPwd);
        User user = this.userMapper.login(phone, userPwd);
        if (user != null) {
            if (user.getIsLogin().intValue() == 1) {
                return ServerResponse.createByErrorMsg("登陆失败, 账户被锁定");
            }

            log.info("用户{}登陆成功, 登陆状态{} ,交易状态{}", new Object[] { user.getId(), user.getIsLogin(), user.getIsLock() });

            this.iSiteLoginLogService.saveLog(user, request);
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMsg("登陆失败，用户名密码错误");
    }




    public User getCurrentUser(HttpServletRequest request) {
        String property = PropertiesUtil.getProperty("user.cookie.name");
//        System.out.println(property);
        String loginToken = request.getHeader(property);
        if (loginToken == null) {
            return null;
        }
        String userJson = RedisShardedPoolUtils.get(loginToken);
        return (User)JsonUtil.string2Obj(userJson, User.class);
    }



    public User getCurrentRefreshUser(HttpServletRequest request) {
        String property = PropertiesUtil.getProperty("user.cookie.name");
        String header = request.getHeader(property);
        if (header == null) {
            return null;
        }
//        String loginToken = CookieUtils.readLoginToken(request, PropertiesUtil.getProperty("user.cookie.name"));
        String userJson = RedisShardedPoolUtils.get(header);
        User user = (User) JsonUtil.string2Obj(userJson, User.class);

        if (user == null) {
            return null;
        } else {
            return this.userMapper.selectByPrimaryKey(user.getId());
        }
    }

    public ServerResponse addOption(String code, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        String stockcode = code;
        if(code.contains("hf")){
            stockcode = code.split("_")[1];
        }
        stockcode = stockcode.replace("sh","").replace("sz","").replace("bj","");
        StockOption dboption = this.stockOptionMapper.findMyOptionIsExistByCode(user.getId(), stockcode);

        if (dboption != null) {
            return ServerResponse.createByErrorMsg("添加失败，自选股已存在");
        }

        Stock stock = new Stock();
        //期货逻辑
        if(code.contains("hf")){
            StockFutures stockFutures = this.stockFuturesMapper.selectFuturesByCode(stockcode);
            if(stockFutures != null){
                stock.setId(stockFutures.getId());
                stock.setStockCode(stockFutures.getFuturesCode());
                stock.setStockGid(stockFutures.getFuturesGid());
                stock.setStockName(stockFutures.getFuturesName());
                stock.setIsLock(0);
            }
        } else if(code.contains("sh") || code.contains("sz")){
         return ServerResponse.createByErrorMsg("添加失败，指数不支持自选");
//            StockIndex stockIndex = this.stockIndexMapper.selectIndexByCode(stockcode);
//            if(stockIndex != null){
//                stock.setId(stockIndex.getId());
//                stock.setStockCode(stockIndex.getIndexCode());
//                stock.setStockGid(stockIndex.getIndexGid()+"zs");
//                stock.setStockName(stockIndex.getIndexName());
//                stock.setIsLock(0);
//            }
        } else {
            stock = this.stockMapper.findStockByCode(code);
        }
        if (stock == null) {
            return ServerResponse.createByErrorMsg("添加失败，股票不存在");
        }
        StockOption stockOption = new StockOption();
        stockOption.setUserId(user.getId());
        stockOption.setStockId(stock.getId());
        stockOption.setAddTime(new Date());
        stockOption.setStockCode(stock.getStockCode());
        stockOption.setStockName(stock.getStockName());
        stockOption.setStockGid(stock.getStockGid());
        stockOption.setIsLock(stock.getIsLock());

        int insertCount = this.stockOptionMapper.insert(stockOption);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("添加自选股成功");
        }
        return ServerResponse.createByErrorMsg("添加失败, 请重试");
    }




    public ServerResponse delOption(String code, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        String stockcode = code;
        if(code.contains("hf")){
            stockcode = code.split("_")[1].toString();
        }
        stockcode = stockcode.replace("sh","").replace("sz","").replace("bj","");
        StockOption dboption = this.stockOptionMapper.findMyOptionIsExistByCode(user.getId(), stockcode);

        if (dboption == null) {
            return ServerResponse.createByErrorMsg("删除失败, 自选股不存在");
        }

        int delCount = this.stockOptionMapper.deleteByPrimaryKey(dboption.getId());
        if (delCount > 0) {
            return ServerResponse.createBySuccessMsg("删除自选股成功");
        }
        return ServerResponse.createByErrorMsg("删除失败, 请重试");
    }



    public ServerResponse isOption(String code, HttpServletRequest request) {
        User user = getCurrentUser(request);

        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        String stockcode = code;
        if(code.contains("hf")){
            stockcode = code.split("_")[1].toString();
        }
        stockcode = stockcode.replace("sh","").replace("sz","").replace("bj","");
        return this.iStockOptionService.isOption(user.getId(), stockcode);
    }




    public ServerResponse getUserInfo(HttpServletRequest request) {
        String cookie_name = PropertiesUtil.getProperty("user.cookie.name");

        String loginToken = request.getHeader(cookie_name);
        String userJson = RedisShardedPoolUtils.get(loginToken);
        User user = (User)JsonUtil.string2Obj(userJson, User.class);
        User dbuser = this.userMapper.selectByPrimaryKey(user.getId());
        UserInfoVO userInfoVO = assembleUserInfoVO(dbuser);

        return ServerResponse.createBySuccess(userInfoVO);
    }


    public ServerResponse updatePwd(String oldPwd, String newPwd, HttpServletRequest request) {
        if (StringUtils.isBlank(oldPwd) || StringUtils.isBlank(newPwd)) {
            return ServerResponse.createByErrorMsg("参数不能为空");
        }

        User user = getCurrentRefreshUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        oldPwd = SymmetricCryptoUtil.encryptPassword(oldPwd);
        if (!oldPwd.equals(user.getUserPwd())) {
            return ServerResponse.createByErrorMsg("密码错误");
        }

        user.setUserPwd(SymmetricCryptoUtil.encryptPassword(newPwd));
        int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("修改成功");
        }
        return ServerResponse.createByErrorMsg("修改失败");
    }



    public ServerResponse checkPhone(String phone) {
        User user = this.userMapper.findByPhone(phone);
        if (user != null) {
            return ServerResponse.createBySuccessMsg("用户已存在");
        }
        return ServerResponse.createByErrorMsg("用户不存在");
    }



    public ServerResponse updatePwd(String phone, String code, String newPwd) {
        if (StringUtils.isBlank(phone) ||
                StringUtils.isBlank(code) ||
                StringUtils.isBlank(newPwd)) {
            return ServerResponse.createByErrorMsg("参数不能为空");
        }


        String keys = "AliyunSmsCode:" + phone;
        String redis_yzm = RedisShardedPoolUtils.get(keys);

        log.info("redis_yzm = {} , code = {}", redis_yzm, code);
        if (!code.equals(redis_yzm)) {
            return ServerResponse.createByErrorMsg("修改密码失败，验证码错误");
        }

        User user = this.userMapper.findByPhone(phone);
        if (user == null) {
            return ServerResponse.createByErrorMsg("用户不存在");
        }

        user.setUserPwd(SymmetricCryptoUtil.encryptPassword(newPwd));
        int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("修改密码成功！");
        }
        return ServerResponse.createByErrorMsg("修改密码失败!");
    }


    public ServerResponse update(User user) {
        log.info("#####修改用户信息####,用户总资金 = {} 可用资金 = {}", user
                .getUserAmt(), user.getEnableAmt());
        log.info("#####修改用户信息####,用户index总资金 = {} index可用资金 = {}", user
                .getUserIndexAmt(), user.getEnableIndexAmt());
        if (user.getAgentId() != null) {
            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(user.getAgentId());
            if (agentUser != null) {
                user.setAgentName(agentUser.getAgentName());
            }
        }
            if (user.getUserPwd()!=null&&!user.getUserPwd().equals("")) {
                user.setUserPwd(SymmetricCryptoUtil.encryptPassword(user.getUserPwd()));
            }
            int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateCount > 0) {
                return ServerResponse.createBySuccessMsg("修改成功");
            }
            return ServerResponse.createByErrorMsg("修改失败");
        }






    public ServerResponse auth(String realName, String idCard, String img1key, String img2key, String img3key, HttpServletRequest request) {
        if (StringUtils.isBlank(realName) ||
                StringUtils.isBlank(idCard) ||
                StringUtils.isBlank(img1key) ||
                StringUtils.isBlank(img2key))
        {

            return ServerResponse.createByErrorMsg("参数不能为空");
        }

        User user = getCurrentRefreshUser(request);
        if (user == null) {
            return ServerResponse.createByErrorMsg("请登录！");
        }

        if (((0 != user.getIsActive().intValue())) & ((3 != user.getIsActive().intValue()) ))
        {
            return ServerResponse.createByErrorMsg("当前状态不能认证");
        }

        user.setNickName(realName);
        user.setRealName(realName);
        user.setIdCard(idCard);

        user.setImg1Key(img1key);
        user.setImg2Key(img2key);
        user.setImg3Key(img3key);
        user.setIsActive(Integer.valueOf(1));

        log.info("##### 用户认证 ####,用户总资金 = {} 可用资金 = {}", user
                .getUserAmt(), user.getEnableAmt());

        int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {

            siteAdminService.alarm("SHIMING", user.getId());

            return ServerResponse.createBySuccessMsg("实名认证中");
        }
        return ServerResponse.createByErrorMsg("实名认证失败");
    }



    public ServerResponse transAmt(Integer amt, Integer type, HttpServletRequest request) {
        User user = getCurrentRefreshUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        if (amt.intValue() <= 0) {
            return ServerResponse.createByErrorMsg("金额不正确");
        }


        if (1 == type.intValue()) {
            if (user.getEnableAmt().compareTo(new BigDecimal(amt.intValue())) == -1) {
                return ServerResponse.createByErrorMsg("融资账户可用资金不足");
            }

            BigDecimal userAmt = user.getUserAmt().subtract(new BigDecimal(amt.intValue()));
            BigDecimal enableAmt = user.getEnableAmt().subtract(new BigDecimal(amt.intValue()));
            BigDecimal userIndexAmt = user.getUserIndexAmt().add(new BigDecimal(amt.intValue()));
            BigDecimal enableIndexAmt = user.getEnableIndexAmt().add(new BigDecimal(amt.intValue()));

            user.setUserAmt(userAmt);
            user.setEnableAmt(enableAmt);
            user.setUserIndexAmt(userIndexAmt);
            user.setEnableIndexAmt(enableIndexAmt);
            int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateCount > 0) {
                saveAmtTransLog(user, type, amt);
                return ServerResponse.createBySuccessMsg("转账成功");
            }
            return ServerResponse.createByErrorMsg("转账失败");
        }



        if (2 == type.intValue()) {
            if (user.getEnableIndexAmt().compareTo(new BigDecimal(amt.intValue())) == -1) {
                return ServerResponse.createByErrorMsg("指数账户可用资金不足");
            }

            BigDecimal userAmt = user.getUserAmt().add(new BigDecimal(amt.intValue()));
            BigDecimal enableAmt = user.getEnableAmt().add(new BigDecimal(amt.intValue()));
            BigDecimal userIndexAmt = user.getUserIndexAmt().subtract(new BigDecimal(amt.intValue()));
            BigDecimal enableIndexAmt = user.getEnableIndexAmt().subtract(new BigDecimal(amt.intValue()));

            user.setUserAmt(userAmt);
            user.setEnableAmt(enableAmt);
            user.setUserIndexAmt(userIndexAmt);
            user.setEnableIndexAmt(enableIndexAmt);
            int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateCount > 0) {
                saveAmtTransLog(user, type, amt);
                return ServerResponse.createBySuccessMsg("转账成功");
            }
            return ServerResponse.createByErrorMsg("转账失败");
        }



        if (3 == type.intValue()) {
            if (user.getEnableAmt().compareTo(new BigDecimal(amt.intValue())) == -1) {
                return ServerResponse.createByErrorMsg("指数账户可用资金不足");
            }

            BigDecimal userAmt = user.getUserAmt().subtract(new BigDecimal(amt.intValue()));
            BigDecimal enableAmt = user.getEnableAmt().subtract(new BigDecimal(amt.intValue()));
            BigDecimal userFutAmt = user.getUserFutAmt().add(new BigDecimal(amt.intValue()));
            BigDecimal enableFutAmt = user.getEnableFutAmt().add(new BigDecimal(amt.intValue()));

            user.setUserAmt(userAmt);
            user.setEnableAmt(enableAmt);
            user.setUserFutAmt(userFutAmt);
            user.setEnableFutAmt(enableFutAmt);
            int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateCount > 0) {
                saveAmtTransLog(user, type, amt);
                return ServerResponse.createBySuccessMsg("转账成功");
            }
            return ServerResponse.createByErrorMsg("转账失败");
        }



        if (4 == type.intValue()) {
            if (user.getEnableFutAmt().compareTo(new BigDecimal(amt.intValue())) == -1) {
                return ServerResponse.createByErrorMsg("期货账户可用资金不足");
            }

            BigDecimal userAmt = user.getUserAmt().add(new BigDecimal(amt.intValue()));
            BigDecimal enableAmt = user.getEnableAmt().add(new BigDecimal(amt.intValue()));
            BigDecimal userFutAmt = user.getUserFutAmt().subtract(new BigDecimal(amt.intValue()));
            BigDecimal enableFutAmt = user.getEnableFutAmt().subtract(new BigDecimal(amt.intValue()));

            user.setUserAmt(userAmt);
            user.setEnableAmt(enableAmt);
            user.setUserFutAmt(userFutAmt);
            user.setEnableFutAmt(enableFutAmt);

            int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateCount > 0) {
                saveAmtTransLog(user, type, amt);
                return ServerResponse.createBySuccessMsg("转账成功");
            }
            return ServerResponse.createByErrorMsg("转账失败");
        }


        return ServerResponse.createByErrorMsg("类型错误");
    }


    private void saveAmtTransLog(User user, Integer type, Integer amt) {
        String amtFrom = "";
        String amtTo = "";
        if (1 == type.intValue()) {
            amtFrom = "融资";
            amtTo = "指数";
        }
        else if (2 == type.intValue()) {
            amtFrom = "指数";
            amtTo = "融资";
        }
        else if (3 == type.intValue()) {
            amtFrom = "融资";
            amtTo = "期货";
        }
        else if (4 == type.intValue()) {
            amtFrom = "期货";
            amtTo = "融资";
        }

        SiteAmtTransLog siteAmtTransLog = new SiteAmtTransLog();
        siteAmtTransLog.setUserId(user.getId());
        siteAmtTransLog.setRealName(user.getRealName());
        siteAmtTransLog.setAgentId(user.getAgentId());
        siteAmtTransLog.setAmtFrom(amtFrom);
        siteAmtTransLog.setAmtTo(amtTo);
        siteAmtTransLog.setTransAmt(new BigDecimal(amt.intValue()));
        siteAmtTransLog.setAddTime(new Date());
        this.siteAmtTransLogMapper.insert(siteAmtTransLog);
    }




    public void ForceSellTask() {
        List<Integer> userIdList = this.iUserPositionService.findDistinctUserIdList();

        log.info("当前有持仓单的用户数量 为 {}", Integer.valueOf(userIdList.size()));

        for (int i = 0; i < userIdList.size(); i++) {
            log.info("=====================");
            Integer userId = (Integer)userIdList.get(i);
            User user = this.userMapper.selectByPrimaryKey(userId);
            if(user == null){
                continue;
            }


            List<UserPosition> userPositions = this.iUserPositionService.findPositionByUserIdAndSellIdIsNull(userId);

            log.info("用户id = {} 姓名 = {} 持仓中订单数： {}", new Object[] { userId, user.getRealName(), Integer.valueOf(userPositions.size()) });


            BigDecimal enable_user_amt = user.getEnableAmt();


            BigDecimal all_freez_amt = new BigDecimal("0");
            for (UserPosition position : userPositions) {

                BigDecimal actual_amt = position.getOrderTotalPrice().divide(new BigDecimal(position
                        .getOrderLever().intValue()), 2, 4);



                all_freez_amt = all_freez_amt.add(actual_amt);
            }


            BigDecimal all_profit_and_lose = new BigDecimal("0");
            PositionVO positionVO = this.iUserPositionService.findUserPositionAllProfitAndLose(userId);
            all_profit_and_lose = positionVO.getAllProfitAndLose();
            SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
            BigDecimal force_stop_percent = siteSetting.getForceStopPercent();
            /*BigDecimal force_stop_amt = force_stop_percent.multiply(all_freez_amt);
            BigDecimal user_force_amt = enable_user_amt.add(force_stop_amt);
            boolean isProfit = false;
            isProfit = (all_profit_and_lose.compareTo(new BigDecimal("0")) == -1 && user_force_amt.compareTo(all_profit_and_lose.negate()) != 1);
            */
            BigDecimal force_stop_amt = enable_user_amt.add(all_freez_amt);

            //(沪深)强制平仓线 = (账户可用资金 + 冻结保证金) *  0.8
            BigDecimal user_force_amt = force_stop_percent.multiply(force_stop_amt);
            BigDecimal fu_user_force_amt = user_force_amt.negate(); //负平仓线
            log.info("用户强制平仓线金额 = {}", user_force_amt);

            boolean isProfit = false;

            //总盈亏<=0  并且  强制负平仓线>=总盈亏
            isProfit = (all_profit_and_lose.compareTo(new BigDecimal("0")) < 1 && fu_user_force_amt.compareTo(all_profit_and_lose) > -1);
            if (isProfit) {
                log.info("强制平仓该用户所有的持仓单");

                int[] arrs = new int[userPositions.size()];
                for (int k = 0; k < userPositions.size(); k++) {
                    UserPosition position = (UserPosition)userPositions.get(k);
                    arrs[k] = position.getId().intValue();
                    try {
                        if(!DateTimeUtil.sameDate(DateTimeUtil.getCurrentDate(),position.getBuyOrderTime())){
                            this.iUserPositionService.sell(position.getPositionSn(), 0);
                        }

                    } catch (Exception e) {
                        log.error("[盈亏达到最大亏损]强制平仓失败...");
                    }
                }


                SiteTaskLog siteTaskLog = new SiteTaskLog();
                siteTaskLog.setTaskType("强平任务-股票持仓");
                String accountType = (user.getAccountType().intValue() == 0) ? "正式用户" : "模拟用户";
                String taskcnt = accountType + "-" + user.getRealName() + "被强平[两融盈亏达到最大亏损] 用户id = " + user.getId() + ", 可用资金 = " + enable_user_amt + "冻结保证金 = " + all_freez_amt + ", 强平比例 = " + force_stop_percent + ", 总盈亏" + all_profit_and_lose + ", 强平线:" + user_force_amt;




                siteTaskLog.setTaskCnt(taskcnt);
                String tasktarget = "此次强平" + userPositions.size() + "条股票持仓订单, 订单号为" + Arrays.toString(arrs);
                siteTaskLog.setTaskTarget(tasktarget);
                siteTaskLog.setAddTime(new Date());
                siteTaskLog.setIsSuccess(Integer.valueOf(0));
                siteTaskLog.setErrorMsg("");
                int insertTaskCount = this.siteTaskLogMapper.insert(siteTaskLog);
                if (insertTaskCount > 0) {
                    log.info("[盈亏达到最大亏损]保存强制平仓task任务成功");
                } else {
                    log.info("[盈亏达到最大亏损]保存强制平仓task任务失败");
                }
            } else {
                log.info("用户未达到强制平仓线，不做强平处理...");
            }

            log.info("=====================");
        }
    }

    /*用户持仓单-单支股票盈亏-强平定时*/
    public void ForceSellOneStockTask() {
        List<Integer> userIdList = this.iUserPositionService.findDistinctUserIdList();
        log.info("当前有持仓单的用户数量 为 {}", Integer.valueOf(userIdList.size()));
        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        BigDecimal force_stop_percent = siteSetting.getForceStopPercent();
        for (int i = 0; i < userIdList.size(); i++) {
            log.info("=====================");
            Integer userId = (Integer)userIdList.get(i);
            User user = this.userMapper.selectByPrimaryKey(userId);
            if(user == null){
                continue;
            }
            List<UserPosition> userPositions = this.iUserPositionService.findPositionByUserIdAndSellIdIsNull(userId);
            log.info("用户id = {} 姓名 = {} 持仓中订单数： {}", new Object[] { userId, user.getRealName(), Integer.valueOf(userPositions.size()) });

            BigDecimal enable_user_amt = user.getEnableAmt();
            BigDecimal all_freez_amt = new BigDecimal("0");
            for (UserPosition position : userPositions) {
                PositionProfitVO positionProfitVO = iUserPositionService.getPositionProfitVO(position);

                //(沪深)单支股票强制平仓线 = (下单总金额 / 杠杆 + 追加保证金) *  0.8
                BigDecimal user_force_amt = position.getOrderTotalPrice().divide(new BigDecimal(position.getOrderLever())).add(position.getMarginAdd()).multiply(force_stop_percent);
                BigDecimal fu_user_force_amt = user_force_amt.negate(); //负平仓线
                log.info("用户强制平仓线金额 = {}", user_force_amt);
                /*if("1601344387923698".equals( position.getPositionSn())){
                    log.info("test = {}", position.getPositionSn());
                }*/
                boolean isProfit = false;
                //总盈亏<=0  并且  强制负平仓线>=总盈亏
                isProfit = (positionProfitVO.getAllProfitAndLose().compareTo(new BigDecimal("0")) < 1 && fu_user_force_amt.compareTo(positionProfitVO.getAllProfitAndLose()) > -1);
                if(isProfit && !DateTimeUtil.sameDate(DateTimeUtil.getCurrentDate(),position.getBuyOrderTime())){
                    try {
                        this.iUserPositionService.sell(position.getPositionSn(), 0);

                        SiteTaskLog siteTaskLog = new SiteTaskLog();
                        siteTaskLog.setTaskType("单股强平任务-股票持仓");
                        String accountType = (user.getAccountType().intValue() == 0) ? "正式用户" : "模拟用户";
                        String taskcnt = accountType + "-" + user.getRealName() + "被强平[两融盈亏达到最大亏损] 用户id = " + user.getId() + ", 可用资金 = " + enable_user_amt + "冻结保证金 = " + all_freez_amt + ", 强平比例 = " + force_stop_percent + ", 总盈亏" + positionProfitVO.getAllProfitAndLose() + ", 强平线:" + user_force_amt;
                        siteTaskLog.setTaskCnt(taskcnt);
                        String tasktarget = "此次强平订单号为：" + position.getPositionSn();
                        siteTaskLog.setTaskTarget(tasktarget);
                        siteTaskLog.setAddTime(new Date());
                        siteTaskLog.setIsSuccess(Integer.valueOf(0));
                        siteTaskLog.setErrorMsg("");
                        int insertTaskCount = this.siteTaskLogMapper.insert(siteTaskLog);
                        if (insertTaskCount > 0) {
                            log.info("[盈亏达到最大亏损]保存强制平仓task任务成功");
                        } else {
                            log.info("[盈亏达到最大亏损]保存强制平仓task任务失败");
                        }
                    } catch (Exception e) {
                        log.error("[盈亏达到最大亏损]强制平仓失败...");
                    }
                }

            }
            log.info("=====================");
        }
    }
    /*用户持仓单-单支股票/止损止盈-强平定时*/
    public void ForceSellOneStockTaskV2() {
        List<Integer> userIdList = this.iUserPositionService.findDistinctUserIdList();
        log.info("当前有持仓单的用户数量 为 {}", Integer.valueOf(userIdList.size()));
        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        BigDecimal force_stop_percent = siteSetting.getForceStopPercent();
        for (int i = 0; i < userIdList.size(); i++) {
            log.info("=====================");
            Integer userId = (Integer)userIdList.get(i);
            User user = this.userMapper.selectByPrimaryKey(userId);
            if(user == null){
                continue;
            }
            List<UserPosition> userPositions = this.iUserPositionService.findPositionByUserIdAndSellIdIsNull(userId);
            log.info("用户id = {} 姓名 = {} 持仓中订单数： {}", new Object[] { userId, user.getRealName(), Integer.valueOf(userPositions.size()) });

            BigDecimal enable_user_amt = user.getEnableAmt();
            BigDecimal all_freez_amt = new BigDecimal("0");
            for (UserPosition position : userPositions) {
//                PositionProfitVO positionProfitVO = iUserPositionService.getPositionProfitVO(position);
//                if (positionProfitVO == null) {
//                    continue;
//                }

                StockListVO stockListVO = new StockListVO();
                    stockListVO = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(position.getStockGid()));
                if (stockListVO == null) {
                    continue;
                }

                if(position.getProfitTargetPrice()!=null && position.getProfitTargetPrice().compareTo(new BigDecimal(stockListVO.getNowPrice())) <= 0 ||position.getStopTargetPrice() != null && position.getStopTargetPrice().compareTo(new BigDecimal(stockListVO.getNowPrice())) >= 0) {

                        try {
                            this.iUserPositionService.sell(position.getPositionSn(), 0);
                            SiteTaskLog siteTaskLog = new SiteTaskLog();
                            siteTaskLog.setTaskType("单股止盈止损强平任务-股票持仓");
                            String accountType = (user.getAccountType().intValue() == 0) ? "正式用户" : "模拟用户";
                            String taskcnt = accountType + "-" + user.getRealName() + "被强平[达到目标盈亏] 用户id = " + user.getId() + ", 可用资金 = " + enable_user_amt + "冻结保证金 = " + all_freez_amt + ", 强平比例 = " + force_stop_percent + ",现价"+stockListVO.getNowPrice()+ ", 目标止盈价格:" + position.getProfitTargetPrice()+ ", 目标止损价格:" + position.getStopTargetPrice();
                            siteTaskLog.setTaskCnt(taskcnt);
                            String tasktarget = "此次强平订单号为：" + position.getPositionSn();
                            siteTaskLog.setTaskTarget(tasktarget);
                            siteTaskLog.setAddTime(new Date());
                            siteTaskLog.setIsSuccess(Integer.valueOf(0));
                            siteTaskLog.setErrorMsg("");
                            int insertTaskCount = this.siteTaskLogMapper.insert(siteTaskLog);
                            if (insertTaskCount > 0) {
                                log.info("[盈利达到目标盈利]保存强制平仓task任务成功");
                            } else {
                                log.info("[盈利达到目标盈利]保存强制平仓task任务失败");
                            }
                        } catch (Exception e) {
                            log.error("[盈利达到目标盈利]强制平仓失败...");
                        }

                }

            }
            log.info("=========止盈止损定时任务============");
        }
    }
    /*用户股票持仓单-强平提醒推送消息定时*/
    public void ForceSellMessageTask() {
        List<Integer> userIdList = this.iUserPositionService.findDistinctUserIdList();

        log.info("当前有持仓单的用户数量 为 {}", Integer.valueOf(userIdList.size()));

        for (int i = 0; i < userIdList.size(); i++) {
            log.info("=====================");
            Integer userId = (Integer)userIdList.get(i);
            User user = this.userMapper.selectByPrimaryKey(userId);
            if(user == null){
                continue;
            }


            List<UserPosition> userPositions = this.iUserPositionService.findPositionByUserIdAndSellIdIsNull(userId);

            log.info("用户id = {} 姓名 = {} 持仓中订单数： {}", new Object[] { userId, user.getRealName(), Integer.valueOf(userPositions.size()) });


            BigDecimal enable_user_amt = user.getEnableAmt();


            BigDecimal all_freez_amt = new BigDecimal("0");
            for (UserPosition position : userPositions) {

                BigDecimal actual_amt = position.getOrderTotalPrice().divide(new BigDecimal(position
                        .getOrderLever().intValue()), 2, 4);



                all_freez_amt = all_freez_amt.add(actual_amt);
            }


            BigDecimal all_profit_and_lose = new BigDecimal("0");
            PositionVO positionVO = this.iUserPositionService.findUserPositionAllProfitAndLose(userId);
            all_profit_and_lose = positionVO.getAllProfitAndLose();
            SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
            BigDecimal force_stop_percent = siteSetting.getForceStopRemindRatio();
            /*BigDecimal force_stop_amt = force_stop_percent.multiply(all_freez_amt);
            BigDecimal user_force_amt = enable_user_amt.add(force_stop_amt);
            boolean isProfit = false;
            isProfit = (all_profit_and_lose.compareTo(new BigDecimal("0")) == -1 && user_force_amt.compareTo(all_profit_and_lose.negate()) != 1);
            */
            BigDecimal force_stop_amt = enable_user_amt.add(all_freez_amt);

            //(沪深)强制平仓线 = (账户可用资金 + 冻结保证金) *  0.8
            BigDecimal user_force_amt = force_stop_percent.multiply(force_stop_amt);
            BigDecimal fu_user_force_amt = user_force_amt.negate(); //负平仓线
            log.info("用户强制平仓线金额 = {}", user_force_amt);

            boolean isProfit = false;

            //总盈亏<=0  并且  强制负平仓线>=总盈亏
            isProfit = (all_profit_and_lose.compareTo(new BigDecimal("0")) < 1 && fu_user_force_amt.compareTo(all_profit_and_lose) > -1);
            if (isProfit) {
                log.info("强制平仓该用户所有的持仓单");
                int count = iSiteMessageService.getIsDayCount(userId,"股票预警");
                if(count == 0){
                    //给达到消息强平提醒用户推送消息
                    SiteMessage siteMessage = new SiteMessage();
                    siteMessage.setUserId(userId);
                    siteMessage.setUserName(user.getRealName());
                    siteMessage.setTypeName("股票预警");
                    siteMessage.setStatus(1);
                    siteMessage.setContent("【股票预警】提醒您，用户id = "+user.getId() + ", 可用资金 = " + enable_user_amt + "冻结保证金 = " + all_freez_amt + ", 强平比例 = " + force_stop_percent + ", 总盈亏" + all_profit_and_lose + ", 提醒线:" + user_force_amt +"，请及时关注哦。");
                    siteMessage.setAddTime(DateTimeUtil.getCurrentDate());
                    iSiteMessageService.insert(siteMessage);
                }

            } else {
                log.info("用户未达到强制平仓线，不做强平处理...");
            }

            log.info("=====================");
        }
    }





    public void ForceSellIndexTask() {
        List<Integer> userIdList = this.iUserIndexPositionService.findDistinctUserIdList();

        log.info("当前有 指数持仓 的用户数量 为 {}", Integer.valueOf(userIdList.size()));

        for (int i = 0; i < userIdList.size(); i++) {
            log.info("=====================");
            Integer userId = (Integer)userIdList.get(i);
            User user = this.userMapper.selectByPrimaryKey(userId);
            if(user == null){
                continue;
            }


            List<UserIndexPosition> userIndexPositions = this.iUserIndexPositionService.findIndexPositionByUserIdAndSellPriceIsNull(userId);

            log.info("用户id = {} 姓名 = {} 持仓中订单数: {}", new Object[] { userId, user
                    .getRealName(), Integer.valueOf(userIndexPositions.size()) });


            IndexPositionVO indexPositionVO = this.iUserIndexPositionService.findUserIndexPositionAllProfitAndLose(userId);


            BigDecimal enable_index_amt = user.getEnableIndexAmt();


            BigDecimal all_freez_amt = indexPositionVO.getAllIndexFreezAmt();

            BigDecimal all_profit_and_lose = indexPositionVO.getAllIndexProfitAndLose();

            log.info("用户 {} 可用资金 = {} 总冻结保证金 = {} 所有持仓单的总盈亏 = {}", new Object[] { userId, enable_index_amt, all_freez_amt, all_profit_and_lose });



            SiteIndexSetting siteIndexSetting = this.iSiteIndexSettingService.getSiteIndexSetting();
            BigDecimal force_stop_percent = siteIndexSetting.getForceSellPercent();
            BigDecimal force_stop_amt = enable_index_amt.add(all_freez_amt);

            //(指数)强制平仓线 = (账户可用资金 + 冻结保证金) *  0.8
            BigDecimal user_force_amt = force_stop_percent.multiply(force_stop_amt);
            BigDecimal fu_user_force_amt = user_force_amt.negate(); //负平仓线
            log.info("用户强制平仓线金额 = {}", user_force_amt);
            boolean isProfit = false;
            //总盈亏<=0  并且  强制负平仓线>=总盈亏
            isProfit = (all_profit_and_lose.compareTo(new BigDecimal("0")) < 1 && fu_user_force_amt.compareTo(all_profit_and_lose) > -1);

            if (isProfit) {
                log.info("强制平仓该用户所有的指数持仓单");

                int[] arrs = new int[userIndexPositions.size()];
                for (int k = 0; k < userIndexPositions.size(); k++) {
                    UserIndexPosition userIndexPosition = (UserIndexPosition)userIndexPositions.get(k);
                    arrs[k] = userIndexPosition.getId().intValue();
                    try {
                        this.iUserIndexPositionService.sellIndex(userIndexPosition.getPositionSn(), 0);
                    }
                    catch (Exception e) {
                        log.error("[盈亏达到最大亏损]强制平仓指数失败...");
                    }
                }


                SiteTaskLog siteTaskLog = new SiteTaskLog();
                siteTaskLog.setTaskType("强平任务-指数持仓");
                String accountType = (user.getAccountType().intValue() == 0) ? "正式用户" : "模拟用户";
                String taskcnt = accountType + "-" + user.getRealName() + "被强平 [指数盈亏达到最大亏损] 用户 id = " + user.getId() + ", 可用资金 = " + enable_index_amt + ", 冻结资金 = " + all_freez_amt + ", 强平比例 = " + force_stop_percent + ", 总盈亏 = " + all_profit_and_lose + ", 强平线 = " + user_force_amt;




                siteTaskLog.setTaskCnt(taskcnt);

                String tasktarget = "此次强平" + userIndexPositions.size() + "条指数持仓订单, 订单号为" + Arrays.toString(arrs);
                siteTaskLog.setTaskTarget(tasktarget);
                siteTaskLog.setAddTime(new Date());
                siteTaskLog.setIsSuccess(Integer.valueOf(0));
                siteTaskLog.setErrorMsg("");
                int insertTaskCount = this.siteTaskLogMapper.insert(siteTaskLog);
                if (insertTaskCount > 0) {
                    log.info("[盈亏达到最大亏损] 保存强制平仓 指数 task任务成功");
                } else {
                    log.info("[盈亏达到最大亏损] 保存强制平仓 指数 task任务失败");
                }
            } else {
                log.info("用户指数持仓未达到强制平仓线, 不做强平处理...");
            }

            log.info("=====================");
        }
    }

    /*指数强平提醒推送消息，每分钟检测一次*/
    public void ForceSellIndexsMessageTask() {
        List<Integer> userIdList = this.iUserIndexPositionService.findDistinctUserIdList();

        log.info("当前有 指数持仓 的用户数量 为 {}", Integer.valueOf(userIdList.size()));

        for (int i = 0; i < userIdList.size(); i++) {
            log.info("=====================");
            Integer userId = (Integer)userIdList.get(i);
            User user = this.userMapper.selectByPrimaryKey(userId);
            if(user == null){
                continue;
            }


            List<UserIndexPosition> userIndexPositions = this.iUserIndexPositionService.findIndexPositionByUserIdAndSellPriceIsNull(userId);

            log.info("用户id = {} 姓名 = {} 持仓中订单数: {}", new Object[] { userId, user
                    .getRealName(), Integer.valueOf(userIndexPositions.size()) });


            IndexPositionVO indexPositionVO = this.iUserIndexPositionService.findUserIndexPositionAllProfitAndLose(userId);


            BigDecimal enable_index_amt = user.getEnableIndexAmt();


            BigDecimal all_freez_amt = indexPositionVO.getAllIndexFreezAmt();

            BigDecimal all_profit_and_lose = indexPositionVO.getAllIndexProfitAndLose();

            log.info("用户 {} 可用资金 = {} 总冻结保证金 = {} 所有持仓单的总盈亏 = {}", new Object[] { userId, enable_index_amt, all_freez_amt, all_profit_and_lose });



            SiteIndexSetting siteIndexSetting = this.iSiteIndexSettingService.getSiteIndexSetting();
            BigDecimal force_stop_percent = siteIndexSetting.getForceStopRemindRatio();
            BigDecimal force_stop_amt = enable_index_amt.add(all_freez_amt);

            //(指数)强制平仓线 = (账户可用资金 + 冻结保证金) *  0.8
            BigDecimal user_force_amt = force_stop_percent.multiply(force_stop_amt);
            BigDecimal fu_user_force_amt = user_force_amt.negate(); //负平仓线
            log.info("用户强制平仓线金额 = {}", user_force_amt);
            boolean isProfit = false;
            //总盈亏<=0  并且  强制负平仓线>=总盈亏
            isProfit = (all_profit_and_lose.compareTo(new BigDecimal("0")) < 1 && fu_user_force_amt.compareTo(all_profit_and_lose) > -1);

            if (isProfit) {
                log.info("强制平仓该用户所有的指数持仓单");

                int count = iSiteMessageService.getIsDayCount(userId,"指数预警");
                if(count == 0){
                    //给达到消息强平提醒用户推送消息
                    SiteMessage siteMessage = new SiteMessage();
                    siteMessage.setUserId(userId);
                    siteMessage.setUserName(user.getRealName());
                    siteMessage.setTypeName("指数预警");
                    siteMessage.setStatus(1);
                    siteMessage.setContent("【指数预警】提醒您，用户id = "+user.getId() + ", 可用资金 = " + enable_index_amt + ", 冻结资金 = " + all_freez_amt + ", 强平比例 = " + force_stop_percent + ", 总盈亏 = " + all_profit_and_lose + ", 提醒线 = " + user_force_amt +"，请及时关注哦。");
                    siteMessage.setAddTime(DateTimeUtil.getCurrentDate());
                    iSiteMessageService.insert(siteMessage);
                }

            } else {
                log.info("用户指数持仓未达到强制平仓线, 不做强平处理...");
            }

            log.info("=====================");
        }
    }

    public void qh1() {
        this.stockPoll.qh1();
    }

    public void zs1() {
        this.stockPoll.zs1();
    }

    public void ForceSellFuturesTask() {
        List<Integer> userIdList = this.iUserFuturesPositionService.findDistinctUserIdList();


        for (int i = 0; i < userIdList.size(); i++) {
            log.info("===================== \n");
            Integer userId = (Integer)userIdList.get(i);
            System.out.println("userId"+userId);
            User user = this.userMapper.selectByPrimaryKey(userId);
            if(user == null){
                continue;
            }

            List<UserFuturesPosition> userFuturesPositions = this.iUserFuturesPositionService.findFuturesPositionByUserIdAndSellPriceIsNull(userId);
            System.out.println("userFuturesPositions"+userFuturesPositions);
            System.out.println("继续");
            log.info("用户id = {} 姓名 = {} 期货持仓中订单数 {}", new Object[] { userId, user
                    .getRealName(), Integer.valueOf(userFuturesPositions.size()) });

            FuturesPositionVO futuresPositionVO = this.iUserFuturesPositionService.findUserFuturesPositionAllProfitAndLose(userId);

            BigDecimal enable_Futures_amt = user.getEnableFutAmt();

            BigDecimal all_deposit_amt = futuresPositionVO.getAllFuturesDepositAmt();

            BigDecimal all_profit_and_lose = futuresPositionVO.getAllFuturesProfitAndLose();

            log.info("用户 {} 可用资金 = {} 总冻结保证金 = {} 所有持仓单的总盈亏 = {}", new Object[] { userId, enable_Futures_amt, all_deposit_amt, all_profit_and_lose });



            SiteFuturesSetting siteFuturesSetting = this.iSiteFuturesSettingService.getSetting();
            BigDecimal force_stop_percent = siteFuturesSetting.getForceSellPercent();
            BigDecimal force_stop_amt = enable_Futures_amt.add(all_deposit_amt);

            //(期货)强制平仓线 = (账户可用资金 + 冻结保证金) *  0.8
            BigDecimal user_force_amt = force_stop_percent.multiply(force_stop_amt);
            BigDecimal fu_user_force_amt = user_force_amt.negate(); //负平仓线
            log.info("用户强制平仓线金额 = {}", user_force_amt);

            boolean isProfit = false;

            //总盈亏<=0  并且  强制负平仓线>=总盈亏
            isProfit = (all_profit_and_lose.compareTo(new BigDecimal("0")) < 1 && fu_user_force_amt.compareTo(all_profit_and_lose) > -1);

            if (isProfit) {
                log.info("强制平仓用户 {} 所有的 期货 持仓单", user.getId());

                int[] arrs = new int[userFuturesPositions.size()];
                for (int k = 0; k < userFuturesPositions.size(); k++) {
                    UserFuturesPosition userFuturesPosition = (UserFuturesPosition)userFuturesPositions.get(k);
                    arrs[k] = userFuturesPosition.getId().intValue();
                    try {
                        this.iUserFuturesPositionService.sellFutures(userFuturesPosition.getPositionSn(), 0);
                    }
                    catch (Exception e) {
                        log.error("[盈亏达到最大亏损] 强制平仓 期货 失败...");
                    }
                }

                SiteTaskLog siteTaskLog = new SiteTaskLog();
                siteTaskLog.setTaskType("强平任务-期货持仓");
                String accountType = (user.getAccountType().intValue() == 0) ? "正式用户" : "模拟用户";
                String taskcnt = accountType + "-" + user.getRealName() + "被强平[期货盈亏达到最大亏损]用户id = " + user.getId() + ", 可用资金 = " + enable_Futures_amt + ", 冻结保证金 = " + all_deposit_amt + ", 强平比例 = " + force_stop_percent + ", 总盈亏" + all_profit_and_lose + ", 强平线:" + user_force_amt;




                siteTaskLog.setTaskCnt(taskcnt);

                String tasktarget = "此次强平" + userFuturesPositions.size() + "条期货持仓订单, 订单号为" + Arrays.toString(arrs);
                siteTaskLog.setTaskTarget(tasktarget);
                siteTaskLog.setAddTime(new Date());
                siteTaskLog.setIsSuccess(Integer.valueOf(0));
                siteTaskLog.setErrorMsg("");
                int insertTaskCount = this.siteTaskLogMapper.insert(siteTaskLog);
                if (insertTaskCount > 0) {
                    log.info("[盈亏达到最大亏损]保存强制平仓 期货 task任务成功");
                } else {
                    log.info("[盈亏达到最大亏损]保存强制平仓 期货 task任务失败");
                }
            } else {
                log.info("用户期货;持仓未达到强制平仓线，不做强平处理...");
            }
            log.info("===================== \n");
        }
    }

    public void ForceSellFuturesMessageTask() {
        List<Integer> userIdList = this.iUserFuturesPositionService.findDistinctUserIdList();


        for (int i = 0; i < userIdList.size(); i++) {
            log.info("===================== \n");
            Integer userId = (Integer)userIdList.get(i);
            System.out.println("userId"+userId);
            User user = this.userMapper.selectByPrimaryKey(userId);
            if(user == null){
                continue;
            }

            List<UserFuturesPosition> userFuturesPositions = this.iUserFuturesPositionService.findFuturesPositionByUserIdAndSellPriceIsNull(userId);
            System.out.println("userFuturesPositions"+userFuturesPositions);
            System.out.println("继续");
            log.info("用户id = {} 姓名 = {} 期货持仓中订单数 {}", new Object[] { userId, user
                    .getRealName(), Integer.valueOf(userFuturesPositions.size()) });

            FuturesPositionVO futuresPositionVO = this.iUserFuturesPositionService.findUserFuturesPositionAllProfitAndLose(userId);

            BigDecimal enable_Futures_amt = user.getEnableFutAmt();

            BigDecimal all_deposit_amt = futuresPositionVO.getAllFuturesDepositAmt();

            BigDecimal all_profit_and_lose = futuresPositionVO.getAllFuturesProfitAndLose();

            log.info("用户 {} 可用资金 = {} 总冻结保证金 = {} 所有持仓单的总盈亏 = {}", new Object[] { userId, enable_Futures_amt, all_deposit_amt, all_profit_and_lose });

            SiteFuturesSetting siteFuturesSetting = this.iSiteFuturesSettingService.getSetting();
            BigDecimal force_stop_percent = siteFuturesSetting.getForceStopRemindRatio();
            BigDecimal force_stop_amt = enable_Futures_amt.add(all_deposit_amt);

            //(期货)强制平仓线 = (账户可用资金 + 冻结保证金) *  0.4
            BigDecimal user_force_amt = force_stop_percent.multiply(force_stop_amt);
            BigDecimal fu_user_force_amt = user_force_amt.negate(); //负平仓线
            log.info("用户消息强制平仓线金额 = {}", user_force_amt);

            boolean isProfit = false;

            //总盈亏<=0  并且  强制负平仓线>=总盈亏
            isProfit = (all_profit_and_lose.compareTo(new BigDecimal("0")) < 1 && fu_user_force_amt.compareTo(all_profit_and_lose) > -1);

            if (isProfit) {
                log.info("强制平仓用户 {} 所有的 期货 持仓单", user.getId());
                int count = iSiteMessageService.getIsDayCount(userId,"期货预警");
                if(count == 0){
                    //给达到消息强平提醒用户推送消息
                    SiteMessage siteMessage = new SiteMessage();
                    siteMessage.setUserId(userId);
                    siteMessage.setUserName(user.getRealName());
                    siteMessage.setTypeName("期货预警");
                    siteMessage.setStatus(1);
                    siteMessage.setContent("【期货预警】提醒您，用户id = "+user.getId() + ", 可用资金 = " + enable_Futures_amt + ", 冻结保证金 = " + all_deposit_amt + ", 强平比例 = " + force_stop_percent + ", 总盈亏" + all_profit_and_lose + ", 提醒线:" + user_force_amt +"，请及时关注哦。");
                    siteMessage.setAddTime(DateTimeUtil.getCurrentDate());
                    iSiteMessageService.insert(siteMessage);
                }


            } else {
                log.info("用户期货;持仓未达到强制平仓线，不做强平处理...");
            }
            log.info("===================== \n");
        }
    }




    public ServerResponse listByAgent(String realName, String phone, Integer agentId, Integer accountType, int pageNum, int pageSize, HttpServletRequest request) {
        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        SiteIndexSetting siteIndexSetting = this.iSiteIndexSettingService.getSiteIndexSetting();
        SiteFuturesSetting siteFuturesSetting = this.iSiteFuturesSettingService.getSetting();


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

        PageHelper.startPage(pageNum, pageSize);

        List<User> users = this.userMapper.listByAgent(realName, phone, searchId, accountType);

        List<AgentUserListVO> agentUserListVOS = Lists.newArrayList();
        for (User user : users) {
            AgentUserListVO agentUserListVO = assembleAgentUserListVO(user, siteSetting
                    .getForceStopPercent(), siteIndexSetting
                    .getForceSellPercent(), siteFuturesSetting.getForceSellPercent());
            agentUserListVOS.add(agentUserListVO);
        }

        PageInfo pageInfo = new PageInfo(users);
        pageInfo.setList(agentUserListVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }



    public ServerResponse addSimulatedAccount(Integer agentId, String phone, String pwd, String amt, Integer accountType, HttpServletRequest request) {
        if (StringUtils.isBlank(phone) || StringUtils.isBlank(pwd)) {
            return ServerResponse.createByErrorMsg("参数不能为空");
        }


        User dbUser = this.userMapper.findByPhone(phone);
        if (dbUser != null) {
            return ServerResponse.createByErrorMsg("手机号已注册");
        }


        if ((new BigDecimal(amt)).compareTo(new BigDecimal("200000")) == 1) {
            return ServerResponse.createByErrorMsg("模拟账户资金不能超过20万");
        }

        amt = "0";   //代理后台添加用户时金额默认为0
        User user = new User();
        user.setAccountType(accountType);
        user.setPhone(phone);
        user.setUserPwd(SymmetricCryptoUtil.encryptPassword(pwd));
        user.setUserAmt(new BigDecimal(amt));
        user.setEnableAmt(new BigDecimal(amt));
        user.setSumChargeAmt(new BigDecimal("0"));
        user.setSumBuyAmt(new BigDecimal("0"));
        user.setIsLock(Integer.valueOf(0));
        user.setIsLogin(Integer.valueOf(0));
        user.setIsActive(Integer.valueOf(0));
        user.setRegTime(new Date());

        if (accountType.intValue() == 1) {
            user.setNickName("模拟用户");
        }

        user.setUserIndexAmt(new BigDecimal("0"));
        user.setEnableIndexAmt(new BigDecimal("0"));
        user.setUserFutAmt(new BigDecimal("0"));
        user.setEnableFutAmt(new BigDecimal("0"));

        if (agentId != null) {
            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
            user.setAgentName(agentUser.getAgentName());
            user.setAgentId(agentUser.getId());
        }

        int insertCount = this.userMapper.insert(user);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("用户添加成功");
        }
        return ServerResponse.createByErrorMsg("用户添加失败");
    }





    public ServerResponse listByAdmin(String realName, String phone, Integer agentId, Integer accountType, int pageNum, int pageSize, HttpServletRequest request) {
        PageHelper.startPage(pageNum, pageSize);

        List<User> users = this.userMapper.listByAdmin(realName, phone, agentId, accountType);

        PageInfo pageInfo = new PageInfo(users);

        return ServerResponse.createBySuccess(pageInfo);
    }



    public ServerResponse findByUserId(Integer userId) { return ServerResponse.createBySuccess(this.userMapper.selectByPrimaryKey(userId)); }




    public ServerResponse updateLock(Integer userId) {
        User user = this.userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMsg("用户不存在");
        }

        if (user.getIsLock().intValue() == 1) {
            user.setIsLock(Integer.valueOf(0));
        } else {
            user.setIsLock(Integer.valueOf(1));
        }

        int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("修改成功");
        }
        return ServerResponse.createByErrorMsg("修改失败");
    }



    @Transactional
    public ServerResponse updateAmt(Integer userId, String amt, Integer direction) {
        if (userId == null || amt == null || direction == null) {
            return ServerResponse.createByErrorMsg("参数不能为空");
        }

        User user = this.userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMsg("用户不存在");
        }

        BigDecimal user_amt = user.getUserAmt();
        BigDecimal user_enable = user.getEnableAmt();

        BigDecimal user_amt_back = new BigDecimal("0");
        BigDecimal user_enable_back = new BigDecimal("0");
        if (direction.intValue() == 0) {
            user_amt_back = user_amt.add(new BigDecimal(amt));
            user_enable_back = user_enable.add(new BigDecimal(amt));
        } else if (direction.intValue() == 1) {

            if (user_amt.compareTo(new BigDecimal(amt)) == -1) {
                return ServerResponse.createByErrorMsg("扣款失败, 总资金不足");
            }
            if (user_enable.compareTo(new BigDecimal(amt)) == -1) {
                return ServerResponse.createByErrorMsg("扣款失败, 可用资不足");
            }

            user_amt_back = user_amt.subtract(new BigDecimal(amt));
            user_enable_back = user_enable.subtract(new BigDecimal(amt));
        } else {
            return ServerResponse.createByErrorMsg("不存在此操作");
        }


        user.setUserAmt(user_amt_back);
        user.setEnableAmt(user_enable_back);
        this.userMapper.updateByPrimaryKeySelective(user);

        if(direction.intValue()==0){
            userRechargeService.inMoneyByAdmin(String.valueOf(amt),"后台充值",userId);
        }else if (direction.intValue()==1){
            userRechargeService.inMoneyByAdmin(new BigDecimal(amt).negate().toPlainString(),"后台充值",userId);
        }else if (direction.intValue()==2){
            return ServerResponse.createBySuccessMsg("修改资金成功");
        }

        SiteTaskLog siteTaskLog = new SiteTaskLog();
        siteTaskLog.setTaskType("管理员修改金额");
        StringBuffer cnt = new StringBuffer();
        cnt.append("管理员修改金额 - ")
                .append((direction.intValue() == 0) ? "入款" : "扣款")
                .append(amt).append("元");
        siteTaskLog.setTaskCnt(cnt.toString());

        StringBuffer target = new StringBuffer();
        target.append("用户id : ").append(user.getId())
                .append("修改前 总资金 = ").append(user_amt).append(" 可用 = ").append(user_enable)
                .append("修改后 总资金 = ").append(user_amt_back).append(" 可用 = ").append(user_enable_back);
        siteTaskLog.setTaskTarget(target.toString());

        siteTaskLog.setIsSuccess(Integer.valueOf(0));
        siteTaskLog.setAddTime(new Date());

        int insertCount = this.siteTaskLogMapper.insert(siteTaskLog);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("修改资金成功");
        }
        return ServerResponse.createByErrorMsg("修改资金失败");
    }




    public ServerResponse delete(Integer userId, HttpServletRequest request) {
        String cookie_name = PropertiesUtil.getProperty("admin.cookie.name");
        String logintoken = CookieUtils.readLoginToken(request, cookie_name);
        String adminJson = RedisShardedPoolUtils.get(logintoken);
        SiteAdmin siteAdmin = (SiteAdmin)JsonUtil.string2Obj(adminJson, SiteAdmin.class);

        log.info("管理员 {} 删除用户 {}", siteAdmin.getAdminName(), userId);


        int delChargeCount = this.iUserRechargeService.deleteByUserId(userId);
        if (delChargeCount > 0) {
            log.info("删除 充值 记录成功");
        } else {
            log.info("删除 充值 记录失败");
        }


        int delWithdrawCount = this.iUserWithdrawService.deleteByUserId(userId);
        if (delWithdrawCount > 0) {
            log.info("删除 提现 记录成功");
        } else {
            log.info("删除 提现 记录失败");
        }


        int delCashCount = this.iUserCashDetailService.deleteByUserId(userId);
        if (delCashCount > 0) {
            log.info("删除 资金 记录成功");
        } else {
            log.info("删除 资金 记录成功");
        }


        int delPositionCount = this.iUserPositionService.deleteByUserId(userId);
        if (delPositionCount > 0) {
            log.info("删除 持仓 记录成功");
        } else {
            log.info("删除 持仓 记录失败");
        }


        int delLogCount = this.iSiteLoginLogService.deleteByUserId(userId);
        if (delLogCount > 0) {
            log.info("删除 登录 记录成功");
        } else {
            log.info("删除 登录 记录失败");
        }


        int delUserCount = this.userMapper.deleteByPrimaryKey(userId);
        if (delUserCount > 0) {
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失败, 查看日志");
    }





    public int CountUserSize(Integer accountType) { return this.userMapper.CountUserSize(accountType); }





    public BigDecimal CountUserAmt(Integer accountType) { return this.userMapper.CountUserAmt(accountType); }




    public BigDecimal CountEnableAmt(Integer accountType) { return this.userMapper.CountEnableAmt(accountType); }




    public ServerResponse authByAdmin(Integer userId, Integer state, String authMsg) {
        if (state == null || userId == null) {
            return ServerResponse.createByErrorMsg("id和state不能为空");
        }

        User user = this.userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMsg("查不到此用户");
        }

        if (state.intValue() == 3) {
            if (StringUtils.isBlank(authMsg)) {
                return ServerResponse.createByErrorMsg("审核失败信息必填");
            }
            user.setAuthMsg(authMsg);
        }

        user.setIsActive(state);

        int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("审核成功");
        }
        return ServerResponse.createByErrorMsg("审核失败");
    }

    @Override
    public ServerResponse findIdWithPwd(HttpServletRequest request) {
        User currentUser = this.iUserService.getCurrentUser(request);
        String idWithPwd = userMapper.findIdWithPwd(currentUser.getPhone());

        if (idWithPwd==null){
            return ServerResponse.createByErrorMsg("请设置提现密码！");
        }else {
            return ServerResponse.createBySuccessMsg(idWithPwd);
        }
    }

    @Override
    public ServerResponse updateWithPwd(String withPwd,HttpServletRequest request) {

        if (StringUtils.isBlank(withPwd)){
            return ServerResponse.createByErrorMsg("参数不能为空");
        }
        User currentUser = this.iUserService.getCurrentUser(request);

        int i = userMapper.updateWithPwd(withPwd, currentUser.getPhone());
        if (i>0){
            return ServerResponse.createBySuccessMsg("修改成功！");
        }else {
            return ServerResponse.createByErrorMsg("修改失败！");
        }
    }


    private AgentUserListVO assembleAgentUserListVO(User user, BigDecimal forcePercent, BigDecimal indexForcePercent, BigDecimal futuresForcePercent) {
        AgentUserListVO agentUserListVO = new AgentUserListVO();

        agentUserListVO.setId(user.getId());
        agentUserListVO.setAgentId(user.getAgentId());
        agentUserListVO.setAgentName(user.getAgentName());
        agentUserListVO.setPhone(user.getPhone());
        agentUserListVO.setRealName(user.getRealName());
        agentUserListVO.setIdCard(user.getIdCard());
        agentUserListVO.setAccountType(user.getAccountType());
        agentUserListVO.setIsLock(user.getIsLock());
        agentUserListVO.setIsLogin(user.getIsLogin());
        agentUserListVO.setRegAddress(user.getRegAddress());
        agentUserListVO.setIsActive(user.getIsActive());


        agentUserListVO.setUserAmt(user.getUserAmt());
        agentUserListVO.setEnableAmt(user.getEnableAmt());

        agentUserListVO.setUserIndexAmt(user.getUserIndexAmt());
        agentUserListVO.setEnableIndexAmt(user.getEnableIndexAmt());

        agentUserListVO.setUserFuturesAmt(user.getUserFutAmt());
        agentUserListVO.setEnableFuturesAmt(user.getEnableFutAmt());



        PositionVO positionVO = this.iUserPositionService.findUserPositionAllProfitAndLose(user.getId());
        BigDecimal allProfitAndLose = positionVO.getAllProfitAndLose();
        BigDecimal allFreezAmt = positionVO.getAllFreezAmt();
        agentUserListVO.setAllProfitAndLose(allProfitAndLose);
        agentUserListVO.setAllFreezAmt(allFreezAmt);

        BigDecimal forceLine = forcePercent.multiply(allFreezAmt);
        forceLine = forceLine.add(user.getEnableAmt());
        agentUserListVO.setForceLine(forceLine);



        IndexPositionVO indexPositionVO = this.iUserIndexPositionService.findUserIndexPositionAllProfitAndLose(user.getId());
        agentUserListVO.setAllIndexProfitAndLose(indexPositionVO.getAllIndexProfitAndLose());
        agentUserListVO.setAllIndexFreezAmt(indexPositionVO.getAllIndexFreezAmt());

        BigDecimal indexForceLine = indexForcePercent.multiply(indexPositionVO.getAllIndexFreezAmt());
        indexForceLine = indexForceLine.add(user.getEnableIndexAmt());
        agentUserListVO.setIndexForceLine(indexForceLine);



        FuturesPositionVO futuresPositionVO = this.iUserFuturesPositionService.findUserFuturesPositionAllProfitAndLose(user.getId());
        agentUserListVO.setAllFuturesFreezAmt(futuresPositionVO.getAllFuturesDepositAmt());
        agentUserListVO.setAllFuturesProfitAndLose(futuresPositionVO.getAllFuturesProfitAndLose());

        BigDecimal futuresForceLine = futuresForcePercent.multiply(futuresPositionVO.getAllFuturesDepositAmt());
        futuresForceLine = futuresForceLine.add(user.getEnableFutAmt());
        agentUserListVO.setFuturesForceLine(futuresForceLine);



        UserBank userBank = this.iUserBankService.findUserBankByUserId(user.getId());
        if (userBank != null) {
            agentUserListVO.setBankName(userBank.getBankName());
            agentUserListVO.setBankNo(userBank.getBankNo());
            agentUserListVO.setBankAddress(userBank.getBankAddress());
        }

        return agentUserListVO;
    }

    private UserInfoVO assembleUserInfoVO(User user) {
        UserInfoVO userInfoVO = new UserInfoVO();

        userInfoVO.setId(user.getId());
        userInfoVO.setAgentId(user.getAgentId());
        userInfoVO.setAgentName(user.getAgentName());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setNickName(user.getNickName());
        userInfoVO.setRealName(user.getRealName());
        userInfoVO.setIdCard(user.getIdCard());
        userInfoVO.setAccountType(user.getAccountType());
        userInfoVO.setRecomPhone(user.getRecomPhone());
        userInfoVO.setIsLock(user.getIsLock());
        userInfoVO.setRegTime(user.getRegTime());
        userInfoVO.setRegIp(user.getRegIp());
        userInfoVO.setRegAddress(user.getRegAddress());
        userInfoVO.setImg1Key(user.getImg1Key());
        userInfoVO.setImg2Key(user.getImg2Key());
        userInfoVO.setImg3Key(user.getImg3Key());
        userInfoVO.setIsActive(user.getIsActive());
        userInfoVO.setAuthMsg(user.getAuthMsg());

        userInfoVO.setEnableAmt(user.getEnableAmt());
        userInfoVO.setTradingAmount(user.getTradingAmount());


        PositionVO positionVO = this.iUserPositionService.findUserPositionAllProfitAndLose(user.getId());
        userInfoVO.setAllFreezAmt(positionVO.getAllFreezAmt());
        BigDecimal allProfitAndLose = positionVO.getAllProfitAndLose();
        userInfoVO.setAllProfitAndLose(allProfitAndLose);

        userInfoVO.setAccountProfitAndLose(this.iUserPositionService.CountPositionAllProfitAndLoseByUser(user.getId()));

        BigDecimal userAllAmt = user.getUserAmt();
        userAllAmt = userAllAmt.add(allProfitAndLose);
        userInfoVO.setUserAmt(userAllAmt);

        userInfoVO.setEnableIndexAmt(user.getEnableIndexAmt());


        IndexPositionVO indexPositionVO = this.iUserIndexPositionService.findUserIndexPositionAllProfitAndLose(user.getId());
        BigDecimal allIndexProfitAndLose = indexPositionVO.getAllIndexProfitAndLose();
        userInfoVO.setAllIndexProfitAndLose(allIndexProfitAndLose);
        userInfoVO.setAllIndexFreezAmt(indexPositionVO.getAllIndexFreezAmt());

        BigDecimal userAllIndexAmt = user.getUserIndexAmt();
        userAllIndexAmt = userAllIndexAmt.add(allIndexProfitAndLose);
        userInfoVO.setUserIndexAmt(userAllIndexAmt);

        userInfoVO.setEnableFuturesAmt(user.getEnableFutAmt());


//        FuturesPositionVO futuresPositionVO = this.iUserFuturesPositionService.findUserFuturesPositionAllProfitAndLose(user.getId());

        userInfoVO.setAllFuturesFreezAmt(BigDecimal.ZERO);


        BigDecimal allFuturesProfitAndLose = BigDecimal.ZERO;
        userInfoVO.setAllFuturesProfitAndLose(allFuturesProfitAndLose);


        BigDecimal userAllFuturesAmt = user.getUserFutAmt();
        userAllFuturesAmt = userAllFuturesAmt.add(allFuturesProfitAndLose);
        userInfoVO.setUserFuturesAmt(userAllFuturesAmt);
        userInfoVO.setDjzj(user.getDjzj());
        return userInfoVO;
    }


    public static void main(String[] args) {
        int a = 3;

        System.out.println((a != 0));
        System.out.println((a != 3));

        System.out.println(((a != 0) ? 1 : 0) & ((a != 3) ? 1 : 0));
        System.out.println((a != 0 && a != 3));


        if (a != 0 && a != 3) {
            System.out.println("不能认证");
        } else {
            System.out.println("可以认证");
        }
    }


    @Override
    public void updateUserAmt(Double amt, Integer user_id) {
        userMapper.updateUserAmt(amt, user_id);
    }


    @Override
    public void forceSellTipTask() {


        String key = "FORCE_SELL_LIST";
        RedisShardedPoolUtils.del(key);

        List<Integer> userIdList = this.iUserPositionService.findDistinctUserIdList();
        log.info("当前有持仓单的用户数量 为 {}", Integer.valueOf(userIdList.size()));
        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        BigDecimal force_stop_percent = siteSetting.getForceStopPercent();
        for (int i = 0; i < userIdList.size(); i++) {
            log.info("=====================");
            Integer userId = (Integer)userIdList.get(i);
            User user = this.userMapper.selectByPrimaryKey(userId);
            if(user == null){
                continue;
            }
            List<UserPosition> userPositions = this.iUserPositionService.findPositionByUserIdAndSellIdIsNull(userId);
            log.info("用户id = {} 姓名 = {} 持仓中订单数： {}", new Object[] { userId, user.getRealName(), Integer.valueOf(userPositions.size()) });

            List<String> codeList = userPositions.stream().map(UserPosition::getStockGid).collect(Collectors.toList());

            Map<String, WjStockExtInfoDTO> stockMap = wjStockApi.getStockBatch(codeList);

            BigDecimal enable_user_amt = user.getEnableAmt();
            BigDecimal all_freez_amt = new BigDecimal("0");
            for (UserPosition position : userPositions) {

                WjStockExtInfoDTO infoDTO = stockMap.get(position.getStockGid());

                //(沪深)单支股票强制平仓线 = (下单总金额 / 杠杆 + 追加保证金) *  0.8
//                BigDecimal user_force_amt = position.getOrderTotalPrice().divide(new BigDecimal(position.getOrderLever())).add(position.getMarginAdd()).multiply(force_stop_percent);
//                BigDecimal fu_user_force_amt = user_force_amt.negate(); //负平仓线
//                log.info("用户强制平仓线金额 = {}", user_force_amt);
                /*if("1601344387923698".equals( position.getPositionSn())){
                    log.info("test = {}", position.getPositionSn());
                }*/
                boolean isProfit = false;
                //总盈亏<=0  并且  强制负平仓线>=总盈亏
//                isProfit = (positionProfitVO.getAllProfitAndLose().compareTo(new BigDecimal("0")) <= 0
//                        && fu_user_force_amt.compareTo(positionProfitVO.getAllProfitAndLose()) > -1);

                BigDecimal rate = infoDTO.getPrice().multiply(new BigDecimal("100"))
                        .divide(position.getBuyOrderPrice(), BigDecimal.ROUND_HALF_UP)
                        .setScale(2, BigDecimal.ROUND_HALF_UP);

                isProfit = rate.compareTo(BigDecimal.ONE.subtract(force_stop_percent).multiply(new BigDecimal("100"))) <= 0;

                String forceKey = String.format("FORCE_POS_%d", position.getId());

                if (isProfit) {
                    String flag = RedisShardedPoolUtils.get(forceKey);
                    isProfit = !StringUtils.equalsIgnoreCase(flag, "1");
                }

                if(isProfit){
                    try {
                        ForceSellPositonDTO dto = new ForceSellPositonDTO();
                        dto.setUserId(user.getId());
                        dto.setRealName(user.getRealName());
                        dto.setStockName(position.getStockName());
                        dto.setPosId(position.getId());
                        log.info("添加用户强平提醒 = {}， {} ， {}", dto.getUserId(), dto.getRealName(), dto.getStockName());
                        RedisShardedPoolUtils.lpush(key, dto);
                        RedisShardedPoolUtils.setEx(forceKey, "1", 1800);
                    } catch (Exception e) {
                        log.error("[盈亏达到最大亏损]强制平仓失败...");
                    }
                }

            }
            log.info("=====================");
        }
    }


    @Override
    public List<ForceSellPositonDTO> getForceList() {
        String key = "FORCE_SELL_LIST";
        return RedisShardedPoolUtils.list(key, ForceSellPositonDTO.class);
    }
}

