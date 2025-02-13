package com.nq.utils.stock.wangji.vo;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Map;

@Data
public class QueryParam {

    private Integer page;
    private Integer limit;

    @JSONField(name = "order_by")
    private String orderBy;

    @JSONField(name = "sort_field")
    private String sortField;

    private String type;


    public Map<String, Object> toParamsMap() {

        String jsonString = JSONObject.toJSONString(this);
        return JSONObject.parseObject(jsonString);

    }

}
