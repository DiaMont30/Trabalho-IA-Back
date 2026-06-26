package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.response.HealthResponse;
import com.plataforma.conversacional.health.ApplicationHealthIndicator;
import com.plataforma.conversacional.health.DatabaseHealthIndicator;
import com.plataforma.conversacional.service.HealthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class HealthServiceImpl implements HealthService {

    private final ApplicationHealthIndicator appIndicator;
    private final DatabaseHealthIndicator dbIndicator;
    private final String appVersion;

    public HealthServiceImpl(ApplicationHealthIndicator appIndicator,
                             DatabaseHealthIndicator dbIndicator,
                             @Value("${app.version:0.0.1-SNAPSHOT}") String appVersion) {
        this.appIndicator = appIndicator;
        this.dbIndicator = dbIndicator;
        this.appVersion = appVersion;
    }

    @Override
    public HealthResponse check() {
        Health appHealth = appIndicator.health();
        Health dbHealth = dbIndicator.health();

        String overallStatus = computeOverallStatus(appHealth.getStatus(), dbHealth.getStatus());
        String dbStatus = dbHealth.getStatus().getCode();

        return new HealthResponse(
                overallStatus,
                dbStatus,
                LocalDateTime.now().toString(),
                appVersion
        );
    }

    private String computeOverallStatus(Status appStatus, Status dbStatus) {
        if (appStatus.equals(Status.DOWN) || dbStatus.equals(Status.DOWN)) {
            return Status.DOWN.getCode();
        }
        if (appStatus.equals(Status.UP) && dbStatus.equals(Status.UP)) {
            return Status.UP.getCode();
        }
        return "DEGRADED";
    }
}
