package com.nq.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nq.pojo.UserStockSubscribe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 新股申购
 * @author lr
 * @date 2020/09/11
 */
@Mapper
@Repository
public interface UserStockSubscribeMapper extends BaseMapper<UserStockSubscribe>   {

    /**
     * [新增]
     * @author lr
     * @date 2020/09/11
     **/
    int insert1(UserStockSubscribe userStockSubscribe);

    /**
     * [刪除]
     * @author lr
     * @date 2020/09/11
     **/
    int delete1(int id);

    /**
     * [更新]
     * @author lr
     * @date 2020/09/11
     **/
    int update1(UserStockSubscribe userStockSubscribe);

    /**
     * [查询] 根据主键 id 查询
     * @author lr
     * @date 2020/09/11
     **/
    UserStockSubscribe load(int id);

    /**
     * [查询] 分页查询
     * @author lr
     * @date 2020/09/11
     **/
    List<UserStockSubscribe> pageList(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("keyword") String keyword);

    /**
     * [查询] 分页查询 count
     * @author lr
     * @date 2020/09/11
     **/
    int pageListCount(int offset, int pagesize);

    /**
     * [查询] 查询用户最新新股申购数据
     * @author lr
     * @date 2020/09/11
     **/
    List<UserStockSubscribe> getOneSubscribeByUserId(String phone);


}

