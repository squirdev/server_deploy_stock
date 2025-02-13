package com.nq.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.dao.StockSubscribeMapper;
import com.nq.dao.UserMapper;
import com.nq.dao.UserPositionMapper;
import com.nq.dao.UserStockSubscribeMapper;
import com.nq.pojo.*;
import com.nq.service.*;
import com.nq.utils.DateTimeUtil;
import com.nq.utils.KeyUtils;
import com.nq.utils.PropertiesUtil;
import com.nq.utils.redis.CookieUtils;
import com.nq.utils.redis.JsonUtil;
import com.nq.utils.redis.RedisShardedPoolUtils;
import com.nq.utils.stock.BuyAndSellUtils;
import com.nq.utils.stock.GeneratePosition;
import com.nq.utils.stock.sina.SinaStockApi;
import com.nq.utils.translate.GoogleTranslateUtil;
import com.nq.vo.stock.StockListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 新股申购
 * @author lr
 * @date 2020/07/24
 */
@Service("IUserStockSubscribeService")
@Slf4j
public class UserStockSubscribeServiceImpl  implements IUserStockSubscribeService {

    @Resource
    private UserStockSubscribeMapper userStockSubscribeMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ISiteMessageService iSiteMessageService;
    @Autowired
    StockSubscribeMapper stockSubscribeMapper;
    @Autowired
    IUserPositionService iUserPositionService;
    @Autowired
    ISiteProductService iSiteProductService;
    @Autowired
    IUserService iUserService;
    @Autowired
    IStockService iStockService;
    @Autowired
    ISiteSettingService iSiteSettingService;
    @Autowired
    ISiteSpreadService iSiteSpreadService;
    @Autowired
    UserPositionMapper userPositionMapper;
    @Autowired
    IAgentAgencyFeeService iAgentAgencyFeeService;
    /**
     * 用户新股申购
     * @param model
     * @return
     */
    @Override
    public ServerResponse insert(UserStockSubscribe model, HttpServletRequest request) throws Exception {
        int ret = 0;
        if (model == null) {
            return ServerResponse.createByErrorMsg("参数错误");
        }
        String property = PropertiesUtil.getProperty("user.cookie.name");
        String header = request.getHeader(property);
        if (header != null) {
            String userJson = RedisShardedPoolUtils.get(header);
            User user = this.iUserService.getCurrentRefreshUser(request);
            if (user == null){
                return ServerResponse.createBySuccessMsg("請先登錄");
            }
            if (model.getNewCode() != null) {
                StockSubscribe stockSubscribe = stockSubscribeMapper.selectOne(new QueryWrapper<StockSubscribe>().eq("code", model.getNewCode()));
                //实名认证开关
                SiteProduct siteProduct = iSiteProductService.getProductSetting();
                if (siteProduct.getRealNameDisplay() && (StringUtils.isBlank(user.getRealName()) || StringUtils.isBlank(user.getIdCard()))) {
                    return ServerResponse.createByErrorMsg("下单失败，请先实名认证");
                }
//                判断休息日不能买入
                if (siteProduct.getHolidayDisplay()) {
                    return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
                }
                //重复申购限制
//                UserStockSubscribe userStockSubscribe = userStockSubscribeMapper.selectOne(new QueryWrapper<UserStockSubscribe>().eq("new_code", model.getNewCode()).eq("user_id", user.getId()));
//                if (userStockSubscribe != null) {
//                    return ServerResponse.createByErrorMsg("请勿重复申购");
//                }
                if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
                    return ServerResponse.createByErrorMsg("下单失败，账户已被锁定");
                }
                if (stockSubscribe == null) {
                    return ServerResponse.createByErrorMsg("新股代码不存在");
                }
                //时间判定当前时间是否是申购时间
                SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
                if (siteSetting == null) {
                    log.error("下单出错，网站设置表不存在");
                    return ServerResponse.createByErrorMsg("下单失败，系统设置错误");
                }
                    String am_begin = siteSetting.getTransAmBegin();
                    String am_end = siteSetting.getTransAmEnd();
                    String pm_begin = siteSetting.getTransPmBegin();
                    String pm_end = siteSetting.getTransPmEnd();
                    boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
                    boolean pm_flag = BuyAndSellUtils.isTransTime(pm_begin, pm_end);
                    log.info("是否在上午交易时间 = {} 是否在下午交易时间 = {}", Boolean.valueOf(am_flag), Boolean.valueOf(pm_flag));
                    if (!am_flag && !pm_flag) {
                        return ServerResponse.createByErrorMsg("申购失败，不在交易时段内");
                    }

                if (model.getApplyNums() == null || model.getApplyNums() > stockSubscribe.getOrderNumber()*10000) {
                    return ServerResponse.createByErrorMsg("购买数量异常或大于发行数量" + stockSubscribe.getOrderNumber()*10000);
                }
                if (model.getType() == 2) {
                    if (user.getEnableAmt().compareTo(new BigDecimal(model.getApplyNums()).multiply(stockSubscribe.getPrice())) < 0) {
                        return ServerResponse.createByErrorMsg("用户可用余额不足，申购条件不满足");
                    }
                    user.setEnableAmt(user.getEnableAmt().subtract(new BigDecimal(model.getApplyNums()).multiply(stockSubscribe.getPrice())));
                    if (user.getDjzj()!=null) {
                        user.setDjzj(user.getDjzj().add(new BigDecimal(model.getApplyNums()).multiply(stockSubscribe.getPrice())));
                    }else
                    {
                        user.setDjzj(new BigDecimal(model.getApplyNums()).multiply(stockSubscribe.getPrice()));
                    }
                    int u = userMapper.updateById(user);
                    if (u <= 0) {
                        return ServerResponse.createByErrorMsg("未知原因，申购失败");
                    }
                }
                model.setUserId(user.getId());
                model.setNewName(stockSubscribe.getName());
                model.setAgentId(user.getAgentId());
                model.setAgentName(user.getAgentName());
                model.setNewType(stockSubscribe.getStockType());
                model.setPhone(user.getPhone());
                model.setBuyPrice(stockSubscribe.getPrice());

                model.setBond(new BigDecimal(model.getApplyNums()).multiply(stockSubscribe.getPrice()));
                model.setRealName(Objects.equals(user.getRealName(), "")||user.getRealName()==null ?"模拟用户无实名":user.getRealName());
                model.setAddTime(new Date());
                model.setOrderNo(KeyUtils.getUniqueKey());
                model.setType(model.getType());
            }

            ret = userStockSubscribeMapper.insert(model);
            if (ret > 0) {
                return ServerResponse.createBySuccessMsg("申购成功");
            } else {
                return ServerResponse.createByErrorMsg("申购失败");
            }
        }
        return ServerResponse.createByErrorMsg("未登录");
    }
    @Override
    public int update(UserStockSubscribe model) {
        int ret = userStockSubscribeMapper.update1(model);
        return ret>0 ? ret: 0;
    }

    /**
     * admin 新股申购-添加和修改
     */
    @Override
    public ServerResponse save(UserStockSubscribe model, HttpServletRequest request) {
        int ret = 0;
//        log.info("model"+model);
        if( model.getId() != null  ){
            if (model.getStatus() == 3||model.getStatus() == 2) {
                model.setEndTime(DateTimeUtil.getCurrentDate());
            }
            UserStockSubscribe userStockSubscribe = userStockSubscribeMapper.load(model.getId());
            if (userStockSubscribe.getStatus() == 5) {
                return ServerResponse.createByErrorMsg("已经转持仓了");
            }
//            else if (userStockSubscribe.getStatus() == 3) {
//                return ServerResponse.createByErrorMsg("已经审核过并且中签了，无法再次更改状态");
//            }else if (userStockSubscribe.getStatus() == 2) {
//                return ServerResponse.createByErrorMsg("已经审核过并且未中签");
//            }

            if( model.getStatus() == 3 && model.getApplyNumber() != null && userStockSubscribe.getStatus() == 1 ){
                if(userStockSubscribe.getApplyNums()< model.getApplyNumber()){
                    return ServerResponse.createByErrorMsg("中签数量超过申购数量");
                }
                model.setBond(userStockSubscribe.getBuyPrice().multiply(BigDecimal.valueOf(model.getApplyNumber())));

                //给用户推送消息
                SiteMessage siteMessage = new SiteMessage();
                siteMessage.setUserId(userStockSubscribe.getUserId());
                siteMessage.setUserName(userStockSubscribe.getRealName());
                siteMessage.setTypeName("新股申购");
                siteMessage.setStatus(1);

                siteMessage.setAddTime(DateTimeUtil.getCurrentDate());


                if (userStockSubscribe.getType() == 2){
                    User user = userMapper.selectByPrimaryKey(userStockSubscribe.getUserId());
                    UserStockSubscribe userStockSubscribe1 = userStockSubscribeMapper.load(model.getId());
                    Integer refundenum = userStockSubscribe1.getApplyNums() - model.getApplyNumber();
//                        log.info("refundenum"+refundenum);
                    Integer refund =refundenum * userStockSubscribe.getBuyPrice().intValue();
//                        log.info("退还金额"+refund);
                    user.setEnableAmt(user.getEnableAmt().add(BigDecimal.valueOf(refund)));
                    user.setDjzj(user.getDjzj().subtract(BigDecimal.valueOf(refund)));
                    int ret1 = userMapper.updateByPrimaryKey(user);
                    if (ret1 <= 0) {
                        return ServerResponse.createByErrorMsg("未知原因，申购失败");
                    }
                    siteMessage.setContent("【新股申购中签】恭喜您，新股申购中签成功，申购金额："+ userStockSubscribe.getBond() +"退还"+refund+"，请及时关注哦。");
                }else {

                    siteMessage.setContent("【新股申购中签】恭喜您，新股申购中签成功，申购金额：" + userStockSubscribe.getBond() + "，请及时关注哦。");
                }
                iSiteMessageService.insert(siteMessage);
            }else
            if( model.getStatus() == 2 && userStockSubscribe.getStatus() == 1) {
                //给达到消息强平提醒用户推送消息
                if (userStockSubscribe.getType() != 2) {
                    SiteMessage siteMessage = new SiteMessage();
                    siteMessage.setUserId(userStockSubscribe.getUserId());
                    siteMessage.setUserName(userStockSubscribe.getRealName());
                    siteMessage.setTypeName("新股申购");
                    siteMessage.setStatus(1);
                    siteMessage.setContent("【新股申购未中签】很遗憾，您的新股申购本次未中签，申购金额：" + userStockSubscribe.getBond() + "。");
                    siteMessage.setAddTime(DateTimeUtil.getCurrentDate());
                    iSiteMessageService.insert(siteMessage);
                }else {
                    User user = userMapper.selectByPrimaryKey(userStockSubscribe.getUserId());
                    user.setEnableAmt(user.getEnableAmt().add(userStockSubscribe.getBond()));
                    user.setDjzj(user.getDjzj().subtract(userStockSubscribe.getBond()));
                    userMapper.updateByPrimaryKey(user);
                    SiteMessage siteMessage = new SiteMessage();
                    siteMessage.setUserId(userStockSubscribe.getUserId());
                    siteMessage.setUserName(userStockSubscribe.getRealName());
                    siteMessage.setTypeName("新股申购");
                    siteMessage.setStatus(1);
                    siteMessage.setContent("【新股申购未中签】很遗憾，您的新股申购本次未中签，申购金额：" + userStockSubscribe.getBond() + "已退还。");
                    siteMessage.setAddTime(DateTimeUtil.getCurrentDate());
                    iSiteMessageService.insert(siteMessage);
                }

            }else if (model.getStatus() == 5){
                return iUserPositionService.newStockToPosition(model.getId());
            }
            int res = userStockSubscribeMapper.update1(model);
            if (res > 0) {
                return ServerResponse.createBySuccessMsg("审核成功");
            } else {
                return ServerResponse.createByErrorMsg("未知原因，审核失败");
            }


        } else{
            if(model.getPhone() != null&&model.getId()==null) {
                User user = userMapper.findByPhone(model.getPhone());
                if (user == null) {
                    return ServerResponse.createByErrorMsg("用户不存在");
                }
                model.setRealName(user.getRealName());
                model.setUserId(user.getId());
                model.setAgentId(user.getAgentId());
                model.setAgentName(user.getAgentName());


//            String cookie_name = PropertiesUtil.getProperty("admin.cookie.name");
//            String logintoken = CookieUtils.readLoginToken(request, cookie_name);
//            String adminJson = RedisShardedPoolUtils.get(logintoken);
//            SiteAdmin siteAdmin = (SiteAdmin) JsonUtil.string2Obj(adminJson, SiteAdmin.class);
                StockSubscribe stockSubscribe = stockSubscribeMapper.selectOne(new QueryWrapper<>(new StockSubscribe()).eq("code", model.getNewCode()));
                if (stockSubscribe == null) {
                    return ServerResponse.createByErrorMsg("失败，新股信息不存在");
                }

                model.setNewName(stockSubscribe.getName());
                model.setBuyPrice(stockSubscribe.getPrice());
                if (model.getApplyNums() > stockSubscribe.getOrderNumber()*10000 || model.getApplyNumber() > stockSubscribe.getOrderNumber()*10000 || model.getApplyNums() < model.getApplyNumber()) {
                    return ServerResponse.createByErrorMsg("申购数量或者中签数量异常");
                }
                model.setBond(model.getBuyPrice().multiply(BigDecimal.valueOf(model.getApplyNumber())));
                model.setAddTime(DateTimeUtil.getCurrentDate());
                model.setOrderNo(KeyUtils.getUniqueKey());
                model.setType(stockSubscribe.getType());
                ret = userStockSubscribeMapper.insert(model);
            }
        }
        if(ret>0){
            return ServerResponse.createBySuccessMsg("操作成功");
        }

        return ServerResponse.createByErrorMsg("操作失败");
    }

    /**
     * 发送站内信
     */
    @Override
    public ServerResponse sendMsg(UserStockSubscribe model, HttpServletRequest request) {
        int ret = 0;

        if(model!=null){
            //所有人发站内信
            if(model.getUserId() == 0){
                List<User> users = this.userMapper.listByAdmin(null, null, null, null);
                for(int k=0;k<users.size();k++){
                    User user = users.get(k);
                    SiteMessage siteMessage = new SiteMessage();
                    siteMessage.setUserId(user.getId());
                    siteMessage.setUserName(user.getRealName());
                    siteMessage.setTypeName("站内消息");
                    siteMessage.setStatus(1);
                    siteMessage.setContent("【站内消息】"+ model.getRemarks() );
                    siteMessage.setAddTime(DateTimeUtil.getCurrentDate());
                    ret = iSiteMessageService.insert(siteMessage);
                }
            } else {
                //指定用户发站内信
                User user = userMapper.selectByPrimaryKey(model.getUserId());
                SiteMessage siteMessage = new SiteMessage();
                siteMessage.setUserId(user.getId());
                siteMessage.setUserName(user.getRealName());
                siteMessage.setTypeName("站内消息");
                siteMessage.setStatus(1);
                siteMessage.setContent("【站内消息】"+ model.getRemarks() );
                siteMessage.setAddTime(DateTimeUtil.getCurrentDate());
                ret = iSiteMessageService.insert(siteMessage);
            }
        }
        if(ret>0){
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失败");
    }


    /*新股申购-查询列表*/
    @Override
    public ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, HttpServletRequest request){
        PageHelper.startPage(pageNum, pageSize);
        List<UserStockSubscribe> listData = this.userStockSubscribeMapper.pageList(pageNum, pageSize, keyword);
        PageInfo pageInfo = new PageInfo(listData);
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /*新股申购-查询详情*/
    @Override
    public ServerResponse getDetail(int id) {
        return ServerResponse.createBySuccess(this.userStockSubscribeMapper.load(id));
    }

    /*新股申购-查询用户最新新股申购数据*/
    @Override
    public ServerResponse getOneSubscribeByUserId(String type,HttpServletRequest request) {
        String property = PropertiesUtil.getProperty("user.cookie.name");
        String header = request.getHeader(property);
        if (header != null) {
            String userJson = RedisShardedPoolUtils.get(header);
            User user = (User) JsonUtil.string2Obj(userJson, User.class);
            if (user == null) {
                return ServerResponse.createByErrorMsg("用户未登录");
            }
            List<UserStockSubscribe> userStockSubscribe = null;
            if (type==null||type.equals("")){
                userStockSubscribe = this.userStockSubscribeMapper.selectList(new QueryWrapper<>(new UserStockSubscribe()).eq("phone", user.getPhone()).orderByDesc("add_time"));
            }else{
                userStockSubscribe = this.userStockSubscribeMapper.selectList(new QueryWrapper<>(new UserStockSubscribe()).eq("phone", user.getPhone()).eq("type", type).orderByDesc("add_time"));
            }

            List<UserStockSubscribe> list = new ArrayList<>();
            for (UserStockSubscribe userStockSubscribe1 : userStockSubscribe) {
                StockSubscribe stockSubscribe = stockSubscribeMapper.selectOne(new QueryWrapper<>(new StockSubscribe()).eq("code", userStockSubscribe1.getNewCode()));
                if (stockSubscribe != null) {
                    list.add(userStockSubscribe1);
                }
            }
//            PageInfo pageInfo = new PageInfo();
//            pageInfo.setList(userStockSubscribe);
//            GoogleTranslateUtil transan = new GoogleTranslateUtil();
//            //list转String
//            String json = JsonUtil.obj2String(list);
//            String translate;
//            try {
//                translate = transan.translate("zh", "en", json);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            //String转list
//            List<UserStockSubscribe> list1 = JsonUtil.string2Obj(translate, List.class, UserStockSubscribe.class);
            return ServerResponse.createBySuccess(list);
        }
        return ServerResponse.createByErrorMsg("请先登录");
    }
    /**
     * 新股申购-用户提交金额
     */
    @Override
    @Transactional
    public ServerResponse userSubmit(Integer id,HttpServletRequest request) {
        int ret = 0;
        String property = PropertiesUtil.getProperty("user.cookie.name");
        String header = request.getHeader(property);
        if (header != null) {
            String userJson = RedisShardedPoolUtils.get(header);
            User user = (User) JsonUtil.string2Obj(userJson, User.class);
            if (user == null) {
                return ServerResponse.createByErrorMsg("用户未登录");
            }
            if(id == null){
                return ServerResponse.createByErrorMsg("参数错误");
            }
            UserStockSubscribe userStockSubscribe = userStockSubscribeMapper.load(id);
            log.info("userStockSubscribe:{}",userStockSubscribe);
            if (userStockSubscribe != null && userStockSubscribe.getUserId().equals(user.getId())) {
                StockSubscribe stockSubscribe = stockSubscribeMapper.selectOne(new QueryWrapper<>(new StockSubscribe()).eq("code", userStockSubscribe.getNewCode()));
                if(userStockSubscribe.getType()== 2 ){
                    return ServerResponse.createByErrorMsg("线下配售无需支付");
                }
                //判断时间
                if (stockSubscribe.getSubscriptionTime()==null || stockSubscribe.getSubscriptionTime().getTime() < DateTimeUtil.getCurrentDate().getTime()) {
                        return ServerResponse.createByErrorMsg("不在认缴时间");
                }
                if (userStockSubscribe.getStatus() == 3) {
                    userStockSubscribe.setSubmitTime(DateTimeUtil.getCurrentDate());
                    userStockSubscribe.setStatus(4);
                    User user1 = userMapper.selectByPrimaryKey(userStockSubscribe.getUserId());
//                log.info("user" + user1);
                    if (user1.getEnableAmt().compareTo(userStockSubscribe.getBond()) < 0) {
                        return ServerResponse.createByErrorMsg("余额不足");
                    }
//                    log.info("原可用资金"+user1.getEnableAmt());
                    BigDecimal enableAmt = user1.getEnableAmt().subtract(userStockSubscribe.getBond());
//                    log.info("enableAmt" + enableAmt);
                    user1.setEnableAmt(enableAmt);
//                    log.info("可用资金" + user1.getEnableAmt()+"保证金"+userStockSubscribe.getBond()+"原djzj"+user1.getDjzj());
                    if (user1.getDjzj() != null) {
                        user1.setDjzj(user1.getDjzj().add(userStockSubscribe.getBond()));
                    }else {
                        user1.setDjzj(userStockSubscribe.getBond());
                    }
                    ret = userMapper.updateByPrimaryKeySelective(user1);
                }
                else {
                    return ServerResponse.createByErrorMsg("未中签无需缴费");
                }
            } else {
                return ServerResponse.createByErrorMsg("新股申购订单不存在！");
            }

            if (ret > 0) {
                ret = userStockSubscribeMapper.update1(userStockSubscribe);
                if (ret > 0) {
                    return ServerResponse.createBySuccessMsg("操作成功");
                } else {
                    return ServerResponse.createByErrorMsg("操作失败");
                }
            }else {
                return ServerResponse.createByErrorMsg("扣款失败");
            }
        }
        return ServerResponse.createByErrorMsg("请先登录");
    }
    /**
     * 新股申购-删除
     *
     * @param id
     * @param request
     * @return
     */
    @Override
    public ServerResponse del(int id, HttpServletRequest request) {
        int ret = 0;
        if(id>0){
            ret = userStockSubscribeMapper.delete1(id);
        }
        if(ret>0){
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失败");
    }
    /**
     * 新股抢筹 下单
     */
    @Override
    public ServerResponse buyNewStockQc(String code, Integer num, HttpServletRequest request) {
        User user = this.iUserService.getCurrentRefreshUser(request);

            if (code == null || "".equals(code) || num == null || num <= 0) {
                return ServerResponse.createByErrorMsg("股票代码不能为空或者购买数量异常");
                }
                StockSubscribe stockSubscribe = stockSubscribeMapper.selectOne(new QueryWrapper<StockSubscribe>().eq("code", code));
                //实名认证开关
                SiteProduct siteProduct = iSiteProductService.getProductSetting();
                if (siteProduct.getRealNameDisplay() && (StringUtils.isBlank(user.getRealName()) || StringUtils.isBlank(user.getIdCard()))) {
                    return ServerResponse.createByErrorMsg("下单失败，请先实名认证");
                }
//                判断休息日不能买入
                if (siteProduct.getHolidayDisplay()) {
                    return ServerResponse.createByErrorMsg("周末或节假日不能交易！");
                }
                //重复申购限制
//                UserStockSubscribe userStockSubscribe = userStockSubscribeMapper.selectOne(new QueryWrapper<UserStockSubscribe>().eq("new_code", model.getNewCode()).eq("user_id", user.getId()));
//                if (userStockSubscribe != null) {
//                    return ServerResponse.createByErrorMsg("请勿重复申购");
//                }
                if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
                    return ServerResponse.createByErrorMsg("下单失败，账户已被锁定");
                }
                if (stockSubscribe == null) {
                    return ServerResponse.createByErrorMsg("新股代码不存在");
                }


                if ("sh".equals(stockSubscribe.getStockType())||"sh".equals(stockSubscribe.getStockType())&& num < 1000){
                        return ServerResponse.createByErrorMsg("沪市新股申购数量最小为1000股");
                }else if ("sz".equals(stockSubscribe.getStockType())||"sz".equals(stockSubscribe.getStockType())&&num < 500){
                    return ServerResponse.createByErrorMsg("深市新股申购数量最小为500股");
                }

                if (user.getEnableAmt().compareTo(new BigDecimal(num).multiply(stockSubscribe.getPrice())) < 0) {
                        return ServerResponse.createByErrorMsg("用户可用余额不足，申购条件不满足");
                    }
                user.setEnableAmt(user.getEnableAmt().subtract(new BigDecimal(num).multiply(stockSubscribe.getPrice())));
//                if (user.getDjzj()!=null) {
//                        user.setDjzj(user.getDjzj().add(new BigDecimal(model.getApplyNums()).multiply(stockSubscribe.getPrice())));
//                }else
//                {
//                        user.setDjzj(new BigDecimal(model.getApplyNums()).multiply(stockSubscribe.getPrice()));
//                }
                int u = userMapper.updateById(user);
                if (u <= 0) {
                        return ServerResponse.createByErrorMsg("未知原因，申购失败");
                }
                UserStockSubscribe userStockSubscribe = new UserStockSubscribe();
                userStockSubscribe.setUserId(user.getId());
                userStockSubscribe.setNewName(stockSubscribe.getName());
                userStockSubscribe.setAgentId(user.getAgentId());
                userStockSubscribe.setAgentName(user.getAgentName());
                userStockSubscribe.setPhone(user.getPhone());
                userStockSubscribe.setApplyNums(num);
                userStockSubscribe.setNewType(stockSubscribe.getStockType());
                userStockSubscribe.setBuyPrice(stockSubscribe.getPrice());
                userStockSubscribe.setNewCode(stockSubscribe.getCode());
                userStockSubscribe.setBond(new BigDecimal(num).multiply(stockSubscribe.getPrice()));
                userStockSubscribe.setRealName(Objects.equals(user.getRealName(), "")||user.getRealName()==null ?"模拟用户无实名":user.getRealName());
                userStockSubscribe.setAddTime(new Date());
                userStockSubscribe.setOrderNo(KeyUtils.getUniqueKey());
                userStockSubscribe.setType(3);
            int ret = userStockSubscribeMapper.insert(userStockSubscribe);
            if (ret > 0) {

                return ServerResponse.createBySuccessMsg("申购抢筹成功");
            } else {
                return ServerResponse.createByErrorMsg("申购抢筹失败");
            }
    }
/**
     * 新股抢筹 股票列表
     */
    @Override
    public ServerResponse getStockQcList(HttpServletRequest request) {
        String nowDate = DateTimeUtil.stampToDate(String.valueOf(System.currentTimeMillis()));
        List<StockSubscribe> stockSubscribeListQc = this.stockSubscribeMapper.selectList(new QueryWrapper<StockSubscribe>().eq("list_date",nowDate));
        return ServerResponse.createBySuccess(stockSubscribeListQc);
    }

    /**
     * 用户新股抢筹列表
     * @param pageNum
     * @param pageSize
     * @param keyword
     * @param request
     * @return
     */
    @Override
    public ServerResponse getQcList(int pageNum, int pageSize, String keyword, HttpServletRequest request) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserStockSubscribe> qcList;
        if (StringUtils.isNotEmpty(keyword)) {
            qcList = userStockSubscribeMapper.selectList(new QueryWrapper<UserStockSubscribe>()
                    .like("phone",keyword)
                    .or().like("new_code",keyword)
                    .or().like("new_name",keyword)
                    .or().like("status",keyword)
                    .eq("type", 3).orderByDesc("add_time"));
        }else {
            qcList = userStockSubscribeMapper.selectList(new QueryWrapper<UserStockSubscribe>().eq("type", 3).orderByDesc("add_time"));
        }
        PageInfo pageInfo = new PageInfo(qcList);
        pageInfo.setList(qcList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 新股抢筹审核
     * @param status
     * @param request
     * @return
     */
    @Override
    public ServerResponse updateQcByAdmin(String id,String status,String num, HttpServletRequest request) {
        if (StringUtils.isEmpty(id)||StringUtils.isEmpty(status)) {
            return ServerResponse.createByErrorMsg("参数错误");
        }
        UserStockSubscribe userStockSubscribe = userStockSubscribeMapper.selectById(id);
        if (userStockSubscribe == null) {
            return ServerResponse.createByErrorMsg("抢筹记录不存在");
        }
        User user = userMapper.selectById(userStockSubscribe.getUserId());
        if (user == null) {
            return ServerResponse.createByErrorMsg("用户不存在");
        }

        if(userStockSubscribe.getStatus() == 1){
            if("2".equals(status)){
                user.setEnableAmt(user.getEnableAmt().add(userStockSubscribe.getBond()));
                int ret = userMapper.updateById(user);
                if (ret <= 0) {
                    return ServerResponse.createByErrorMsg("未知原因，审核失败");
                }
                userStockSubscribe.setStatus(2);
                userStockSubscribe.setEndTime(new Date());
                int ret1 = userStockSubscribeMapper.updateById(userStockSubscribe);
                if (ret1 > 0) {
                    return ServerResponse.createBySuccessMsg("审核成功");
                } else {
                    return ServerResponse.createByErrorMsg("审核失败");
                }
            }else if ("3".equals(status)){
                if (StringUtils.isEmpty(num) || Integer.parseInt(num) <= 0||Integer.parseInt(num)>userStockSubscribe.getApplyNums()) {
                    return ServerResponse.createByErrorMsg("中签数量不能为空，且不能大于申购数量");
                }
                Stock stock = (Stock) this.iStockService.findStockByCode(userStockSubscribe.getNewCode()).getData();
                if (stock == null) {
                    log.info("股票不存在");
                    return ServerResponse.createByErrorMsg("股票不存在");
                }
                SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
                if (siteSetting == null) {
                    log.error("下单出错，网站设置表不存在");
                    return ServerResponse.createByErrorMsg("下单失败，系统设置错误");
                }
                BigDecimal buy_amt = (userStockSubscribe.getBuyPrice()).multiply(new BigDecimal(num));
                UserPosition userPosition = new UserPosition();
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
                userPosition.setBuyOrderPrice(userStockSubscribe.getBuyPrice());
                userPosition.setOrderDirection("买涨");
                userPosition.setOrderNum(Integer.valueOf(num));

                if (stock.getStockPlate() != null) {
                    userPosition.setStockPlate(stock.getStockPlate());
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
                log.info("创建模拟持仓 手续费（配资后总资金 * 百分比） = {}", buy_fee_amt);
                userPosition.setOrderFee(buy_fee_amt);


                BigDecimal buy_yhs_amt = buy_amt.multiply(siteSetting.getDutyFee()).setScale(2, 4);
                log.info("创建模拟持仓 印花税（配资后总资金 * 百分比） = {}", buy_yhs_amt);
                userPosition.setOrderSpread(buy_yhs_amt);
                StockListVO stockListVO = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(stock.getStockGid()));
                BigDecimal now_price = new BigDecimal(stockListVO.getNowPrice());

//                if (now_price.compareTo(new BigDecimal("0")) == 0) {
//                    log.info(stock.getStockGid()+"报价0，");
//                    return ServerResponse.createByErrorMsg("报价0，请稍后再试");
//
//                }

//                double stock_crease = stockListVO.getHcrate().doubleValue();
//                SiteSpread siteSpread = iSiteSpreadService.findSpreadRateOne(new BigDecimal(stock_crease), buy_amt, stock.getStockCode(), now_price);
                BigDecimal spread_rate_amt = new BigDecimal("0");
//                if(siteSpread != null){
//                    spread_rate_amt = buy_amt.multiply(siteSpread.getSpreadRate()).setScale(2, 4);
//                    log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", siteSpread.getSpreadRate(), spread_rate_amt);
//                } else{
//                    log.info("用户购买点差费（配资后总资金 * 百分比{}） = {}", "设置异常", spread_rate_amt);
//                }

                userPosition.setSpreadRatePrice(spread_rate_amt);


                BigDecimal profit_and_lose = new BigDecimal("0");
                userPosition.setProfitAndLose(profit_and_lose);


                BigDecimal all_profit_and_lose = profit_and_lose.subtract(buy_fee_amt).subtract(buy_yhs_amt).subtract(spread_rate_amt);
                userPosition.setAllProfitAndLose(all_profit_and_lose);


                userPosition.setOrderStayDays(Integer.valueOf(0));
                userPosition.setOrderStayFee(new BigDecimal("0"));
                userPosition.setSpreadRatePrice(new BigDecimal("0"));

                int insertPositionCount = this.userPositionMapper.insert(userPosition);
                if (insertPositionCount > 0) {
                    log.info("【抢筹创建持仓】保存记录成功");
                } else {
                    log.error("【抢筹创建持仓】保存记录出错");
                }
                iAgentAgencyFeeService.AgencyFeeIncome(1,userPosition.getPositionSn());
                userStockSubscribe.setStatus(3);
                userStockSubscribe.setEndTime(new Date());
                userStockSubscribe.setFixTime(new Date());
                userStockSubscribe.setApplyNumber(Integer.valueOf(num));
                this.userStockSubscribeMapper.updateById(userStockSubscribe);
                BigDecimal reimburse = new BigDecimal(userStockSubscribe.getApplyNums()-Integer.parseInt(num)).multiply(userStockSubscribe.getBuyPrice());
                user.setEnableAmt(user.getEnableAmt().add(reimburse));
                int ret = userMapper.updateById(user);
                if (ret <= 0) {
                    return ServerResponse.createByErrorMsg("未知原因，审核失败");
                } else {
                    return ServerResponse.createBySuccess("新股抢筹审核通过，以转入用户持仓，订单号：" + userPosition.getPositionSn());
                }
            }
            }
        userStockSubscribe.setStatus(Integer.valueOf(status));
        userStockSubscribe.setApplyNumber(Integer.valueOf(num));
        userStockSubscribe.setEndTime(new Date());
        int res = this.userStockSubscribeMapper.updateById(userStockSubscribe);
        if (res <= 0) {
            return ServerResponse.createByErrorMsg("修改状态失败");
        } else {
            return ServerResponse.createBySuccessMsg("修改状态成功");
        }
    }

    /**
     * 新股抢筹添加
     * @param phone
     * @param code
     * @param num
     * @param request
     * @return
     */

    @Override
    public ServerResponse addQcByAdmin(String phone, String code, String num, HttpServletRequest request) {
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code) || StringUtils.isEmpty(num)) {
            return ServerResponse.createByErrorMsg("参数错误");
        }
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));
        if (user == null) {
            return ServerResponse.createByErrorMsg("用户不存在");
        }
        StockSubscribe stockSubscribe = stockSubscribeMapper.selectOne(new QueryWrapper<StockSubscribe>().eq("code", code));
        if (stockSubscribe == null) {
            return ServerResponse.createByErrorMsg("新股代码不存在");
        }
        UserStockSubscribe userStockSubscribe = new UserStockSubscribe();
        userStockSubscribe.setOrderNo(KeyUtils.getUniqueKey());
        userStockSubscribe.setUserId(user.getId());
        userStockSubscribe.setRealName(user.getRealName() == null ? "模拟用户无实名" : user.getRealName());
        userStockSubscribe.setPhone(user.getPhone());
        userStockSubscribe.setAgentId(user.getAgentId());
        userStockSubscribe.setAgentName(user.getAgentName());
        userStockSubscribe.setNewCode(stockSubscribe.getCode());
        userStockSubscribe.setNewName(stockSubscribe.getName());
        userStockSubscribe.setBond(new BigDecimal(num).multiply(stockSubscribe.getPrice()));
        userStockSubscribe.setBuyPrice(stockSubscribe.getPrice());
        userStockSubscribe.setApplyNums(Integer.valueOf(num));
        userStockSubscribe.setType(3);
        userStockSubscribe.setStatus(1);
        userStockSubscribe.setAddTime(new Date());
        userStockSubscribe.setNewType(stockSubscribe.getStockType());
        int ret = userStockSubscribeMapper.insert(userStockSubscribe);
        if (ret > 0) {
            return ServerResponse.createBySuccessMsg("添加成功");
        } else {
            return ServerResponse.createByErrorMsg("添加失败");
        }
    }

    @Override
    public ServerResponse getzqjkl(HttpServletRequest request) {
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null) {
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        List<UserStockSubscribe> userStockSubscribes = userStockSubscribeMapper.selectList(new QueryWrapper<UserStockSubscribe>().eq("phone", user.getPhone()).ge("status", 3).orderByDesc("add_time"));
        return ServerResponse.createBySuccess(userStockSubscribes);
    }
}