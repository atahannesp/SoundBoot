package com.example.soundboot.service.segmentationService;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface AudioSegmentationService {
    Path segment(MultipartFile file, String trackId) throws IOException;

    Path saveToTemp(MultipartFile file) throws IOException;

    Path prepareOutputDir(String trackId) throws IOException;

    void runFfmpeg(Path inputFile, Path outputDir);

    String getExtension(String filename);

    void deleteSegments(String trackId) throws IOException;
}
