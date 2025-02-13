package com.nq.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.pojo.ConvertBond;
import com.nq.service.ConvertBondService;
import com.nq.dao.ConvertBondMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Administrator
* @description 针对表【convert_bond(转债债券表)】的数据库操作Service实现
* @createDate 2022-12-05 16:33:20
*/
@Service
public class ConvertBondServiceImpl extends ServiceImpl<ConvertBondMapper, ConvertBond>
    implements ConvertBondService{
    @Autowired
    private ConvertBondMapper convertBondMapper;

    @Override
    public ServerResponse listByPage(Integer page, Integer size) {
        PageHelper.startPage(page,size);
          List<ConvertBond> convertBonds = this.convertBondMapper.selectList(null);
        PageInfo pageInfo = new PageInfo(convertBonds);
        return ServerResponse.createBySuccess(pageInfo);
    }
}




