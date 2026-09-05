package com.example.ticketsystem.queue.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ticket.queue")
public class QueueProperties {

    private int admissionBatchSize = 50;
    private Duration activeTokenTtl = Duration.ofMinutes(5);
    private boolean schedulerEnabled = true;

    public int getAdmissionBatchSize() {
        return admissionBatchSize;
    }

    public void setAdmissionBatchSize(int admissionBatchSize) {
        this.admissionBatchSize = admissionBatchSize;
    }

    public Duration getActiveTokenTtl() {
        return activeTokenTtl;
    }

    public void setActiveTokenTtl(Duration activeTokenTtl) {
        this.activeTokenTtl = activeTokenTtl;
    }

    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    public void setSchedulerEnabled(boolean schedulerEnabled) {
        this.schedulerEnabled = schedulerEnabled;
    }
}
