package com.hims.request;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class DgMasCollectionRequest {

    private String collectionCode;
    private String collectionName;
}
