package com.plataforma.conversacional.service.impl;

import com.plataforma.conversacional.dto.response.HealthResponse;
import com.plataforma.conversacional.health.ApplicationHealthIndicator;
import com.plataforma.conversacional.health.DatabaseHealthIndicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthServiceImplTest {

    @Mock
    private ApplicationHealthIndicator appIndicator;

    @Mock
    private DatabaseHealthIndicator dbIndicator;

    private HealthServiceImpl healthService;

    @BeforeEach
    void setUp() {
        healthService = new HealthServiceImpl(appIndicator, dbIndicator, "0.0.1-SNAPSHOT");
    }

    @Test
    void check_ShouldReturnUp_WhenBothIndicatorsUp() {
        when(appIndicator.health()).thenReturn(Health.up().build());
        when(dbIndicator.health()).thenReturn(Health.up().build());

        HealthResponse result = healthService.check();

        assertEquals(Status.UP.getCode(), result.status());
        assertNotNull(result.timestamp());
        assertNotNull(result.version());
    }

    @Test
    void check_ShouldReturnDown_WhenAppIsDown() {
        when(appIndicator.health()).thenReturn(Health.down().build());
        when(dbIndicator.health()).thenReturn(Health.up().build());

        HealthResponse result = healthService.check();

        assertEquals(Status.DOWN.getCode(), result.status());
    }

    @Test
    void check_ShouldReturnDown_WhenDbIsDown() {
        when(appIndicator.health()).thenReturn(Health.up().build());
        when(dbIndicator.health()).thenReturn(Health.down().build());

        HealthResponse result = healthService.check();

        assertEquals(Status.DOWN.getCode(), result.status());
    }

    @Test
    void check_ShouldReturnDown_WhenBothDown() {
        when(appIndicator.health()).thenReturn(Health.down().build());
        when(dbIndicator.health()).thenReturn(Health.down().build());

        HealthResponse result = healthService.check();

        assertEquals(Status.DOWN.getCode(), result.status());
    }

    @Test
    void check_ShouldIncludeDatabaseStatus() {
        when(appIndicator.health()).thenReturn(Health.up().build());
        when(dbIndicator.health()).thenReturn(Health.up().withDetail("database", "PostgreSQL").build());

        HealthResponse result = healthService.check();

        assertEquals(Status.UP.getCode(), result.database());
    }

    @Test
    void check_ShouldUseConfiguredAppVersion() {
        when(appIndicator.health()).thenReturn(Health.up().build());
        when(dbIndicator.health()).thenReturn(Health.up().build());

        HealthResponse result = healthService.check();

        assertNotNull(result.version());
    }
}
