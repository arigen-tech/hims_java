package com.hims.m1.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "ABDM_API_LOG")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Abdm_ApiLog implements Serializable {

    @Id
    @Column(name = "ABDM_API_LOG_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "abdm_api_log_seq_gen")
    @SequenceGenerator(
            name = "abdm_api_log_seq_gen",
            sequenceName = "ABDM_API_LOG_SEQ",
            allocationSize = 1
    )
    private Long abdmApiLogId;

    @Column(name = "SERVER_API_END_POINT")
    private String serverApiEndPoint;

    @Column(name = "LOCAL_API_END_POINT")
    private String localApiEndPoint;

    @Column(name = "STATUS_CODE")
    private String statusCode;

    @JsonIgnore
    @Column(name = "REQUEST_BODY")
    private String requestBody;

    @Column(name = "RESPONSE_BODY")
    private String responseBody;

    @JsonIgnore
    @Column(name = "HOSPITAL_ID")
    private Long hospitalId;

    @JsonIgnore
    @Column(name = "CREATED_BY")
    private Long createdBy;

    @JsonIgnore
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate = LocalDateTime.now();

    @JsonIgnore
    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @Column(name = "IS_ACTIVE")
    private Long isActive = 1L;

}
