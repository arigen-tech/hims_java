package com.hims.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PacsProperties {

    @Value("${pacs.weasis.connector.url}")
    private String weasisConnectorUrl;

    public String getWeasisConnectorUrl() {
        return weasisConnectorUrl;
    }
}
