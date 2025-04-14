package com.hims.request;

import lombok.*;

import java.time.Instant;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasUserTypeRequest {
    private String userTypeName;
    private Long lastChgBy;

    private String status;
    private String hospitalStaff;
    private Long mapId;
}
