package com.example.ebazaarobackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class StorageServiceTests {
    private StorageService storageService;

    @TempDir
    private Path tempLocation;

    @BeforeEach
    void setUp() {
        storageService = new StorageService();
        ReflectionTestUtils.setField(storageService, "rootLocation", tempLocation);
    }

    @Test
    void shouldReturnStringWhenSuccessfullySaved() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        String testDir = "testdir";
        String result = storageService.store(mockFile, testDir);

        assertThat(result).startsWith("testdir/");
        assertThat(result).contains("test.jpg");

        Path physicalFile = tempLocation.resolve(result);
        assertThat(Files.exists(physicalFile)).isTrue();

        String content = Files.readString(physicalFile);
        assertThat(content).isEqualTo("test");
    }

    @Test
    void shouldDeleteFileWhenExists() throws IOException {
        String relativePath = "test/todelete.jpg";
        Path fileToDelete = tempLocation.resolve(relativePath);
        Files.createDirectories(fileToDelete.getParent());
        Files.writeString(fileToDelete, "test");

        assertThat(Files.exists(fileToDelete)).isTrue();

        storageService.delete(relativePath);

        assertThat(Files.exists(fileToDelete)).isFalse();
    }

    @Test
    void shouldDoNothingIfFileDoesntExist() {
        String missingPath = "test/nonexisting.jpg";

        storageService.delete(missingPath);
    }
}
