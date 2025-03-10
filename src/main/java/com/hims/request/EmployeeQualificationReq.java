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
public class EmployeeQualificationReq {

    private Long employeeQualificationId;
    private String institutionName;
    private Integer completionYear;
    private String qualificationName;
    private MultipartFile filePath;
}
