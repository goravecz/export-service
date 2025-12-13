package com.nn.importservice.logging;

import org.slf4j.MDC;

import java.util.UUID;

public class LoggingContext {

    private static final String SERVICE = "service";
    private static final String OPERATION = "operation";
    private static final String CORRELATION_ID = "correlationId";
    private static final String SERVICE_NAME = "import-service";

    public static void setOperation(String operation) {
        MDC.put(SERVICE, SERVICE_NAME);
        MDC.put(OPERATION, operation);
        if (MDC.get(CORRELATION_ID) == null) {
            MDC.put(CORRELATION_ID, generateCorrelationId());
        }
    }

    public static void clear() {
        MDC.clear();
    }

    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID);
    }

    public static void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID, correlationId);
    }

    private static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
