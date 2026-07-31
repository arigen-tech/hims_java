package com.hims.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreIssueMResponse {
    private Long StoreIssueMId;
    private String issueNo;
    private LocalDateTime issueDate;
    private Long indentMId;
    private String indentNo;
    private LocalDateTime indentDate;

}
