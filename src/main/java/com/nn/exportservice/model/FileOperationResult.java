package com.nn.exportservice.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of file operation containing successful and failed file names
 */
public class FileOperationResult {
    private final List<String> successfulFiles;
    private final List<FileOperationError> errors;

    public FileOperationResult() {
        this.successfulFiles = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public void addSuccess(String fileName) {
        successfulFiles.add(fileName);
    }

    public void addError(String fileName, String errorMessage) {
        errors.add(new FileOperationError(fileName, errorMessage));
    }

    public List<String> getSuccessfulFiles() {
        return new ArrayList<>(successfulFiles);
    }

    public List<FileOperationError> getErrors() {
        return new ArrayList<>(errors);
    }

    public int getSuccessCount() {
        return successfulFiles.size();
    }

    public int getErrorCount() {
        return errors.size();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isFullySuccessful() {
        return errors.isEmpty();
    }

    public record FileOperationError(String fileName, String errorMessage) {}
}
