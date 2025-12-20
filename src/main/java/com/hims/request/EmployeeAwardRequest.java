package com.hims.request;


import com.hims.entity.MasEmployee;
import lombok.*;

import java.time.Instant;
@Data
@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class EmployeeAwardRequest {
    private String awardSummary;
}
