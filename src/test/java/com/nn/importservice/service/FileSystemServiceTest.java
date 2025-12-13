package com.nn.importservice.service;

import com.nn.importservice.config.FileSystemProperties;
import com.nn.importservice.exception.FileSystemException;
import com.nn.importservice.model.FileOperationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemServiceTest {

    @TempDir
    Path tempDir;

    private FileSystemService fileSystemService;
    private Path tmpFolder;
    private Path exportFolder;

    @BeforeEach
    void setUp() throws IOException {
        tmpFolder = tempDir.resolve("tmp");
        exportFolder = tempDir.resolve("export");
        Files.createDirectories(tmpFolder);

        FileSystemProperties properties = new FileSystemProperties(
                tmpFolder.toString(),
                exportFolder.toString()
        );
        fileSystemService = new FileSystemService(properties);
    }

    @Test
    void testListFilesByPrefix_WithMatchingFiles() throws IOException {
        Files.createFile(tmpFolder.resolve("redemption_001.txt"));
        Files.createFile(tmpFolder.resolve("redemption_002.txt"));
        Files.createFile(tmpFolder.resolve("outpay_001.txt"));

        List<Path> result = fileSystemService.listFilesByPrefix("redemption_");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getFileName().toString().startsWith("redemption_")));
    }

    @Test
    void testListFilesByPrefix_WithNoMatchingFiles() throws IOException {
        Files.createFile(tmpFolder.resolve("outpay_001.txt"));
        Files.createFile(tmpFolder.resolve("outpay_002.txt"));

        List<Path> result = fileSystemService.listFilesByPrefix("redemption_");

        assertTrue(result.isEmpty());
    }

    @Test
    void testListFilesByPrefix_WhenTmpFolderDoesNotExist() {
        FileSystemProperties properties = new FileSystemProperties(
                "/non/existent/path",
                exportFolder.toString()
        );
        FileSystemService service = new FileSystemService(properties);

        List<Path> result = service.listFilesByPrefix("redemption_");

        assertTrue(result.isEmpty());
    }

    @Test
    void testListFilesByPrefix_ThrowsExceptionOnIOError() throws IOException {
        // Create a file instead of a directory to force IOException
        Path fileInsteadOfDir = tempDir.resolve("not_a_directory.txt");
        Files.createFile(fileInsteadOfDir);

        FileSystemProperties properties = new FileSystemProperties(
                fileInsteadOfDir.toString(),
                exportFolder.toString()
        );
        FileSystemService service = new FileSystemService(properties);

        assertThrows(FileSystemException.class, () -> service.listFilesByPrefix("test_"));
    }

    @Test
    void testMoveFiles_Successfully() throws IOException {
        Path file1 = Files.createFile(tmpFolder.resolve("test1.txt"));
        Path file2 = Files.createFile(tmpFolder.resolve("test2.txt"));

        FileOperationResult result = fileSystemService.moveFiles(List.of(file1, file2));

        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getErrorCount());
        assertTrue(result.isFullySuccessful());
        assertTrue(result.getSuccessfulFiles().contains("test1.txt"));
        assertTrue(result.getSuccessfulFiles().contains("test2.txt"));

        // Verify files moved to export folder
        assertTrue(Files.exists(exportFolder.resolve("test1.txt")));
        assertTrue(Files.exists(exportFolder.resolve("test2.txt")));

        // Verify files removed from tmp folder
        assertFalse(Files.exists(file1));
        assertFalse(Files.exists(file2));
    }

    @Test
    void testMoveFiles_CreatesExportFolderIfNotExists() throws IOException {
        Path file = Files.createFile(tmpFolder.resolve("test.txt"));

        FileOperationResult result = fileSystemService.moveFiles(List.of(file));

        assertEquals(1, result.getSuccessCount());
        assertTrue(Files.exists(exportFolder));
        assertTrue(Files.exists(exportFolder.resolve("test.txt")));
    }

    @Test
    void testMoveFiles_WithEmptyList() {
        FileOperationResult result = fileSystemService.moveFiles(List.of());

        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getErrorCount());
        assertTrue(result.isFullySuccessful());
    }

    @Test
    void testMoveFiles_ReplacesExistingFile() throws IOException {
        Files.createDirectories(exportFolder);
        Path existingFile = Files.createFile(exportFolder.resolve("test.txt"));
        Files.writeString(existingFile, "old content");

        Path newFile = Files.createFile(tmpFolder.resolve("test.txt"));
        Files.writeString(newFile, "new content");

        FileOperationResult result = fileSystemService.moveFiles(List.of(newFile));

        assertEquals(1, result.getSuccessCount());
        assertEquals("new content", Files.readString(exportFolder.resolve("test.txt")));
    }

    @Test
    void testMoveFiles_HandlesMixedSuccessAndFailure() throws IOException {
        Path validFile = Files.createFile(tmpFolder.resolve("valid.txt"));
        Path nonExistentFile = tmpFolder.resolve("nonexistent.txt");

        FileOperationResult result = fileSystemService.moveFiles(List.of(validFile, nonExistentFile));

        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getErrorCount());
        assertFalse(result.isFullySuccessful());
        assertTrue(result.hasErrors());
        assertTrue(result.getSuccessfulFiles().contains("valid.txt"));
        assertTrue(Files.exists(exportFolder.resolve("valid.txt")));
        
        // Verify error details
        assertEquals("nonexistent.txt", result.getErrors().get(0).fileName());
        assertNotNull(result.getErrors().get(0).errorMessage());
    }

    @Test
    void testListFilesByPrefix_OwnAndBenFiles() throws IOException {
        Files.createFile(tmpFolder.resolve("own_and_ben_001.txt"));
        Files.createFile(tmpFolder.resolve("own_and_ben_002.txt"));
        Files.createFile(tmpFolder.resolve("own_and_ben_003.txt"));
        Files.createFile(tmpFolder.resolve("redemption_001.txt"));

        List<Path> result = fileSystemService.listFilesByPrefix("own_and_ben_");

        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(p -> p.getFileName().toString().startsWith("own_and_ben_")));
    }
}
