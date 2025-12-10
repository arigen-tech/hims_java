package com.hims.request;

import com.hims.entity.MasBedStatus;
import com.hims.entity.MasBedType;
import com.hims.entity.MasRoom;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MasBedRequest {
    private String bedNumber;
    private Long roomId;
    private Long bedTypeId;
    private Long bedStatusId;





}
