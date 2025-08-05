package com.hims.response;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DgMasCollectionResponse {

    private Long collectionId;


    private String collectionCode;


    private String collectionName;

    private String status;


    private String lastChgBy;


    private LocalDateTime lastChgDate;


    private String lastChgTime;
}
