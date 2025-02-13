package com.nq.service.impl;


import com.github.pagehelper.PageHelper;

import com.github.pagehelper.PageInfo;

import com.nq.common.ServerResponse;

import com.nq.dao.AgentUserMapper;

import com.nq.dao.UserCashDetailMapper;

import com.nq.pojo.AgentUser;

import com.nq.pojo.User;

import com.nq.pojo.UserCashDetail;

import com.nq.service.IAgentUserService;

import com.nq.service.IUserCashDetailService;

import com.nq.service.IUserService;


import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

 @Service("iUserCashDetailService")
 public class UserCashDetailServiceImpl implements IUserCashDetailService {

   @Autowired
   UserCashDetailMapper userCashDetailMapper;

   @Autowired
   IUserService iUserService;

   @Autowired
   IAgentUserService iAgentUserService;

   @Autowired
   AgentUserMapper agentUserMapper;



   public ServerResponse<PageInfo> findUserCashDetailList(Integer positionId, HttpServletRequest request, int pageNum, int pageSize) {

     PageHelper.startPage(pageNum, pageSize);



     User user = this.iUserService.getCurrentUser(request);
     if (user == null ){
       return ServerResponse.createBySuccessMsg("請先登錄");
     }


     List<UserCashDetail> userCashDetails = this.userCashDetailMapper.findUserCashDetailList(user.getId(), positionId);



     PageInfo pageInfo = new PageInfo(userCashDetails);



     return ServerResponse.createBySuccess(pageInfo);

   }

   public ServerResponse<PageInfo> listByAgent(Integer userId, String userName, Integer agentId, Integer positionId, HttpServletRequest request, int pageNum, int pageSize) {

     AgentUser currentAgent = this.iAgentUserService.getCurrentAgent(request);
     if (currentAgent ==null){
       return    ServerResponse.createByError("請先登錄",null);
     }


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

     List<UserCashDetail> cashDetails = this.userCashDetailMapper.listByAgent(userId, userName, searchId, positionId);

     PageInfo pageInfo = new PageInfo(cashDetails);

     return ServerResponse.createBySuccess(pageInfo);

   }

   public ServerResponse<PageInfo> listByAdmin(Integer userId, String userName, Integer agentId, Integer positionId, int pageNum, int pageSize) {

     PageHelper.startPage(pageNum, pageSize);

     List<UserCashDetail> cashDetails = this.userCashDetailMapper.listByAdmin(userId, userName, agentId, positionId);

     PageInfo pageInfo = new PageInfo(cashDetails);

     return ServerResponse.createBySuccess(pageInfo);

   }


   public int deleteByUserId(Integer userId) { return this.userCashDetailMapper.deleteByUserId(userId); }

   public ServerResponse delCash(Integer cashId) {
     if (cashId == null) {
       return ServerResponse.createByErrorMsg("删除id不能为空");
     }
     int updateCount = this.userCashDetailMapper.deleteByPrimaryKey(cashId);
     if (updateCount > 0) {
       return ServerResponse.createBySuccessMsg("删除成功");
     }
     return ServerResponse.createByErrorMsg("删除失败");
   }

 }
