package com.example.demo.application;

import com.example.demo.service.BoxUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class BoxUploadJob implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(BoxUploadJob.class);
    private final BoxUploadService boxUploadService;

    @Value("${upload.zip.path}")
    private String zipPath;

    @Value("${upload.parallelism}")
    private int parallelism;

    @Value("${upload.file.count:#{null}}")
    private Integer fileCount;

    @Value("${box.folder.id}")
    private String folderId;

    public BoxUploadJob(BoxUploadService boxUploadService) {
        this.boxUploadService = boxUploadService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("========== Configuration ==========");
        logger.info("ZIP_PATH: {}", zipPath);
        logger.info("PARALLELISM: {}", parallelism);
        logger.info("FILE_COUNT: {}", fileCount != null ? fileCount : "ALL");
        logger.info("BOX_FOLDER_ID: {}", folderId);
        logger.info("===================================");

        Path extractDir = Paths.get("extracted");
        cleanAndCreateDirectory(extractDir);
        extractZipFile(zipPath, extractDir);

        List<Path> files = Files.list(extractDir)
                .filter(p -> p.toString().toLowerCase().endsWith(".csv"))
                .sorted()
                .collect(Collectors.toList());

        int targetCount = files.size();
        if (fileCount != null && fileCount > 0 && fileCount < targetCount) {
            files = files.subList(0, fileCount);
            targetCount = fileCount;
        }

        ExecutorService executor = Executors.newFixedThreadPool(parallelism);
        logger.info("Uploading {} files with parallelism={}", targetCount, parallelism);

        Instant start = Instant.now();

        List<CompletableFuture<Void>> futures = files.stream()
                .map(path -> CompletableFuture.runAsync(() -> 
                    boxUploadService.uploadFile(path, folderId), executor))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        long millis = Duration.between(start, Instant.now()).toMillis();
        logger.info("All uploads completed in {} ms ({} seconds)", millis, millis / 1000.0);
    }

    private void cleanAndCreateDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        Files.createDirectories(dir);
    }

    private void extractZipFile(String zipPath, Path extractDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    Path filePath = extractDir.resolve(entry.getName());
                    Files.createDirectories(filePath.getParent());
                    try (OutputStream os = Files.newOutputStream(filePath)) {
                        zis.transferTo(os);
                    }
                }
            }
        }
    }
}