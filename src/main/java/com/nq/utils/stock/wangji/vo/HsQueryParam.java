package com.nq.utils.stock.wangji.vo;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Map;

@Data
public class HsQueryParam {

    private Integer page = 1;
    private Integer limit = 500;

    private String sort = "changeRate";

    private String asc = "0";

    private String market = "hs";



    public Map<String, Object> toParamsMap() {

        String jsonString = JSONObject.toJSONString(this);
        return JSONObject.parseObject(jsonString);

    }
}
