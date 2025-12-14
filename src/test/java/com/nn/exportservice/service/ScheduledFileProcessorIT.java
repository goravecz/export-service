package com.nn.exportservice.service;

import com.nn.exportservice.config.FileSystemProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ScheduledFileProcessorIT {

    @Autowired
    private FileSystemProperties fileSystemProperties;

    @Autowired
    private ScheduledFileProcessor scheduledFileProcessor;

    private Path tmpFolder;
    private Path exportFolder;
    private Path testResourcesFolder;

    @BeforeEach
    void setUp() throws IOException {
        tmpFolder = Paths.get(fileSystemProperties.tmpFolder());
        exportFolder = Paths.get(fileSystemProperties.exportFolder());
        testResourcesFolder = Paths.get("src/test/resources/testfiles");

        // Create directories
        Files.createDirectories(tmpFolder);
        Files.createDirectories(exportFolder);

        // Clean up any existing files
        cleanDirectory(tmpFolder);
        cleanDirectory(exportFolder);
    }

    @AfterEach
    void tearDown() throws IOException {
        cleanDirectory(tmpFolder);
        cleanDirectory(exportFolder);
    }

    @Test
    void testProcessRedemptionFiles_MovesFilesToExportFolder() throws IOException {
        // Copy test files to tmp folder
        copyTestFilesToTmp("redemption_01.txt", "redemption_02.txt");

        // Process files
        scheduledFileProcessor.processRedemptionFiles();

        // Verify files moved to export folder
        assertTrue(Files.exists(exportFolder.resolve("redemption_01.txt")));
        assertTrue(Files.exists(exportFolder.resolve("redemption_02.txt")));

        // Verify files removed from tmp folder
        assertFalse(Files.exists(tmpFolder.resolve("redemption_01.txt")));
        assertFalse(Files.exists(tmpFolder.resolve("redemption_02.txt")));
    }

    @Test
    void testProcessOutpayFiles_MovesFilesToExportFolder() throws IOException {
        // Copy test files to tmp folder
        copyTestFilesToTmp("outpay_01.txt", "outpay_02.txt", "outpay_03.txt");

        // Process files
        scheduledFileProcessor.processOutpayFiles();

        // Verify files moved to export folder
        assertTrue(Files.exists(exportFolder.resolve("outpay_01.txt")));
        assertTrue(Files.exists(exportFolder.resolve("outpay_02.txt")));
        assertTrue(Files.exists(exportFolder.resolve("outpay_03.txt")));

        // Verify files removed from tmp folder
        assertFalse(Files.exists(tmpFolder.resolve("outpay_01.txt")));
        assertFalse(Files.exists(tmpFolder.resolve("outpay_02.txt")));
        assertFalse(Files.exists(tmpFolder.resolve("outpay_03.txt")));
    }

    @Test
    void testProcessOwnAndBenFiles_MovesFilesToExportFolder() throws IOException {
        // Copy test files to tmp folder
        copyTestFilesToTmp("own_and_ben_01.txt", "own_and_ben_02.txt");

        // Process files
        scheduledFileProcessor.processOwnAndBenFiles();

        // Verify files moved to export folder
        assertTrue(Files.exists(exportFolder.resolve("own_and_ben_01.txt")));
        assertTrue(Files.exists(exportFolder.resolve("own_and_ben_02.txt")));

        // Verify files removed from tmp folder
        assertFalse(Files.exists(tmpFolder.resolve("own_and_ben_01.txt")));
        assertFalse(Files.exists(tmpFolder.resolve("own_and_ben_02.txt")));
    }

    @Test
    void testProcessMixedFileTypes_OnlyProcessesMatchingPrefix() throws IOException {
        // Copy different file types to tmp folder
        copyTestFilesToTmp("redemption_01.txt", "outpay_01.txt", "own_and_ben_01.txt");

        // Process only redemption files
        scheduledFileProcessor.processRedemptionFiles();

        // Verify only redemption files moved
        assertTrue(Files.exists(exportFolder.resolve("redemption_01.txt")));
        assertFalse(Files.exists(exportFolder.resolve("outpay_01.txt")));
        assertFalse(Files.exists(exportFolder.resolve("own_and_ben_01.txt")));

        // Verify other files still in tmp
        assertTrue(Files.exists(tmpFolder.resolve("outpay_01.txt")));
        assertTrue(Files.exists(tmpFolder.resolve("own_and_ben_01.txt")));
    }

    @Test
    void testProcessWithNoFiles_KeepsDirectoriesEmpty() throws IOException {
        // Process with no files in tmp folder
        scheduledFileProcessor.processRedemptionFiles();
        scheduledFileProcessor.processOutpayFiles();
        scheduledFileProcessor.processOwnAndBenFiles();

        // Verify no files were created in export folder
        try (Stream<Path> files = Files.list(exportFolder)) {
            assertEquals(0, files.count(), "Export folder should remain empty");
        }
        
        // Verify tmp folder is still empty
        try (Stream<Path> files = Files.list(tmpFolder)) {
            assertEquals(0, files.count(), "Tmp folder should remain empty");
        }
    }

    private void copyTestFilesToTmp(String... fileNames) throws IOException {
        for (String fileName : fileNames) {
            Path source = testResourcesFolder.resolve(fileName);
            Path destination = tmpFolder.resolve(fileName);
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void cleanDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (Stream<Path> stream = Files.list(directory)) {
                stream.filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                // Ignore
                            }
                        });
            }
        }
    }
}
