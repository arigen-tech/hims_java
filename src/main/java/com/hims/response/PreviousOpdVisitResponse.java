package com.hims.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PreviousOpdVisitResponse {
   private LocalDate visitDate;
   private String doctorName;
   private String department;
   private String icdDiag;
   private String workingDiag;


}
