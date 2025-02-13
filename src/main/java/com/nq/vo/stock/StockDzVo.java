package com.nq.vo.stock;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
@Data
public class StockDzVo {

    private Integer id;

    private String stockName;

    private String stockCode;
    private BigDecimal price;

    private String stockType;

    private String stockGid;

    private String stockPlate;

    private Integer isLock;

    private Integer stockNum;

//    private String password;

    private Date startTime;

    private Date endTime;

}
