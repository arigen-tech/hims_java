package com.hims.jwt;

public class TokenWithExpiry {
    private String token;
    private long expiryTime;

    public TokenWithExpiry(String token, long expiryTime) {
        this.token = token;
        this.expiryTime = expiryTime;
    }

    public String getToken() {
        return token;
    }

    public long getExpiryTime() {
        return expiryTime;
    }
}
