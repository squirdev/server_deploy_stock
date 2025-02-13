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
 * @TableName convert_bond
 */
@TableName(value ="convert_bond")
@Data
public class ConvertBond implements Serializable {
    @TableId(type = IdType.AUTO,value = "id")
    private Integer id;

    private String bondBuyCode;

    private String bondName;

    private String bondType;

    private String bondCode;

    private String stockCode;

    private BigDecimal price;

    private Date applyDate;

    private Date pubDate;

    private Date listDate;

    private Integer surplus;

    private Integer applyLimit;

    private Integer status;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}