package com.hims.m1.service;

import com.hims.m1.entity.RequestLog;
import com.hims.m1.repository.RequestLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequestLogService {
    @Autowired
    private RequestLogRepository requestLogRepository;

    public RequestLog save(RequestLog log) {
//        return requestLogRepository.save(log);
        return null;
    }
}
