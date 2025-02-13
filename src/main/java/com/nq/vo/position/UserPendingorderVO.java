package com.nq.vo.position;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @TableName user_pendingorder
 */

@Data
public class UserPendingorderVO  {
    private Integer id;
    private String stockId;
    private String stockName;

    private Integer buyNum;

    private Integer buyType;

    private Integer lever;

    private BigDecimal profitTarget;

    private BigDecimal stopTarget;

    private BigDecimal nowPrice;

    private BigDecimal targetPrice;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;

    private Integer status;

    private static final long serialVersionUID = 1L;
}