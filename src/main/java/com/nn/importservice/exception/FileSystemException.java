package com.nn.importservice.exception;

/**
 * Exception thrown when file system operations fail
 */
public class FileSystemException extends RuntimeException {

    public FileSystemException(String message) {
        super(message);
    }

    public FileSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
