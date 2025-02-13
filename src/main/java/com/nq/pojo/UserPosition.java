package com.nq.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@TableName(value ="user_position")
public class UserPosition implements Serializable {
    @TableId(type = IdType.AUTO,value = "id")
    private Integer id;
    private Integer positionType;
    private String positionSn;
    private Integer userId;
    private String nickName;
    private Integer agentId;
    private String stockName;
    private String stockCode;
    private String stockGid;
    private String stockSpell;
    private String buyOrderId;
    private Date buyOrderTime;
    private BigDecimal buyOrderPrice;
    private String sellOrderId;
    private Date sellOrderTime;

    public void setId(Integer id) {
        this.id = id;
    }

    private BigDecimal sellOrderPrice;
    private BigDecimal profitTargetPrice;
    private BigDecimal stopTargetPrice;
    private String orderDirection;
    private Integer orderNum;
    private Integer orderLever;
    private BigDecimal orderTotalPrice;
    private BigDecimal orderFee;

    private BigDecimal sellOrderFee;

    private BigDecimal orderSpread;
    private BigDecimal orderStayFee;
    private Integer orderStayDays;
    private BigDecimal profitAndLose;
    private BigDecimal allProfitAndLose;
    private Integer isLock;
    private String lockMsg;
    private String stockPlate;
    /*点差费金额*/
    private BigDecimal spreadRatePrice;
    /*追加保证金额*/
    private BigDecimal marginAdd;

    public void setPositionType(Integer positionType) {
        this.positionType = positionType;
    }

