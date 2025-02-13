package com.nq.service;

import com.nq.pojo.AgentDistributionUser;

public interface IAgentDistributionUserService {
    /**
     * 新增
     */
    int insert(AgentDistributionUser agentDistributionUser);

    /**
     * 更新
     */
    int update(AgentDistributionUser agentDistributionUser);
}
