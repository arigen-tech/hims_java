package com.hims.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class EmployeeDocumentReq {
    private Long employeeDocumentId;
    private String documentName;
    private MultipartFile filePath;
}
