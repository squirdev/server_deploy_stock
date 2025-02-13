package com.nq.service.impl;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.nq.common.ServerResponse;
import com.nq.dao.StockMapper;
import com.nq.pojo.SiteSetting;
import com.nq.pojo.Stock;
import com.nq.pojo.StockDz;
import com.nq.service.StockDzService;
import com.nq.dao.StockDzMapper;
import com.nq.utils.DateTimeUtil;
import com.nq.utils.stock.sina.SinaStockApi;
import com.nq.vo.stock.StockDzVo;
import com.nq.vo.stock.StockListVO;
import com.nq.vo.stock.StockVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Administrator
* @description 针对表【stock_dz】的数据库操作Service实现
* @createDate 2022-12-03 17:09:01
*/
@Service
@Slf4j
public class StockDzServiceImpl extends ServiceImpl<StockDzMapper, StockDz>
    implements StockDzService{
    @Autowired
    private StockDzMapper stockDzMapper;
    @Autowired
    private SiteSettingServiceImpl siteSettingService;
    @Autowired
    private StockMapper stockMapper;
    @Override
    public ServerResponse getDzList() {
        List<StockDz> list = stockDzMapper.selectList(new QueryWrapper<StockDz>().eq("is_show", 1));
        List<StockDzVo> stockDzVos = Lists.newArrayList();
        for (StockDz stockDz : list) {
            StockListVO stockListVO = SinaStockApi.assembleStockListVO(SinaStockApi.getSinaStock(stockDz.getStockGid()));
            BigDecimal price = new BigDecimal(stockListVO.getNowPrice()).multiply(stockDz.getDiscount());

            StockDzVo stockDzVo = new StockDzVo();
            stockDzVo.setId(stockDz.getId());
            stockDzVo.setStockName(stockDz.getStockName());
            stockDzVo.setStockCode(stockDz.getStockCode());
            stockDzVo.setPrice(price);
            stockDzVo.setStockType(stockDz.getStockType());
            stockDzVo.setStockGid(stockDz.getStockGid());
            stockDzVo.setStockPlate(stockDz.getStockPlate());
            stockDzVo.setIsLock(stockDz.getIsLock());
            stockDzVo.setStockNum(stockDz.getStockNum());
            stockDzVo.setStartTime(stockDz.getStartTime());
            stockDzVo.setEndTime(stockDz.getEndTime());
            stockDzVos.add(stockDzVo);
        }
        return ServerResponse.createBySuccess(stockDzVos);
    }

    @Override
    public ServerResponse addByAdmin(String stockCode, String stockNum, String password, String startTime, String endTime, String discount) {
        if (stockCode == null || stockCode.equals("")||stockNum == null || stockNum.equals("")||password == null || discount == null|| discount.equals("")||
                password.equals("")|| startTime == null || startTime.equals("")||endTime == null || endTime.equals("")){
            return ServerResponse.createByErrorMsg("参数不能为空");
        }
        Stock stock = stockMapper.findStockByCode(stockCode);
        if (stock == null){
            return ServerResponse.createByErrorMsg("股票代码不存在");
        }
        StockDz stockDz = new StockDz();
        stockDz.setStockName(stock.getStockName());
        stockDz.setStockCode(stock.getStockCode());
        stockDz.setStockType(stock.getStockType());
        stockDz.setStockGid(stock.getStockGid());
        stockDz.setStockPlate(stock.getStockPlate());
        stockDz.setIsLock(stock.getIsLock());
        stockDz.setIsShow(1);
        stockDz.setAddTime(new Date());
        stockDz.setSpreadRate(stock.getSpreadRate());
        stockDz.setIncreaseRatio(BigDecimal.ZERO);
        stockDz.setStockNum(Integer.valueOf(stockNum));
        stockDz.setPassword(password);
        stockDz.setStartTime(DateTimeUtil.strToDate(startTime));
        stockDz.setEndTime(DateTimeUtil.strToDate(endTime));
        stockDz.setDiscount(new BigDecimal(discount));
        int res = stockDzMapper.insert(stockDz);
        if (res > 0) {
            return ServerResponse.createBySuccessMsg("添加成功");
        }
        return ServerResponse.createByErrorMsg("添加失败");
    }

    @Override
    public ServerResponse getDzListByAdmin(String Keywords) {
        List<StockDz> list = null;
//        log.info("Keywords:{}",Keywords);
        if (Keywords != null && !"".equals(Keywords)){
            list = stockDzMapper.selectList(new QueryWrapper<StockDz>().like("stock_name", Keywords).or().like("stock_code", Keywords).or().like("stock_type", Keywords));
        }else {
            list = stockDzMapper.selectList(new QueryWrapper<StockDz>().orderByDesc("id"));
        }
        log.info("list:{}",list);
        return ServerResponse.createBySuccess(list);
    }

    @Override
    public ServerResponse updByAdmin(StockDz model) {
//        log.info("model:{}",model);
        return stockDzMapper.updateById(model) > 0 ? ServerResponse.createBySuccess("修改成功") : ServerResponse.createByErrorMsg("修改失败");
    }

    @Override
    public ServerResponse deleteByAdmin(String id) {
        int res =  stockDzMapper.deleteById(id);
        if (res > 0) {
            return ServerResponse.createBySuccess("删除成功");
        }
        return ServerResponse.createByErrorMsg("删除失败");
    }
}




