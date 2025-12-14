package com.nn.exportservice.controller;

import com.nn.exportservice.dto.ErrorDetail;
import com.nn.exportservice.dto.FileExportResponse;
import com.nn.exportservice.mapper.FileExportMapper;
import com.nn.exportservice.model.FileOperationResult;
import com.nn.exportservice.model.FileType;
import com.nn.exportservice.service.FileSystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileExportControllerTest {

    @Mock
    private FileSystemService fileSystemService;

    @Mock
    private FileExportMapper fileExportMapper;

    @InjectMocks
    private FileExportController controller;

    private List<Path> mockFiles;
    private FileOperationResult mockResult;
    private FileExportResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockFiles = List.of(
                Paths.get("/tmp/redemption_01.txt"),
                Paths.get("/tmp/redemption_02.txt")
        );
        
        mockResult = new FileOperationResult();
        mockResult.addSuccess("redemption_01.txt");
        mockResult.addSuccess("redemption_02.txt");
        
        mockResponse = new FileExportResponse(
                "REDEMPTION",
                2,
                List.of("redemption_01.txt", "redemption_02.txt"),
                List.of()
        );
    }

    @Test
    void testExportRedemptionFiles_Success() {
        when(fileSystemService.listFilesByPrefix("redemption")).thenReturn(mockFiles);
        when(fileSystemService.moveFiles(mockFiles)).thenReturn(mockResult);
        when(fileExportMapper.toResponse(FileType.REDEMPTION, mockResult)).thenReturn(mockResponse);

        ResponseEntity<FileExportResponse> response = controller.exportRedemptionFiles();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("REDEMPTION", response.getBody().fileType());
        assertEquals(2, response.getBody().filesProcessed());
        assertTrue(response.getBody().errors().isEmpty());

        verify(fileSystemService).listFilesByPrefix("redemption");
        verify(fileSystemService).moveFiles(mockFiles);
        verify(fileExportMapper).toResponse(FileType.REDEMPTION, mockResult);
    }

    @Test
    void testExportRedemptionFiles_NoFiles() {
        FileOperationResult emptyResult = new FileOperationResult();
        FileExportResponse emptyResponse = new FileExportResponse(
                "REDEMPTION",
                0,
                List.of(),
                List.of()
        );

        when(fileSystemService.listFilesByPrefix("redemption")).thenReturn(List.of());
        when(fileSystemService.moveFiles(List.of())).thenReturn(emptyResult);
        when(fileExportMapper.toResponse(FileType.REDEMPTION, emptyResult)).thenReturn(emptyResponse);

        ResponseEntity<FileExportResponse> response = controller.exportRedemptionFiles();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().filesProcessed());
        assertTrue(response.getBody().successfulFiles().isEmpty());

        verify(fileSystemService).listFilesByPrefix("redemption");
        verify(fileSystemService).moveFiles(List.of());
    }

    @Test
    void testExportRedemptionFiles_WithErrors() {
        FileOperationResult resultWithErrors = new FileOperationResult();
        resultWithErrors.addSuccess("redemption_01.txt");
        resultWithErrors.addError("redemption_02.txt", "Failed to move file");

        FileExportResponse responseWithErrors = new FileExportResponse(
                "REDEMPTION",
                1,
                List.of("redemption_01.txt"),
                List.of(new ErrorDetail("redemption_02.txt", "Failed to move file"))
        );

        when(fileSystemService.listFilesByPrefix("redemption")).thenReturn(mockFiles);
        when(fileSystemService.moveFiles(mockFiles)).thenReturn(resultWithErrors);
        when(fileExportMapper.toResponse(FileType.REDEMPTION, resultWithErrors)).thenReturn(responseWithErrors);

        ResponseEntity<FileExportResponse> response = controller.exportRedemptionFiles();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().filesProcessed());
        assertEquals(1, response.getBody().errors().size());
        assertEquals("redemption_02.txt", response.getBody().errors().get(0).fileName());

        verify(fileSystemService).listFilesByPrefix("redemption");
        verify(fileSystemService).moveFiles(mockFiles);
    }

    @Test
    void testExportOutpayFiles_Success() {
        List<Path> outpayFiles = List.of(
                Paths.get("/tmp/outpay_01.txt"),
                Paths.get("/tmp/outpay_02.txt")
        );

        FileOperationResult outpayResult = new FileOperationResult();
        outpayResult.addSuccess("outpay_01.txt");
        outpayResult.addSuccess("outpay_02.txt");

        FileExportResponse outpayResponse = new FileExportResponse(
                "OUTPAY",
                2,
                List.of("outpay_01.txt", "outpay_02.txt"),
                List.of()
        );

        when(fileSystemService.listFilesByPrefix("outpay")).thenReturn(outpayFiles);
        when(fileSystemService.moveFiles(outpayFiles)).thenReturn(outpayResult);
        when(fileExportMapper.toResponse(FileType.OUTPAY, outpayResult)).thenReturn(outpayResponse);

        ResponseEntity<FileExportResponse> response = controller.exportOutpayFiles();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OUTPAY", response.getBody().fileType());
        assertEquals(2, response.getBody().filesProcessed());

        verify(fileSystemService).listFilesByPrefix("outpay");
        verify(fileSystemService).moveFiles(outpayFiles);
        verify(fileExportMapper).toResponse(FileType.OUTPAY, outpayResult);
    }

    @Test
    void testExportOutpayFiles_NoFiles() {
        FileOperationResult emptyResult = new FileOperationResult();
        FileExportResponse emptyResponse = new FileExportResponse(
                "OUTPAY",
                0,
                List.of(),
                List.of()
        );

        when(fileSystemService.listFilesByPrefix("outpay")).thenReturn(List.of());
        when(fileSystemService.moveFiles(List.of())).thenReturn(emptyResult);
        when(fileExportMapper.toResponse(FileType.OUTPAY, emptyResult)).thenReturn(emptyResponse);

        ResponseEntity<FileExportResponse> response = controller.exportOutpayFiles();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().filesProcessed());

        verify(fileSystemService).listFilesByPrefix("outpay");
    }

    @Test
    void testExportOwnAndBenFiles_Success() {
        List<Path> ownAndBenFiles = List.of(
                Paths.get("/tmp/own_and_ben_01.txt"),
                Paths.get("/tmp/own_and_ben_02.txt")
        );

        FileOperationResult ownAndBenResult = new FileOperationResult();
        ownAndBenResult.addSuccess("own_and_ben_01.txt");
        ownAndBenResult.addSuccess("own_and_ben_02.txt");

        FileExportResponse ownAndBenResponse = new FileExportResponse(
                "OWN_AND_BEN",
                2,
                List.of("own_and_ben_01.txt", "own_and_ben_02.txt"),
                List.of()
        );

        when(fileSystemService.listFilesByPrefix("own_and_ben")).thenReturn(ownAndBenFiles);
        when(fileSystemService.moveFiles(ownAndBenFiles)).thenReturn(ownAndBenResult);
        when(fileExportMapper.toResponse(FileType.OWN_AND_BEN, ownAndBenResult)).thenReturn(ownAndBenResponse);

        ResponseEntity<FileExportResponse> response = controller.exportOwnAndBenFiles();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OWN_AND_BEN", response.getBody().fileType());
        assertEquals(2, response.getBody().filesProcessed());

        verify(fileSystemService).listFilesByPrefix("own_and_ben");
        verify(fileSystemService).moveFiles(ownAndBenFiles);
        verify(fileExportMapper).toResponse(FileType.OWN_AND_BEN, ownAndBenResult);
    }

    @Test
    void testExportOwnAndBenFiles_NoFiles() {
        FileOperationResult emptyResult = new FileOperationResult();
        FileExportResponse emptyResponse = new FileExportResponse(
                "OWN_AND_BEN",
                0,
                List.of(),
                List.of()
        );

        when(fileSystemService.listFilesByPrefix("own_and_ben")).thenReturn(List.of());
        when(fileSystemService.moveFiles(List.of())).thenReturn(emptyResult);
        when(fileExportMapper.toResponse(FileType.OWN_AND_BEN, emptyResult)).thenReturn(emptyResponse);

        ResponseEntity<FileExportResponse> response = controller.exportOwnAndBenFiles();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().filesProcessed());

        verify(fileSystemService).listFilesByPrefix("own_and_ben");
    }

    @Test
    void testExportRedemptionFiles_ServiceThrowsException() {
        when(fileSystemService.listFilesByPrefix("redemption")).thenThrow(new RuntimeException("Service error"));

        assertThrows(RuntimeException.class, () -> controller.exportRedemptionFiles());

        verify(fileSystemService).listFilesByPrefix("redemption");
        verify(fileSystemService, never()).moveFiles(anyList());
        verify(fileExportMapper, never()).toResponse(any(), any());
    }

    @Test
    void testExportOutpayFiles_ServiceThrowsException() {
        when(fileSystemService.listFilesByPrefix("outpay")).thenThrow(new RuntimeException("Service error"));

        assertThrows(RuntimeException.class, () -> controller.exportOutpayFiles());

        verify(fileSystemService).listFilesByPrefix("outpay");
        verify(fileSystemService, never()).moveFiles(anyList());
    }

    @Test
    void testExportOwnAndBenFiles_ServiceThrowsException() {
        when(fileSystemService.listFilesByPrefix("own_and_ben")).thenThrow(new RuntimeException("Service error"));

        assertThrows(RuntimeException.class, () -> controller.exportOwnAndBenFiles());

        verify(fileSystemService).listFilesByPrefix("own_and_ben");
        verify(fileSystemService, never()).moveFiles(anyList());
    }

    @Test
    void testExportRedemptionFiles_VerifyCorrectFileTypePassedToMapper() {
        when(fileSystemService.listFilesByPrefix("redemption")).thenReturn(mockFiles);
        when(fileSystemService.moveFiles(mockFiles)).thenReturn(mockResult);
        when(fileExportMapper.toResponse(FileType.REDEMPTION, mockResult)).thenReturn(mockResponse);

        controller.exportRedemptionFiles();
        
        verify(fileExportMapper).toResponse(FileType.REDEMPTION, mockResult);
        verifyNoMoreInteractions(fileExportMapper);
    }

    @Test
    void testExportOutpayFiles_VerifyCorrectFileTypePassedToMapper() {
        List<Path> outpayFiles = List.of(Paths.get("/tmp/outpay_01.txt"));
        FileOperationResult outpayResult = new FileOperationResult();
        outpayResult.addSuccess("outpay_01.txt");
        FileExportResponse outpayResponse = new FileExportResponse("OUTPAY", 1, List.of("outpay_01.txt"), List.of());

        when(fileSystemService.listFilesByPrefix("outpay")).thenReturn(outpayFiles);
        when(fileSystemService.moveFiles(outpayFiles)).thenReturn(outpayResult);
        when(fileExportMapper.toResponse(FileType.OUTPAY, outpayResult)).thenReturn(outpayResponse);

        controller.exportOutpayFiles();
        
        verify(fileExportMapper).toResponse(FileType.OUTPAY, outpayResult);
        verifyNoMoreInteractions(fileExportMapper);
    }

    @Test
    void testExportOwnAndBenFiles_VerifyCorrectFileTypePassedToMapper() {
        List<Path> ownAndBenFiles = List.of(Paths.get("/tmp/own_and_ben_01.txt"));
        FileOperationResult ownAndBenResult = new FileOperationResult();
        ownAndBenResult.addSuccess("own_and_ben_01.txt");
        FileExportResponse ownAndBenResponse = new FileExportResponse("OWN_AND_BEN", 1, List.of("own_and_ben_01.txt"), List.of());

        when(fileSystemService.listFilesByPrefix("own_and_ben")).thenReturn(ownAndBenFiles);
        when(fileSystemService.moveFiles(ownAndBenFiles)).thenReturn(ownAndBenResult);
        when(fileExportMapper.toResponse(FileType.OWN_AND_BEN, ownAndBenResult)).thenReturn(ownAndBenResponse);

        controller.exportOwnAndBenFiles();
        
        verify(fileExportMapper).toResponse(FileType.OWN_AND_BEN, ownAndBenResult);
        verifyNoMoreInteractions(fileExportMapper);
    }

    @Test
    void testExportRedemptionFiles_VerifyCorrectPrefixUsed() {
        FileOperationResult emptyResult = new FileOperationResult();
        FileExportResponse emptyResponse = new FileExportResponse("REDEMPTION", 0, List.of(), List.of());

        when(fileSystemService.listFilesByPrefix("redemption")).thenReturn(List.of());
        when(fileSystemService.moveFiles(List.of())).thenReturn(emptyResult);
        when(fileExportMapper.toResponse(FileType.REDEMPTION, emptyResult)).thenReturn(emptyResponse);

        controller.exportRedemptionFiles();
        
        verify(fileSystemService).listFilesByPrefix("redemption");
        verify(fileSystemService, times(1)).listFilesByPrefix(anyString());
    }

    @Test
    void testExportOutpayFiles_VerifyCorrectPrefixUsed() {
        FileOperationResult emptyResult = new FileOperationResult();
        FileExportResponse emptyResponse = new FileExportResponse("OUTPAY", 0, List.of(), List.of());

        when(fileSystemService.listFilesByPrefix("outpay")).thenReturn(List.of());
        when(fileSystemService.moveFiles(List.of())).thenReturn(emptyResult);
        when(fileExportMapper.toResponse(FileType.OUTPAY, emptyResult)).thenReturn(emptyResponse);

        controller.exportOutpayFiles();
        
        verify(fileSystemService).listFilesByPrefix("outpay");
        verify(fileSystemService, times(1)).listFilesByPrefix(anyString());
    }

    @Test
    void testExportOwnAndBenFiles_VerifyCorrectPrefixUsed() {
        FileOperationResult emptyResult = new FileOperationResult();
        FileExportResponse emptyResponse = new FileExportResponse("OWN_AND_BEN", 0, List.of(), List.of());

        when(fileSystemService.listFilesByPrefix("own_and_ben")).thenReturn(List.of());
        when(fileSystemService.moveFiles(List.of())).thenReturn(emptyResult);
        when(fileExportMapper.toResponse(FileType.OWN_AND_BEN, emptyResult)).thenReturn(emptyResponse);

        controller.exportOwnAndBenFiles();
        
        verify(fileSystemService).listFilesByPrefix("own_and_ben");
        verify(fileSystemService, times(1)).listFilesByPrefix(anyString());
    }
}
