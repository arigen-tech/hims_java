package com.hims.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeasisLaunchResponse {

    private String studyInstanceUid;
    private String weasisUrl;
}