    public void setPositionSn(String positionSn) {
        this.positionSn = positionSn;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public void setStockGid(String stockGid) {
        this.stockGid = stockGid;
    }

    public void setStockSpell(String stockSpell) {
        this.stockSpell = stockSpell;
    }

    public void setBuyOrderId(String buyOrderId) {
        this.buyOrderId = buyOrderId;
    }

    public void setBuyOrderTime(Date buyOrderTime) {
        this.buyOrderTime = buyOrderTime;
    }

    public void setBuyOrderPrice(BigDecimal buyOrderPrice) {
        this.buyOrderPrice = buyOrderPrice;
    }

    public void setSellOrderId(String sellOrderId) {
        this.sellOrderId = sellOrderId;
    }

    public void setSellOrderTime(Date sellOrderTime) {
        this.sellOrderTime = sellOrderTime;
    }

    public void setSellOrderPrice(BigDecimal sellOrderPrice) {
        this.sellOrderPrice = sellOrderPrice;
    }

    public void setProfitTargetPrice(BigDecimal profitTargetPrice) {
        this.profitTargetPrice = profitTargetPrice;
    }

    public void setStopTargetPrice(BigDecimal stopTargetPrice) {
        this.stopTargetPrice = stopTargetPrice;
    }

    public void setOrderDirection(String orderDirection) {
        this.orderDirection = orderDirection;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public void setOrderLever(Integer orderLever) {
        this.orderLever = orderLever;
    }

    public void setOrderTotalPrice(BigDecimal orderTotalPrice) {
        this.orderTotalPrice = orderTotalPrice;
    }

    public void setOrderFee(BigDecimal orderFee) {
        this.orderFee = orderFee;
    }

    public void setOrderSpread(BigDecimal orderSpread) {
        this.orderSpread = orderSpread;
    }

    public void setOrderStayFee(BigDecimal orderStayFee) {
        this.orderStayFee = orderStayFee;
    }

    public void setOrderStayDays(Integer orderStayDays) {
        this.orderStayDays = orderStayDays;
    }

    public void setProfitAndLose(BigDecimal profitAndLose) {
        this.profitAndLose = profitAndLose;
    }

    public void setAllProfitAndLose(BigDecimal allProfitAndLose) {
        this.allProfitAndLose = allProfitAndLose;
    }

    public void setIsLock(Integer isLock) {
        this.isLock = isLock;
    }

    public void setLockMsg(String lockMsg) {
        this.lockMsg = lockMsg;
    }

    public void setStockPlate(String stockPlate) {
        this.stockPlate = stockPlate;
    }

    public BigDecimal getSellOrderFee() {
        return sellOrderFee;
    }

    public void setSellOrderFee(BigDecimal sellOrderFee) {
        this.sellOrderFee = sellOrderFee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPosition that = (UserPosition) o;
        return Objects.equals(id, that.id) && Objects.equals(positionType, that.positionType) && Objects.equals(positionSn, that.positionSn) && Objects.equals(userId, that.userId) && Objects.equals(nickName, that.nickName) && Objects.equals(agentId, that.agentId) && Objects.equals(stockName, that.stockName) && Objects.equals(stockCode, that.stockCode) && Objects.equals(stockGid, that.stockGid) && Objects.equals(stockSpell, that.stockSpell) && Objects.equals(buyOrderId, that.buyOrderId) && Objects.equals(buyOrderTime, that.buyOrderTime) && Objects.equals(buyOrderPrice, that.buyOrderPrice) && Objects.equals(sellOrderId, that.sellOrderId) && Objects.equals(sellOrderTime, that.sellOrderTime) && Objects.equals(sellOrderPrice, that.sellOrderPrice) && Objects.equals(profitTargetPrice, that.profitTargetPrice) && Objects.equals(stopTargetPrice, that.stopTargetPrice) && Objects.equals(orderDirection, that.orderDirection) && Objects.equals(orderNum, that.orderNum) && Objects.equals(orderLever, that.orderLever) && Objects.equals(orderTotalPrice, that.orderTotalPrice) && Objects.equals(orderFee, that.orderFee) && Objects.equals(sellOrderFee, that.sellOrderFee) && Objects.equals(orderSpread, that.orderSpread) && Objects.equals(orderStayFee, that.orderStayFee) && Objects.equals(orderStayDays, that.orderStayDays) && Objects.equals(profitAndLose, that.profitAndLose) && Objects.equals(allProfitAndLose, that.allProfitAndLose) && Objects.equals(isLock, that.isLock) && Objects.equals(lockMsg, that.lockMsg) && Objects.equals(stockPlate, that.stockPlate) && Objects.equals(spreadRatePrice, that.spreadRatePrice) && Objects.equals(marginAdd, that.marginAdd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, positionType, positionSn, userId, nickName, agentId, stockName, stockCode, stockGid, stockSpell, buyOrderId, buyOrderTime, buyOrderPrice, sellOrderId, sellOrderTime, sellOrderPrice, profitTargetPrice, stopTargetPrice, orderDirection, orderNum, orderLever, orderTotalPrice, orderFee, sellOrderFee, orderSpread, orderStayFee, orderStayDays, profitAndLose, allProfitAndLose, isLock, lockMsg, stockPlate, spreadRatePrice, marginAdd);
    }

    public UserPosition() {
    }

    @ConstructorProperties({"id", "positionType", "positionSn", "userId", "nickName", "agentId", "stockName", "stockCode", "stockGid", "stockSpell", "buyOrderId", "buyOrderTime", "buyOrderPrice", "sellOrderId", "sellOrderTime", "sellOrderPrice", "profitTargetPrice", "stopTargetPrice", "orderDirection", "orderNum", "orderLever", "orderTotalPrice", "orderFee", "sellOrderFee", "orderSpread", "orderStayFee", "orderStayDays", "profitAndLose", "allProfitAndLose", "isLock", "lockMsg", "stockPlate", "spreadRatePrice", "marginAdd"})
    public UserPosition(Integer id, Integer positionType, String positionSn, Integer userId, String nickName, Integer agentId, String stockName, String stockCode, String stockGid, String stockSpell, String buyOrderId, Date buyOrderTime, BigDecimal buyOrderPrice, String sellOrderId, Date sellOrderTime, BigDecimal sellOrderPrice, BigDecimal profitTargetPrice, BigDecimal stopTargetPrice, String orderDirection, Integer orderNum, Integer orderLever, BigDecimal orderTotalPrice, BigDecimal orderFee, BigDecimal sellOrderFee, BigDecimal orderSpread, BigDecimal orderStayFee, Integer orderStayDays, BigDecimal profitAndLose, BigDecimal allProfitAndLose, Integer isLock, String lockMsg, String stockPlate, BigDecimal spreadRatePrice, BigDecimal marginAdd) {
        this.id = id;
        this.positionType = positionType;
        this.positionSn = positionSn;
        this.userId = userId;
        this.nickName = nickName;
        this.agentId = agentId;
        this.stockName = stockName;
        this.stockCode = stockCode;
        this.stockGid = stockGid;
        this.stockSpell = stockSpell;
        this.buyOrderId = buyOrderId;
        this.buyOrderTime = buyOrderTime;
        this.buyOrderPrice = buyOrderPrice;
        this.sellOrderId = sellOrderId;
        this.sellOrderTime = sellOrderTime;
        this.sellOrderPrice = sellOrderPrice;
        this.profitTargetPrice = profitTargetPrice;
        this.stopTargetPrice = stopTargetPrice;
        this.orderDirection = orderDirection;
        this.orderNum = orderNum;
        this.orderLever = orderLever;
        this.orderTotalPrice = orderTotalPrice;
        this.orderFee = orderFee;
        this.sellOrderFee = sellOrderFee;
        this.orderSpread = orderSpread;
        this.orderStayFee = orderStayFee;
        this.orderStayDays = orderStayDays;
        this.profitAndLose = profitAndLose;
        this.allProfitAndLose = allProfitAndLose;
        this.isLock = isLock;
        this.lockMsg = lockMsg;
        this.stockPlate = stockPlate;
        this.spreadRatePrice = spreadRatePrice;
        this.marginAdd = marginAdd;
    }


    public Integer getId() {
        return this.id;
    }

    public Integer getPositionType() {
        return this.positionType;
    }

    public String getPositionSn() {
        return this.positionSn;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public String getNickName() {
        return this.nickName;
    }

    public Integer getAgentId() {
        return this.agentId;
    }

    public String getStockName() {
        return this.stockName;
    }

    public String getStockCode() {
        return this.stockCode;
    }

    public String getStockGid() {
        return this.stockGid;
    }

    public String getStockSpell() {
        return this.stockSpell;
    }

    public String getBuyOrderId() {
        return this.buyOrderId;
    }

    public Date getBuyOrderTime() {
        return this.buyOrderTime;
    }

    public BigDecimal getBuyOrderPrice() {
        return this.buyOrderPrice;
    }

    public String getSellOrderId() {
        return this.sellOrderId;
    }

    public Date getSellOrderTime() {
        return this.sellOrderTime;
    }

    public BigDecimal getSellOrderPrice() {
        return this.sellOrderPrice;
    }

    public BigDecimal getProfitTargetPrice() {
        return this.profitTargetPrice;
    }

    public BigDecimal getStopTargetPrice() {
        return this.stopTargetPrice;
    }

    public String getOrderDirection() {
        return this.orderDirection;
    }

    public Integer getOrderNum() {
        return this.orderNum;
    }

    public Integer getOrderLever() {
        return this.orderLever;
    }

    public BigDecimal getOrderTotalPrice() {
        return this.orderTotalPrice;
    }

    public BigDecimal getOrderFee() {
        return this.orderFee;
    }

    public BigDecimal getOrderSpread() {
        return this.orderSpread;
    }

    public BigDecimal getOrderStayFee() {
        return this.orderStayFee;
    }

    public Integer getOrderStayDays() {
        return this.orderStayDays;
    }

    public BigDecimal getProfitAndLose() {
        return this.profitAndLose;
    }

    public BigDecimal getAllProfitAndLose() {
        return this.allProfitAndLose;
    }


    public Integer getIsLock() {
        return this.isLock;
    }

    public String getLockMsg() {
        return this.lockMsg;
    }

    public String getStockPlate() {
        return this.stockPlate;
    }

    public BigDecimal getSpreadRatePrice() {
        return spreadRatePrice;
    }

    public void setSpreadRatePrice(BigDecimal spreadRatePrice) {
        this.spreadRatePrice = spreadRatePrice;
    }

    public BigDecimal getMarginAdd() {
        return marginAdd;
    }

    public void setMarginAdd(BigDecimal marginAdd) {
        this.marginAdd = marginAdd;
    }
}

