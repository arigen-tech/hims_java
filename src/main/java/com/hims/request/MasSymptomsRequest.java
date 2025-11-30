package com.hims.request;

import lombok.Data;

@Data
public class MasSymptomsRequest {
    private String symptomsCode;
    private String symptomsName;
    private String mostCommonUse;
}
