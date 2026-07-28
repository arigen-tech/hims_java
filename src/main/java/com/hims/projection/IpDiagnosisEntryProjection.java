package com.hims.projection;

import java.time.LocalDateTime;

public interface IpDiagnosisEntryProjection {

    Long getInpatientId();

    Long getIcdId();

    String getIcdCode();

    String getIcdName();

    String getRemark();

    String getDiagnosisType();

    String getStatus();

    String getDiagnosis();

    LocalDateTime getDateTime();
}