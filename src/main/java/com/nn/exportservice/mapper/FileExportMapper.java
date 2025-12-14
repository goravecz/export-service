package com.nn.exportservice.mapper;

import com.nn.exportservice.dto.ErrorDetail;
import com.nn.exportservice.dto.FileExportResponse;
import com.nn.exportservice.model.FileOperationResult;
import com.nn.exportservice.model.FileType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FileExportMapper {

    public FileExportResponse toResponse(FileType fileType, FileOperationResult result) {
        List<ErrorDetail> errors = result.getErrors().stream()
                .map(error -> new ErrorDetail(error.fileName(), error.errorMessage()))
                .toList();

        return new FileExportResponse(
                fileType.name(),
                result.getSuccessCount(),
                result.getSuccessfulFiles(),
                errors
        );
    }
}
