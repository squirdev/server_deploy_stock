package com.nq.utils.stock.wangji.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WjStockInfoDTO {

    private String type; // 类型
    private String prodCode; // 产品代码
    private String prodShortcode; // 代码简写
    private String prodName; // 股票名称
    private BigDecimal lastPx; // 最新价格
    private BigDecimal highPx; // 最高价格
    private BigDecimal lowPx; // 最低价格
    private BigDecimal openPx; // 今日开盘价
    private BigDecimal preclosePx; // 昨日收盘价
    private BigDecimal pxChangeRate; // 涨跌率（需乘以100%）
    private BigDecimal pxChange; // 涨跌额
    private BigDecimal avgPx; // 均价
    private Long turnoverVolume; // 成交量
    private BigDecimal turnoverValue; // 成交额
    private BigDecimal turnoverRatio; // 换手率
    private BigDecimal volumeRatio; // 量比
    private BigDecimal highLimit; // 涨停价格
    private BigDecimal lowLimit; // 跌停价格
    private Long businessAmountOut; // 外盘成交量
    private Long businessAmountIn; // 内盘成交量
    private BigDecimal committee; // 委比（需乘以100%）
    private BigDecimal amplitude; // 振幅（需乘以100%）
    private BigDecimal yeargrowRatio; // 今年以来涨幅（需乘以100%）
    private BigDecimal pe; // 市盈率（需乘以100%）
    private BigDecimal dynPe; // 动态市盈率（需乘以100%）
    private BigDecimal peRate; // TTM市盈率（需乘以100%）
    private String bps; // 每股净资产
    private BigDecimal pbRate; // 市净率（需乘以100%）
    private BigDecimal psRate; // 市销率（需乘以100%）
    private String roeRate; // 净资产收益率（需乘以100%）
    private Long totalShares; // 总股本
    private Long circulationShares; // 流通股本
    private BigDecimal marketValue; // 总市值
    private BigDecimal circulationValue; // 流通市值

}
