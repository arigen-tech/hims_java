package com.hims.response;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter

public class DgMasSampleResponse {
    private Long id;
    private String sampleCode;
    private String sampleDescription;
    private String status;

}
