package com.nn.exportservice.controller;

import com.nn.exportservice.config.FileSystemProperties;
import com.nn.exportservice.dto.FileExportResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.task.scheduling.enabled=false")
@ActiveProfiles("test")
class FileExportControllerIT {

    @TestConfiguration
    static class RestClientTestConfiguration {
        @Bean
        public RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Autowired
    private FileSystemProperties fileSystemProperties;

    private RestClient restClient;
    private Path tmpFolder;
    private Path exportFolder;
    private Path testResourcesFolder;

    @BeforeEach
    void setUp() throws IOException {
        restClient = restClientBuilder.baseUrl("http://localhost:" + port).build();
        
        tmpFolder = Paths.get(fileSystemProperties.tmpFolder());
        exportFolder = Paths.get(fileSystemProperties.exportFolder());
        testResourcesFolder = Paths.get("src/test/resources/testfiles");

        Files.createDirectories(tmpFolder);
        Files.createDirectories(exportFolder);

        cleanDirectory(tmpFolder);
        cleanDirectory(exportFolder);
    }

    @AfterEach
    void tearDown() throws IOException {
        cleanDirectory(tmpFolder);
        cleanDirectory(exportFolder);
    }

    @Test
    void testExportRedemptionFiles_WithFiles() throws IOException {
        copyTestFilesToTmp("redemption_01.txt", "redemption_02.txt");

        FileExportResponse response = restClient.post()
                .uri("/v1/api/export/redemption")
                .retrieve()
                .body(FileExportResponse.class);

        assertNotNull(response);
        assertEquals("REDEMPTION", response.fileType());
        assertEquals(2, response.filesProcessed());
        assertEquals(2, response.successfulFiles().size());
        assertTrue(response.errors().isEmpty());

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertTrue(Files.exists(exportFolder.resolve("redemption_01.txt")));
                    assertTrue(Files.exists(exportFolder.resolve("redemption_02.txt")));
                });
    }

    @Test
    void testExportRedemptionFiles_WithNoFiles() {
        FileExportResponse response = restClient.post()
                .uri("/v1/api/export/redemption")
                .retrieve()
                .body(FileExportResponse.class);

        assertNotNull(response);
        assertEquals("REDEMPTION", response.fileType());
        assertEquals(0, response.filesProcessed());
        assertTrue(response.successfulFiles().isEmpty());
        assertTrue(response.errors().isEmpty());
    }

    @Test
    void testExportOutpayFiles_WithFiles() throws IOException {
        copyTestFilesToTmp("outpay_01.txt", "outpay_02.txt", "outpay_03.txt");

        FileExportResponse response = restClient.post()
                .uri("/v1/api/export/outpay")
                .retrieve()
                .body(FileExportResponse.class);

        assertNotNull(response);
        assertEquals("OUTPAY", response.fileType());
        assertEquals(3, response.filesProcessed());
        assertEquals(3, response.successfulFiles().size());
        assertTrue(response.errors().isEmpty());

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertTrue(Files.exists(exportFolder.resolve("outpay_01.txt")));
                    assertTrue(Files.exists(exportFolder.resolve("outpay_02.txt")));
                    assertTrue(Files.exists(exportFolder.resolve("outpay_03.txt")));
                });
    }

    @Test
    void testExportOwnAndBenFiles_WithFiles() throws IOException {
        copyTestFilesToTmp("own_and_ben_01.txt", "own_and_ben_02.txt");

        FileExportResponse response = restClient.post()
                .uri("/v1/api/export/own-and-ben")
                .retrieve()
                .body(FileExportResponse.class);

        assertNotNull(response);
        assertEquals("OWN_AND_BEN", response.fileType());
        assertEquals(2, response.filesProcessed());
        assertEquals(2, response.successfulFiles().size());
        assertTrue(response.errors().isEmpty());

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertTrue(Files.exists(exportFolder.resolve("own_and_ben_01.txt")));
                    assertTrue(Files.exists(exportFolder.resolve("own_and_ben_02.txt")));
                });
    }

    @Test
    void testExportFiles_OnlyProcessesMatchingType() throws IOException {
        copyTestFilesToTmp("redemption_01.txt", "outpay_01.txt", "own_and_ben_01.txt");

        FileExportResponse response = restClient.post()
                .uri("/v1/api/export/redemption")
                .retrieve()
                .body(FileExportResponse.class);

        assertNotNull(response);
        assertEquals(1, response.filesProcessed());
        assertEquals(1, response.successfulFiles().size());
        assertTrue(response.successfulFiles().contains("redemption_01.txt"));

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    assertTrue(Files.exists(exportFolder.resolve("redemption_01.txt")));
                    assertTrue(Files.exists(tmpFolder.resolve("outpay_01.txt")));
                    assertTrue(Files.exists(tmpFolder.resolve("own_and_ben_01.txt")));
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
                                throw new RuntimeException("Failed to delete file: " + path, e);
                            }
                        });
            }
        }
    }
}
