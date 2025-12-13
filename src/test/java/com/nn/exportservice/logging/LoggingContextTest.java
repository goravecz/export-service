package com.nn.exportservice.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingContextTest {

    @AfterEach
    void cleanup() {
        LoggingContext.clear();
    }

    @Test
    void testSetOperation_SetsServiceOperationAndCorrelationId() {
        LoggingContext.setOperation("test_operation");

        assertThat(MDC.get("service")).isEqualTo("import-service");
        assertThat(MDC.get("operation")).isEqualTo("test_operation");
        assertThat(MDC.get("correlationId")).isNotNull();
    }

    @Test
    void testSetOperation_PreservesExistingCorrelationId() {
        String existingCorrelationId = "existing-correlation-id";
        LoggingContext.setCorrelationId(existingCorrelationId);
        
        LoggingContext.setOperation("test_operation");

        assertThat(MDC.get("correlationId")).isEqualTo(existingCorrelationId);
    }

    @Test
    void testClear_RemovesAllMDCEntries() {
        LoggingContext.setOperation("test_operation");
        
        LoggingContext.clear();

        assertThat(MDC.get("service")).isNull();
        assertThat(MDC.get("operation")).isNull();
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void testGetCorrelationId_ReturnsCurrentCorrelationId() {
        LoggingContext.setOperation("test_operation");
        
        String correlationId = LoggingContext.getCorrelationId();

        assertThat(correlationId).isNotNull();
        assertThat(correlationId).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void testSetCorrelationId_SetsCustomCorrelationId() {
        String customId = "custom-id-12345";
        
        LoggingContext.setCorrelationId(customId);

        assertThat(LoggingContext.getCorrelationId()).isEqualTo(customId);
    }
}
