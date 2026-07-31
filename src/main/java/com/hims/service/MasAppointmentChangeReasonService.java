package com.hims.service;


import com.hims.entity.MasAppointmentChangeReason;
import com.hims.response.ApiResponse;
import com.hims.response.MasAppointmentChangeReasonResponse;

import java.util.List;

public interface MasAppointmentChangeReasonService {

    ApiResponse<List<MasAppointmentChangeReasonResponse>> getAllReasons(int flag);

}