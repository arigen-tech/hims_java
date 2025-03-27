package com.hims.request;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class BatchUpdateRequest {
    private List<ApplicationStatusUpdate> applicationStatusUpdates;

    private List<TemplateApplicationAssignment> templateApplicationAssignments;

    @Getter
    @Setter
    public static class ApplicationStatusUpdate {
        private String appId;
        private String status;
    }

    @Getter
    @Setter
    public static class TemplateApplicationAssignment {
        private Long templateId;
        private String appId;
        private Long lastChgBy;
        private Long orderNo;
    }
}