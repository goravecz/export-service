package com.nn.exportservice.exception;

import com.nn.exportservice.dto.ErrorDetail;
import com.nn.exportservice.dto.FileExportResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileSystemException.class)
    public ResponseEntity<FileExportResponse> handleFileSystemException(FileSystemException e) {
        log.error("File system error: {}", e.getMessage(), e);
        
        FileExportResponse response = new FileExportResponse(
                "UNKNOWN",
                0,
                List.of(),
                List.of(new ErrorDetail("system", e.getMessage()))
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FileExportResponse> handleGenericException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        
        FileExportResponse response = new FileExportResponse(
                "UNKNOWN",
                0,
                List.of(),
                List.of(new ErrorDetail("system", "An unexpected error occurred: " + e.getMessage()))
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
