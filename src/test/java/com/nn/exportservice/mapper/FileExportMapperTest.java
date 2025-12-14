package com.nn.exportservice.mapper;

import com.nn.exportservice.dto.FileExportResponse;
import com.nn.exportservice.model.FileOperationResult;
import com.nn.exportservice.model.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileExportMapperTest {

    private FileExportMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FileExportMapper();
    }

    @Test
    void testToResponse_WithSuccessfulFiles() {
        FileOperationResult result = new FileOperationResult();
        result.addSuccess("file1.txt");
        result.addSuccess("file2.txt");

        FileExportResponse response = mapper.toResponse(FileType.REDEMPTION, result);

        assertEquals("REDEMPTION", response.fileType());
        assertEquals(2, response.filesProcessed());
        assertEquals(2, response.successfulFiles().size());
        assertTrue(response.successfulFiles().contains("file1.txt"));
        assertTrue(response.successfulFiles().contains("file2.txt"));
        assertTrue(response.errors().isEmpty());
    }

    @Test
    void testToResponse_WithErrors() {
        FileOperationResult result = new FileOperationResult();
        result.addSuccess("file1.txt");
        result.addError("file2.txt", "Permission denied");

        FileExportResponse response = mapper.toResponse(FileType.OUTPAY, result);

        assertEquals("OUTPAY", response.fileType());
        assertEquals(1, response.filesProcessed());
        assertEquals(1, response.successfulFiles().size());
        assertEquals(1, response.errors().size());
        assertEquals("file2.txt", response.errors().get(0).fileName());
        assertEquals("Permission denied", response.errors().get(0).errorMessage());
    }

    @Test
    void testToResponse_WithNoFiles() {
        FileOperationResult result = new FileOperationResult();

        FileExportResponse response = mapper.toResponse(FileType.OWN_AND_BEN, result);

        assertEquals("OWN_AND_BEN", response.fileType());
        assertEquals(0, response.filesProcessed());
        assertTrue(response.successfulFiles().isEmpty());
        assertTrue(response.errors().isEmpty());
    }

    @Test
    void testToResponse_WithMultipleErrors() {
        FileOperationResult result = new FileOperationResult();
        result.addError("file1.txt", "File not found");
        result.addError("file2.txt", "Permission denied");
        result.addError("file3.txt", "Disk full");

        FileExportResponse response = mapper.toResponse(FileType.REDEMPTION, result);

        assertEquals(0, response.filesProcessed());
        assertEquals(3, response.errors().size());
    }
}
