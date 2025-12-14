package com.nn.exportservice.controller;

import com.nn.exportservice.dto.FileExportResponse;
import com.nn.exportservice.logging.LoggingContext;
import com.nn.exportservice.mapper.FileExportMapper;
import com.nn.exportservice.model.FileOperationResult;
import com.nn.exportservice.model.FileType;
import com.nn.exportservice.service.FileSystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/api/export")
public class FileExportController {

    private final FileSystemService fileSystemService;
    private final FileExportMapper fileExportMapper;

    public FileExportController(FileSystemService fileSystemService, FileExportMapper fileExportMapper) {
        this.fileSystemService = fileSystemService;
        this.fileExportMapper = fileExportMapper;
    }

    @PostMapping("/redemption")
    public ResponseEntity<FileExportResponse> exportRedemptionFiles() {
        return processFileExport(FileType.REDEMPTION);
    }

    @PostMapping("/outpay")
    public ResponseEntity<FileExportResponse> exportOutpayFiles() {
        return processFileExport(FileType.OUTPAY);
    }

    @PostMapping("/own-and-ben")
    public ResponseEntity<FileExportResponse> exportOwnAndBenFiles() {
        return processFileExport(FileType.OWN_AND_BEN);
    }

    private ResponseEntity<FileExportResponse> processFileExport(FileType fileType) {
        LoggingContext.setOperation("manual_file_export");
        try {
            log.info("Manual export triggered for fileType={}", fileType);

            List<Path> files = fileSystemService.listFilesByPrefix(fileType.getPrefixPattern());
            FileOperationResult result = fileSystemService.moveFiles(files);

            FileExportResponse response = fileExportMapper.toResponse(fileType, result);

            log.info("Manual export completed for fileType={} successful={} errors={}",
                    fileType, result.getSuccessCount(), result.getErrorCount());

            return ResponseEntity.ok(response);
        } finally {
            LoggingContext.clear();
        }
    }
}
