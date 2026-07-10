package com.hims.m1.util;


import com.hims.m1.response.ParseErrorResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Iterator;
import java.util.Map;


public class ErrorHandel {

    public static ParseErrorResponse parseError(WebClientResponseException ex) {

        ParseErrorResponse parseErrorResponse = new ParseErrorResponse();
        String errorCode = "";
        String errorMessage = "";
        try {
            JsonNode root = new ObjectMapper().readTree(ex.getResponseBodyAsString());
            if (root.isArray() && root.size() > 0) {
                JsonNode firstNode = root.get(0);
                errorCode = resolveCode(firstNode, errorCode);
                errorMessage = resolveMessage(firstNode, errorCode);

            } else if (root.has("error")) {
                JsonNode errorNode = root.path("error");
                errorCode = resolveCode(errorNode, errorCode);
                errorMessage = resolveMessage(errorNode, errorCode);

            } else if (root.has("message") || root.has("code") || root.has("description")) {
                errorCode = resolveCode(root, errorCode);
                errorMessage = resolveMessage(root, errorCode);
            } else if (root.has("loginId")) {
                errorMessage = root.toString();
            } else if (root.has("otpValue")) {
                errorMessage = "Entered OTP is invalid. Please enter a valid OTP.";
            } else {
                Iterator<Map.Entry<String, JsonNode>> fields = root.fields();

                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String key = entry.getKey();
                    if ("timestamp".equalsIgnoreCase(key)) {
                        continue;
                    }
                    errorMessage = entry.getValue().asText();
                    break;
                }
            }
        } catch (Exception parseEx) {
            errorMessage = ex.getResponseBodyAsString();
        }
        parseErrorResponse.setErrorMsg(errorMessage);
        parseErrorResponse.setErrorCode(errorCode);

        return parseErrorResponse;
    }

    private static String resolveCode(JsonNode node, String fallbackCode) {
        String nestedCode = firstNestedCode(node);
        if (!nestedCode.isBlank()) {
            return nestedCode;
        }

        if (node != null && node.has("code")) {
            return node.path("code").asText(fallbackCode);
        }
        return fallbackCode;
    }

    private static String resolveMessage(JsonNode node, String errorCode) {
        if ("ABDM-1204".equalsIgnoreCase(errorCode)) {
            return "Entered OTP is invalid. Please enter a valid OTP.";
        }

        String nestedMessage = firstNestedMessage(node);
        if (!nestedMessage.isBlank()) {
            return nestedMessage;
        }

        String message = textValue(node, "message");
        String description = textValue(node, "description");

        if (!message.isBlank() && !description.isBlank()) {
            if (message.equalsIgnoreCase(description)) {
                return message;
            }
            return message + " " + description;
        }

        if (!description.isBlank()) {
            return description;
        }

        return message;
    }

    private static String firstNestedMessage(JsonNode node) {
        return firstNestedField(node, "message");
    }

    private static String firstNestedCode(JsonNode node) {
        return firstNestedField(node, "code");
    }

    private static String firstNestedField(JsonNode node, String fieldName) {
        if (node == null) {
            return "";
        }

        String[] containerFields = {"details", "errorStatus"};
        for (String containerField : containerFields) {
            JsonNode detailsNode = node.path(containerField);
            if (!detailsNode.isArray() || detailsNode.isEmpty()) {
                continue;
            }

            JsonNode firstDetail = detailsNode.get(0);
            String value = textValue(firstDetail, fieldName);
            if (!value.isBlank()) {
                return value;
            }
        }

        return "";
    }

    private static String textValue(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName) || node.path(fieldName).isNull()) {
            return "";
        }
        return node.path(fieldName).asText("").trim();
    }

}
