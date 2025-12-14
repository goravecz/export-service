package com.nn.exportservice.exception;

import com.nn.exportservice.dto.FileExportResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleFileSystemException() {
        FileSystemException exception = new FileSystemException("Disk full");

        ResponseEntity<FileExportResponse> response = handler.handleFileSystemException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UNKNOWN", response.getBody().fileType());
        assertEquals(0, response.getBody().filesProcessed());
        assertTrue(response.getBody().successfulFiles().isEmpty());
        assertEquals(1, response.getBody().errors().size());
        assertEquals("system", response.getBody().errors().get(0).fileName());
        assertEquals("Disk full", response.getBody().errors().get(0).errorMessage());
    }

    @Test
    void testHandleFileSystemExceptionWithCause() {
        IOException cause = new IOException("IO error");
        FileSystemException exception = new FileSystemException("Failed to read file", cause);

        ResponseEntity<FileExportResponse> response = handler.handleFileSystemException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().errors().size());
        assertEquals("Failed to read file", response.getBody().errors().get(0).errorMessage());
    }

    @Test
    void testHandleGenericException() {
        RuntimeException exception = new RuntimeException("Something went wrong");

        ResponseEntity<FileExportResponse> response = handler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UNKNOWN", response.getBody().fileType());
        assertEquals(0, response.getBody().filesProcessed());
        assertTrue(response.getBody().successfulFiles().isEmpty());
        assertEquals(1, response.getBody().errors().size());
        assertEquals("system", response.getBody().errors().get(0).fileName());
        assertTrue(response.getBody().errors().get(0).errorMessage().contains("Something went wrong"));
    }

    @Test
    void testHandleGenericExceptionWithNullPointerException() {
        NullPointerException exception = new NullPointerException("Null reference");

        ResponseEntity<FileExportResponse> response = handler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().errors().size());
        assertTrue(response.getBody().errors().get(0).errorMessage().contains("Null reference"));
    }
}
