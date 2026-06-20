package com.hims.m1.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;



@Component
@Slf4j
@RequiredArgsConstructor
public class HeaderInterceptor implements HandlerInterceptor {



    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }



        return true;
    }



}
