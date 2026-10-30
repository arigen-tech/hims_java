package com.hims.m2.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hims.m2.request.CareContextLinkRequest;
import com.hims.m2.response.CareContextLinkPending;

public interface CareContextLinkService {

    String initiate(CareContextLinkRequest request);

    void onGenerateToken(String requestId, JsonNode payload);

    void onAddContexts(String requestId, JsonNode payload);

    CareContextLinkPending status(String requestId);
}
