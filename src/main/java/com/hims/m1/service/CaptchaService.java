package com.hims.m1.service;

import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private final ConcurrentHashMap<String, Integer> captchaStore = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateCaptcha() {
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;

        int result = a + b;

        String captchaId = String.valueOf(System.currentTimeMillis());
        captchaStore.put(captchaId, result);

        return String.format("%s: %d + %d = ?", captchaId, a, b);
    }

    public boolean verifyCaptcha(String captchaId, int userAnswer) {
        Integer correctAnswer = captchaStore.get(captchaId);

        if (correctAnswer != null && correctAnswer == userAnswer) {
            captchaStore.remove(captchaId);
            return true;
        }
        return false;
    }
}
