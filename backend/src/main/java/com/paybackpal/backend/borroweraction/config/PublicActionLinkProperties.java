package com.paybackpal.backend.borroweraction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.public-actions")
public class PublicActionLinkProperties {
    private String baseUrl = "http://localhost:8080";
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getNormalizedBaseUrl() {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:8080";
        }

        String trimmedBaseUrl = baseUrl.trim();

        if (trimmedBaseUrl.endsWith("/")) {
            return trimmedBaseUrl.substring(0, trimmedBaseUrl.length() - 1);
        }

        return trimmedBaseUrl;
    }
}