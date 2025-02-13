package com.nq.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
* @Description:  新股申购
* @Param:
* @return:
* @Author: tf
* @Date: 2022/10/25
*/
@Data
public class UserStockSubscribe implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value="id",type= IdType.AUTO)
    private Integer id;
    /**
    *訂單編號
    */
    private String orderNo;
    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 代理id
     */
    private Integer agentId;

    /**
     * 代理姓名
     */
    private String agentName;

    /**
     * 申购股票代码
     */
    private String newCode;
    /**
     * 申购股票名称
     */
    private String newName;
    /**
     * 申购股票类型
     */
    private String newType;
    /**
     * 保证金
     *
     */
    private BigDecimal bond;
    /**
     * 发行价格
     *
     */
    private BigDecimal buyPrice;
    /**
     * 申购数量
     *
     */
    private Integer applyNums;
    /**
     * 中签数量
     *
     */

    private Integer applyNumber;
    private Integer type;
    /**
     * 申购状态
     *状态：1、待审核，2、未中签，3、已中签，4、已缴纳 5.已转持仓
     */
    private Integer status;
    /**
     * 添加时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date addTime;

    /**
     * 提交时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date submitTime;

    /**
     * 中签审核时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date endTime;
    /**
     * 转持仓时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date fixTime;
    /**
     * 备注
     */
    private String remarks;

    public UserStockSubscribe() {
    }

}
