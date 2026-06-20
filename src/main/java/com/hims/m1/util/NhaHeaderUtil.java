package com.hims.m1.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

public class NhaHeaderUtil {


    private static final DateTimeFormatter TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .appendInstant(3)
            .toFormatter();

    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    public static String generateTimestamp() {
        return TIMESTAMP_FORMATTER.format(Instant.now());
    }



    public static Duration getApiTimeDuration() {
        return Duration.ofSeconds(30);
    }





    public static String decryptData(String encryptedData, byte[] aesKey) throws Exception {

        byte[] decoded = Base64.getDecoder().decode(encryptedData);

        byte[] iv = Arrays.copyOfRange(decoded, 0, 12);
        byte[] cipherText = Arrays.copyOfRange(decoded, 12, decoded.length);

        SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        return new String(cipher.doFinal(cipherText));
    }






    public static byte[] decryptAESKey(String encryptedKey) throws Exception {


        byte[] key = decryptAESKey_RSA(encryptedKey);
        if (key == null) {
            key = decryptAESKey_SHA256(encryptedKey);
        }
        if (key == null) {
            key = decryptAESKey_SHA1(encryptedKey);
        }

        return key;
    }
    public static byte[] decryptAESKey_RSA(String encryptedKey) throws Exception {

        try {
            byte[] encryptedKeyBytes = Base64.getDecoder().decode(encryptedKey.trim());

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(
                    Base64.getDecoder().decode("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDx6di5xoR44rCybaRQd9d65LW4qDIstNh4lGNhRfHGRi8FXcTs38CFJQ46jHmovMfyIv6f+QEVWKbew3IinQXr9Q+PpD1aSSoglvEdI9vDB0MRXmtbonhoQCkYDTJBDVb8nycKDbrsB+TozJ9K39dYDLrtT8MZD1pARYHroHopqlu2qtPOyJdfiiC61T/6NxfcSH6K80BsT+nhD6waEnZOIebW6EM93RSLUS2hAxypMbwi79GKiBqb9+heGpEjA0dknhetZst8hJTaSb+a+E5MJXElgSgDubYb2Ui5RaZ2UKMPKSJH1msFNInbJ6nxm/k30Gv5tzvsfDMJvdVzExiPAgMBAAECggEAUsNr88cArHD1/d14AVW24WMc7sOQgu4OP1aZF378BBcf4CigBmBYt9ShJRPJTZrklyk0zts8bSaq1HyucDwjegIZ5E1O9jQg1wx9Cjip53P1FJymgy+3HQeSZ5mgL1RWhdAXZZ+j6zuaK7FR8CZbwNJ+I1GOEGVJvcJp/AinstfdBw9wJTBBW6xSpHv8/GiN4EPIJElysLlYbypWks6s2jLxdgXsb+nx7M4dGk5zD1qY7QhnR8qlUgYDc3bDBsN+fJraI0153QS08elZJ5ISJhDFH4bJJYF4qxdAHFwpNIUBxcHiC+mU6dAgz+dR9Sn6THMQQlzV7kAKGsGE83NVUQKBgQD6e7MmGctk72UUJ/HvZ3VB4qV0mc9XfLD9yXv3lyLFCMz4M6RvkXQV/fuvhXz1lcF8XOoie6rw5a/Rmnhze0CC76bm6I57SN3R5of5804mt2GFhYt39gyqXeqYePkAzBPuokNpCxzCjkf+e3c4r/5z1N/nuM3XGZc2fw8fQIqTnwKBgQD3PdP3tXPdjf0BAouE8+dJvv/6sWUMMoWaFEqSlRZkAOn/quG+ipTCiFUlOl+zHAdHMYhKwgBbQEnF97wwJVZR7+ae6yfNAfvnMLk/gqVs2x2pIv63kY84mGXc++7vs0/MIwGqKxVjn42YGS2h5UDWyJG7HbRTv275x+/BZ3TVEQKBgQDgiRqiwOSu6lp1owkhTmCyWvVufKJ9Dm2qUn8qBvVIIwd6RvDUglLJlA+V29eUXlY/oRrRJoKpx3vALPRksMu3lBTNURLIOR1/F2Q1D4Gy+xgUlQ3cz1ezMNUnMOolaMtFU3eSJj/1t42Z9ht9WWaDdwPvuJ3sgCgo1eU8YqLWiQKBgDr9yJag991UJJraFZoKbBQfe4pAS2DmqRQDZWumIOqJgUucZTKGdEoaJ16zl+PvexkUi3Vy5ozfQPr4SjGepwLz363pY9y5bXZTHNA3hur7OQ+DJw17sJWLXzqj7fXJbe/CJoKItxjHGAzDbSzku1zEDGl+A+m7kmoMt3sJCUpRAoGAJbXszem0gJwCiSuwcvoip/zvRPLqM69J+5PbPP5Xj6iFvvAT0d5BfojD/wfE7en53k+OWV6oMiVIerQEdzHdY8BGVpNYSHSIi+Fm/4iKMkqIuUiV6iHMJawP6psSadZ0srcAvQzTpKnUV6cdKo7O4Eap5qEgSt4wjl4ax7fnd7s=".trim())
            );

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            return cipher.doFinal(encryptedKeyBytes);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] decryptAESKey_SHA256(String encryptedKey) throws Exception {
        try {
            byte[] encryptedKeyBytes = Base64.getDecoder().decode(encryptedKey.trim());

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(
                    Base64.getDecoder().decode("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDx6di5xoR44rCybaRQd9d65LW4qDIstNh4lGNhRfHGRi8FXcTs38CFJQ46jHmovMfyIv6f+QEVWKbew3IinQXr9Q+PpD1aSSoglvEdI9vDB0MRXmtbonhoQCkYDTJBDVb8nycKDbrsB+TozJ9K39dYDLrtT8MZD1pARYHroHopqlu2qtPOyJdfiiC61T/6NxfcSH6K80BsT+nhD6waEnZOIebW6EM93RSLUS2hAxypMbwi79GKiBqb9+heGpEjA0dknhetZst8hJTaSb+a+E5MJXElgSgDubYb2Ui5RaZ2UKMPKSJH1msFNInbJ6nxm/k30Gv5tzvsfDMJvdVzExiPAgMBAAECggEAUsNr88cArHD1/d14AVW24WMc7sOQgu4OP1aZF378BBcf4CigBmBYt9ShJRPJTZrklyk0zts8bSaq1HyucDwjegIZ5E1O9jQg1wx9Cjip53P1FJymgy+3HQeSZ5mgL1RWhdAXZZ+j6zuaK7FR8CZbwNJ+I1GOEGVJvcJp/AinstfdBw9wJTBBW6xSpHv8/GiN4EPIJElysLlYbypWks6s2jLxdgXsb+nx7M4dGk5zD1qY7QhnR8qlUgYDc3bDBsN+fJraI0153QS08elZJ5ISJhDFH4bJJYF4qxdAHFwpNIUBxcHiC+mU6dAgz+dR9Sn6THMQQlzV7kAKGsGE83NVUQKBgQD6e7MmGctk72UUJ/HvZ3VB4qV0mc9XfLD9yXv3lyLFCMz4M6RvkXQV/fuvhXz1lcF8XOoie6rw5a/Rmnhze0CC76bm6I57SN3R5of5804mt2GFhYt39gyqXeqYePkAzBPuokNpCxzCjkf+e3c4r/5z1N/nuM3XGZc2fw8fQIqTnwKBgQD3PdP3tXPdjf0BAouE8+dJvv/6sWUMMoWaFEqSlRZkAOn/quG+ipTCiFUlOl+zHAdHMYhKwgBbQEnF97wwJVZR7+ae6yfNAfvnMLk/gqVs2x2pIv63kY84mGXc++7vs0/MIwGqKxVjn42YGS2h5UDWyJG7HbRTv275x+/BZ3TVEQKBgQDgiRqiwOSu6lp1owkhTmCyWvVufKJ9Dm2qUn8qBvVIIwd6RvDUglLJlA+V29eUXlY/oRrRJoKpx3vALPRksMu3lBTNURLIOR1/F2Q1D4Gy+xgUlQ3cz1ezMNUnMOolaMtFU3eSJj/1t42Z9ht9WWaDdwPvuJ3sgCgo1eU8YqLWiQKBgDr9yJag991UJJraFZoKbBQfe4pAS2DmqRQDZWumIOqJgUucZTKGdEoaJ16zl+PvexkUi3Vy5ozfQPr4SjGepwLz363pY9y5bXZTHNA3hur7OQ+DJw17sJWLXzqj7fXJbe/CJoKItxjHGAzDbSzku1zEDGl+A+m7kmoMt3sJCUpRAoGAJbXszem0gJwCiSuwcvoip/zvRPLqM69J+5PbPP5Xj6iFvvAT0d5BfojD/wfE7en53k+OWV6oMiVIerQEdzHdY8BGVpNYSHSIi+Fm/4iKMkqIuUiV6iHMJawP6psSadZ0srcAvQzTpKnUV6cdKo7O4Eap5qEgSt4wjl4ax7fnd7s=".trim())
            );

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");

            OAEPParameterSpec spec = new OAEPParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    PSource.PSpecified.DEFAULT
            );

            cipher.init(Cipher.DECRYPT_MODE, privateKey, spec);
            return cipher.doFinal(encryptedKeyBytes);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] decryptAESKey_SHA1(String encryptedKey) throws Exception {
        try {
            byte[] encryptedKeyBytes = Base64.getDecoder().decode(encryptedKey.trim());

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(
                    Base64.getDecoder().decode("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDx6di5xoR44rCybaRQd9d65LW4qDIstNh4lGNhRfHGRi8FXcTs38CFJQ46jHmovMfyIv6f+QEVWKbew3IinQXr9Q+PpD1aSSoglvEdI9vDB0MRXmtbonhoQCkYDTJBDVb8nycKDbrsB+TozJ9K39dYDLrtT8MZD1pARYHroHopqlu2qtPOyJdfiiC61T/6NxfcSH6K80BsT+nhD6waEnZOIebW6EM93RSLUS2hAxypMbwi79GKiBqb9+heGpEjA0dknhetZst8hJTaSb+a+E5MJXElgSgDubYb2Ui5RaZ2UKMPKSJH1msFNInbJ6nxm/k30Gv5tzvsfDMJvdVzExiPAgMBAAECggEAUsNr88cArHD1/d14AVW24WMc7sOQgu4OP1aZF378BBcf4CigBmBYt9ShJRPJTZrklyk0zts8bSaq1HyucDwjegIZ5E1O9jQg1wx9Cjip53P1FJymgy+3HQeSZ5mgL1RWhdAXZZ+j6zuaK7FR8CZbwNJ+I1GOEGVJvcJp/AinstfdBw9wJTBBW6xSpHv8/GiN4EPIJElysLlYbypWks6s2jLxdgXsb+nx7M4dGk5zD1qY7QhnR8qlUgYDc3bDBsN+fJraI0153QS08elZJ5ISJhDFH4bJJYF4qxdAHFwpNIUBxcHiC+mU6dAgz+dR9Sn6THMQQlzV7kAKGsGE83NVUQKBgQD6e7MmGctk72UUJ/HvZ3VB4qV0mc9XfLD9yXv3lyLFCMz4M6RvkXQV/fuvhXz1lcF8XOoie6rw5a/Rmnhze0CC76bm6I57SN3R5of5804mt2GFhYt39gyqXeqYePkAzBPuokNpCxzCjkf+e3c4r/5z1N/nuM3XGZc2fw8fQIqTnwKBgQD3PdP3tXPdjf0BAouE8+dJvv/6sWUMMoWaFEqSlRZkAOn/quG+ipTCiFUlOl+zHAdHMYhKwgBbQEnF97wwJVZR7+ae6yfNAfvnMLk/gqVs2x2pIv63kY84mGXc++7vs0/MIwGqKxVjn42YGS2h5UDWyJG7HbRTv275x+/BZ3TVEQKBgQDgiRqiwOSu6lp1owkhTmCyWvVufKJ9Dm2qUn8qBvVIIwd6RvDUglLJlA+V29eUXlY/oRrRJoKpx3vALPRksMu3lBTNURLIOR1/F2Q1D4Gy+xgUlQ3cz1ezMNUnMOolaMtFU3eSJj/1t42Z9ht9WWaDdwPvuJ3sgCgo1eU8YqLWiQKBgDr9yJag991UJJraFZoKbBQfe4pAS2DmqRQDZWumIOqJgUucZTKGdEoaJ16zl+PvexkUi3Vy5ozfQPr4SjGepwLz363pY9y5bXZTHNA3hur7OQ+DJw17sJWLXzqj7fXJbe/CJoKItxjHGAzDbSzku1zEDGl+A+m7kmoMt3sJCUpRAoGAJbXszem0gJwCiSuwcvoip/zvRPLqM69J+5PbPP5Xj6iFvvAT0d5BfojD/wfE7en53k+OWV6oMiVIerQEdzHdY8BGVpNYSHSIi+Fm/4iKMkqIuUiV6iHMJawP6psSadZ0srcAvQzTpKnUV6cdKo7O4Eap5qEgSt4wjl4ax7fnd7s=".trim())
            );

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");

            OAEPParameterSpec spec = new OAEPParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA1,
                    PSource.PSpecified.DEFAULT
            );

            cipher.init(Cipher.DECRYPT_MODE, privateKey, spec);
            return cipher.doFinal(encryptedKeyBytes);
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, String> generateKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);

            KeyPair pair = keyGen.generateKeyPair();

            String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());

            Map<String, String> keys = new HashMap<>();
            keys.put("publicKey", publicKey);
            keys.put("privateKey", privateKey);

            System.out.println("publicKey: "+publicKey);
            System.out.println("privateKey: "+privateKey);

            return keys;

        } catch (Exception e) {
            throw new RuntimeException("Key generation failed", e);
        }
    }


//    byte[] aesKey = helperUtill.decryptAESKey(req.getKey());
//
//    String username = helperUtill.decryptData(req.getUsername(), aesKey);
//    String userPassword = helperUtill.decryptData(req.getPassword(), aesKey);


}
