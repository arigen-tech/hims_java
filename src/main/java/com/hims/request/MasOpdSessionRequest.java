package com.hims.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Data
@Getter
@Setter
public class MasOpdSessionRequest {

    private String sessionName;

//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")

    private String fromTime;

//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")

    private String endTime;


    private String status;


    private String lasChgBy;
}