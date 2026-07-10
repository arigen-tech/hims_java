package com.hims.response;

import lombok.*;

//@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MasIcdResponse {
    private Long icdId;
    private String icdCode;
    private String icdName;
//    private String status;
}
