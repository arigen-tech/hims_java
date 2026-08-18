package com.hims.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationTheatreRequest {

    private String otCode;

    private String otName;

    private String otType;

    private String location;
}