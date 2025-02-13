package com.nq.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 新股
 * @TableName stock_subscribe
 */
@Data
public class StockSubscribe implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer newlistId;

    /**
     * 新股名称
     */
    private String name;

    /**
     * 申购代码
     */
    private String code;
    /**
     * 类型 sh sz hk us
     */
    private String stockType;
    /**
     * 发行价格
     */
    private BigDecimal price;
    //市盈率
    private String pe;

    /**
     * 发行数量
     */
    private Long orderNumber;

    /**
     * 顯示状态
     */
    private Integer zt;
    /**
     * 鎖定状态
     */
    private Integer isLock;
    /**
     * 申购日期
     */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date subscribeTime;
    /**
     * 上市日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date listDate;
    /**
     * 中签日期
     */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date subscriptionTime;

    /**
     * 类型
     *1.新股申购 2.线下配售
     */
    private Integer type;

    /**
     * 是否上市
    */
    private static final long serialVersionUID = 1L;
}