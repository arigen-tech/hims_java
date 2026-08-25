package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnverifiedReturnHeaderResponse {

    private Long returnMId;
    private String returnNo;
    private Long indentMId;
    private String indentNo;
    private LocalDateTime indentDate;
    private String indentBy;
    private  Long issueMIId;
    private String issueNo;
    private LocalDateTime issuedDate;
    private  String issuedBy;
    private Long receiveMId;
    private LocalDateTime receivedTime;
    private String receivedBy;
    private Long receivedDeptId;
    private String receivedDeptName;
    private Long issuedDeptId;
    private String issuedDeptName;
    private LocalDateTime returnDate;
    private String returnedBy;
}
