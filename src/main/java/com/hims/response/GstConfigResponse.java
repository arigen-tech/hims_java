package com.hims.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class GstConfigResponse {

    private Boolean gstApplicable;
    private Double gstPercent;
}
