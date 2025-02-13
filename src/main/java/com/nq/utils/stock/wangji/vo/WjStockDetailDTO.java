package com.nq.utils.stock.wangji.vo;

import com.nq.utils.DateTimeUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

@Data
public class WjStockDetailDTO {

    private String day;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume;


    public Long getTimeStamp() throws Exception {

        if (StringUtils.isEmpty(this.day)) {
            return null;
        }

        return DateTimeUtil.parse(this.day).getTime();

    }
}
