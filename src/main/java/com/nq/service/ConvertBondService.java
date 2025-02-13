package com.nq.service;

import com.nq.common.ServerResponse;
import com.nq.pojo.ConvertBond;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【convert_bond(转债债券表)】的数据库操作Service
* @createDate 2022-12-05 16:33:20
*/
public interface ConvertBondService extends IService<ConvertBond> {

    ServerResponse listByPage(Integer page, Integer size);
}
