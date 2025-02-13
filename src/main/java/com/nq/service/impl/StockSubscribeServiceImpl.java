package com.nq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.nq.common.ServerResponse;
import com.nq.pojo.Stock;
import com.nq.pojo.StockSubscribe;
import com.nq.service.IStockSubscribeService;
import com.nq.dao.StockSubscribeMapper;
import com.nq.utils.DateTimeUtil;
import com.nq.utils.stock.sina.SinaStockApi;
import com.nq.vo.stock.StockAdminListVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author 11527
* @description 针对表【stock_subscribe(新股)】的数据库操作Service实现
* @createDate 2022-10-24 23:27:27
*/
@Service
@Slf4j
public class StockSubscribeServiceImpl extends ServiceImpl<StockSubscribeMapper, StockSubscribe>
    implements IStockSubscribeService {
    @Autowired
    StockSubscribeMapper stockSubscribeMapper;

/**
* @Description: 用户新股列表
* @Param:
* @return:
* @Author: tf
* @Date: 2022/10/25
*/
    @Override
    public ServerResponse<PageInfo> list( int pageNum, int pageSize,String name,String code,Integer zt,Integer isLock,Integer type, HttpServletRequest request) {
        PageHelper.startPage(pageNum, pageSize);
       //name和code模糊查询
        QueryWrapper<StockSubscribe> queryWrapper = new QueryWrapper();
        if (name != null && !name.equals("")) {
            queryWrapper.like("name", name);
        }
        if (code != null && !code.equals("")) {
            queryWrapper.like("code", code);
        }
        if (zt != null && !zt.equals("")) {
            queryWrapper.eq("zt", zt);
        }
        if(isLock!=null && !isLock.equals("")){
            queryWrapper.eq("is_lock",isLock);
        }
        if (type != null && !type.equals("")) {
            queryWrapper.eq("type", type);
        }
        String s = DateTimeUtil.dateToStr(new Date());
        //subscribe_time大于当前时间
        queryWrapper.ge("subscribe_time", DateTimeUtil.dateToStr1(new Date())).orderByAsc("subscribe_time");
        List<StockSubscribe> stockSubscribeList = this.stockSubscribeMapper.selectList(queryWrapper);

//        List<StockAdminListVO> stockAdminListVOS = Lists.newArrayList();
        PageInfo pageInfo = new PageInfo(stockSubscribeList);
        pageInfo.setList(stockSubscribeList);
        return ServerResponse.createBySuccess(pageInfo);
    }


    @Override
    public ServerResponse<PageInfo> listbyadmin(int pageNum, int pageSize,String name,String code,Integer zt,Integer isLock,Integer type, HttpServletRequest request) {
        PageHelper.startPage(pageNum, pageSize);
        //name和code模糊查询
        QueryWrapper<StockSubscribe> queryWrapper = new QueryWrapper();
        if (name != null && !name.equals("")) {
            queryWrapper.like("name", name);
        }
        if (code != null && !code.equals("")) {
            queryWrapper.like("code", code);
        }
        if (zt != null && !zt.equals("")) {
            queryWrapper.eq("zt", zt);
        }
        if(isLock!=null && !isLock.equals("")){
            queryWrapper.eq("is_lock",isLock);
        }
        if (type != null && !type.equals("")) {
            queryWrapper.eq("type", type);
        }
        String s = DateTimeUtil.dateToStr(new Date());
        //subscribe_time大于当前时间
        queryWrapper.orderByAsc("subscribe_time");
        List<StockSubscribe> stockSubscribeList = this.stockSubscribeMapper.selectList(queryWrapper);

//        List<StockAdminListVO> stockAdminListVOS = Lists.newArrayList();
        PageInfo pageInfo = new PageInfo(stockSubscribeList);
        pageInfo.setList(stockSubscribeList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
* @Description: 新增新股
* @Param:
* @return:
* @Author: tf
* @Date: 2022/10/25
*/
    @Override
    public ServerResponse add(StockSubscribe model, HttpServletRequest request) {
        //判断是否已经存在
        log.info("model:{}",model);
        List<StockSubscribe> stockSubscribeList = this.stockSubscribeMapper.selectList(new QueryWrapper<StockSubscribe>().eq("code", model.getCode()));
        if (stockSubscribeList != null && stockSubscribeList.size()>0) {
            return ServerResponse.createByErrorMsg("新股已经存在，不要重复添加");
        }
        String sinaStock = SinaStockApi.getSinaStock(model.getStockType()+model.getCode());
        String[] arrayOfString = sinaStock.split(",");
//        if (arrayOfString.length < 10)
//        return ServerResponse.createByErrorMsg("数据源无该新股");
        //添加新股
        int resultCount = this.stockSubscribeMapper.insert(model);
        if (resultCount > 0) {
            return ServerResponse.createBySuccessMsg("添加新股成功");
        }
        return ServerResponse.createByErrorMsg("添加新股失败");
    }
    /**
    * @Description:  修改新股
    * @Param:
    * @return:
    * @Author: tf
    * @Date: 2022/10/25
    */
    @Override
    public ServerResponse update(StockSubscribe model, HttpServletRequest request) {
        StockSubscribe stockSubscribe = this.stockSubscribeMapper.selectById(model.getNewlistId());
        if (stockSubscribe == null) {
            return ServerResponse.createByErrorMsg("新股不存在");
        }
        //修改新股
        int resultCount = this.stockSubscribeMapper.updateById(model);
        if (resultCount > 0) {
            return ServerResponse.createBySuccessMsg("修改新股成功");
        }
        return ServerResponse.createByErrorMsg("修改新股失败");
    }
    /**
    * @Description:  删除新股
    * @Param:
    * @return:
    * @Author: tf
    * @Date: 2022/10/25
    */
    @Override
    public ServerResponse del(Integer id, HttpServletRequest request) {
        StockSubscribe stockSubscribe = this.stockSubscribeMapper.selectById(id);
        if (stockSubscribe == null) {
            return ServerResponse.createByErrorMsg("新股不存在");
        }
        //删除新股
        int resultCount = this.stockSubscribeMapper.deleteById(id);
        if (resultCount > 0) {
            return ServerResponse.createBySuccessMsg("删除新股成功");
        }
        return ServerResponse.createByErrorMsg("删除新股失败");
    }


    /**
     * @Description:  新股抢筹列表
     */
    @Override
    public ServerResponse newStockQc(HttpServletRequest request) {
        String nowDate = DateTimeUtil.stampToDate(String.valueOf(System.currentTimeMillis()));
        List<StockSubscribe> stockSubscribeListQc = this.stockSubscribeMapper.selectList(new QueryWrapper<StockSubscribe>().eq("list_date",nowDate));
        return ServerResponse.createBySuccess(stockSubscribeListQc);
    }



}




