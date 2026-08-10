package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BedDetailsByWardResponse {
    private Long bedId;
    private String bedNo;
}
