package com.hims.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationTheatreResponse {

    private Long otId;

    private String otCode;

    private String otName;

    private String otType;

    private String location;

    private String status;

    private String lastChgBy;

    private LocalDateTime lastChgDate;
}