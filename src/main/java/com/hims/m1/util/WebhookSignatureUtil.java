package com.hims.m1.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Utility for webhook signature verification.
 * Implements HMAC-SHA256 signature verification with constant-time comparison.
 */
@Component
public class WebhookSignatureUtil {

    private static final Logger logger = LoggerFactory.getLogger(WebhookSignatureUtil.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Verifies webhook signature using HMAC-SHA256.
     * 
     * @param payload The webhook payload (raw JSON string)
     * @param signature The signature provided in header
     * @param secret The shared secret key
     * @return true if signature is valid, false otherwise
     */
    public boolean verifyHmacSha256(String payload, String signature, String secret) {
        if (payload == null || signature == null || secret == null) {
            logger.warn("Webhook verification failed: null payload, signature, or secret");
            return false;
        }

        try {
            // Compute HMAC-SHA256 signature
            String computedSignature = computeHmacSha256(payload, secret);
            
            // Constant-time comparison to prevent timing attacks
            boolean isValid = constantTimeEquals(computedSignature, signature);
            
            if (!isValid) {
                logger.warn("Webhook signature verification failed");
                logger.debug("Expected: {}, Got: {}", computedSignature.substring(0, Math.min(10, computedSignature.length())), 
                        signature.substring(0, Math.min(10, signature.length())));
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Error verifying webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Computes HMAC-SHA256 signature for given payload and secret.
     * 
     * @param payload The payload to sign
     * @param secret The secret key
     * @return Hex-encoded signature
     */
    public String computeHmacSha256(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), 
                HMAC_SHA256
        );
        mac.init(secretKeySpec);
        
        byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        
        // Convert to hex string
        return HexFormat.of().formatHex(hmacBytes);
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     * Compares two strings in a way that takes the same amount of time
     * regardless of where the first difference occurs.
     * 
     * @param a First string
     * @param b Second string
     * @return true if strings are equal, false otherwise
     */
    public boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }

        // If lengths differ, still compare to prevent timing leak
        int lengthA = a.length();
        int lengthB = b.length();
        int maxLength = Math.max(lengthA, lengthB);

        // Compare bytes
        int result = lengthA ^ lengthB; // XOR lengths

        for (int i = 0; i < maxLength; i++) {
            // Use modulo to prevent index out of bounds while maintaining constant time
            char charA = i < lengthA ? a.charAt(i) : 0;
            char charB = i < lengthB ? b.charAt(i) : 0;
            result |= charA ^ charB;
        }

        return result == 0;
    }

    /**
     * Verifies webhook signature with SHA256 hash (alternative method).
     * Some systems use SHA256 hash instead of HMAC.
     * 
     * @param payload The webhook payload
     * @param signature The signature provided
     * @return true if valid
     */
    public boolean verifySha256(String payload, String signature) {
        if (payload == null || signature == null) {
            return false;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            String computedHash = HexFormat.of().formatHex(hash);
            
            return constantTimeEquals(computedHash, signature);
            
        } catch (Exception e) {
            logger.error("Error verifying SHA256 signature: {}", e.getMessage());
            return false;
        }
    }
}

