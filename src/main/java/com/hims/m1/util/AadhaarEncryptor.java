package com.hims.m1.util;


import com.hims.m1.abdm_response.AbdmCertificateResponse;
import com.hims.m1.abdm_response.AbdmSessionApiResponse;
import com.hims.m1.client.AbdmProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class AadhaarEncryptor {

    @Autowired
    private AbdmProperties abdmProperties;


    private final WebClient webClient;

    public AadhaarEncryptor(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }


    public PublicKey parsePublicKey(String public_key) throws Exception {
        String keyContent = public_key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s", ""); // Remove all whitespace including newlines

        byte[] decoded = Base64.getDecoder().decode(keyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }


    public String doEncrypt(String plainText, AbdmCertificateResponse cer_response) {
        try {
            Cipher cipher = Cipher.getInstance(cer_response.getEncryptionAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, parsePublicKey(cer_response.getPublicKey()));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Aadhaar encryption failed", e);
        }
    }


    @SuppressWarnings("null")
    public AbdmCertificateResponse fetchCertificates() {
        String url = abdmProperties.getUrls().getAbhaCertificate();
        AbdmCertificateResponse responseString = null;

        try {

            String urlTOken = abdmProperties.getUrls().getGatewaySession();
            AbdmSessionApiResponse sessionToken = null;
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("clientId", abdmProperties.getClient().getId());
            requestBody.put("clientSecret", abdmProperties.getClient().getSecret());
            requestBody.put("grantType", "client_credentials");
            sessionToken = webClient.post()
                    .uri(urlTOken)
                    .header("Content-Type", "application/json")
                    .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                    .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                    .header("X-CM-ID", "sbx") // unchanged as requested
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(AbdmSessionApiResponse.class)
                    .block();

            responseString = webClient.get()
                    .uri(url)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("REQUEST-ID", NhaHeaderUtil.generateRequestId())
                    .header("TIMESTAMP", NhaHeaderUtil.generateTimestamp())
                    .header("Authorization", "Bearer " + sessionToken.getAccessToken())
                    .retrieve()
                    .bodyToMono(AbdmCertificateResponse.class)
                    .block();

            responseString.setSession(sessionToken);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseString;

    }


}
