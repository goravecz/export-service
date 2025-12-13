package com.nn.exportservice.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileOperationResultTest {

    @Test
    void testNewResult_IsEmpty() {
        FileOperationResult result = new FileOperationResult();

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrorCount()).isZero();
        assertThat(result.isFullySuccessful()).isTrue();
        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void testAddSuccess_IncrementsCount() {
        FileOperationResult result = new FileOperationResult();

        result.addSuccess("file1.txt");
        result.addSuccess("file2.txt");

        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getSuccessfulFiles()).containsExactly("file1.txt", "file2.txt");
    }

    @Test
    void testAddError_IncrementsCount() {
        FileOperationResult result = new FileOperationResult();

        result.addError("file1.txt", "Permission denied");
        result.addError("file2.txt", "File not found");

        assertThat(result.getErrorCount()).isEqualTo(2);
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.isFullySuccessful()).isFalse();
    }

    @Test
    void testGetErrors_ReturnsCorrectDetails() {
        FileOperationResult result = new FileOperationResult();

        result.addError("file1.txt", "Permission denied");

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).fileName()).isEqualTo("file1.txt");
        assertThat(result.getErrors().get(0).errorMessage()).isEqualTo("Permission denied");
    }

    @Test
    void testMixedResult() {
        FileOperationResult result = new FileOperationResult();

        result.addSuccess("success1.txt");
        result.addSuccess("success2.txt");
        result.addError("error1.txt", "IO Error");

        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getErrorCount()).isEqualTo(1);
        assertThat(result.isFullySuccessful()).isFalse();
        assertThat(result.hasErrors()).isTrue();
    }

    @Test
    void testGetters_ReturnDefensiveCopies() {
        FileOperationResult result = new FileOperationResult();
        result.addSuccess("file.txt");

        var successList = result.getSuccessfulFiles();
        successList.add("modified.txt");

        // Original should not be modified
        assertThat(result.getSuccessfulFiles()).containsExactly("file.txt");
    }
}
