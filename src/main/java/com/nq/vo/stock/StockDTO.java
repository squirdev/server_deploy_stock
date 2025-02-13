package com.nq.vo.stock;

import com.alibaba.fastjson.JSONObject;
import com.nq.utils.DateTimeUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;

@Data
public class StockDTO {

    private Integer id;

    private String d;

    // 开盘价（元）
    private BigDecimal o;

    // 最高价（元）
    private BigDecimal h;
    // 最低价（元）
    private BigDecimal l;

    private BigDecimal c;

    // 	成交量（手）
    private Long v;
    private BigDecimal e;

    // 振幅（%）
    private BigDecimal zf;

    // 	换手（%）
    private BigDecimal hs;
    private BigDecimal zd;
    private BigDecimal zde;


    // 五分钟涨跌幅（%）
    private BigDecimal fm;


    // 量比（%）
    private BigDecimal lb;


    // 市盈率
    private BigDecimal pe;

    // 涨跌幅
    private BigDecimal pc;

    // 当前价格（元）
    private BigDecimal p;


    // 流通市值（元）
    private BigDecimal lt;


    // 总市值（元）
    private BigDecimal sz;
    private BigDecimal zsz;


    // 成交额（元）
    private BigDecimal cje;


    // 涨跌额（元）
    private BigDecimal ud;

    // 昨日收盘价（元）
    private BigDecimal yc;

    // 涨速（%）
    private BigDecimal zs;

    // 市净率
    private BigDecimal sjl;

    // 60日涨跌幅（%）
    private BigDecimal zdf60;


    // 年初至今涨跌幅（%）
    private BigDecimal zdfnc;

    // 更新时间
    private String t;


    // 代码
    private String dm;

    // 名称
    private String mc;

    // 交易所，"sh"表示上证，"sz"表示深证
    private String jys;

    // 连板数
    private Integer lbc;

    // 首次封板时间（HH:mm:ss）
    private String fbt;

    // 最后封板时间（HH:mm:ss）
    private String lbt;

    // 封板资金（元）
    private BigDecimal zj;

    // 	炸板次数
    private Integer zbc;

    // 涨停统计（x天/y板）
    private String tj;


    private JSONObject mmwp;


    private String dateFormat;

    public Long getTimeStamp() throws Exception {

        if (StringUtils.isEmpty(this.d)) {
            return null;
        }

        return DateTimeUtil.parse(this.d).getTime();

    }


    public BigDecimal getCustUd() {
        if (this.ud != null) {
            return this.ud;
        }
        if (this.zf == null || this.p == null) {
            return null;
        }

        return this.p.multiply(this.zf)
                .divide(this.zf.add(new BigDecimal(100)), RoundingMode.CEILING)
                .setScale(2, RoundingMode.HALF_UP);

    }


}
