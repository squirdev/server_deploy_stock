package com.nq.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @TableName stock_dz
 */
@TableName(value ="stock_dz")
@Data
public class StockDz implements Serializable {
    @TableId(type = IdType.AUTO,value = "id")
    private Integer id;

    private String stockName;

    private String stockCode;

    private String stockType;

    private String stockGid;

    private String stockPlate;

    private Integer isLock;

    private Integer isShow;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date addTime;

    private BigDecimal spreadRate;

    private BigDecimal increaseRatio;

    private Integer stockNum;

    private String password;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    private BigDecimal discount;

    private static final long serialVersionUID = 1L;
}