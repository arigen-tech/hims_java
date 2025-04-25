package com.hims.request;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
public class DgMasSampleRequest {
    private String sampleCode;
    private String sampleDescription;
    private String status;

}
