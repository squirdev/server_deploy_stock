package com.nq.vo.user;


import com.nq.vo.position.ForceSellPositonDTO;
import lombok.Data;

import java.util.List;

@Data
public class AlarmDTO {

    private Integer adminId;
    private String type;
    private Integer bizId;

    private List<ForceSellPositonDTO> forceSellList;
}
