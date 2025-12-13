package com.nn.exportservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "filesystem")
public record FileSystemProperties(
    String tmpFolder,
    String exportFolder
) {}
