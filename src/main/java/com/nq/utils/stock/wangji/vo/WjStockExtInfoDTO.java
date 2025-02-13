package com.nq.utils.stock.wangji.vo;


import com.nq.utils.BigDecimalUtil;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WjStockExtInfoDTO {

    private Integer stockId;

    private String name;            // 股票名称
    private String symbol;
    private BigDecimal price;       // 当前交易价格
    private BigDecimal open;        // 开盘价
    private BigDecimal preclose;    // 昨日收盘价
    private BigDecimal high;        // 最高价
    private BigDecimal low;         // 最低价
    private BigDecimal change;      // 价格变动
    private BigDecimal changeRate;  // 价格变动百分比
    private BigDecimal bid;         // 当前买入价
    private BigDecimal ask;         // 当前卖出价
    private long volume;            // 成交量
    private BigDecimal value;       // 成交金额
    private long bid1_vol;          // 买一数量
    private BigDecimal bid1;        // 买一报价
    private long bid2_vol;          // 买二数量
    private BigDecimal bid2;        // 买二报价
    private long bid3_vol;          // 买三数量
    private BigDecimal bid3;        // 买三报价
    private long bid4_vol;          // 买四数量
    private BigDecimal bid4;        // 买四报价
    private long bid5_vol;          // 买五数量
    private BigDecimal bid5;        // 买五报价
    private long ask1_vol;          // 卖一数量
    private BigDecimal ask1;        // 卖一报价
    private long ask2_vol;          // 卖二数量
    private BigDecimal ask2;        // 卖二报价
    private long ask3_vol;          // 卖三数量
    private BigDecimal ask3;        // 卖三报价
    private long ask4_vol;          // 卖四数量
    private BigDecimal ask4;        // 卖四报价
    private long ask5_vol;          // 卖五数量
    private BigDecimal ask5;        // 卖五报价
    private long update_time;       // 更新时间 (时间戳)



    public String getChangeRateStr() {
        if (this.getChangeRate() == null) {
            return null;
        }
        return BigDecimalUtil.formatWithNoTQ(this.getChangeRate());
    }

}
