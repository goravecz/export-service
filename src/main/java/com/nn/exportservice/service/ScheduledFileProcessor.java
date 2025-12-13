package com.nn.exportservice.service;

import com.nn.exportservice.logging.LoggingContext;
import com.nn.exportservice.model.FileOperationResult;
import com.nn.exportservice.model.FileType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
public class ScheduledFileProcessor {

    private final FileSystemService fileSystemService;

    public ScheduledFileProcessor(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    @Scheduled(cron = "${scheduling.redemption-cron}")
    public void processRedemptionFiles() {
        processFilesByType(FileType.REDEMPTION);
    }

    @Scheduled(cron = "${scheduling.outpay-cron}")
    public void processOutpayFiles() {
        processFilesByType(FileType.OUTPAY);
    }

    @Scheduled(cron = "${scheduling.own-and-ben-cron}")
    public void processOwnAndBenFiles() {
        processFilesByType(FileType.OWN_AND_BEN);
    }

    private void processFilesByType(FileType fileType) {
        LoggingContext.setOperation("scheduled_file_processing");
        try {
            log.info("Starting scheduled processing for fileType={}", fileType);
            
            List<Path> files = fileSystemService.listFilesByPrefix(fileType.getPrefixPattern());
            
            if (files.isEmpty()) {
                log.info("No files found for fileType={}", fileType);
                return;
            }
            
            FileOperationResult result = fileSystemService.moveFiles(files);
            
            log.info("Completed scheduled processing for fileType={} successful={} errors={}", 
                    fileType, result.getSuccessCount(), result.getErrorCount());
                    
            if (result.hasErrors()) {
                result.getErrors().forEach(error -> 
                    log.warn("File move error: fileName={} error={}", error.fileName(), error.errorMessage())
                );
            }
        } catch (Exception e) {
            log.error("Failed scheduled processing for fileType={} error={}", fileType, e.getMessage(), e);
        } finally {
            LoggingContext.clear();
        }
    }
}
