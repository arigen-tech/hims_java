package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class IpdWardResponse {
    private Long wardId;
    private String wardName;

}
