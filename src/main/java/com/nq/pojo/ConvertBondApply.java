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
 * @TableName convert_bond_apply
 */
@TableName(value ="convert_bond_apply")
@Data
public class ConvertBondApply implements Serializable {
    @TableId(type = IdType.AUTO,value = "id")
    private Integer id;

    private Integer agentId;

    private Integer userId;

    private String phone;

    private Integer bondId;

    private BigDecimal applyMoney;

    private Integer applyNum;

    private Integer sucNum;

    private BigDecimal sucMony;

    private Date applyDate;

    private Integer status;

    private BigDecimal refundMony;

    private Date sucDate;

    private static final long serialVersionUID = 1L;
}