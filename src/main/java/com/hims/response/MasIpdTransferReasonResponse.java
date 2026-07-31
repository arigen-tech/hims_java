package com.hims.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasIpdTransferReasonResponse {

    private Long transferReasonId;

    private String transferReasonName;

    private String description;

    private String status;

}