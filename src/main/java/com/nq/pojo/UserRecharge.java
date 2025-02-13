package com.nq.pojo;


import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRecharge {

    private Integer id;
    @Excel(name = "用户id")
    private Integer userId;
    @Excel(name = "用户名")
    private String nickName;
    @Excel(name = "代理id")
    private Integer agentId;
    @Excel(name = "订单号")
    private String orderSn;

    private String paySn;
    @Excel(name = "充值渠道" ,replace = { "支付宝_0", "对公打款_1" })
    private String payChannel;
    /**
     * 转账用户名
     */
    @Excel(name = "转账用户名")
    private String payUsername;
    /**
     * 认证图片
     */
    @Excel(name = "认证图片")
    private String payUrl;
    @Excel(name = "充值金额")
    private BigDecimal payAmt;
    @Excel(name = "状态" ,replace = { "审核中_0", "成功_1", "失败_2" })
    private Integer orderStatus;

    private String orderDesc;

    @Excel(name = "申请时间", databaseFormat = "yyyyMMddHHmmss", format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "支付时间", databaseFormat = "yyyyMMddHHmmss", format = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /*支付通道主键id*/
    private Integer payId;
    /**
     * 渠道账号（平台银行卡号）
     */
    private String channelAccount;
    /**
     * 渠道名（开户行）
     */
    private String channelType;
    /**
     * 账号名称，主体（银行收款人）
     */
    private String channelName;

}
