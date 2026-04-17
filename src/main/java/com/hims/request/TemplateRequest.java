package com.hims.request;

import lombok.Data;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Data
public class TemplateRequest {
    private String templateType;
    private Long procedure;//procedure and surgery
    private String templateName;
    private List<TemplateItemRequest> templateItemRequests;


}