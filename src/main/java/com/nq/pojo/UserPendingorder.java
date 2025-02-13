package com.nq.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * @TableName user_pendingorder
 */
@TableName(value ="user_pendingorder")
@Data
public class UserPendingorder implements Serializable {
    @TableId(type = IdType.AUTO,value = "id")
    private Integer id;

    private Integer userId;

    private String stockId;

    private Integer buyNum;

    private Integer buyType;

    private Integer lever;

    private BigDecimal profitTarget;

    private BigDecimal stopTarget;

    private BigDecimal nowPrice;

    private BigDecimal targetPrice;

    private Date addTime;

    private Integer status;

    private static final long serialVersionUID = 1L;
}