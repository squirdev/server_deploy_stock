package com.nq.utils.stock.wangji.vo;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class WjBaseRet<T> {

    private Integer code;
    private String message;

    private T data;



    public boolean isSuccess() {
        return StringUtils.equalsIgnoreCase(message, "OK");
    }
}
