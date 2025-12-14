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
import java.time.Duration;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that verifies scheduled methods are actually triggered automatically
 * by Spring's @Scheduled annotation at the configured intervals.
 */
@SpringBootTest
@ActiveProfiles("test")
class SchedulingExecutionIT {

    @Autowired
    private FileSystemProperties fileSystemProperties;

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
    void testRedemptionScheduler_ProcessesFilesAutomatically() throws IOException {
        // Copy redemption test files to tmp folder
        copyTestFilesToTmp("redemption_01.txt", "redemption_02.txt");

        // Wait for the scheduler to process redemption files (scheduled every 2 seconds in test profile)
        // Files should be moved from tmp to export automatically
        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertTrue(Files.exists(exportFolder.resolve("redemption_01.txt")),
                            "redemption_01.txt should be moved to export folder");
                    assertTrue(Files.exists(exportFolder.resolve("redemption_02.txt")),
                            "redemption_02.txt should be moved to export folder");
                    assertFalse(Files.exists(tmpFolder.resolve("redemption_01.txt")),
                            "redemption_01.txt should be removed from tmp folder");
                    assertFalse(Files.exists(tmpFolder.resolve("redemption_02.txt")),
                            "redemption_02.txt should be removed from tmp folder");
                });
    }

    @Test
    void testOutpayScheduler_ProcessesFilesAutomatically() throws IOException {
        // Copy outpay test files to tmp folder
        copyTestFilesToTmp("outpay_01.txt", "outpay_02.txt");

        // Wait for the scheduler to process outpay files (scheduled every 4 seconds in test profile)
        await()
                .atMost(Duration.ofSeconds(8))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertTrue(Files.exists(exportFolder.resolve("outpay_01.txt")),
                            "outpay_01.txt should be moved to export folder");
                    assertTrue(Files.exists(exportFolder.resolve("outpay_02.txt")),
                            "outpay_02.txt should be moved to export folder");
                });
    }

    @Test
    void testOwnAndBenScheduler_ProcessesFilesAutomatically() throws IOException {
        // Copy own_and_ben test files to tmp folder
        copyTestFilesToTmp("own_and_ben_01.txt", "own_and_ben_02.txt");

        // Wait for the scheduler to process own_and_ben files (scheduled every 6 seconds in test profile)
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertTrue(Files.exists(exportFolder.resolve("own_and_ben_01.txt")),
                            "own_and_ben_01.txt should be moved to export folder");
                    assertTrue(Files.exists(exportFolder.resolve("own_and_ben_02.txt")),
                            "own_and_ben_02.txt should be moved to export folder");
                });
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
