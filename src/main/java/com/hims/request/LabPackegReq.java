package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class LabPackegReq {
    private Long packegId;
    private LocalDate appointmentDate;
}
