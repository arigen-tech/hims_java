package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BedResponse {
    private Long bedId;
    private String bedName;
}
