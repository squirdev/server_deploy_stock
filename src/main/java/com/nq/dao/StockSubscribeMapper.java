package com.nq.dao;

import com.nq.pojo.StockSubscribe;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 11527
* @description 针对表【stock_subscribe(新股)】的数据库操作Mapper
* @createDate 2022-10-24 23:27:27
* @Entity com.nq.pojo.StockSubscribe
*/
@Mapper
public interface StockSubscribeMapper extends BaseMapper<StockSubscribe> {

}




