package com.hims.response;

import com.hims.entity.MasBedStatus;
import com.hims.entity.MasBedType;
import com.hims.entity.MasRoom;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MasBedResponse {
    private Long bedId;
    private Long roomId;
    private String  roomName;
    private String bedNumber;
    private Long bedTypeId;
    private String bedTypeName;
    private Long bedStatusId;
    private String bedStatusName;
    private String status;
    private LocalDate lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;
}
