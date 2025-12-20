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
public class EmployeeSpecialtyInterestRequest {

    private String interestSummary;
}
