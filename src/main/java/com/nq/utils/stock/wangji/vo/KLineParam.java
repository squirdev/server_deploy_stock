package com.nq.utils.stock.wangji.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.Map;

@Data
public class KLineParam {

    private String symbol;
    private String type = "5";
    private Long limit = 500L;



    public Map<String, Object> toParamsMap() {

        String jsonString = JSONObject.toJSONString(this);
        return JSONObject.parseObject(jsonString);

    }
}
