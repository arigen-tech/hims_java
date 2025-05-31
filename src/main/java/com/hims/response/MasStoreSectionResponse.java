package com.hims.response;

import com.hims.entity.MasItemType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class MasStoreSectionResponse {

    private Integer sectionId;
    private String sectionCode;
    private String sectionName;
    private LocalDate lastChgDate;
    private String lastChgTime;
    private String status;
    private String lastChgBy;
    private Integer hospitalId;
    private Integer masItemType;

}
