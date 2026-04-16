package com.hims.response;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MasVisitTypeResponse {

    private Long visitTypeId;
    private String visitTypeCode;
    private String visitTypeName;
    private String description;
    private String status;
    private String lastChangedBy;
    private LocalDateTime lastChangedDate;

}