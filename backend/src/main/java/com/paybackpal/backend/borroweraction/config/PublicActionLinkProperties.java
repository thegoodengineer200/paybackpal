package com.paybackpal.backend.borroweraction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.public-actions")
public class PublicActionLinkProperties {
    private String baseUrl = "http://localhost:8080";
    private long remindMeLaterDelayHours = 24;
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getRemindMeLaterDelayHours() {
        return remindMeLaterDelayHours;
    }

    public void setRemindMeLaterDelayHours(long remindMeLaterDelayHours) {
        this.remindMeLaterDelayHours = remindMeLaterDelayHours;
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

    public Duration getRemindMeLaterDelay() {
        return Duration.ofHours(remindMeLaterDelayHours);
    }
}