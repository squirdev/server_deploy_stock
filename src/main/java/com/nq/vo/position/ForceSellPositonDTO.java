package com.nq.vo.position;


import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class ForceSellPositonDTO {

    private Integer userId;
    private String realName;
    private String stockName;

    private Integer posId;

}
