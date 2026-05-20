package com.example.soundboot.service.segmentationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Implementation of the AudioSegmentationService interface.
 * Handles processing and dividing audio files into HLS segments using FFmpeg.
 */
@Service
public class AudioSegmentationServiceImplementation implements AudioSegmentationService {

    private static final Logger log = LoggerFactory.getLogger(AudioSegmentationServiceImplementation.class);

    @Value("${ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    /**
     * The base directory where HLS segments will be stored.
     */
    private static final String BASE_DIR = "uploads/hls/";

    /**
     * Main method to segment an audio file.
     * It saves the uploaded file to a temporary location, runs FFmpeg to create HLS segments,
     * and then cleans up the temporary file.
     *
     * @param file    the audio file uploaded by the user.
     * @param trackId a unique identifier (UUID) for the song.
     * @return the path to the directory containing the HLS segments.
     * @throws IOException if an I/O error occurs during file operations.
     */
    @Override
    public Path segment(MultipartFile file, String trackId) throws IOException {
        Path inputFile = saveToTemp(file);
        Path outputDir = prepareOutputDir(trackId);
        try {
            runFfmpeg(inputFile, outputDir);
        } finally {
            Files.deleteIfExists(inputFile);
        }
        return outputDir;
    }

    /**
     * Saves the uploaded MultipartFile to a temporary file on disk.
     * This is necessary because FFmpeg operates on file paths.
     *
     * @param file the uploaded audio file.
     * @return the path to the created temporary file.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public Path saveToTemp(MultipartFile file) throws IOException {
        String extension = getExtension(file.getOriginalFilename());
        Path tempFile = Files.createTempFile("audio-", extension);
        file.transferTo(tempFile.toFile());
        return tempFile;
    }

    /**
     * Prepares the output directory for the HLS segments.
     * Creates a directory structure like 'uploads/hls/{trackId}/'.
     *
     * @param trackId the unique identifier for the song.
     * @return the path to the created output directory.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public Path prepareOutputDir(String trackId) throws IOException {
        Path outputDir = Paths.get(BASE_DIR + trackId);
        Files.createDirectories(outputDir);
        log.info("Output directory created: {}", outputDir);
        return outputDir;
    }

    /**
     * Executes the FFmpeg process to segment the audio file into HLS format.
     * Each segment is 10 seconds long, encoded with AAC at 128k bitrate.
     *
     * @param inputFile the path to the input audio file.
     * @param outputDir the directory where the HLS segments will be saved.
     * @throws RuntimeException if the FFmpeg process fails or returns a non-zero exit code.
     */
    @Override
    public void runFfmpeg(Path inputFile, Path outputDir) {
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-i", inputFile.toString(),
                "-c:a", "aac",
                "-b:a", "128k",
                "-hls_time", "10",
                "-hls_list_size", "0",
                "-hls_segment_filename", outputDir.resolve("seg%03d.ts").toString(),
                outputDir.resolve("index.m3u8").toString()
        );

        try {
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(line -> log.debug("FFmpeg: {}", line));
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg process failed with exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to run FFmpeg process: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the file extension from a filename (e.g., ".mp3", ".wav").
     *
     * @param filename the name of the file.
     * @return the file extension, or ".mp3" as a default.
     */
    @Override
    public String getExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return ".mp3";
    }

    /**
     * Deletes all HLS segments associated with a specific trackId.
     * This is used for cleanup when a song is deleted.
     *
     * @param trackId the unique identifier of the song to be deleted.
     * @throws IOException if an I/O error occurs during deletion.
     */
    @Override
    public void deleteSegments(String trackId) throws IOException {
        Path outputDir = Paths.get(BASE_DIR + trackId);
        if (Files.exists(outputDir)) {

            try (Stream<Path> paths = Files.walk(outputDir)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.error("Failed to delete file: {}", path, e);
                            }
                        });
            }
            log.info("Deleted HLS segments for trackId: {}", trackId);
        }
    }
}
