package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShiftHandoverResponse {
    private Long noteId;
    private Long inpatientId;
    private LocalDateTime dateTime;
    private String notes;
    private String entered;
}
