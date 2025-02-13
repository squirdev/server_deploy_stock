package com.nq.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nq.common.ServerResponse;
import com.nq.dao.SiteMessageMapper;
import com.nq.pojo.SiteMessage;
import com.nq.pojo.User;
import com.nq.service.ISiteMessageService;
import com.nq.service.IUserService;
import com.nq.vo.agent.AgentAgencyFeeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * site_message
 * @author lr
 * @date 2020/07/16
 */
@Service("ISiteMessageService")
public class SiteMessageServiceImpl implements ISiteMessageService {

    @Resource
    private SiteMessageMapper siteMessageMapper;

    @Autowired
    IUserService iUserService;

    @Override
    public int insert(SiteMessage siteMessage) {
        int ret = 0;
        if (siteMessage == null) {
            return 0;
        }

        ret = siteMessageMapper.insert(siteMessage);
        return ret;
    }

    @Override
    public int update(SiteMessage siteMessage) {
        int ret = siteMessageMapper.update(siteMessage);
        return ret>0 ? ret: 0;
    }

    @Override
    public int delete(int id) {
        int ret = siteMessageMapper.delete(id);
        return ret>0 ? ret: 0;
    }

    /*查询用户站内消息列表
     * */
    @Override
    public ServerResponse<PageInfo> getSiteMessageByUserIdList(int pageNum, int pageSize, int userId, HttpServletRequest request) {
        Page<AgentAgencyFeeVO> page = PageHelper.startPage(pageNum, pageSize);
        int uid = 0;
        //总后台查所有,其他人走登录信息userid
        if(userId != 999){
            User user = this.iUserService.getCurrentRefreshUser(request);
            if (user == null){
                return ServerResponse.createBySuccessMsg("請先登錄");
            }
            uid = user.getId();
        }
        User user = this.iUserService.getCurrentRefreshUser(request);
        List<SiteMessage> siteMessageList = this.siteMessageMapper.getSiteMessageByUserIdList(uid);
        PageInfo pageInfo = new PageInfo(page);
        pageInfo.setList(siteMessageList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public int getIsDayCount(Integer userId, String typeName) {
        int ret = siteMessageMapper.getIsDayCount(userId, typeName);
        return ret>0 ? ret: 0;
    }

    @Override
    public int updateMessageStatus(HttpServletRequest request) {
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null){
            return 0;
        }
        int ret = siteMessageMapper.updateMessageStatus(user.getId());
        return ret>0 ? ret: 0;
    }

    @Override
    public int getUnreadCount(HttpServletRequest request) {
        User user = this.iUserService.getCurrentRefreshUser(request);
       if (user == null){
           return 0;
       }
        int ret = siteMessageMapper.getUnreadCount(user.getId());
        return ret>0 ? ret: 0;
    }

    public ServerResponse del(Integer id, HttpServletRequest request) {
        if (id == null) {
            return ServerResponse.createByErrorMsg("id不能为空");
        }

        int updateCount = this.siteMessageMapper.delete(id);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("删除成功");
        }
        return ServerResponse.createByErrorMsg("删除失败");
    }

}
