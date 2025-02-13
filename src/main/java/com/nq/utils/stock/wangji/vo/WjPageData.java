package com.nq.utils.stock.wangji.vo;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class WjPageData {

    private List<JSONArray> candle;

    private Long count;
    private Integer limit;
    private Integer page;
    private String type;

    private List<String> fields;



    public List<JSONObject> getDataList() {

        if (CollectionUtils.isEmpty(candle)) {
            return null;
        }

        return candle.stream().map(ja -> {
            JSONObject jo = new JSONObject();
            for (int i = 0; i < fields.size(); i++) {
                jo.put(fields.get(i), ja.get(i));
            }
            return jo;
        }).collect(Collectors.toList());

    }



    public boolean isLast() {
        return count <= limit * page;
    }


}
