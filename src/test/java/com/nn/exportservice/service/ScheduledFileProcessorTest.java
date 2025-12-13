package com.nn.exportservice.service;

import com.nn.exportservice.model.FileOperationResult;
import com.nn.exportservice.model.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledFileProcessorTest {

    @Mock
    private FileSystemService fileSystemService;

    @InjectMocks
    private ScheduledFileProcessor scheduledFileProcessor;

    private FileOperationResult successResult;
    private FileOperationResult resultWithErrors;
    private List<Path> testFiles;

    @BeforeEach
    void setUp() {
        successResult = new FileOperationResult();
        successResult.addSuccess("file1.txt");
        successResult.addSuccess("file2.txt");

        resultWithErrors = new FileOperationResult();
        resultWithErrors.addSuccess("file1.txt");
        resultWithErrors.addError("file2.txt", "File locked");

        testFiles = List.of(
                Paths.get("/tmp/file1.txt"),
                Paths.get("/tmp/file2.txt")
        );
    }

    @Test
    void testProcessRedemptionFiles_WithFiles() {
        when(fileSystemService.listFilesByPrefix(FileType.REDEMPTION.getPrefixPattern()))
                .thenReturn(testFiles);
        when(fileSystemService.moveFiles(testFiles))
                .thenReturn(successResult);

        scheduledFileProcessor.processRedemptionFiles();

        verify(fileSystemService).listFilesByPrefix(FileType.REDEMPTION.getPrefixPattern());
        verify(fileSystemService).moveFiles(testFiles);
    }

    @Test
    void testProcessRedemptionFiles_WithNoFiles() {
        when(fileSystemService.listFilesByPrefix(FileType.REDEMPTION.getPrefixPattern()))
                .thenReturn(Collections.emptyList());

        scheduledFileProcessor.processRedemptionFiles();

        verify(fileSystemService).listFilesByPrefix(FileType.REDEMPTION.getPrefixPattern());
        verify(fileSystemService, never()).moveFiles(any());
    }

    @Test
    void testProcessRedemptionFiles_WithErrors() {
        when(fileSystemService.listFilesByPrefix(FileType.REDEMPTION.getPrefixPattern()))
                .thenReturn(testFiles);
        when(fileSystemService.moveFiles(testFiles))
                .thenReturn(resultWithErrors);

        scheduledFileProcessor.processRedemptionFiles();

        verify(fileSystemService).listFilesByPrefix(FileType.REDEMPTION.getPrefixPattern());
        verify(fileSystemService).moveFiles(testFiles);
    }

    @Test
    void testProcessRedemptionFiles_WithException() {
        when(fileSystemService.listFilesByPrefix(anyString()))
                .thenThrow(new RuntimeException("File system error"));

        // Should not throw exception, just log it
        scheduledFileProcessor.processRedemptionFiles();

        verify(fileSystemService).listFilesByPrefix(FileType.REDEMPTION.getPrefixPattern());
        verify(fileSystemService, never()).moveFiles(any());
    }

    @Test
    void testProcessOutpayFiles_WithFiles() {
        when(fileSystemService.listFilesByPrefix(FileType.OUTPAY.getPrefixPattern()))
                .thenReturn(testFiles);
        when(fileSystemService.moveFiles(testFiles))
                .thenReturn(successResult);

        scheduledFileProcessor.processOutpayFiles();

        verify(fileSystemService).listFilesByPrefix(FileType.OUTPAY.getPrefixPattern());
        verify(fileSystemService).moveFiles(testFiles);
    }

    @Test
    void testProcessOutpayFiles_WithNoFiles() {
        when(fileSystemService.listFilesByPrefix(FileType.OUTPAY.getPrefixPattern()))
                .thenReturn(Collections.emptyList());

        scheduledFileProcessor.processOutpayFiles();

        verify(fileSystemService).listFilesByPrefix(FileType.OUTPAY.getPrefixPattern());
        verify(fileSystemService, never()).moveFiles(any());
    }

    @Test
    void testProcessOwnAndBenFiles_WithFiles() {
        when(fileSystemService.listFilesByPrefix(FileType.OWN_AND_BEN.getPrefixPattern()))
                .thenReturn(testFiles);
        when(fileSystemService.moveFiles(testFiles))
                .thenReturn(successResult);

        scheduledFileProcessor.processOwnAndBenFiles();

        verify(fileSystemService).listFilesByPrefix(FileType.OWN_AND_BEN.getPrefixPattern());
        verify(fileSystemService).moveFiles(testFiles);
    }

    @Test
    void testProcessOwnAndBenFiles_WithNoFiles() {
        when(fileSystemService.listFilesByPrefix(FileType.OWN_AND_BEN.getPrefixPattern()))
                .thenReturn(Collections.emptyList());

        scheduledFileProcessor.processOwnAndBenFiles();

        verify(fileSystemService).listFilesByPrefix(FileType.OWN_AND_BEN.getPrefixPattern());
        verify(fileSystemService, never()).moveFiles(any());
    }
}
