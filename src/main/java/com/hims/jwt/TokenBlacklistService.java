package com.hims.jwt;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklistService {

    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    // Add token to blacklist
    public void addToBlacklist(String token, long expirationTime) {
        blacklistedTokens.put(token, expirationTime);
    }

    // Check if token is blacklisted
    public boolean isBlacklisted(String token) {
        Long expiry = blacklistedTokens.get(token);
        if (expiry != null && expiry > System.currentTimeMillis()) {
            return true;
        }
        blacklistedTokens.remove(token); // Cleanup expired tokens
        return false;
    }
}

