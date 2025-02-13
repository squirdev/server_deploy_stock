package com.nq.pojo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

import java.util.Date;


public class UserWithdraw {

    private Integer id;
    @Excel(name = "用户id")
    private Integer userId;
    @Excel(name = "用户名")
    private String nickName;
    @Excel(name = "代理id")
    private Integer agentId;
    @Excel(name = "出金金额")
    private BigDecimal withAmt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "申请时间", databaseFormat = "yyyyMMddHHmmss", format = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "出金时间", databaseFormat = "yyyyMMddHHmmss", format = "yyyy-MM-dd HH:mm:ss")
    private Date transTime;

    private String withName;
    @Excel(name = "银行卡号")
    private String bankNo;
    @Excel(name = "银行名称")
    private String bankName;
    @Excel(name = "银行支行")
    private String bankAddress;

    @Excel(name = "状态" ,replace = { "审核中_0", "成功_1", "失败_2", "取消_3"  })
    private Integer withStatus;
    @Excel(name = "手续费")
    private BigDecimal withFee;
    @Excel(name = "原因")
    private String withMsg;


    public UserWithdraw(Integer id, Integer userId, String nickName, Integer agentId, BigDecimal withAmt, Date applyTime, Date transTime, String withName, String bankNo, String bankName, String bankAddress, Integer withStatus, BigDecimal withFee, String withMsg) {

        this.id = id;

        this.userId = userId;

        this.nickName = nickName;

        this.agentId = agentId;

        this.withAmt = withAmt;

        this.applyTime = applyTime;

        this.transTime = transTime;

        this.withName = withName;

        this.bankNo = bankNo;

        this.bankName = bankName;

        this.bankAddress = bankAddress;

        this.withStatus = withStatus;

        this.withFee = withFee;

        this.withMsg = withMsg;

    }

    public UserWithdraw() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public BigDecimal getWithAmt() {
        return withAmt;
    }

    public void setWithAmt(BigDecimal withAmt) {
        this.withAmt = withAmt;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public Date getTransTime() {
        return transTime;
    }

    public void setTransTime(Date transTime) {
        this.transTime = transTime;
    }

    public String getWithName() {
        return withName;
    }

    public void setWithName(String withName) {
        this.withName = withName;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAddress() {
        return bankAddress;
    }

    public void setBankAddress(String bankAddress) {
        this.bankAddress = bankAddress;
    }

    public Integer getWithStatus() {
        return withStatus;
    }

    public void setWithStatus(Integer withStatus) {
        this.withStatus = withStatus;
    }

    public BigDecimal getWithFee() {
        return withFee;
    }

    public void setWithFee(BigDecimal withFee) {
        this.withFee = withFee;
    }

    public String getWithMsg() {
        return withMsg;
    }

    public void setWithMsg(String withMsg) {
        this.withMsg = withMsg;
    }
}