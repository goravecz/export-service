package com.nn.exportservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scheduling")
public record SchedulingProperties(
    String redemptionCron,
    String outpayCron,
    String ownAndBenCron
) {}
