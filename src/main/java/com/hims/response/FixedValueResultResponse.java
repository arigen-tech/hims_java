package com.hims.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FixedValueResultResponse {
    private Long fixedId;
    private String fixedValue;
}
