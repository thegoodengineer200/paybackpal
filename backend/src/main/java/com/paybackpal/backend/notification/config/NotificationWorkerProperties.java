package com.paybackpal.backend.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.notifications")
public class NotificationWorkerProperties {

    private boolean schedulerEnabled = true;
    private int batchSize = 20;
    private long pollIntervalMs = 10_000;
    private int maxRetryCount = 3;
    private long retryDelaySeconds = 300;

    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    public void setSchedulerEnabled(boolean schedulerEnabled) {
        this.schedulerEnabled = schedulerEnabled;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getPollIntervalMs() {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public long getRetryDelaySeconds() {
        return retryDelaySeconds;
    }

    public void setRetryDelaySeconds(long retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
    }

    public Duration getRetryDelay() {
        return Duration.ofSeconds(retryDelaySeconds);
    }

    public int getSafeBatchSize() {
        if (batchSize <= 0) {
            return 20;
        }

        return batchSize;
    }

    public int getSafeMaxRetryCount() {
        if (maxRetryCount < 0) {
            return 3;
        }

        return maxRetryCount;
    }
}