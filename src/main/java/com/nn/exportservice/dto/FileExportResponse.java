package com.nn.exportservice.dto;

import java.util.List;

public record FileExportResponse(
    String fileType,
    int filesProcessed,
    List<String> successfulFiles,
    List<ErrorDetail> errors
) {}
