package com.nn.exportservice.service;

import com.nn.exportservice.config.FileSystemProperties;
import com.nn.exportservice.exception.FileSystemException;
import com.nn.exportservice.model.FileOperationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileSystemService {

    private final FileSystemProperties fileSystemProperties;

    public FileSystemService(FileSystemProperties fileSystemProperties) {
        this.fileSystemProperties = fileSystemProperties;
    }

    /**
     * Lists files in the tmp folder matching the given prefix pattern
     *
     * @param prefixPattern the prefix pattern to match files against
     * @return list of file paths matching the pattern
     * @throws FileSystemException if unable to list files
     */
    public List<Path> listFilesByPrefix(String prefixPattern) {
        try {
            Path tmpPath = Paths.get(fileSystemProperties.tmpFolder());

            if (!Files.exists(tmpPath)) {
                log.warn("tmp folder does not exist path={}", tmpPath);
                return List.of();
            }

            if (!Files.isDirectory(tmpPath)) {
                throw new FileSystemException("Failed to list files with prefix: " + prefixPattern + " - path is not a directory: " + tmpPath);
            }

            try (Stream<Path> stream = Files.walk(tmpPath, 1)) {
                List<Path> matchingFiles = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().startsWith(prefixPattern))
                        .collect(Collectors.toList());
                
                log.info("prefix={} count={}", prefixPattern, matchingFiles.size());
                return matchingFiles;
            }
        } catch (IOException e) {
            log.error("failed to list files prefix={} error={}", prefixPattern, e.getMessage(), e);
            throw new FileSystemException("Failed to list files with prefix: " + prefixPattern, e);
        }
    }

    /**
     * Moves files from tmp folder to export folder
     * Individual file failures are recorded in the result, but don't stop the operation
     *
     * @param filePaths list of file paths to move
     * @return FileOperationResult containing successful files and errors
     * @throws FileSystemException if unable to create export directory
     */
    public FileOperationResult moveFiles(List<Path> filePaths) {
        FileOperationResult result = new FileOperationResult();
        Path exportPath = Paths.get(fileSystemProperties.exportFolder());

        try {
            if (!Files.exists(exportPath)) {
                Files.createDirectories(exportPath);
                log.info("created directory path={}", exportPath);
            }
        } catch (IOException e) {
            log.error("failed to create directory path={} error={}", exportPath, e.getMessage(), e);
            throw new FileSystemException("Failed to create export directory: " + exportPath, e);
        }

        for (Path sourcePath : filePaths) {
            String fileName = sourcePath.getFileName().toString();
            Path destinationPath = exportPath.resolve(fileName);

            try {
                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                result.addSuccess(fileName);
                log.info("filename={} from={} to={}", fileName, sourcePath, destinationPath);
            } catch (IOException e) {
                result.addError(fileName, e.getMessage());
                log.error("failed to move file filename={} error={}", fileName, e.getMessage(), e);
            }
        }

        log.info("moved {} files, {} errors", result.getSuccessCount(), result.getErrorCount());
        return result;
    }

}
