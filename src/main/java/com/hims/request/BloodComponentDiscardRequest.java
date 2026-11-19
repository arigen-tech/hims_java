package com.hims.request;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodComponentDiscardRequest {

    private Long inventoryId;
    private Long discardReasonId;
    private String remarks;
}
